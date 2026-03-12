package com.luan.takeaway.takeaway.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.core.util.RedisUtils;
import com.luan.takeaway.takeaway.common.constant.TakeawayStatusConstants;
import com.luan.takeaway.takeaway.common.dto.DeductStockRequest;
import com.luan.takeaway.takeaway.common.dto.DishPurchaseItemDTO;
import com.luan.takeaway.takeaway.common.dto.OrderDTO;
import com.luan.takeaway.takeaway.common.entity.WmAddress;
import com.luan.takeaway.takeaway.common.entity.WmDish;
import com.luan.takeaway.takeaway.common.entity.WmDeliveryUserExt;
import com.luan.takeaway.takeaway.common.entity.WmMerchantUserExt;
import com.luan.takeaway.takeaway.common.entity.WmCustomerUserExt;
import com.luan.takeaway.takeaway.common.entity.WmOrder;
import com.luan.takeaway.takeaway.common.entity.WmOrderItem;
import com.luan.takeaway.takeaway.common.mapper.WmDeliveryUserExtMapper;
import com.luan.takeaway.takeaway.common.mapper.WmAddressMapper;
import com.luan.takeaway.takeaway.common.mapper.WmCustomerUserExtMapper;
import com.luan.takeaway.takeaway.common.mapper.WmMerchantUserExtMapper;
import com.luan.takeaway.takeaway.common.mapper.WmOrderItemMapper;
import com.luan.takeaway.takeaway.common.mapper.WmOrderMapper;
import com.luan.takeaway.takeaway.dish.api.DishApi;
import com.luan.takeaway.takeaway.order.constant.OrderAutoCancelMqConstants;
import com.luan.takeaway.takeaway.order.dto.ws.OrderStatusWsMessage;
import com.luan.takeaway.takeaway.order.message.OrderStatusMqPublisher;
import com.luan.takeaway.takeaway.order.mq.dto.OrderAutoCancelEvent;
import com.luan.takeaway.takeaway.order.service.WmOrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class WmOrderServiceImpl extends ServiceImpl<WmOrderMapper, WmOrder> implements WmOrderService {

	private final WmOrderItemMapper wmOrderItemMapper;

	private final WmAddressMapper wmAddressMapper;

	private final WmMerchantUserExtMapper wmMerchantUserExtMapper;

	private final WmCustomerUserExtMapper wmCustomerUserExtMapper;

	private final WmDeliveryUserExtMapper wmDeliveryUserExtMapper;

	private final DishApi dishApi;

	/**
	 * RabbitMQ 消息发送模板。
	 */
	private final RabbitTemplate rabbitTemplate;

	/**
	 * JSON 序列化工具，用于构建延时消息体。
	 */
	private final ObjectMapper objectMapper;

	private final OrderStatusMqPublisher orderStatusMqPublisher;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public OrderDTO createOrder(OrderDTO request) {
		if (request == null) {
			throw new IllegalArgumentException("下单参数不能为空");
		}
		if (request.getCustomerUserId() == null) {
			throw new IllegalArgumentException("客户用户ID不能为空");
		}
		if (request.getDeliveryAddressId() == null) {
			throw new IllegalArgumentException("收货地址ID不能为空");
		}
		if (request.getItems() == null || request.getItems().isEmpty()) {
			throw new IllegalArgumentException("订单明细不能为空");
		}
		validateDeliveryAddress(request.getDeliveryAddressId(), request.getCustomerUserId());

		List<Long> dishIds = request.getItems().stream().map(DishPurchaseItemDTO::getDishId).distinct().toList();
		R<List<WmDish>> dishResp = dishApi.listByIds(request.getMerchantUserId(), dishIds);
		List<WmDish> dishes = unwrap(dishResp, "查询菜品失败");
		Map<Long, WmDish> dishMap = new HashMap<>(dishes.size());
		dishes.forEach(dish -> dishMap.put(dish.getId(), dish));

		BigDecimal totalAmount = BigDecimal.ZERO;
		for (DishPurchaseItemDTO item : request.getItems()) {
			WmDish dish = dishMap.get(item.getDishId());
			if (dish == null) {
				throw new IllegalArgumentException("菜品不存在: " + item.getDishId());
			}
			BigDecimal itemAmount = dish.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
			totalAmount = totalAmount.add(itemAmount);
		}

		WmOrder order = new WmOrder();
		order.setOrderNo(generateOrderNo());
		order.setCustomerUserId(request.getCustomerUserId());
		order.setMerchantUserId(request.getMerchantUserId());
		order.setDeliveryAddressId(request.getDeliveryAddressId());
		order.setTotalAmount(totalAmount);
		order.setPayAmount(totalAmount);
		order.setOrderStatus(TakeawayStatusConstants.Order.WAIT_PAY);
		order.setRemark(request.getRemark());
		// 先落库再发延时消息，保证消息里的 orderId/orderNo 一定存在且可查询。
		save(order);
		// 下单后立刻发送“超时自动取消”延时消息。
		// 若用户在 10 分钟内完成支付，消费者侧会因状态不匹配而跳过取消（幂等）。
		publishAutoCancelDelayEvent(order);

		DeductStockRequest deductRequest = new DeductStockRequest();
		deductRequest.setMerchantUserId(request.getMerchantUserId());
		// 传递订单号用于下游库存异步落库的幂等控制：
		// Dish 服务消费 MQ 后会基于 orderNo 写入消费完成标记，避免重复消息导致重复扣减数据库库存。
		deductRequest.setOrderNo(order.getOrderNo());
		deductRequest.setItems(request.getItems());
		R<Boolean> deductResp = dishApi.deductStock(deductRequest);
		Boolean deductSuccess = unwrap(deductResp, "扣减库存失败");
		if (!Boolean.TRUE.equals(deductSuccess)) {
			throw new IllegalStateException("扣减库存失败");
		}

		List<WmOrderItem> orderItems = new ArrayList<>(request.getItems().size());
		for (DishPurchaseItemDTO item : request.getItems()) {
			WmDish dish = dishMap.get(item.getDishId());
			if (dish == null) {
				throw new IllegalArgumentException("菜品不存在: " + item.getDishId());
			}
			BigDecimal itemAmount = dish.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

			WmOrderItem orderItem = new WmOrderItem();
			orderItem.setOrderId(order.getId());
			orderItem.setDishId(dish.getId());
			orderItem.setDishName(dish.getDishName());
			orderItem.setDishPrice(dish.getPrice());
			orderItem.setQuantity(item.getQuantity());
			orderItem.setItemAmount(itemAmount);
			wmOrderItemMapper.insert(orderItem);
			orderItems.add(orderItem);
		}

		OrderDTO orderDTO = new OrderDTO();
		BeanUtils.copyProperties(order, orderDTO);
		orderDTO.setOrderItems(orderItems);
		fillAutoCancelInfo(order, orderDTO);
		return orderDTO;
	}

	@Override
	public OrderDTO detail(Long orderId) {
		WmOrder order = getById(orderId);
		if (order == null) {
			return null;
		}
		List<WmOrderItem> items = wmOrderItemMapper.selectList(Wrappers.<WmOrderItem>lambdaQuery()
			.eq(WmOrderItem::getOrderId, orderId)
			.orderByAsc(WmOrderItem::getId));
		String merchantName = null;
		String riderName = null;
		String customerName = null;
		WmAddress customerAddress = null;
		WmAddress merchantAddress = null;
		if (order.getDeliveryAddressId() != null) {
			customerAddress = wmAddressMapper.selectById(order.getDeliveryAddressId());
		}
		if (order.getCustomerUserId() != null) {
			WmCustomerUserExt customer = wmCustomerUserExtMapper.selectOne(
				Wrappers.<WmCustomerUserExt>lambdaQuery().eq(WmCustomerUserExt::getUserId, order.getCustomerUserId()));
			customerName = customer != null ? customer.getRealName() : null;
		}
		if (order.getMerchantUserId() != null) {
			WmMerchantUserExt merchant = wmMerchantUserExtMapper.selectOne(
				Wrappers.<WmMerchantUserExt>lambdaQuery().eq(WmMerchantUserExt::getUserId, order.getMerchantUserId()));
			merchantName = merchant != null ? merchant.getMerchantName() : null;
			if (merchant != null && merchant.getStoreAddressId() != null) {
				merchantAddress = wmAddressMapper.selectById(merchant.getStoreAddressId());
			}
		}
		if (order.getDeliveryUserId() != null) {
			WmDeliveryUserExt rider = wmDeliveryUserExtMapper.selectOne(
				Wrappers.<WmDeliveryUserExt>lambdaQuery().eq(WmDeliveryUserExt::getUserId, order.getDeliveryUserId()));
			riderName = rider != null ? rider.getRealName() : null;
		}
		OrderDTO orderDTO = new OrderDTO();
		BeanUtils.copyProperties(order, orderDTO);
		orderDTO.setCustomerName(customerName);
		orderDTO.setMerchantName(merchantName);
		orderDTO.setDeliveryRiderName(riderName);
		orderDTO.setCustomerAddress(customerAddress);
		orderDTO.setMerchantAddress(merchantAddress);
		orderDTO.setOrderItems(items);
		fillAutoCancelInfo(order, orderDTO);
		return orderDTO;
	}

	@Override
	public Page<OrderDTO> queryPage(Page<OrderDTO> page, Long customerUserId, Long merchantUserId, Long deliveryUserId,
			String status) {
		Page<WmOrder> queryPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
		Page<WmOrder> orderPage = page(queryPage, Wrappers.<WmOrder>lambdaQuery()
			.eq(customerUserId != null, WmOrder::getCustomerUserId, customerUserId)
			.eq(merchantUserId != null, WmOrder::getMerchantUserId, merchantUserId)
			.eq(deliveryUserId != null, WmOrder::getDeliveryUserId, deliveryUserId)
			.eq(status != null && !status.isBlank(), WmOrder::getOrderStatus, status)
			.orderByDesc(WmOrder::getCreateTime));

		List<WmOrder> records = orderPage.getRecords();
		if (records == null || records.isEmpty()) {
			return new Page<OrderDTO>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
		}

		Set<Long> merchantUserIds = records.stream()
			.map(WmOrder::getMerchantUserId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Set<Long> customerUserIds = records.stream()
			.map(WmOrder::getCustomerUserId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Set<Long> customerAddressIds = records.stream()
			.map(WmOrder::getDeliveryAddressId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Map<Long, WmMerchantUserExt> merchantMap = new HashMap<>(merchantUserIds.size());
		Map<Long, String> merchantNameMap = new HashMap<>(merchantUserIds.size());
		Map<Long, String> customerNameMap = new HashMap<>(customerUserIds.size());
		Map<Long, WmAddress> customerAddressMap = new HashMap<>(customerAddressIds.size());
		if (!merchantUserIds.isEmpty()) {
			wmMerchantUserExtMapper
				.selectList(Wrappers.<WmMerchantUserExt>lambdaQuery().in(WmMerchantUserExt::getUserId, merchantUserIds))
				.forEach(merchant -> {
					merchantMap.put(merchant.getUserId(), merchant);
					merchantNameMap.put(merchant.getUserId(), merchant.getMerchantName());
				});
		}
		if (!customerUserIds.isEmpty()) {
			wmCustomerUserExtMapper
				.selectList(Wrappers.<WmCustomerUserExt>lambdaQuery().in(WmCustomerUserExt::getUserId, customerUserIds))
				.forEach(customer -> customerNameMap.put(customer.getUserId(), customer.getRealName()));
		}
		if (!customerAddressIds.isEmpty()) {
			wmAddressMapper.selectList(Wrappers.<WmAddress>lambdaQuery().in(WmAddress::getId, customerAddressIds))
				.forEach(address -> customerAddressMap.put(address.getId(), address));
		}

		Set<Long> merchantAddressIds = merchantMap.values()
			.stream()
			.map(WmMerchantUserExt::getStoreAddressId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Map<Long, WmAddress> merchantAddressMap = new HashMap<>(merchantAddressIds.size());
		if (!merchantAddressIds.isEmpty()) {
			wmAddressMapper.selectList(Wrappers.<WmAddress>lambdaQuery().in(WmAddress::getId, merchantAddressIds))
				.forEach(address -> merchantAddressMap.put(address.getId(), address));
		}

		Set<Long> deliveryUserIds = records.stream()
			.map(WmOrder::getDeliveryUserId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Set<Long> orderIds = records.stream()
			.map(WmOrder::getId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Map<Long, String> riderNameMap = new HashMap<>(deliveryUserIds.size());
		Map<Long, List<WmOrderItem>> orderItemsMap = new HashMap<>(orderIds.size());
		if (!deliveryUserIds.isEmpty()) {
			wmDeliveryUserExtMapper
				.selectList(Wrappers.<WmDeliveryUserExt>lambdaQuery().in(WmDeliveryUserExt::getUserId, deliveryUserIds))
				.forEach(rider -> riderNameMap.put(rider.getUserId(), rider.getRealName()));
		}
		if (!orderIds.isEmpty()) {
			wmOrderItemMapper.selectList(Wrappers.<WmOrderItem>lambdaQuery()
				.in(WmOrderItem::getOrderId, orderIds)
				.orderByAsc(WmOrderItem::getId))
				.forEach(item -> orderItemsMap.computeIfAbsent(item.getOrderId(), key -> new java.util.ArrayList<>())
					.add(item));
		}

		List<OrderDTO> voRecords = records.stream().map(order -> {
			OrderDTO orderDTO = new OrderDTO();
			BeanUtils.copyProperties(order, orderDTO);
			orderDTO.setCustomerName(customerNameMap.get(order.getCustomerUserId()));
			orderDTO.setMerchantName(merchantNameMap.get(order.getMerchantUserId()));
			orderDTO.setDeliveryRiderName(riderNameMap.get(order.getDeliveryUserId()));
			orderDTO.setCustomerAddress(customerAddressMap.get(order.getDeliveryAddressId()));
			WmMerchantUserExt merchant = merchantMap.get(order.getMerchantUserId());
			if (merchant != null) {
				orderDTO.setMerchantAddress(merchantAddressMap.get(merchant.getStoreAddressId()));
			}
			orderDTO.setOrderItems(orderItemsMap.getOrDefault(order.getId(), java.util.Collections.emptyList()));
			fillAutoCancelInfo(order, orderDTO);
			return orderDTO;
		}).toList();

		Page<OrderDTO> result = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
		result.setRecords(voRecords);
		return result;
	}

	@Override
	public boolean markPaid(Long orderId) {
		// 先校验订单是否存在，避免返回“仅待支付订单可支付”时掩盖“订单不存在”。
		mustGet(orderId);
		// 通过“带状态条件”的原子更新实现并发安全：
		// 只有当前状态仍是 WAIT_PAY 时才允许置为 PAID。
		// 这样可以避免与延时取消消费者并发更新时出现状态覆盖。
		boolean updated = update(Wrappers.<WmOrder>lambdaUpdate()
			.eq(WmOrder::getId, orderId)
			.eq(WmOrder::getOrderStatus, TakeawayStatusConstants.Order.WAIT_PAY)
			.set(WmOrder::getOrderStatus, TakeawayStatusConstants.Order.PAID)
			.set(WmOrder::getPayTime, LocalDateTime.now()));
		if (!updated) {
			throw new IllegalStateException("仅待支付订单可支付");
		}
		return true;
	}

	@Override
	public boolean merchantAccept(Long orderId) {
		WmOrder order = mustGet(orderId);
		if (!TakeawayStatusConstants.Order.PAID.equals(order.getOrderStatus())) {
			throw new IllegalStateException("仅已支付订单可接单");
		}
		WmOrder update = new WmOrder();
		update.setId(orderId);
		update.setOrderStatus(TakeawayStatusConstants.Order.MERCHANT_ACCEPTED);
		update.setAcceptTime(LocalDateTime.now());
		boolean updated = updateById(update);
		if (updated) {
			// 状态真正落库成功后再发通知，避免出现“用户先收到推送，但数据库还是旧状态”的错觉。
			pushOrderStatusByMq(OrderStatusWsMessage.merchantAccepted(orderId, order.getCustomerUserId()));
		}
		return updated;
	}

	@Override
	public boolean deliveryStart(Long orderId, Long deliveryUserId) {
		// 锁 key 按订单维度构造，确保“同一个订单”的抢单流程在同一时刻只允许一个线程进入。
		String lockKey = "takeaway:order:delivery:start:lock:" + orderId;
		// lock value 使用随机串，便于 releaseLock 做 value 比对，避免误删掉其他线程/实例持有的锁。
		String lockValue = UUID.randomUUID().toString();

		// 锁超时设置为 10 秒：
		// 1) 足够覆盖一次“查订单 + 更新状态”的数据库操作；
		// 2) 即使服务异常退出，也能依赖过期时间自动释放，避免死锁。
		boolean locked = RedisUtils.getLock(lockKey, lockValue, 10);
		if (!locked) {
			// 没抢到锁说明当前订单正在被其他骑手并发处理，直接失败并提示前端重试。
			throw new IllegalStateException("当前订单正在被其他骑手抢单，请稍后重试");
		}

		try {
			// 拿到锁后再查最新订单状态，防止使用旧数据做业务判断。
			WmOrder order = mustGet(orderId);
			if (!TakeawayStatusConstants.Order.MERCHANT_ACCEPTED.equals(order.getOrderStatus())) {
				// 只有“商家已接单”状态允许骑手开始配送。
				// 当前状态若已变更（例如已被其他骑手抢走），这里会直接阻断流程。
				throw new IllegalStateException("仅商家已接单订单可开始配送");
			}

			// 状态迁移：商家已接单 -> 配送中，同时写入骑手 ID 和配送开始时间。
			WmOrder update = new WmOrder();
			update.setId(orderId);
			update.setDeliveryUserId(deliveryUserId);
			update.setOrderStatus(TakeawayStatusConstants.Order.DELIVERING);
			update.setDeliveryStartTime(LocalDateTime.now());
			boolean updated = updateById(update);
			if (updated) {
				// 只有抢单成功（状态推进到配送中）才通知用户，避免并发抢单时重复推送。
				pushOrderStatusByMq(
						OrderStatusWsMessage.riderAccepted(orderId, order.getCustomerUserId(), deliveryUserId));
			}
			return updated;
		}
		finally {
			// 无论业务成功/失败都尝试释放锁。
			// releaseLock 内部通过 Lua 做“key + value”原子校验后删除，避免误释放他人的锁。
			RedisUtils.releaseLock(lockKey, lockValue);
		}
	}

	@Override
	public boolean finish(Long orderId) {
		WmOrder order = mustGet(orderId);
		if (!TakeawayStatusConstants.Order.DELIVERING.equals(order.getOrderStatus())) {
			throw new IllegalStateException("仅配送中订单可完成");
		}
		WmOrder update = new WmOrder();
		update.setId(orderId);
		update.setOrderStatus(TakeawayStatusConstants.Order.FINISHED);
		update.setFinishTime(LocalDateTime.now());
		boolean updated = updateById(update);
		if (updated) {
			// 配送完成通知同样依赖数据库状态变更结果，保持“先事实、后通知”的顺序。
			pushOrderStatusByMq(
					OrderStatusWsMessage.deliveryFinished(orderId, order.getCustomerUserId(), order.getDeliveryUserId()));
		}
		return updated;
	}

	@Override
	public boolean cancel(Long orderId) {
		WmOrder order = mustGet(orderId);
		if (TakeawayStatusConstants.Order.FINISHED.equals(order.getOrderStatus())) {
			throw new IllegalStateException("已完成订单不可取消");
		}
		WmOrder update = new WmOrder();
		update.setId(orderId);
		update.setOrderStatus(TakeawayStatusConstants.Order.CANCELED);
		update.setCancelTime(LocalDateTime.now());
		return updateById(update);
	}

	@Override
	public boolean autoCancelIfUnpaid(Long orderId) {
		// 自动取消采用幂等条件更新：
		// 仅当订单仍为待支付时才转为已取消。
		// 返回 false 代表订单已经支付/已取消/状态已推进，属于正常情况。
		return update(Wrappers.<WmOrder>lambdaUpdate()
			.eq(WmOrder::getId, orderId)
			.eq(WmOrder::getOrderStatus, TakeawayStatusConstants.Order.WAIT_PAY)
			.set(WmOrder::getOrderStatus, TakeawayStatusConstants.Order.CANCELED)
			.set(WmOrder::getCancelTime, LocalDateTime.now()));
	}

	private WmOrder mustGet(Long orderId) {
		WmOrder order = getById(orderId);
		if (order == null) {
			throw new IllegalArgumentException("订单不存在: " + orderId);
		}
		return order;
	}

	private <T> T unwrap(R<T> response, String errorMsg) {
		if (response == null || response.getCode() != 0) {
			throw new IllegalStateException(errorMsg);
		}
		return response.getData();
	}

	private String generateOrderNo() {
		// 订单号组成：业务前缀 + 毫秒时间戳 + 随机数，兼顾可读性与低碰撞概率。
		return "WM" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 10000);
	}

	/**
	 * 统一补充订单自动取消展示信息。
	 *
	 * - autoCancelDeadlineTs：用于前端刷新后也能继续准确倒计时。
	 */
	private void fillAutoCancelInfo(WmOrder order, OrderDTO orderDTO) {
		if (order == null || order.getCreateTime() == null) {
			return;
		}
		LocalDateTime deadline = order.getCreateTime()
			.plusNanos(OrderAutoCancelMqConstants.AUTO_CANCEL_DELAY_MS * 1_000_000L);
		long deadlineTs = deadline.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		orderDTO.setAutoCancelDeadlineTs(deadlineTs);
	}

	private void pushOrderStatusByMq(OrderStatusWsMessage message) {
		if (message == null || message.getUserId() == null) {
			return;
		}

		try {
			// 这里把业务对象转成 JSON 文本发给 MQ。
			// upms 消费后不关心订单领域对象，只需要把该文本透传给前端即可。
			String messageText = objectMapper.writeValueAsString(message);
			if (orderStatusMqPublisher.publish(message.getOrderId(), message.getUserId(), message.getEventType(), messageText)) {
				return;
			}
			// 与商家审核通知保持一致：当前阶段只记录失败，不在业务线程里做阻塞重试。
			log.warn("订单状态通知MQ发送失败, orderId={}, userId={}, eventType={}", message.getOrderId(), message.getUserId(),
					message.getEventType());
		}
		catch (JsonProcessingException e) {
			log.error("订单状态通知消息序列化失败, orderId={}, userId={}, eventType={}", message.getOrderId(), message.getUserId(),
					message.getEventType(), e);
		}
		catch (Exception e) {
			log.warn("订单状态通知消息推送异常, orderId={}, userId={}, eventType={}, message={}", message.getOrderId(),
					message.getUserId(), message.getEventType(), e.getMessage());
		}
	}

	private void publishAutoCancelDelayEvent(WmOrder order) {
		// 消息体只携带必要字段：orderId 用于执行取消，orderNo/createTime 用于日志和审计。
		OrderAutoCancelEvent event = new OrderAutoCancelEvent();
		event.setOrderId(order.getId());
		event.setOrderNo(order.getOrderNo());
		event.setCreateTime(order.getCreateTime());
		try {
			// 使用“消息级 TTL”控制延时（10 分钟）。
			// 消息先进入 delay queue，过期后由 dead-letter exchange 转发到消费队列。
			rabbitTemplate.convertAndSend(OrderAutoCancelMqConstants.DELAY_EXCHANGE,
					OrderAutoCancelMqConstants.DELAY_ROUTING_KEY, objectMapper.writeValueAsString(event), message -> {
					message.getMessageProperties()
						.setExpiration(String.valueOf(OrderAutoCancelMqConstants.AUTO_CANCEL_DELAY_MS));
					return message;
				});
		}
		catch (JsonProcessingException e) {
			// 序列化失败通常为代码或对象结构问题，直接中断下单事务，避免“订单已创建但无超时保障”。
			throw new IllegalStateException("发送订单自动取消消息失败", e);
		}
		catch (Exception e) {
			// MQ 不可用时同样中断事务，确保“订单创建”和“超时保障”要么都成功，要么都失败。
			log.error("发送订单自动取消消息失败, orderId={}, orderNo={}", order.getId(), order.getOrderNo(), e);
			throw new IllegalStateException("发送订单自动取消消息失败", e);
		}
	}

	private void validateDeliveryAddress(Long deliveryAddressId, Long customerUserId) {
		WmAddress address = wmAddressMapper.selectById(deliveryAddressId);
		if (address == null) {
			throw new IllegalArgumentException("收货地址不存在: " + deliveryAddressId);
		}
		if (!customerUserId.equals(address.getUserId())) {
			throw new IllegalArgumentException("收货地址不属于当前下单用户");
		}
	}

}

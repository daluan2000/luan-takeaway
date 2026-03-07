package com.pig4cloud.pig.takeaway.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayStatusConstants;
import com.pig4cloud.pig.takeaway.common.dto.DeductStockRequest;
import com.pig4cloud.pig.takeaway.common.dto.DishPurchaseItemDTO;
import com.pig4cloud.pig.takeaway.common.dto.OrderDTO;
import com.pig4cloud.pig.takeaway.common.entity.WmAddress;
import com.pig4cloud.pig.takeaway.common.entity.WmDish;
import com.pig4cloud.pig.takeaway.common.entity.WmDeliveryUserExt;
import com.pig4cloud.pig.takeaway.common.entity.WmMerchantUserExt;
import com.pig4cloud.pig.takeaway.common.entity.WmOrder;
import com.pig4cloud.pig.takeaway.common.entity.WmOrderItem;
import com.pig4cloud.pig.takeaway.common.mapper.WmDeliveryUserExtMapper;
import com.pig4cloud.pig.takeaway.common.mapper.WmAddressMapper;
import com.pig4cloud.pig.takeaway.common.mapper.WmMerchantUserExtMapper;
import com.pig4cloud.pig.takeaway.common.mapper.WmOrderItemMapper;
import com.pig4cloud.pig.takeaway.common.mapper.WmOrderMapper;
import com.pig4cloud.pig.takeaway.dish.api.DishApi;
import com.pig4cloud.pig.takeaway.order.service.WmOrderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

@Service
@AllArgsConstructor
public class WmOrderServiceImpl extends ServiceImpl<WmOrderMapper, WmOrder> implements WmOrderService {

	private final WmOrderItemMapper wmOrderItemMapper;

	private final WmAddressMapper wmAddressMapper;

	private final WmMerchantUserExtMapper wmMerchantUserExtMapper;

	private final WmDeliveryUserExtMapper wmDeliveryUserExtMapper;

	private final DishApi dishApi;

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
		save(order);

		DeductStockRequest deductRequest = new DeductStockRequest();
		deductRequest.setMerchantUserId(request.getMerchantUserId());
		deductRequest.setItems(request.getItems());
		R<Boolean> deductResp = dishApi.deductStock(deductRequest);
		Boolean deductSuccess = unwrap(deductResp, "扣减库存失败");
		if (!Boolean.TRUE.equals(deductSuccess)) {
			throw new IllegalStateException("扣减库存失败");
		}

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
		}

		return toOrderDTO(order, null, null);
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
		if (order.getMerchantUserId() != null) {
			WmMerchantUserExt merchant = wmMerchantUserExtMapper.selectOne(
				Wrappers.<WmMerchantUserExt>lambdaQuery().eq(WmMerchantUserExt::getUserId, order.getMerchantUserId()));
			merchantName = merchant != null ? merchant.getMerchantName() : null;
		}
		if (order.getDeliveryUserId() != null) {
			WmDeliveryUserExt rider = wmDeliveryUserExtMapper.selectOne(
				Wrappers.<WmDeliveryUserExt>lambdaQuery().eq(WmDeliveryUserExt::getUserId, order.getDeliveryUserId()));
			riderName = rider != null ? rider.getRealName() : null;
		}
		OrderDTO orderDTO = toOrderDTO(order, merchantName, riderName);
		orderDTO.setOrderItems(items);
		return orderDTO;
	}

	@Override
	public Page<OrderDTO> queryPage(Page<WmOrder> page, Long customerUserId, Long merchantUserId, Long deliveryUserId,
			String status) {
		Page<WmOrder> orderPage = page(page, Wrappers.<WmOrder>lambdaQuery()
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
		Map<Long, String> merchantNameMap = new HashMap<>(merchantUserIds.size());
		if (!merchantUserIds.isEmpty()) {
			wmMerchantUserExtMapper
				.selectList(Wrappers.<WmMerchantUserExt>lambdaQuery().in(WmMerchantUserExt::getUserId, merchantUserIds))
				.forEach(merchant -> merchantNameMap.put(merchant.getUserId(), merchant.getMerchantName()));
		}

		Set<Long> deliveryUserIds = records.stream()
			.map(WmOrder::getDeliveryUserId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Map<Long, String> riderNameMap = new HashMap<>(deliveryUserIds.size());
		if (!deliveryUserIds.isEmpty()) {
			wmDeliveryUserExtMapper
				.selectList(Wrappers.<WmDeliveryUserExt>lambdaQuery().in(WmDeliveryUserExt::getUserId, deliveryUserIds))
				.forEach(rider -> riderNameMap.put(rider.getUserId(), rider.getRealName()));
		}

		List<OrderDTO> voRecords = records.stream().map(order -> {
			OrderDTO orderDTO = new OrderDTO();
			BeanUtils.copyProperties(order, orderDTO);
			orderDTO.setMerchantName(merchantNameMap.get(order.getMerchantUserId()));
			orderDTO.setDeliveryRiderName(riderNameMap.get(order.getDeliveryUserId()));
			return orderDTO;
		}).toList();

		Page<OrderDTO> result = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
		result.setRecords(voRecords);
		return result;
	}

	@Override
	public boolean markPaid(Long orderId) {
		WmOrder order = mustGet(orderId);
		if (!TakeawayStatusConstants.Order.WAIT_PAY.equals(order.getOrderStatus())) {
			throw new IllegalStateException("仅待支付订单可支付");
		}
		WmOrder update = new WmOrder();
		update.setId(orderId);
		update.setOrderStatus(TakeawayStatusConstants.Order.PAID);
		update.setPayTime(LocalDateTime.now());
		return updateById(update);
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
		return updateById(update);
	}

	@Override
	public boolean deliveryStart(Long orderId, Long deliveryUserId) {
		WmOrder order = mustGet(orderId);
		if (!TakeawayStatusConstants.Order.MERCHANT_ACCEPTED.equals(order.getOrderStatus())) {
			throw new IllegalStateException("仅商家已接单订单可开始配送");
		}
		WmOrder update = new WmOrder();
		update.setId(orderId);
		update.setDeliveryUserId(deliveryUserId);
		update.setOrderStatus(TakeawayStatusConstants.Order.DELIVERING);
		update.setDeliveryStartTime(LocalDateTime.now());
		return updateById(update);
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
		return updateById(update);
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
		return "WM" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 10000);
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

	private OrderDTO toOrderDTO(WmOrder order, String merchantName, String riderName) {
		OrderDTO orderDTO = new OrderDTO();
		BeanUtils.copyProperties(order, orderDTO);
		orderDTO.setMerchantName(merchantName);
		orderDTO.setDeliveryRiderName(riderName);
		return orderDTO;
	}

}

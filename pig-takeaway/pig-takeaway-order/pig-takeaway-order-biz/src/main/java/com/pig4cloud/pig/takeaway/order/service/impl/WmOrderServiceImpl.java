package com.pig4cloud.pig.takeaway.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayStatusConstants;
import com.pig4cloud.pig.takeaway.common.dto.CreateOrderRequest;
import com.pig4cloud.pig.takeaway.common.dto.DeductStockRequest;
import com.pig4cloud.pig.takeaway.common.dto.DishPurchaseItemDTO;
import com.pig4cloud.pig.takeaway.common.entity.WmDish;
import com.pig4cloud.pig.takeaway.common.entity.WmOrder;
import com.pig4cloud.pig.takeaway.common.entity.WmOrderItem;
import com.pig4cloud.pig.takeaway.common.vo.OrderCreateResultVO;
import com.pig4cloud.pig.takeaway.common.vo.OrderDetailVO;
import com.pig4cloud.pig.takeaway.dish.api.DishApi;
import com.pig4cloud.pig.takeaway.order.mapper.WmOrderItemMapper;
import com.pig4cloud.pig.takeaway.order.mapper.WmOrderMapper;
import com.pig4cloud.pig.takeaway.order.service.WmOrderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@AllArgsConstructor
public class WmOrderServiceImpl extends ServiceImpl<WmOrderMapper, WmOrder> implements WmOrderService {

	private final WmOrderItemMapper wmOrderItemMapper;

	private final DishApi dishApi;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public OrderCreateResultVO createOrder(CreateOrderRequest request) {
		if (request.getItems() == null || request.getItems().isEmpty()) {
			throw new IllegalArgumentException("订单明细不能为空");
		}

		List<Long> dishIds = request.getItems().stream().map(DishPurchaseItemDTO::getDishId).distinct().toList();
		R<List<WmDish>> dishResp = dishApi.listByIds(request.getMerchantUserId(), dishIds);
		List<WmDish> dishes = unwrap(dishResp, "查询菜品失败");
		Map<Long, WmDish> dishMap = new HashMap<>(dishes.size());
		dishes.forEach(dish -> dishMap.put(dish.getId(), dish));

		BigDecimal totalAmount = BigDecimal.ZERO;
		WmOrder order = new WmOrder();
		order.setOrderNo(generateOrderNo());
		order.setCustomerUserId(request.getCustomerUserId());
		order.setMerchantUserId(request.getMerchantUserId());
		order.setDeliveryAddressId(request.getDeliveryAddressId());
		order.setOrderStatus(TakeawayStatusConstants.Order.WAIT_PAY);
		order.setRemark(request.getRemark());
		save(order);

		for (DishPurchaseItemDTO item : request.getItems()) {
			WmDish dish = dishMap.get(item.getDishId());
			if (dish == null) {
				throw new IllegalArgumentException("菜品不存在: " + item.getDishId());
			}
			BigDecimal itemAmount = dish.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
			totalAmount = totalAmount.add(itemAmount);

			WmOrderItem orderItem = new WmOrderItem();
			orderItem.setOrderId(order.getId());
			orderItem.setDishId(dish.getId());
			orderItem.setDishName(dish.getDishName());
			orderItem.setDishPrice(dish.getPrice());
			orderItem.setQuantity(item.getQuantity());
			orderItem.setItemAmount(itemAmount);
			wmOrderItemMapper.insert(orderItem);
		}

		DeductStockRequest deductRequest = new DeductStockRequest();
		deductRequest.setMerchantUserId(request.getMerchantUserId());
		deductRequest.setItems(request.getItems());
		R<Boolean> deductResp = dishApi.deductStock(deductRequest);
		Boolean deductSuccess = unwrap(deductResp, "扣减库存失败");
		if (!Boolean.TRUE.equals(deductSuccess)) {
			throw new IllegalStateException("扣减库存失败");
		}

		order.setTotalAmount(totalAmount);
		order.setPayAmount(totalAmount);
		updateById(order);

		OrderCreateResultVO result = new OrderCreateResultVO();
		result.setOrderId(order.getId());
		result.setOrderNo(order.getOrderNo());
		result.setTotalAmount(totalAmount);
		result.setPayAmount(totalAmount);
		return result;
	}

	@Override
	public OrderDetailVO detail(Long orderId) {
		WmOrder order = getById(orderId);
		if (order == null) {
			return null;
		}
		List<WmOrderItem> items = wmOrderItemMapper.selectList(Wrappers.<WmOrderItem>lambdaQuery()
			.eq(WmOrderItem::getOrderId, orderId)
			.orderByAsc(WmOrderItem::getId));
		OrderDetailVO vo = new OrderDetailVO();
		vo.setOrder(order);
		vo.setItems(items);
		return vo;
	}

	@Override
	public Page<WmOrder> queryPage(Page<WmOrder> page, Long customerUserId, Long merchantUserId, Long deliveryUserId,
			String status) {
		return page(page, Wrappers.<WmOrder>lambdaQuery()
			.eq(customerUserId != null, WmOrder::getCustomerUserId, customerUserId)
			.eq(merchantUserId != null, WmOrder::getMerchantUserId, merchantUserId)
			.eq(deliveryUserId != null, WmOrder::getDeliveryUserId, deliveryUserId)
			.eq(status != null && !status.isBlank(), WmOrder::getOrderStatus, status)
			.orderByDesc(WmOrder::getCreateTime));
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

}

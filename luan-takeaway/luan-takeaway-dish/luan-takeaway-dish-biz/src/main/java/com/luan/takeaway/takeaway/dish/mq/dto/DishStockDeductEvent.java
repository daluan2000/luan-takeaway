package com.luan.takeaway.takeaway.dish.mq.dto;

import lombok.Data;

import java.util.Map;

/**
 * 菜品库存扣减事件。
 */
@Data
public class DishStockDeductEvent {

	private Long merchantUserId;

	private String orderNo;

	private Map<Long, Integer> items;

}

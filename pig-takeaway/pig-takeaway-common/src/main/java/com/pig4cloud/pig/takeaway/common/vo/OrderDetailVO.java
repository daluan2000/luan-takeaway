package com.pig4cloud.pig.takeaway.common.vo;

import com.pig4cloud.pig.takeaway.common.entity.WmOrder;
import com.pig4cloud.pig.takeaway.common.entity.WmOrderItem;
import lombok.Data;

import java.util.List;

@Data
public class OrderDetailVO {

	private WmOrder order;

	private List<WmOrderItem> items;

}

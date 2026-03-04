package com.pig4cloud.pig.takeaway.pay.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.takeaway.common.dto.PayRequest;
import com.pig4cloud.pig.takeaway.common.entity.WmOrderPay;

public interface WmOrderPayService extends IService<WmOrderPay> {

	boolean mockPay(PayRequest request);

	Page<WmOrderPay> pageByOrderId(Page<WmOrderPay> page, Long orderId);

}

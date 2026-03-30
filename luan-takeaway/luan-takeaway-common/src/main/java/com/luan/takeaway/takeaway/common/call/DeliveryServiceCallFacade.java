package com.luan.takeaway.takeaway.common.call;

import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.dto.CreateDeliveryOrderRequest;

/**
 * 配送服务调用门面接口
 * <p>
 * 定义配送服务的所有调用方法，由具体实现类提供本地调用或远程调用
 *
 * @author luan
 */
public interface DeliveryServiceCallFacade {

	/**
	 * 创建配送单
	 * @param request 创建配送单请求
	 * @return 是否成功
	 */
	R<Boolean> createDeliveryOrder(CreateDeliveryOrderRequest request);

}

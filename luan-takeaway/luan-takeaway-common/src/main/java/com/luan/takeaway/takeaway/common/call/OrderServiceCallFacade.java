package com.luan.takeaway.takeaway.common.call;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.dto.OrderDTO;
import com.luan.takeaway.takeaway.common.entity.WmOrder;

/**
 * 订单服务调用门面接口
 * <p>
 * 定义订单服务的所有调用方法，由具体实现类提供本地调用或远程调用
 *
 * @author luan
 */
public interface OrderServiceCallFacade {

	/**
	 * 按ID查询订单
	 * @param orderId 订单ID
	 * @return 订单信息
	 */
	R<WmOrder> getById(Long orderId);

	/**
	 * 标记订单已支付
	 * @param orderId 订单ID
	 * @return 是否成功
	 */
	R<Boolean> markPaid(Long orderId);

	/**
	 * 商家接单
	 * @param orderId 订单ID
	 * @return 是否成功
	 */
	R<Boolean> merchantAccept(Long orderId);

	/**
	 * 开始配送
	 * @param orderId 订单ID
	 * @param deliveryUserId 配送员用户ID
	 * @return 是否成功
	 */
	R<Boolean> deliveryStart(Long orderId, Long deliveryUserId);

	/**
	 * 完成订单
	 * @param orderId 订单ID
	 * @return 是否成功
	 */
	R<Boolean> finish(Long orderId);

	/**
	 * 分页查询订单列表
	 * @param current 当前页
	 * @param size 每页大小
	 * @param customerUserId 客户用户ID
	 * @param merchantUserId 商家用户ID
	 * @param deliveryUserId 配送员用户ID
	 * @param status 订单状态
	 * @return 订单分页结果
	 */
	R<Page<OrderDTO>> page(long current, long size, Long customerUserId, Long merchantUserId, Long deliveryUserId,
			String status);

}

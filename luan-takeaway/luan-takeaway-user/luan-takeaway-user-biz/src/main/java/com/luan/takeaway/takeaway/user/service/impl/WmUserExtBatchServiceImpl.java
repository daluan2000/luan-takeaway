package com.luan.takeaway.takeaway.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.luan.takeaway.takeaway.common.constant.TakeawayStatusConstants;
import com.luan.takeaway.takeaway.common.dto.BatchUserExtDTO;
import com.luan.takeaway.takeaway.common.dto.BatchUserExtRequest;
import com.luan.takeaway.takeaway.common.dto.BatchUserExtResult;
import com.luan.takeaway.takeaway.common.dto.BatchUserExtResultItem;
import com.luan.takeaway.takeaway.common.entity.WmCustomerUserExt;
import com.luan.takeaway.takeaway.common.entity.WmDeliveryUserExt;
import com.luan.takeaway.takeaway.common.entity.WmMerchantUserExt;
import com.luan.takeaway.takeaway.common.mapper.WmCustomerUserExtMapper;
import com.luan.takeaway.takeaway.common.mapper.WmDeliveryUserExtMapper;
import com.luan.takeaway.takeaway.common.mapper.WmMerchantUserExtMapper;
import com.luan.takeaway.takeaway.user.service.WmUserExtBatchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户扩展信息批量导入服务实现
 *
 * <p>功能说明：实现用户扩展信息的批量导入，支持客户、商家、骑手三种类型。
 * 根据 userType 分发到对应的处理方法进行数据导入。
 *
 * <p>处理流程：
 * 1. 遍历导入项列表
 * 2. 根据 userType 调用 processImportItem 分发到对应处理方法
 * 3. 各处理方法检查用户是否已有扩展信息，若无则创建新记录
 * 4. 汇总成功/失败数量并返回结果
 *
 * @author system
 * @see WmUserExtBatchService
 * @see BatchUserExtRequest
 * @see BatchUserExtResult
 */
@Slf4j
@Service
@AllArgsConstructor
public class WmUserExtBatchServiceImpl implements WmUserExtBatchService {

	private final WmCustomerUserExtMapper wmCustomerUserExtMapper;
	private final WmMerchantUserExtMapper wmMerchantUserExtMapper;
	private final WmDeliveryUserExtMapper wmDeliveryUserExtMapper;

	/**
	 * 批量导入用户扩展信息
	 *
	 * <p>功能说明：遍历导入项列表，逐个处理并记录结果。
	 *
	 * @param request 批量导入请求
	 * @return 批量导入结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchUserExtResult batchImport(BatchUserExtRequest request) {
		List<BatchUserExtResultItem> resultItems = new ArrayList<>();
		int successCount = 0;
		int failCount = 0;

		List<BatchUserExtDTO> items = request.getItems();
		if (items == null || items.isEmpty()) {
			return BatchUserExtResult.builder()
				.total(0)
				.successCount(0)
				.failCount(0)
				.results(resultItems)
				.build();
		}

		for (BatchUserExtDTO item : items) {
			BatchUserExtResultItem resultItem = processImportItem(item);
			resultItems.add(resultItem);
			if (resultItem.isSuccess()) {
				successCount++;
			}
			else {
				failCount++;
			}
		}

		return BatchUserExtResult.builder()
			.total(items.size())
			.successCount(successCount)
			.failCount(failCount)
			.results(resultItems)
			.build();
	}

	/**
	 * 处理单个导入项
	 *
	 * <p>功能说明：根据 userType 分发到对应的处理方法。
	 *
	 * <p>支持的类型：
	 * - customer：客户扩展信息
	 * - merchant：商家扩展信息
	 * - delivery：骑手扩展信息
	 *
	 * @param dto 单个导入项
	 * @return 单条导入结果
	 */
	private BatchUserExtResultItem processImportItem(BatchUserExtDTO dto) {
		String userType = dto.getUserType();

		if (!StringUtils.hasText(userType)) {
			return BatchUserExtResultItem.builder()
				.userId(dto.getUserId())
				.userType(userType)
				.success(false)
				.errorMessage("用户类型不能为空")
				.build();
		}

		return switch (userType.toLowerCase()) {
			case "customer" -> processCustomerImport(dto);
			case "merchant" -> processMerchantImport(dto);
			case "delivery" -> processDeliveryImport(dto);
			default -> BatchUserExtResultItem.builder()
				.userId(dto.getUserId())
				.userType(userType)
				.success(false)
				.errorMessage("不支持的用户类型: " + userType + "，支持类型：customer, merchant, delivery")
				.build();
		};
	}

	/**
	 * 处理客户扩展信息导入
	 *
	 * <p>功能说明：创建客户扩展信息记录。
	 *
	 * <p>处理步骤：
	 * 1. 检查用户是否已有客户信息
	 * 2. 创建 WmCustomerUserExt 记录，设置用户ID和真实姓名
	 * 3. 插入数据库并返回结果
	 *
	 * @param dto 导入项
	 * @return 单条导入结果
	 */
	private BatchUserExtResultItem processCustomerImport(BatchUserExtDTO dto) {
		try {
			// 检查用户是否已存在客户信息
			Long existCount = wmCustomerUserExtMapper.selectCount(
				Wrappers.<WmCustomerUserExt>lambdaQuery().eq(WmCustomerUserExt::getUserId, dto.getUserId()));
			if (existCount != null && existCount > 0) {
				return BatchUserExtResultItem.builder()
					.userId(dto.getUserId())
					.userType("customer")
					.success(false)
					.errorMessage("用户已存在客户信息")
					.build();
			}

			WmCustomerUserExt customer = new WmCustomerUserExt();
			customer.setUserId(dto.getUserId());
			customer.setRealName(dto.getRealName());


			if (wmCustomerUserExtMapper.insert(customer) <= 0) {
				return BatchUserExtResultItem.builder()
					.userId(dto.getUserId())
					.userType("customer")
					.success(false)
					.errorMessage("插入客户信息失败")
					.build();
			}

			return BatchUserExtResultItem.builder()
				.userId(dto.getUserId())
				.userType("customer")
				.success(true)
				.extId(customer.getId())
				.build();
		}
		catch (Exception e) {
			log.error("导入客户信息失败: userId={}", dto.getUserId(), e);
			return BatchUserExtResultItem.builder()
				.userId(dto.getUserId())
				.userType("customer")
				.success(false)
				.errorMessage("导入失败: " + e.getMessage())
				.build();
		}
	}

	/**
	 * 处理商家扩展信息导入
	 *
	 * <p>功能说明：创建商家扩展信息记录。
	 *
	 * <p>处理步骤：
	 * 1. 检查用户是否已有商家信息
	 * 2. 创建 WmMerchantUserExt 记录，设置联系人、店铺名称、地址等
	 * 3. 设置默认审核状态（待审核）和营业状态（营业中）
	 * 4. 插入数据库并返回结果
	 *
	 * @param dto 导入项
	 * @return 单条导入结果
	 */
	private BatchUserExtResultItem processMerchantImport(BatchUserExtDTO dto) {
		try {
			// 检查用户是否已存在商家信息
			Long existCount = wmMerchantUserExtMapper.selectCount(
				Wrappers.<WmMerchantUserExt>lambdaQuery().eq(WmMerchantUserExt::getUserId, dto.getUserId()));
			if (existCount != null && existCount > 0) {
				return BatchUserExtResultItem.builder()
					.userId(dto.getUserId())
					.userType("merchant")
					.success(false)
					.errorMessage("用户已存在商家信息")
					.build();
			}

			WmMerchantUserExt merchant = new WmMerchantUserExt();
			merchant.setUserId(dto.getUserId());
			merchant.setContactName(dto.getRealName());
			merchant.setMerchantName(dto.getStoreName());
			merchant.setStoreAddressId(dto.getStoreAddressId());

			// 设置审核状态，默认待审核
			if (StringUtils.hasText(dto.getAuditStatus())) {
				merchant.setAuditStatus(dto.getAuditStatus());
			}
			else {
				merchant.setAuditStatus(TakeawayStatusConstants.Merchant.AUDIT_PENDING);
			}

			// 设置营业状态，默认营业
			if (StringUtils.hasText(dto.getBusinessStatus())) {
				merchant.setBusinessStatus(dto.getBusinessStatus());
			}
			else {
				merchant.setBusinessStatus(TakeawayStatusConstants.Merchant.BUSINESS_OPEN);
			}

			if (wmMerchantUserExtMapper.insert(merchant) <= 0) {
				return BatchUserExtResultItem.builder()
					.userId(dto.getUserId())
					.userType("merchant")
					.success(false)
					.errorMessage("插入商家信息失败")
					.build();
			}

			return BatchUserExtResultItem.builder()
				.userId(dto.getUserId())
				.userType("merchant")
				.success(true)
				.extId(merchant.getId())
				.build();
		}
		catch (Exception e) {
			log.error("导入商家信息失败: userId={}", dto.getUserId(), e);
			return BatchUserExtResultItem.builder()
				.userId(dto.getUserId())
				.userType("merchant")
				.success(false)
				.errorMessage("导入失败: " + e.getMessage())
				.build();
		}
	}

	/**
	 * 处理骑手扩展信息导入
	 *
	 * <p>功能说明：创建骑手扩展信息记录。
	 *
	 * <p>处理步骤：
	 * 1. 检查用户是否已有骑手信息
	 * 2. 创建 WmDeliveryUserExt 记录，设置姓名和配送范围
	 * 3. 设置默认在线状态（离线）和雇佣状态（在职）
	 * 4. 插入数据库并返回结果
	 *
	 * @param dto 导入项
	 * @return 单条导入结果
	 */
	private BatchUserExtResultItem processDeliveryImport(BatchUserExtDTO dto) {
		try {
			// 检查用户是否已存在骑手信息
			Long existCount = wmDeliveryUserExtMapper.selectCount(
				Wrappers.<WmDeliveryUserExt>lambdaQuery().eq(WmDeliveryUserExt::getUserId, dto.getUserId()));
			if (existCount != null && existCount > 0) {
				return BatchUserExtResultItem.builder()
					.userId(dto.getUserId())
					.userType("delivery")
					.success(false)
					.errorMessage("用户已存在骑手信息")
					.build();
			}

			WmDeliveryUserExt delivery = new WmDeliveryUserExt();
			delivery.setUserId(dto.getUserId());
			delivery.setRealName(dto.getRealName());

			// 骑手配送范围
			if (dto.getDeliveryScopeKm() != null) {
				delivery.setDeliveryScopeKm(dto.getDeliveryScopeKm());
			}

			// 设置在线状态，默认离线
			if (StringUtils.hasText(dto.getOnlineStatus())) {
				delivery.setOnlineStatus(dto.getOnlineStatus());
			}
			else {
				delivery.setOnlineStatus(TakeawayStatusConstants.Delivery.ONLINE_OFF);
			}

			// 设置雇佣状态，默认在职
			if (StringUtils.hasText(dto.getEmploymentStatus())) {
				delivery.setEmploymentStatus(dto.getEmploymentStatus());
			}
			else {
				delivery.setEmploymentStatus(TakeawayStatusConstants.Delivery.EMPLOYMENT_ON);
			}

			if (wmDeliveryUserExtMapper.insert(delivery) <= 0) {
				return BatchUserExtResultItem.builder()
					.userId(dto.getUserId())
					.userType("delivery")
					.success(false)
					.errorMessage("插入骑手信息失败")
					.build();
			}

			return BatchUserExtResultItem.builder()
				.userId(dto.getUserId())
				.userType("delivery")
				.success(true)
				.extId(delivery.getId())
				.build();
		}
		catch (Exception e) {
			log.error("导入骑手信息失败: userId={}", dto.getUserId(), e);
			return BatchUserExtResultItem.builder()
				.userId(dto.getUserId())
				.userType("delivery")
				.success(false)
				.errorMessage("导入失败: " + e.getMessage())
				.build();
		}
	}

}

package com.luan.takeaway.takeaway.user.dto.ws;

import com.luan.takeaway.admin.api.dto.ws.AbstractWsMessage;
import com.luan.takeaway.admin.api.dto.ws.WsMessageStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 商家审核结果 WebSocket 消息。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class MerchantAuditResultWsMessage extends AbstractWsMessage {

	public static final String CATEGORY = "BUSINESS";

	public static final String BUSINESS_TYPE = "MERCHANT";

	public static final String EVENT_TYPE = "AUDIT_RESULT";

	private Long merchantId;

	private Long userId;

	private String auditStatus;

	public static MerchantAuditResultWsMessage approved(Long merchantId, Long userId) {
		MerchantAuditResultWsMessage message = new MerchantAuditResultWsMessage();
		message.setCategory(CATEGORY);
		message.setBusinessType(BUSINESS_TYPE);
		message.setEventType(EVENT_TYPE);
		message.setStatus(WsMessageStatus.SUCCESS);
		message.setTitle("商家审核结果通知");
		message.setContent("商家审核已通过");
		message.setTimestamp(System.currentTimeMillis());
		message.setMerchantId(merchantId);
		message.setUserId(userId);
		message.setAuditStatus("1");
		return message;
	}

}

package com.luan.takeaway.takeaway.common.constant;

public interface TakeawayStatusConstants {

	interface Common {

		String DEL_NORMAL = "0";

		String DEL_DELETED = "1";

	}

	interface Merchant {

		String BUSINESS_REST = "0";

		String BUSINESS_OPEN = "1";

		String AUDIT_PENDING = "0";

		String AUDIT_APPROVED = "1";

		String AUDIT_REJECTED = "2";

	}

	interface Dish {

		String SALE_OFF = "0";

		String SALE_ON = "1";

	}

	// '订单状态 0待支付 1已支付（未接单） 2已接单（未配送） 3配送中 4已完成 5已取消',
	interface Order {

		String WAIT_PAY = "0";

		String PAID = "1";

		String MERCHANT_ACCEPTED = "2";

		String DELIVERING = "3";

		String FINISHED = "4";

		String CANCELED = "5";

	}

	interface Pay {

		String WAIT_PAY = "0";

		String SUCCESS = "1";

		String FAILED = "2";

		String CHANNEL_MOCK = "0";

	}

	interface Delivery {

		String WAIT_ACCEPT = "0";

		String ACCEPTED = "1";

		String DELIVERING = "2";

		String ARRIVED = "3";

		String CANCELED = "4";

		String ONLINE_OFF = "0";

		String ONLINE_ON = "1";

		String EMPLOYMENT_OFF = "0";

		String EMPLOYMENT_ON = "1";

	}

}

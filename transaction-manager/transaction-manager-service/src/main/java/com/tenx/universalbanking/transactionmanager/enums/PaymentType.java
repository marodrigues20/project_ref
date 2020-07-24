package com.tenx.universalbanking.transactionmanager.enums;

public enum PaymentType {

	SINGLE_IMMEDIATE_PAYMENT("10"),
	FUTURE_DATED_PAYMENT("11"),
	STANDING_ORDER_PAYMENT("12");

	private final String paymentType;

	PaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getPaymentType() {
		return paymentType;
	}
}

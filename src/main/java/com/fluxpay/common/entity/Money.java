package com.fluxpay.common.entity;

public class Money {
    private int amountUnit;
    private String currency;

    public Money(int amountUnit, String currency){
        this.amountUnit = amountUnit;
        this.currency = currency;
    }

    public Money of(int amountUnit, String currency){
        return new Money(amountUnit, currency);
    }

    public Money inr(int amountUnit, String currency){
        return
    }
}

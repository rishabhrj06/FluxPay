package com.fluxpay.common.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Money {
    private int amountUnits;
    private String currency;

    public static Money of(int amountUnits, String currency){
        return new Money(amountUnits, currency);
    }

    public static Money inr(int amountUnits){
        return new Money(amountUnits, "INR");
    }

    public Money add(Money other){
        if(!this.currency.equals(other.currency)){
            throw new IllegalArgumentException("Currency Mismatch");
        }
        return new Money(this.amountUnits + other.amountUnits, this.currency);
    }

    public Money sub(Money other){
        if(!this.currency.equals(other.currency)){
            throw new IllegalArgumentException("Currency Mismatch");
        }
        return new Money(this.amountUnits - other.amountUnits, this.currency);
    }
}

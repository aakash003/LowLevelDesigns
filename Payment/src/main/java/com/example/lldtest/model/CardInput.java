package com.example.lldtest.model;

import com.example.lldtest.service.interfaces.IPaymentInput;

public class CardInput  {

    private String cardNumber; //123-343-123

    private Integer cvv;

    private Integer pin;

    public CardInput(String cardNumber, Integer cvv, Integer pin) {
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.pin = pin;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Integer getCvv() {
        return cvv;
    }

    public void setCvv(Integer cvv) {
        this.cvv = cvv;
    }

    public Integer getPin() {
        return pin;
    }

    public void setPin(Integer pin) {
        this.pin = pin;
    }
}

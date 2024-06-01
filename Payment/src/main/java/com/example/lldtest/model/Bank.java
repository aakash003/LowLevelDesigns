package com.example.lldtest.model;

import com.example.lldtest.service.interfaces.IPaymentMethod;

import java.util.List;
import java.util.Random;

public class Bank {

    private List<IPaymentMethod> acceptedPaymentMethods;

    private String bankId;

    public Bank(List<IPaymentMethod> acceptedPaymentMethods, String bankId) {
        this.acceptedPaymentMethods = acceptedPaymentMethods;
        this.bankId = bankId;
    }

    public List<IPaymentMethod> getAcceptedPaymentMethods() {
        return acceptedPaymentMethods;
    }

    public void setAcceptedPaymentMethods(List<IPaymentMethod> acceptedPaymentMethods) {
        this.acceptedPaymentMethods = acceptedPaymentMethods;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public boolean makePayment(Integer amount){
        Random rd = new Random();
        return rd.nextBoolean();

    }




}

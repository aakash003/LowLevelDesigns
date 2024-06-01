package com.example.lldtest.service;


import com.example.lldtest.service.interfaces.IPaymentInput;
import com.example.lldtest.service.interfaces.IPaymentMethod;

public class UpiPaymentService implements IPaymentMethod {

    private boolean isDisabled;

    private String id;

    private IPaymentInput paymentInput;

    public UpiPaymentService(boolean isDisabled, String i, IPaymentInput paymentInput) {
        this.isDisabled = isDisabled;
        this.id = id;
        this.paymentInput = paymentInput;
    }

    public boolean isDisabled() {
        return isDisabled;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void disablePaymentMethod(){
        this.isDisabled = true;
    }


}

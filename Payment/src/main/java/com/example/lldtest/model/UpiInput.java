package com.example.lldtest.model;

import com.example.lldtest.service.interfaces.IPaymentInput;

public class UpiInput {

    private String uipId;

    public UpiInput(String uipId) {
        this.uipId = uipId;
    }

    public String getUipId() {
        return uipId;
    }

    public void setUipId(String uipId) {
        this.uipId = uipId;
    }
}

package com.example.lldtest.service;


import com.example.lldtest.service.interfaces.IPaymentInput;
import com.example.lldtest.service.interfaces.IPaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentService implements IPaymentMethod {


    private boolean isDisabled;

    private String id; // name

    private IPaymentInput paymentInput;

    public CardPaymentService(boolean isDisabled, String id, IPaymentInput paymentInput) {
        this.isDisabled = isDisabled;
        this.id = id;
        this.paymentInput = paymentInput;
    }

    @Override
    public void disablePaymentMethod(){
        this.isDisabled = true;
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
}

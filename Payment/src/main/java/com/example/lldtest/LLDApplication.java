package com.example.lldtest;
import org.springframework.stereotype.Component;

@Component
public class LLDApplication {

    public static void main(String[] args) {
        PaymentMethodApplication paymentMethodApplication = new PaymentMethodApplication();
        paymentMethodApplication.startApplication();
    }

}

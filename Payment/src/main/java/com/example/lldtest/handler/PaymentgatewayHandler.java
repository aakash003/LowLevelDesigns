package com.example.lldtest.handler;

import com.example.lldtest.model.Bank;
import com.example.lldtest.model.PaymentGateway;
import com.example.lldtest.service.interfaces.IPaymentMethod;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Data
public class PaymentgatewayHandler {
    private PaymentGateway paymentGateway;

    List<IPaymentMethod> paymentMethods;


    public PaymentgatewayHandler(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
        this.paymentMethods = new ArrayList<>();
    }

    public List<IPaymentMethod> supportedPaymentMethods() {
        return paymentMethods;
    }

    public boolean addSupportForPaymode(IPaymentMethod paymentMethod) {
        return paymentMethods.add(paymentMethod);
    }

    public void removePaymode(String paymentMethodId) {
        for (IPaymentMethod paymentMethod : paymentMethods) {
            if (paymentMethod.getId().equals(paymentMethodId)) ;
            paymentMethod.disablePaymentMethod();
        }
    }

    public List<Bank> getBanks(IPaymentMethod paymentMethod) {
        List<Bank> bankList = paymentGateway.getSubscribedBanks();
        List<Bank> banks = new ArrayList<>();
        for (Bank bank : bankList) {
            if (bank.getAcceptedPaymentMethods().contains(paymentMethod))
                banks.add(bank);
        }
        return banks;
    }

    public void changeWeightBank(Bank bank, Integer wei) {
        this.paymentGateway.bankweightMap.put(bank, this.paymentGateway.bankweightMap.get(bank) + wei);
    }

    public Map<Bank, Integer> getStatergy() {
        return paymentGateway.bankweightMap;
    }
}

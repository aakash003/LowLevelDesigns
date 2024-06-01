package com.example.lldtest.service;



import com.example.lldtest.model.Bank;
import com.example.lldtest.model.PaymentGateway;
import com.example.lldtest.service.interfaces.IBankStatergy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class WeightedStrategy implements IBankStatergy {

    private PaymentGateway paymentGateway;

    public void modifyWeight(Bank bank, Integer weight){
        this.paymentGateway.bankweightMap.put(bank,weight);
    }

    public Map<Bank, Integer> getBankweightMap() {
        return this.paymentGateway.bankweightMap;
    }

    public void setBankweightMap(Map<Bank, Integer> bankweightMap) {
        this.paymentGateway.bankweightMap = bankweightMap;
    }

    @Override
    public Bank getBankByWeight(PaymentGateway paymentGateway, List<Bank> bankList) {
        Bank bank = null;
        Integer bestWeight = Integer.MIN_VALUE;
        for(Bank bank1 : bankList){
            if(bestWeight< paymentGateway.bankweightMap.get(bank1)){
                bank = bank1;
            }
        }
        return bank;
    }
}

package com.example.lldtest.model;

import com.example.lldtest.model.Bank;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PaymentGateway {

    private String id;

    List<Bank> subscribedBanks;

    public Map<Bank, Integer> bankweightMap;

    public PaymentGateway(String id, List<Bank> subscribedBanks) {
        this.bankweightMap = new HashMap<>();
        this.id = id;
        this.subscribedBanks = subscribedBanks;
    }

}

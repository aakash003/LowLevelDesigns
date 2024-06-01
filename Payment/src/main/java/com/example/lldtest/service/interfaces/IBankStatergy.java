package com.example.lldtest.service.interfaces;

import com.example.lldtest.model.Bank;
import com.example.lldtest.model.PaymentGateway;

import java.util.List;

public interface IBankStatergy {

    Bank getBankByWeight(PaymentGateway paymentGateway, List<Bank> bankList);

}

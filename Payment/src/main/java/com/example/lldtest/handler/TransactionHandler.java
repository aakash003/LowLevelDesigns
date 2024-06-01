package com.example.lldtest.handler;


import com.example.lldtest.model.Bank;
import com.example.lldtest.model.Client;
import com.example.lldtest.model.PaymentGateway;
import com.example.lldtest.model.Transaction;
import com.example.lldtest.service.interfaces.IBankStatergy;
import com.example.lldtest.service.interfaces.IPaymentInput;
import com.example.lldtest.service.interfaces.IPaymentMethod;
import com.example.lldtest.service.WeightedStrategy;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

public class TransactionHandler {



    public boolean makePayment(ClientHandler clientHandler,PaymentgatewayHandler paymentgatewayHandler,Client client, IPaymentMethod paymentMethod, Integer amount, IPaymentInput paymentInput) {
        if (clientHandler.hasClient(client.getClientId()) && clientHandler.supportsPayment(client, paymentMethod)) {
            IBankStatergy bankStatergy = new WeightedStrategy();
            List<Bank> bankList = paymentgatewayHandler.getBanks(paymentMethod);

            Bank bank = bankStatergy.getBankByWeight(paymentgatewayHandler.getPaymentGateway(), bankList);
            boolean transactionResult = bank.makePayment(amount);
            if (transactionResult) {
                paymentgatewayHandler.changeWeightBank(bank, 1);
                System.out.println("Payment Successful");
                Transaction transaction = new Transaction(UUID.randomUUID().toString(),amount, true);
                System.out.println(transaction);
            } else {
                Transaction transaction = new Transaction(UUID.randomUUID().toString(),amount, false);
                System.out.println("Payment Failed");
                System.out.println(transaction);
                paymentgatewayHandler.changeWeightBank(bank, -1);
            }
            return transactionResult;
        }
        return false;
    }


}

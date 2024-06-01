package com.example.lldtest;

import com.example.lldtest.handler.ClientHandler;
import com.example.lldtest.handler.PaymentgatewayHandler;
import com.example.lldtest.handler.TransactionHandler;
import com.example.lldtest.model.Bank;
import com.example.lldtest.model.Client;
import com.example.lldtest.service.CardPaymentService;
import com.example.lldtest.model.PaymentGateway;
import com.example.lldtest.service.UpiPaymentService;
import com.example.lldtest.service.interfaces.IPaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PaymentMethodApplication {

    public void startApplication() {
        List<IPaymentMethod> paymentMethodList = new ArrayList<>();
        paymentMethodList.add(new CardPaymentService(false, "card", null));
        paymentMethodList.add(new UpiPaymentService(false, "upi", null));

        Bank bank1  = new Bank(paymentMethodList, "icici");
        Bank bank2  = new Bank(paymentMethodList, "hdfc");

        PaymentGateway paymentGateway = new PaymentGateway("1", List.of(bank1, bank2));
        paymentGateway.getBankweightMap().put(bank1, 70);
        paymentGateway.getBankweightMap().put(bank2, 30);


        PaymentgatewayHandler paymentgatewayHandler = new PaymentgatewayHandler(paymentGateway);
        paymentgatewayHandler.addSupportForPaymode(new CardPaymentService(false, "card", null));
        paymentgatewayHandler.addSupportForPaymode(new UpiPaymentService(false, "upi", null));

        ClientHandler clientHandler = new ClientHandler();
        List<String> paymentMethodIds = new ArrayList<>();
        paymentMethodIds.add("card");
        paymentMethodIds.add("upi");
        clientHandler.addClient(paymentgatewayHandler, "1", paymentMethodIds, "flipkart");

        Client client = clientHandler.getClientById("1");

        TransactionHandler transactionHandler = new TransactionHandler();
        transactionHandler.makePayment(clientHandler,paymentgatewayHandler,client, paymentMethodList.get(0), 100, null);

        for (IPaymentMethod paymentMethod: paymentMethodList) {
            paymentMethod.getId();
        }
    }
}

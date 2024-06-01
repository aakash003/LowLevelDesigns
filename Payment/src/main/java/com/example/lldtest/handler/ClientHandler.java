package com.example.lldtest.handler;

import com.example.lldtest.model.Client;
import com.example.lldtest.service.interfaces.IPaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

public class ClientHandler {

    List<Client> clientList = new ArrayList<>();


    public void addClient(PaymentgatewayHandler paymentGateway,String clientId, List<String> paymentMethodIds, String clientName) {
        List<IPaymentMethod> supportedPaymentMethods = new ArrayList<>();
        for (IPaymentMethod paymentMethod : paymentGateway.supportedPaymentMethods()) {
            if (paymentMethodIds.contains(paymentMethod.getId())) {
                supportedPaymentMethods.add(paymentMethod);
            }
        }
        Client client = new Client(clientId, clientName, false, supportedPaymentMethods);
        clientList.add(client);
    }


    public void disableClient(Client client) {
        if (hasClient(client.getClientId()))
            clientList.remove(client);

    }

    public Client getClientById(String id) {
        for (Client client : clientList) {
            if (client.getClientId().equals(id)) {
                return client;
            }
        }
        return null;
    }

    public boolean hasClient(String clientId) { // handled null
        Client client = getClientById(clientId);
        if (client.isDisabled()) {
            return false;
        }
        return true;

    }

    public boolean supportsPayment(Client client, IPaymentMethod paymentMethod) {
           return client.getSubscribedPaymentMethods().stream().anyMatch(payment -> payment.getId().equals(paymentMethod.getId()));
    }


}

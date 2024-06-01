package com.example.lldtest.model;

import com.example.lldtest.service.interfaces.IPaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Client {

    private String clientId;

    private String clientName;

    private boolean isDisabled;


    List<IPaymentMethod> subscribedPaymentMethods;

    public boolean isDisabled() {
        return isDisabled;
    }

}

package com.example.lldtest.model;

public class Transaction {

    private String transactionId;
    private Integer transactionAmount;

    private boolean transactionStatus;

    public Transaction(String transactionId, Integer transactionAmount, boolean transactionStatus) {
        this.transactionId = transactionId;
        this.transactionAmount = transactionAmount;
        this.transactionStatus = transactionStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(Integer transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public boolean isTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(boolean transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
}

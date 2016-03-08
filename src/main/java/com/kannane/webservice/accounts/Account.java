package com.kannane.webservice.accounts;

import com.kannane.webservice.ServiceException;

public class Account {

    private final Long id;
    private final String name;
    private Double balance;

    public Account(Long id, String name) {
        this(id, name, 0.0);
    }

    public Account(Long id, String name, Double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public void withdraw(Double amount) {
        //Let us assume we cannot go into negative
        if (amount > balance) {
            throw new ServiceException("Could not withdraw [" + amount + "]. " +
                    "Balance available is only [" + balance + "]");
        }
        balance -= amount;
    }

    public void deposit(Double amount) {
        //Let us assume the sum is not going to be near Double.MAX
        balance += amount;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getBalance() {
        return balance;
    }
}

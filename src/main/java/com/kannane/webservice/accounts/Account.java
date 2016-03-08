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
        if (amount > balance) {
            throw new ServiceException("Could not withdraw [" + amount + "]. " +
                    "Balance available is only [" + balance + "]");
        }
        balance -= amount;
    }

    public void deposit(Double amount) {
        if (balance + amount <= balance) {
            throw new ServiceException("Rounding error with balance. Balance went too high");
        }
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

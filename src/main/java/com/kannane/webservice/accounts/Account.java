package com.kannane.webservice.accounts;

import com.kannane.webservice.ServiceException;

public class Account {

    private final Long id;
    private final String name;
    private Double balance;

    public Account(Long id, String name, Double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    void withdraw(Double amount) {
        if (amount > balance) {
            throw new ServiceException("Could not withdraw [" + amount + "]. " +
                    "Balance available is only [" + balance + "]");
        }
        balance -= amount;
    }

    void deposit(Double amount) {
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Account{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", balance=").append(balance);
        sb.append('}');
        return sb.toString();
    }
}

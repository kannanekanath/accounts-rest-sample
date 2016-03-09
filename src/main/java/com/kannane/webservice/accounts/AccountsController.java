package com.kannane.webservice.accounts;

import com.kannane.webservice.ServiceException;
import spark.Request;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

public class AccountsController {

    private final AccountService accountService;

    public AccountsController(AccountService accountService) {
        this.accountService = accountService;
    }

    public Account loadAccount(Request request) {
        Long id = parseLong(request.params(":id"));
        Optional<Account> account = accountService.findAccount(id);
        account.orElseThrow(() -> new ServiceException("No account found for id [" + id + "]", 404));
        return account.get();
    }

    public Map<String, String> transferMoney(Request req) {
        accountService.transferMoney(parseLong(req.params(":from")),
                parseLong(req.params(":to")), parseDouble(req.params(":amount")));
        return Collections.singletonMap("success", "true");
    }

    public Account createAccount(Request request) {
        Account account = new Account(0L, request.queryParams("name"), parseDouble(request.queryParams("balance")));
        return accountService.createAccount(account);
    }

    public Account deleteAccount(Request request) {
        return accountService.deleteAccount(parseLong(request.params(":id")));
    }
}

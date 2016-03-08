package com.kannane.webservice;

import com.google.gson.Gson;
import com.kannane.webservice.accounts.Account;
import com.kannane.webservice.accounts.AccountService;
import com.kannane.webservice.accounts.InMemoryAccountService;
import com.kannane.webservice.response.JsonTransformer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.post;

public class Server {

    /**
     * In real world this will be via a Dependency Injection framework like GUICE *NOT SPRING*
     */
    private AccountService accountService = new InMemoryAccountService();

    public static void main(String[] args) {
        new Server().startRouting();
    }

    public void startRouting() {
        get("/", (req, res) -> "This is the root of the app. Please visit individual paths/resources");
        get("/account/:id", ((req, res) -> {
            String id = req.params(":id");
            Optional<Account> account = accountService.findAccountById(id);
            //Perhaps we can make this show 404 as that would be strictly REST
            account.orElseThrow(() -> new ServiceException("No account found for id [" + id + "]", 404));
            return account.get();
        }), new JsonTransformer());
        post("/transfer/from/:from/to/:to/amount/:amount", ((req, res) -> {
            accountService.transferMoney(req.params(":from"), req.params(":to"),
                    Double.parseDouble(req.params(":amount")));
            return Collections.singletonMap("success", "true");
        }), new JsonTransformer());
        exception(Exception.class, (e, request, response) -> {
            int statusCode = 500;
            if (e instanceof ServiceException) {
                //Application can indicate what status code to propogate
                statusCode = ((ServiceException) e).getHttpErrorCode();
            }
            response.status(statusCode);
            Map<String, String> messages = new HashMap<>();
            messages.put("exception", e.getClass().getName());
            messages.put("message", e.getMessage());
            //We can optionally/in debug mode show the entire stack trace to the user
            Gson gson = new Gson();
            response.body(gson.toJson(messages));
        });
    }

}

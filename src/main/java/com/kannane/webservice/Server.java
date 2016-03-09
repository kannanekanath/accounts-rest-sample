package com.kannane.webservice;

import com.google.gson.Gson;
import com.kannane.webservice.accounts.AccountsController;
import com.kannane.webservice.accounts.InMemoryAccountService;
import com.kannane.webservice.response.JsonTransformer;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class Server {

    /**
     * In real world this will be via a Dependency Injection framework like GUICE *NOT SPRING*
     */
    private final AccountsController accountsController = new AccountsController(new InMemoryAccountService());

    public static void main(String[] args) {
        new Server().startRouting();
    }

    public void startRouting() {
        before((request, response) -> response.header("Content-Type", "application/json"));
        get("/", (req, res) -> "This is the root of the app. Please visit individual paths/resources");
        get("/account/:id", (req, res) -> accountsController.loadAccount(req), new JsonTransformer());
        put("/account", (req, res) -> accountsController.createAccount(req), new JsonTransformer());
        delete("/account/:id", (req, res) -> accountsController.deleteAccount(req), new JsonTransformer());
        post("/transfer/from/:from/to/:to/amount/:amount",
                (req, res) -> accountsController.transferMoney(req), new JsonTransformer());
        exception(Exception.class, (e, request, response) -> {
            int statusCode = 500;
            if (e instanceof ServiceException) {
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

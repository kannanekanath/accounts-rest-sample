package com.kannane.webservice.accounts;

import com.kannane.webservice.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spark.Spark;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

public class AccountServiceDSLTest {

    private String path;

    @Before
    public void before() {
        Server server = new Server();
        server.startRouting();
        //Wait until we are ready to serve requests
        Spark.awaitInitialization();
        path = "http://localhost:4567";
        insertAccount("user1", 45.0);
        insertAccount("user2", 97.5);
        insertAccount("user3", 89.0);
        insertAccount("user4", Double.MAX_VALUE);
    }

    private void insertAccount(String name, Double balance) {
        given().queryParam("name", name).queryParam("balance", balance).put(path + "/account")
                .then().statusCode(is(200));
    }

    @After
    public void after() {
        Spark.stop();
    }

    @Test
    public void testRootPathWorks() {
        given().
                when().get(path + "/").
                then().statusCode(is(200));
    }

    @Test
    public void testNonExistingPath() {
        given().
                when().get(path + "/random").
                then().statusCode(is(404));
    }

    @Test
    public void testCreateAccount() {
        given().
            queryParam("name", "user20").
            queryParam("balance", 45).
        when().
            put(path + "/account").
        then().
            statusCode(is(200)).
            body("name", is("user20")).
            body("balance", is((float) 45)).
            body("id", notNullValue());
    }

    @Test
    public void testGetAccount() {
        given().
            pathParam("id", 1).
        when().
            get(path + "/account/{id}").
        then().
            statusCode(is(200)).
            body("name", is("user1")).
            body("id", is(1)).
            body("balance", is(new Float(45.0)));
    }

    @Test
    public void testGetInvalidAccount() {
        given().
            pathParam("id", 2345).
        when().
            get(path + "/account/{id}").
        then().
            statusCode(is(404));
    }

    @Test
    public void testTransferPositive() {
        given().
            pathParam("fromAccount", 1).
            pathParam("toAccount", 2).
            pathParam("amount", 20).
        when().
            post(path + "/transfer/from/{fromAccount}/to/{toAccount}/amount/{amount}").
        then().
            statusCode(is(200)).
            body("success", is("true"));

        given().
            pathParam("fromAccount", 1).
        when().
            get(path + "/account/{fromAccount}").
        then().
            body("balance", is((float) 25));

        given().
            pathParam("fromAccount", 2).
        when().
            get(path + "/account/{fromAccount}").
        then().
            body("balance", is((float) 117.5));
    }

    @Test
    public void testInvalidTransferFlows() {
        given().
            pathParam("fromAccount", 231234).
            pathParam("toAccount", 2).
            pathParam("amount", 20).
        when().
            post(path + "/transfer/from/{fromAccount}/to/{toAccount}/amount/{amount}").
        then().
            statusCode(is(500));

        given().
            pathParam("fromAccount", 1).
            pathParam("toAccount", 2).
            pathParam("amount", 200000).
        when().
            post(path + "/transfer/from/{fromAccount}/to/{toAccount}/amount/{amount}").
        then().
            statusCode(is(500)).
            body("message", startsWith("Could not withdraw"));

        given().
            pathParam("fromAccount", 1).
            pathParam("toAccount", 1).
            pathParam("amount", 1).
        when().
            post(path + "/transfer/from/{fromAccount}/to/{toAccount}/amount/{amount}").
        then().
            statusCode(is(500)).
            body("message", startsWith("The from and to account should not be the same"));

        given().
            pathParam("fromAccount", 2).
            pathParam("toAccount", 4).
            pathParam("amount", 2).
        when().
            post(path + "/transfer/from/{fromAccount}/to/{toAccount}/amount/{amount}").
        then().
            statusCode(is(500)).
            body("message", startsWith("Rounding error with balance"));
    }
}

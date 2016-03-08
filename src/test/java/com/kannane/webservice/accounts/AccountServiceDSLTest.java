package com.kannane.webservice.accounts;

import com.kannane.webservice.Server;
import org.junit.*;
import spark.Spark;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

public class AccountServiceDSLTest {

    private Server server;
    private String path;

    @Before
    public void before() {
        server = new Server();
        server.startRouting();
        //Wait until we are ready to serve requests
        Spark.awaitInitialization();
        path = "http://localhost:4567";
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
        given().pathParam("fromAccount", 1).
                pathParam("toAccount", 2).
                pathParam("amount", 20).
                when().
                post(path + "/transfer/from/{fromAccount}/to/{toAccount}/amount/{amount}").
                then().
                statusCode(is(200)).
                body("success", is("true"));
        given().pathParam("fromAccount", 1).
                when().
                get(path + "/account/{fromAccount}").
                then().
                body("balance", is((float) 25));
        given().pathParam("fromAccount", 2).
                when().
                get(path + "/account/{fromAccount}").
                then().
                body("balance", is((float) 99.5));
    }

    @Test
    public void testInvalidTransferFlows() {
        given().pathParam("fromAccount", 231234).
                pathParam("toAccount", 2).
                pathParam("amount", 20).
                when().
                post(path + "/transfer/from/{fromAccount}/to/{toAccount}/amount/{amount}").
                then().
                statusCode(is(500));
        given().pathParam("fromAccount", 1).
                pathParam("toAccount", 2).
                pathParam("amount", 200000).
                when().
                post(path + "/transfer/from/{fromAccount}/to/{toAccount}/amount/{amount}").
                then().
                statusCode(is(500)).
                body("message", startsWith("Could not withdraw"));
        given().pathParam("fromAccount", 1).
                pathParam("toAccount", 1).
                pathParam("amount", 200000).
                when().
                post(path + "/transfer/from/{fromAccount}/to/{toAccount}/amount/{amount}").
                then().
                statusCode(is(500)).
                body("message", startsWith("The from and to account should not be the same"));
        given().pathParam("fromAccount", 2).
                pathParam("toAccount", 4).
                pathParam("amount", 2).
                when().
                post(path + "/transfer/from/{fromAccount}/to/{toAccount}/amount/{amount}").
                then().
                statusCode(is(500)).
                body("message", startsWith("Rounding error with balance"));
    }

}

package com.kannane.webservice.accounts;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kannane.webservice.Server;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.*;
import spark.Spark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccountServiceTest {

    private static Server server;
    private static HttpHost host;
    private static Type mapType = new TypeToken<HashMap<String, String>>() {}.getType();
    //this is per test only
    private DefaultHttpClient httpclient;

    @BeforeClass
    public static void beforeClass() {
        server = new Server();
        server.startRouting();
        //Wait until we are ready to serve requests
        Spark.awaitInitialization();
        host = new HttpHost("localhost", 4567, "http");
    }

    @AfterClass
    public static void afterClass() {
        Spark.stop();
    }

    @Before
    public void before() {
        httpclient = new DefaultHttpClient();
    }

    @After
    public void after() {
        httpclient.getConnectionManager().shutdown();
    }

    @Test
    public void testSimplePath() throws IOException {
        HttpResponse response = httpclient.execute(host, new HttpGet("/"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        readStream(response.getEntity().getContent()); //if you don't read response out we cannot reuse client
        //Test non existing path
        response = httpclient.execute(host, new HttpGet("/random"));
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetAccountFlows() throws IOException {
        HttpResponse response = httpclient.execute(host, new HttpGet("/account/1"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        String content = readStream(response.getEntity().getContent());
        Gson gson = new Gson();
        Map map = gson.fromJson(content, mapType);
        assertEquals("1", map.get("id"));
        assertEquals("user1", map.get("name"));
        assertEquals("45.0", map.get("balance"));

        //Let us test some invalid get flows
        response = httpclient.execute(host, new HttpGet("/account/45"));
        //We can perhaps make the server throw 404 for accounts that are not there
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testTransferFlows() throws IOException {
        HttpResponse response = httpclient.execute(host, new HttpPost("/transfer/from/1/to/2/amount/20"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        String content = readStream(response.getEntity().getContent());
        assertTrue(content.contains("success"));
        //ensure balances have changed
        response = httpclient.execute(host, new HttpGet("/account/1"));
        content = readStream(response.getEntity().getContent());
        Gson gson = new Gson();
        Map map = gson.fromJson(content, mapType);
        assertEquals("25.0", map.get("balance"));
        response = httpclient.execute(host, new HttpGet("/account/2"));
        content = readStream(response.getEntity().getContent());
        map = gson.fromJson(content, mapType);
        assertEquals("99.5", map.get("balance"));

        //invalid accounts
        response = httpclient.execute(host, new HttpPost("/transfer/from/14/to/2/amount/20"));
        assertEquals(500, response.getStatusLine().getStatusCode());
        readStream(response.getEntity().getContent());

        //amount > balance
        response = httpclient.execute(host, new HttpPost("/transfer/from/1/to/2/amount/20000"));
        assertEquals(500, response.getStatusLine().getStatusCode());
        readStream(response.getEntity().getContent());

        //amount not a number
        response = httpclient.execute(host, new HttpPost("/transfer/from/1/to/2/amount/abc"));
        assertEquals(500, response.getStatusLine().getStatusCode());
        readStream(response.getEntity().getContent());
    }
    /**
     * Possibly read up the entire stream
     */
    private String readStream(InputStream is) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
            return r.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}

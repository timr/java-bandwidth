package com.bandwidth.sdk.driver;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.*;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class HttpRestDriverTest {

    private static final String USER_ID = "userId";
    private static final String TOKEN = "token";
    private static final String SECRET = "secret";
    private HttpRestDriver driver;

    @Before
    public void setUp() throws Exception {
        driver = new HttpRestDriver(USER_ID, TOKEN, SECRET);
    }

    @Test
    public void shouldPrepareGetRequest() {
        HttpUriRequest request = driver.setupRequest("https://api.catapult.inetwork.com", HttpMethod.GET, Collections.<String, String>emptyMap());
        assertThat(request, instanceOf(HttpGet.class));

        HttpGet httpGet = (HttpGet) request;
        assertThat(httpGet.getFirstHeader("Accept").getValue(), equalTo("application/json"));
        assertThat(httpGet.getFirstHeader("Accept-Charset").getValue(), equalTo("utf-8"));

        String auth = new String(Base64.encodeBase64((TOKEN + ":" + SECRET).getBytes()));
        assertThat(httpGet.getFirstHeader("Authorization").getValue(), equalTo("Basic " + auth));
    }

    @Test
    public void shouldPreparePostRequest() {
        HttpUriRequest request = driver.setupRequest("https://api.catapult.inetwork.com", HttpMethod.POST, Collections.<String, String>emptyMap());
        assertThat(request, instanceOf(HttpPost.class));

        HttpPost httpPost = (HttpPost) request;
        assertThat(httpPost.getFirstHeader("Accept").getValue(), equalTo("application/json"));
        assertThat(httpPost.getFirstHeader("Accept-Charset").getValue(), equalTo("utf-8"));

        String auth = new String(Base64.encodeBase64((TOKEN + ":" + SECRET).getBytes()));
        assertThat(httpPost.getFirstHeader("Authorization").getValue(), equalTo("Basic " + auth));
    }

    @Test
    public void shouldPreparePutRequest() {
        HttpUriRequest request = driver.setupRequest("https://api.catapult.inetwork.com", HttpMethod.PUT, Collections.<String, String>emptyMap());
        assertThat(request, instanceOf(HttpPut.class));

        HttpPut httpPut = (HttpPut) request;
        assertThat(httpPut.getFirstHeader("Accept").getValue(), equalTo("application/json"));
        assertThat(httpPut.getFirstHeader("Accept-Charset").getValue(), equalTo("utf-8"));

        String auth = new String(Base64.encodeBase64((TOKEN + ":" + SECRET).getBytes()));
        assertThat(httpPut.getFirstHeader("Authorization").getValue(), equalTo("Basic " + auth));
    }

    @Test
    public void shouldPrepareDeleteRequest() {
        HttpUriRequest request = driver.setupRequest("https://api.catapult.inetwork.com", HttpMethod.DELETE, Collections.<String, String>emptyMap());
        assertThat(request, instanceOf(HttpDelete.class));

        HttpDelete httpDelete = (HttpDelete) request;
        assertThat(httpDelete.getFirstHeader("Accept").getValue(), equalTo("application/json"));
        assertThat(httpDelete.getFirstHeader("Accept-Charset").getValue(), equalTo("utf-8"));

        String auth = new String(Base64.encodeBase64((TOKEN + ":" + SECRET).getBytes()));
        assertThat(httpDelete.getFirstHeader("Authorization").getValue(), equalTo("Basic " + auth));
    }

    @Test
    public void shouldRequestAccountInfo() throws IOException {
        MockHttpClient httpClient = new MockHttpClient();
        driver.setHttpClient(httpClient);

        HttpResponse response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        response.addHeader(new BasicHeader("Content-Type", "application/json"));
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream("{\"balance\": \"538.37250\",  \"accountType\": \"pre-pay\"}".getBytes()));
        response.setEntity(entity);

        httpClient.response = response;

        JSONObject jsonObject = driver.requestAccountInfo();
        assertThat(jsonObject, notNullValue());
        assertThat(jsonObject.get("balance").toString(), equalTo("538.37250"));
        assertThat(jsonObject.get("accountType").toString(), equalTo("pre-pay"));

        HttpUriRequest request = httpClient.lastRequest;
        assertThat(request, instanceOf(HttpGet.class));

        HttpGet httpGet = (HttpGet) request;
        assertThat(httpGet.getURI(), equalTo(URI.create("https://api.catapult.inetwork.com/v1/users/userId/account")));
    }

    @Test
    public void shouldRequestAccountTransactions() throws IOException {
        MockHttpClient httpClient = new MockHttpClient();
        driver.setHttpClient(httpClient);

        HttpResponse response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        response.addHeader(new BasicHeader("Content-Type", "application/json"));
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream("[{\"id\":\"id1\",\"time\":\"2014-08-05T22:32:44Z\",\"amount\":\"0.00\",\"type\":\"charge\",\"units\":1,\"productType\":\"call-in\",\"number\":\"+number1\"},{\"id\":\"id2\",\"time\":\"2014-08-05T02:32:59Z\",\"amount\":\"0.00\",\"type\":\"charge\",\"units\":1,\"productType\":\"sms-in\",\"number\":\"+number2\"}]".getBytes()));
        response.setEntity(entity);

        httpClient.response = response;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("maxItems", "1000");
        params.put("size", "10");
        JSONArray array = driver.requestAccountTransactions(params);
        assertThat(array, notNullValue());
        assertThat(((JSONObject) array.get(0)).get("id").toString(), equalTo("id1"));
        assertThat(((JSONObject) array.get(0)).get("number").toString(), equalTo("+number1"));

        HttpUriRequest request = httpClient.lastRequest;
        assertThat(request, instanceOf(HttpGet.class));

        HttpGet httpGet = (HttpGet) request;
        assertThat(httpGet.getURI(), equalTo(URI.create("https://api.catapult.inetwork.com/v1/users/userId/account/transactions?maxItems=1000&size=10")));

    }

    @Test
    public void shouldRequestApplications() throws IOException {
        MockHttpClient httpClient = new MockHttpClient();
        driver.setHttpClient(httpClient);

        HttpResponse response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        response.addHeader(new BasicHeader("Content-Type", "application/json"));
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream("[{\"id\":\"id1\",\"incomingCallUrl\":\"https://postBack\",\"incomingSmsUrl\":\"https://message\",\"name\":\"App1\",\"autoAnswer\":false},{\"id\":\"id2\",\"incomingCallUrl\":\"http:///call/callback.json\",\"incomingSmsUrl\":\"http:///sms/callback.json\",\"name\":\"App2\",\"autoAnswer\":true}]".getBytes()));
        response.setEntity(entity);

        httpClient.response = response;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("page", "2");
        params.put("size", "10");
        JSONArray array = driver.requestApplications(params);
        assertThat(array, notNullValue());
        assertThat(((JSONObject) array.get(0)).get("id").toString(), equalTo("id1"));
        assertThat(((JSONObject) array.get(0)).get("incomingCallUrl").toString(), equalTo("https://postBack"));

        HttpUriRequest request = httpClient.lastRequest;
        assertThat(request, instanceOf(HttpGet.class));

        HttpGet httpGet = (HttpGet) request;
        assertThat(httpGet.getURI(), equalTo(URI.create("https://api.catapult.inetwork.com/v1/users/userId/applications?page=2&size=10")));
    }

    @Test
    public void shouldRequestLocalAvailableNumbers() throws IOException {
        MockHttpClient httpClient = new MockHttpClient();
        driver.setHttpClient(httpClient);

        HttpResponse response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        response.addHeader(new BasicHeader("Content-Type", "application/json"));
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream("[{\"price\":\"0.00\",\"state\":\"CA\",\"number\":\"num1\",\"nationalNumber\":\"nationalNum1\",\"rateCenter\":\"STOCKTON\",\"city\":\"STOCKTON\"},{\"price\":\"0.00\",\"state\":\"CA\",\"number\":\"num2\",\"nationalNumber\":\"nationalNum2\",\"rateCenter\":\"STOCKTON\",\"city\":\"STOCKTON\"}]".getBytes()));
        response.setEntity(entity);

        httpClient.response = response;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("quantity", "2");
        JSONArray array = driver.requestLocalAvailableNumbers(params);
        assertThat(array, notNullValue());
        assertThat(((JSONObject) array.get(0)).get("number").toString(), equalTo("num1"));
        assertThat(((JSONObject) array.get(0)).get("nationalNumber").toString(), equalTo("nationalNum1"));

        HttpUriRequest request = httpClient.lastRequest;
        assertThat(request, instanceOf(HttpGet.class));

        HttpGet httpGet = (HttpGet) request;
        assertThat(httpGet.getURI(), equalTo(URI.create("https://api.catapult.inetwork.com/v1/availableNumbers/local?quantity=2")));
    }

    @Test
    public void shouldRequestTollFreeAvailableNumbers() throws IOException {
        MockHttpClient httpClient = new MockHttpClient();
        driver.setHttpClient(httpClient);

        HttpResponse response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        response.addHeader(new BasicHeader("Content-Type", "application/json"));
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream("[{\"price\":\"0.00\",\"number\":\"n1\",\"nationalNumber\":\"nn1\"},{\"price\":\"0.00\",\"number\":\"n2\",\"nationalNumber\":\"nn2\"}]".getBytes()));
        response.setEntity(entity);

        httpClient.response = response;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("quantity", "2");
        JSONArray array = driver.requestTollFreeAvailableNumbers(params);
        assertThat(array, notNullValue());
        assertThat(((JSONObject) array.get(0)).get("number").toString(), equalTo("n1"));
        assertThat(((JSONObject) array.get(0)).get("nationalNumber").toString(), equalTo("nn1"));

        HttpUriRequest request = httpClient.lastRequest;
        assertThat(request, instanceOf(HttpGet.class));

        HttpGet httpGet = (HttpGet) request;
        assertThat(httpGet.getURI(), equalTo(URI.create("https://api.catapult.inetwork.com/v1/availableNumbers/tollFree?quantity=2")));
    }
}
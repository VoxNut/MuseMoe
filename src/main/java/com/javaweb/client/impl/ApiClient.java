package com.javaweb.client.impl;

import com.javaweb.utils.HttpDeleteWithBody;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


// Update the ApiClient interface
public interface ApiClient {
    String get(String url) throws IOException;

    String post(String url, String jsonBody) throws IOException;

    String put(String url, String jsonBody) throws IOException;

    String putSimple(String url) throws IOException;

    String putWithParams(String url, Map<String, Object> params) throws IOException;

    String delete(String url) throws IOException;

    String deleteWithBody(String url, String jsonBody) throws IOException;

    String postWithFormParam(String url, String paramName, Object value) throws IOException;


    String putWithFormParams(String url, Map<String, Object> params) throws IOException;

}


class HttpApiClient implements ApiClient {
    private final CloseableHttpClient httpClient;

    @Override
    public String putSimple(String url) throws IOException {
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Accept", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String responseBody = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : "";

            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                throw new IOException("HTTP Request failed with status: " + statusCode +
                        ", response: " + responseBody);
            }
        }
    }

    @Override
    public String putWithFormParams(String url, Map<String, Object> params) throws IOException {
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Accept", "application/json");
        httpPut.setHeader("Content-Type", "application/x-www-form-urlencoded");

        if (params != null && !params.isEmpty()) {
            List<NameValuePair> nameValuePairs = new ArrayList<>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                nameValuePairs.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
            }

            httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs, StandardCharsets.UTF_8));
        }

        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                throw new IOException("HTTP Request failed with status: " + statusCode +
                        ", response: " + responseBody);
            }
        }
    }

    public HttpApiClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String putWithParams(String url, Map<String, Object> params) throws IOException {
        // Build URL with parameters
        StringBuilder urlWithParams = new StringBuilder(url);

        if (params != null && !params.isEmpty()) {
            urlWithParams.append("?");
            boolean first = true;

            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (!first) {
                    urlWithParams.append("&");
                }
                urlWithParams.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                urlWithParams.append("=");
                urlWithParams.append(URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8));
                first = false;
            }
        }

        HttpPut httpPut = new HttpPut(urlWithParams.toString());
        httpPut.setHeader("Accept", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                throw new IOException("HTTP Request failed with status: " + statusCode +
                        ", response: " + responseBody);
            }
        }
    }

    @Override
    public String get(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public String post(String url, String jsonBody) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");

        if (jsonBody != null && !jsonBody.isEmpty()) {
            HttpEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
        }

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public String put(String url, String jsonBody) throws IOException {
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Content-Type", "application/json");
        httpPut.setHeader("Accept", "application/json");

        if (jsonBody != null && !jsonBody.isEmpty()) {
            HttpEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
            httpPut.setEntity(entity);
        }

        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public String delete(String url) throws IOException {
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setHeader("Accept", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(httpDelete)) {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public String deleteWithBody(String url, String jsonBody) throws IOException {
        //Custom HttpDeleteWithBody

        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url);
        httpDelete.setHeader("Content-Type", "application/json");
        httpDelete.setHeader("Accept", "application/json");

        if (jsonBody != null && !jsonBody.isEmpty()) {
            HttpEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
            httpDelete.setEntity(entity);
        }

        try (CloseableHttpResponse response = httpClient.execute(httpDelete)) {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public String postWithFormParam(String url, String paramName, Object value) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        // Create form parameters
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(paramName, String.valueOf(value)));
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                throw new IOException("HTTP Request failed with status: " + statusCode +
                        ", response: " + responseBody);
            }
        }
    }


}
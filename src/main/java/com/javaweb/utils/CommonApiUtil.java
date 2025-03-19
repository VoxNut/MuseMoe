package com.javaweb.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.UserDTO;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class CommonApiUtil {




    //USER
    public static Set<UserDTO> fetchAllUsersBaseOnRole(RoleType role) {
        try {
            CloseableHttpClient httpClient = HttpClientProvider.getHttpClient();
            String url = "http://localhost:8081/api/user?status=1&roles=" + role;
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", "application/json");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            response.close();

            if (statusCode == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(responseBody, new TypeReference<Set<UserDTO>>() {
                }).stream().filter(UserDTO::hasFullName).collect(Collectors.toCollection(TreeSet::new));
            } else {
                System.err.println("Failed to fetch users. ProductStatus code: " + statusCode + "\nResponse: " + responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptySet();
    }

    public static UserDTO fetchUserbyUsername(String username) {
        try {
            CloseableHttpClient httpClient = HttpClientProvider.getHttpClient();
            String url = "http://localhost:8081/api/user/username/" + URLEncoder.encode(username, "UTF-8") + "/user_dto";
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", "application/json");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseBody, UserDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static UserDTO fetchUserById(Long id) {
        try {
            CloseableHttpClient httpClient = HttpClientProvider.getHttpClient();
            String url = "http://localhost:8081/api/user/" + id + "/user_dto";
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", "application/json");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseBody, UserDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static UserDTO fetchUserByPhone(String phone) {
        try {
            CloseableHttpClient httpClient = HttpClientProvider.getHttpClient();
            String url = "http://localhost:8081/api/user/phone/" + URLEncoder.encode(phone, "UTF-8") + "/user_dto";
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", "application/json");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseBody, UserDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



}

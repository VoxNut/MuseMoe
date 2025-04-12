package com.javaweb.client.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public interface UrlEncoder {
    String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

    String encode(String value);
}

class StandardUrlEncoder implements UrlEncoder {
    @Override
    public String encode(String value) {
        return encode(value, DEFAULT_ENCODING);
    }

    public String encode(String value, String encoding) {
        return getString(value, encoding, DEFAULT_ENCODING);
    }

    public static String getString(String value, String encoding, String defaultEncoding) {
        if (value == null) {
            return "";
        }
        try {
            return URLEncoder.encode(value, encoding);
        } catch (UnsupportedEncodingException e) {
            try {
                return URLEncoder.encode(value, defaultEncoding);
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException("UTF-8 encoding not supported", ex);
            }
        }
    }


}
package com.javaweb.utils.client.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public interface ResponseParser {

    <T> T parseObject(String json, Class<T> valueType) throws JsonProcessingException;


    <T> T parseReference(String json, TypeReference<T> typeReference) throws JsonProcessingException;

}


class JsonResponseParser implements ResponseParser {
    private final ObjectMapper objectMapper;

    public JsonResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T parseObject(String json, Class<T> valueType) throws JsonProcessingException {
        return objectMapper.readValue(json, valueType);
    }

    @Override
    public <T> T parseReference(String json, TypeReference<T> typeReference) throws JsonProcessingException {
        return objectMapper.readValue(json, typeReference);
    }
}
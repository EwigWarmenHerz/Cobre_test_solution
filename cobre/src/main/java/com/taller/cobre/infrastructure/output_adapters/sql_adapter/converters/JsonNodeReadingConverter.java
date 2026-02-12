package com.taller.cobre.infrastructure.output_adapters.sql_adapter.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;


@ReadingConverter
public class JsonNodeReadingConverter implements Converter<Json, JsonNode> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public JsonNode convert(Json source) {
        try {
            return mapper.readTree(source.asString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }



}
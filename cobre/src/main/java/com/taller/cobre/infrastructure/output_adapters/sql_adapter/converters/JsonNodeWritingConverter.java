package com.taller.cobre.infrastructure.output_adapters.sql_adapter.converters;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class JsonNodeWritingConverter implements Converter<JsonNode, String> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convert(JsonNode source) {
        return source.toString();
    }

}

package net.bwskyd.music.songservice.config.advice.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DetailsSerializer extends JsonSerializer<Map<String, List<String>>> {
    @Override
    public void serialize(Map<String, List<String>> value,
                          JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        for (Map.Entry<String, List<String>> entry : value.entrySet()) {
            String joined = String.join(", ", entry.getValue());
            gen.writeStringField(entry.getKey(), joined);
        }
        gen.writeEndObject();
    }
}
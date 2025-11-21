package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Custom deserializer that can handle Integer, String, or Object for integer fields
 * This allows AI service to return either format without breaking deserialization
 */
public class FlexibleIntegerDeserializer extends JsonDeserializer<Integer> {
    
    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        
        // If it's null, return null
        if (node.isNull()) {
            return null;
        }
        
        // If it's already a number, return as integer
        if (node.isNumber()) {
            return node.asInt();
        }
        
        // If it's a string, try to parse
        if (node.isTextual()) {
            String text = node.asText();
            if (text == null || text.trim().isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException e) {
                // If can't parse, return null or 0
                return null;
            }
        }
        
        // If it's an object, try to extract meaningful value
        if (node.isObject()) {
            // Common patterns from AI responses:
            // {"value": 80} or {"percentage": 80} or {"target": 80}
            if (node.has("value")) {
                return node.get("value").asInt();
            }
            if (node.has("percentage")) {
                return node.get("percentage").asInt();
            }
            if (node.has("target")) {
                return node.get("target").asInt();
            }
            if (node.has("accuracy")) {
                return node.get("accuracy").asInt();
            }
            // If none of the above, return null
            return null;
        }
        
        // For any other type, return null
        return null;
    }
}

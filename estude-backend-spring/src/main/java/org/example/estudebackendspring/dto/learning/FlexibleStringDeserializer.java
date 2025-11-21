package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom deserializer that can handle both String and Array for string fields
 * This allows AI service to return either format without breaking deserialization
 */
public class FlexibleStringDeserializer extends JsonDeserializer<String> {
    
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        
        // If it's a string, return as is
        if (node.isTextual()) {
            return node.asText();
        }
        
        // If it's an array, join elements with newlines or bullets
        if (node.isArray()) {
            List<String> items = new ArrayList<>();
            node.forEach(item -> items.add(item.asText()));
            
            // Join with bullet points for better readability
            if (items.isEmpty()) {
                return "";
            }
            if (items.size() == 1) {
                return items.get(0);
            }
            return "• " + String.join("\n• ", items);
        }
        
        // If null, return empty string
        if (node.isNull()) {
            return "";
        }
        
        // For any other type, convert to string
        return node.asText();
    }
}

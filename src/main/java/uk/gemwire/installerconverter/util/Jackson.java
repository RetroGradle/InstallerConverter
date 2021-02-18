package uk.gemwire.installerconverter.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class Jackson {

    public static final ObjectMapper JSON;

    static {
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);

        JSON = new ObjectMapper()
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setDefaultPrettyPrinter(new DefaultPrettyPrinter().withObjectIndenter(indenter).withArrayIndenter(indenter));
    }

    public static ObjectNode read(String content) throws JsonProcessingException {
        return (ObjectNode) JSON.readTree(content);
    }

    public static <T> T read(String content, Class<T> clazz) throws JsonProcessingException {
        return JSON.readValue(content, clazz);
    }

    public static <T> T read(InputStream stream, Class<T> clazz) throws IOException {
        return JSON.readValue(stream, clazz);
    }

    public static String write(JsonNode node) throws JsonProcessingException {
        return JSON.writeValueAsString(node);
    }

    public static void write(OutputStream stream, JsonNode node) throws IOException {
        JSON.writeValue(stream, node);
    }

    public static JsonNodeFactory factory() {
        return JSON.getNodeFactory();
    }

}

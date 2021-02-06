package uk.gemwire.installerconverter.util;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Jackson {

    public static final ObjectMapper JSON;

    static {
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);

        JSON = new ObjectMapper()
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setDefaultPrettyPrinter(new DefaultPrettyPrinter().withObjectIndenter(indenter).withArrayIndenter(indenter));
    }

    public static ObjectNode read(File file) throws IOException {
        return (ObjectNode) JSON.readTree(file);
    }

    public static ObjectNode read(String string) throws IOException {
        return (ObjectNode) JSON.readTree(string);
    }

    public static String write(JsonNode node) throws JsonProcessingException {
        return JSON.writer().writeValueAsString(node);
    }
}

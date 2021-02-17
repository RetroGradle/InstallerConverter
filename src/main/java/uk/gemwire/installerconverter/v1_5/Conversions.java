package uk.gemwire.installerconverter.v1_5;

import java.util.Locale;
import java.util.Objects;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public abstract class Conversions {

    public static String convertId(String id) throws IllegalArgumentException {
        String[] parts = id.split("-");

        if (parts.length < 3 || parts.length > 4) throw new IllegalArgumentException("Invalid id length: " + id);
        if(!parts[1].toLowerCase(Locale.ROOT).startsWith("forge")) throw new IllegalArgumentException("Invalid id: " + id);

        if (Objects.equals(parts[0], parts[2])) {
            return parts[0] + "-forge-" + parts[1].toLowerCase(Locale.ROOT).replaceFirst("forge", "");
        }

        return parts[0] + "-forge-" + parts[2];
    }

    public static ArrayNode createCommentNode(JsonNodeFactory factory) {
        ArrayNode node = factory.arrayNode();
        node.add("Please do not automate the download and installation of Forge.");
        node.add("Our efforts are supported by ads from the download page.");
        node.add("If you MUST automate this, please consider supporting the project through https://www.patreon.com/LexManos/");
        return node;
    }

    public static String convertWelcome(String welcome) {
        if (Objects.equals(welcome, "Welcome to the simple forge installer."))
            return "Welcome to the simple Forge installer.";

        return welcome;
    }
}

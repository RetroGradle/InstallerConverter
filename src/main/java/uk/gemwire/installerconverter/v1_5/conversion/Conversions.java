package uk.gemwire.installerconverter.v1_5.conversion;

import java.util.Locale;
import java.util.Objects;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public abstract class Conversions {

    public static String convertId(String id) throws IllegalArgumentException {
        String[] parts = id.split("-");

        if (parts.length < 2 || parts.length > 4) throw new IllegalArgumentException("Invalid id length: " + id);
        if(!parts[1].toLowerCase(Locale.ROOT).startsWith("forge")) throw new IllegalArgumentException("Invalid id: " + id);

        if (parts.length == 2 || Objects.equals(parts[0], parts[2])) {
            return parts[0] + "-forge-" + parts[1].toLowerCase(Locale.ROOT).replaceFirst("forge", "");
        }

        return parts[0] + "-forge-" + parts[2];
    }

    public static String convertVersion(String version) throws IllegalArgumentException {
        String[] parts = version.split("-");

        if (parts.length < 2 || parts.length > 3) throw new IllegalArgumentException("Invalid version length: " + version);

        if (parts.length == 2 || Objects.equals(parts[0], parts[2])) {
            return parts[0] + "-" + parts[1];
        }

        throw new IllegalArgumentException("Invalid version: " + version);
    }

    public static String asComparableVersion(String version) {
        return asComparable(convertVersion(version));
    }

    public static String asComparable(String version) {
        String[] parts = version.split("-", 2);
        if (parts.length != 2) throw new IllegalArgumentException("Invalid version: " + version);

        String minecraftVersion = asComparableMinecraft(parts[0]);
        String forgeVersion     = asComparableForge(parts[1]);

        return minecraftVersion + "|" + forgeVersion;
    }

    private static String asComparableMinecraft(String version) {
        String[] parts = version.split("\\.", 3);
        if (parts.length != 3) throw new IllegalArgumentException("Invalid version: " + version);

        int a = Integer.parseInt(parts[0]);
        int b = Integer.parseInt(parts[1]);
        int c = Integer.parseInt(parts[2]);

        return String.format("%d", a) + String.format("%02d", b) + String.format("%02d", c);
    }

    private static String asComparableForge(String version) {
        String[] parts = version.split("\\.", 4);
        if (parts.length != 4) throw new IllegalArgumentException("Invalid version: " + version);

        int a = Integer.parseInt(parts[0]);
        int b = Integer.parseInt(parts[1]);
        int c = Integer.parseInt(parts[2]);
        int d = Integer.parseInt(parts[3]);

        return String.format("%02d", a) + String.format("%02d", b) + String.format("%02d", c) + String.format("%04d", d);
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

    public static String convertProfile(String profileName) {
        if (Objects.equals("Forge", profileName))
            return "forge";

        return profileName;
    }
}

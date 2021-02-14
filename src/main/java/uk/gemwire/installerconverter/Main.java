package uk.gemwire.installerconverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.raw.Converter;
import uk.gemwire.installerconverter.util.Jackson;
import uk.gemwire.installerconverter.util.Maven;
import uk.gemwire.installerconverter.util.Pair;
import uk.gemwire.installerconverter.v1_5.InstallProfile;
import uk.gemwire.installerconverter.v1_5.LibraryInfo;

/**
 * @author RetroGradle
 */
public class Main {
    public static void main(String... args) throws IOException {
        //printRaw();
        //
        //printObj();

        System.out.println("Converting org.ow2.asm:asm-all:5.2");

        LibraryInfo info = Jackson.JSON.readValue(
            "{"
            + "      \"name\": \"org.ow2.asm:asm-all:5.2\",\n"
            + "      \"url\" : \"http://files.minecraftforge.net/maven/\",\n"
            + "      \"checksums\" : [ \"2ea49e08b876bbd33e0a7ce75c8f371d29e1f10a\" ],\n"
            + "      \"serverreq\":true,\n"
            + "      \"clientreq\":true\n"
            + "    }",
            LibraryInfo.class
        );

        ObjectNode node = info.convert(Jackson.JSON.getNodeFactory());
        String url = node.with("downloads").with("artifact").get("url").asText();
        Pair<String, Long> pair = Maven.calculateSHA1andSize(new URL(url));
        System.out.println(pair);
    }

    public static void printRaw() throws IOException {
        ObjectNode profile = Jackson.read(new File("src/test/resources/install_profile_1.12.2.json"));

        Pair<ObjectNode, ObjectNode> modified = Converter.convertProfile(profile);

        System.out.println("raw install_profile.json");
        System.out.println(Jackson.write(modified.left()));
        System.out.println("raw version.json");
        System.out.println(Jackson.write(modified.right()));
    }

    public static void printObj() throws IOException {
        InstallProfile profile = Jackson.JSON.readValue(new File("src/test/resources/install_profile_1.12.2.json"), InstallProfile.class);

        profile.validate();

        Pair<ObjectNode, ObjectNode> modified = profile.convert(Jackson.JSON.getNodeFactory());

        System.out.println("obj install_profile.json");
        System.out.println(Jackson.write(modified.left()));
        System.out.println("obj version.json");
        System.out.println(Jackson.write(modified.right()));
    }

}

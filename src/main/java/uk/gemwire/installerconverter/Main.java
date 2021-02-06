package uk.gemwire.installerconverter;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.raw.Converter;
import uk.gemwire.installerconverter.util.Jackson;
import uk.gemwire.installerconverter.util.Pair;
import uk.gemwire.installerconverter.v1_5.InstallProfile;

/**
 * @author RetroGradle
 */
public class Main {
    public static void main(String... args) throws IOException {
        printRaw();

        printObj();
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

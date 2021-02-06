package uk.gemwire.installerconverter;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.raw.Converter;
import uk.gemwire.installerconverter.util.Jackson;
import uk.gemwire.installerconverter.util.Pair;

/**
 * @author RetroGradle
 */
public class Main {
    public static void main(String... args) throws IOException {
        ObjectNode profile = Jackson.read(new File("src/test/resources/install_profile_1.12.2.json"));

        Pair<ObjectNode, ObjectNode> modified = Converter.convertProfile(profile);

        System.out.println("install_profile.json");
        System.out.println(Jackson.write(modified.left()));
        System.out.println("version.json");
        System.out.println(Jackson.write(modified.right()));

    }
}

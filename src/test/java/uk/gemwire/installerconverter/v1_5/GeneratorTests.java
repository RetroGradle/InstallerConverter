package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.InstallerGenerator;
import uk.gemwire.installerconverter.util.Jackson;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;
import uk.gemwire.installerconverter.util.maven.Maven;
import uk.gemwire.installerconverter.v1_5.conversion.Converted;
import uk.gemwire.installerconverter.v1_5.util.TestResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class GeneratorTests {

    @ParameterizedTest
    @ValueSource(strings = { "1.6.1-8.9.0.749" })
    void generateInstaller(final String version) throws IOException {
        Config config = createConfig();
        InstallProfile profile = InstallerGenerator.generate(version);
        profile.validate();

        Converted pair = profile.convert(config, Jackson.factory());
        compareConversion(version, "install-profile", pair.install());
        compareConversion(version, "version-info", pair.version());
    }

    Config createConfig() throws IOException {
        return Config
            .withDefaults()
            .withIcon("{ICON}")
            .withCachingResolver() //TODO: .withResolver(TestResolver)
            .setup()
            .transformResolver(resolver -> new TestResolver(resolver)
                .add(Maven.FORGE, Artifact.of("net.minecraftforge:forge:1.6.1-8.9.0.749"), CachedArtifactInfo.of("{SHA1}", -1,""))
            );
    }

    <T> void compareConversion(String version, String type, ObjectNode actual) throws IOException {
        String data = version + "/" + type + ".expected.json";
        assertEquals(getTestData(data), Jackson.write(actual).replace("\r\n", "\n"));
    }

    String getTestData(String name) throws IOException {
        try {
            //noinspection UnstableApiUsage
            return Files.readString(Path.of(Resources.getResource("data/" + name).toURI())).replace("\r\n", "\n");
        } catch (URISyntaxException e) {
            fail("Failed to create URI for test data " + name);
            return null;
        }
    }

}

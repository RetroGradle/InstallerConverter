package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.util.Jackson;
import uk.gemwire.installerconverter.util.common.Pair;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;
import uk.gemwire.installerconverter.util.maven.Maven;
import uk.gemwire.installerconverter.v1_5.util.TestResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ConverterTests {

    @Test
    void convertInstall() throws IOException {
        compareConversion("1.12.2", "install-profile", Install.class, createConfig());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.5.2", "1.6.4", "1.12.2"})
    void convertVersionInfo(final String version) throws IOException {
        compareConversion(version, "version-info", VersionInfo.class, Pair.of(createConfig(), version));
    }

    Config createConfig() throws IOException {
        return Config
            .withDefaults()
            .withIcon("{ICON}")
            .withCachingResolver() //TODO: .withResolver(TestResolver)
            .setup()
            .transformResolver(resolver -> new TestResolver(resolver)
                .add(Maven.FORGE, Artifact.of("net.minecraftforge:forge:1.6.4-9.11.1.965"), CachedArtifactInfo.of("36cc314edb97df84528382d5c3d2cce46d75de11", 1972443, ""))
                .add(Maven.FORGE, Artifact.of("net.minecraftforge:forge:1.5.2-7.8.1.738"), CachedArtifactInfo.of("76223709288287a6a8d22ab16b43a6ab2a284a0d", 2033732,""))
            );
    }

    <T> void compareConversion(String version, String name, Class<? extends IConvertable<? extends JsonNode, T>> clazz, T context) throws IOException {
        String expected = version + "/" + name + ".expected.json";
        String data = version + "/" + name + ".input.json";
        assertEquals(getTestData(expected), Jackson.write(Jackson.read(getTestData(data), clazz).convert(context, Jackson.factory())).replace("\r\n", "\n"));
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

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
import uk.gemwire.installerconverter.util.IConvertable;
import uk.gemwire.installerconverter.util.Jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ConverterTests {

    @Test
    void convertInstall() throws IOException {
        compareConversion("1.12.2", "install-profile", Install.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.6.4", "1.12.2"})
    void convertVersionInfo(final String version) throws IOException {
        compareConversion(version, "version-info", VersionInfo.class);
    }

    void compareConversion(String version, String name, Class<? extends IConvertable<? extends JsonNode, Config>> clazz) throws IOException {
        compareConversion(version + "/" + name + ".expected.json", version + "/" + name + ".input.json", clazz, Config
            .withDefaults()
            .withIcon("{ICON}")
            .withCachingResolver() //TODO: .withResolver(TestResolver)
            .setup()
        );
    }

    void compareConversion(String expected, String data, Class<? extends IConvertable<? extends JsonNode, Config>> clazz, Config config) throws IOException {
        assertEquals(getTestData(expected), Jackson.write(Jackson.read(getTestData(data), clazz).convert(config, Jackson.factory())).replace("\r\n", "\n"));
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

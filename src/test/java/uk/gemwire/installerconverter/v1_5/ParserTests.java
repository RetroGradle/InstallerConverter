package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.io.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gemwire.installerconverter.util.Jackson;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

public class ParserTests {

    @ParameterizedTest
    @ValueSource(strings = {"1.5.2", "1.6.4", "1.7.10", "1.8.9", "1.9.4", "1.10.2", "1.11.2", "1.12.2"})
    public void parseVersion(final String version) {
        assertDoesNotThrow(() -> parse(version).validate());
    }

    InstallProfile parse(String version) throws Exception {
        return Jackson.read(getTestData("install-profile/{version}.json".replace("{version}", version)), InstallProfile.class);
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

package uk.gemwire.installerconverter.v1_5;

import java.io.InputStream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gemwire.installerconverter.util.Jackson;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ParserTests {

    InstallProfile parse(String version) throws Exception {
        InputStream stream = ParserTests.class.getResourceAsStream("/install_profile_{version}.json".replace("{version}", version));
        return Jackson.read(stream, InstallProfile.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.5.2", "1.6.4", "1.7.10", "1.8.9", "1.9.4", "1.10.2", "1.11.2", "1.12.2"})
    public void parseVersion(final String version) {
        assertDoesNotThrow(() -> parse(version).validate());
    }
}

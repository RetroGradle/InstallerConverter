package uk.gemwire.installerconverter.v1_5;

import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gemwire.installerconverter.util.Jackson;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ParserTests {

    private static final ObjectReader InstallProfileReader = Jackson.JSON.readerFor(InstallProfile.class);
    private static final ObjectReader VersionInfoReader = Jackson.JSON.readerFor(VersionInfo.class);

    InstallProfile parse(String version) throws Exception {
        InputStream stream = ParserTests.class.getResourceAsStream("/install_profile_{version}.json".replace("{version}", version));
        return InstallProfileReader.readValue(stream);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.7.10", "1.8.9", "1.9.4", "1.10.2", "1.11.2", "1.12.2"})
    public void parseVersion(final String version) {
        assertDoesNotThrow(() -> parse(version).validate());
    }
}

package uk.gemwire.installerconverter.v1_5;

import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.jupiter.api.Test;
import uk.gemwire.installerconverter.util.Jackson;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ParserTests {

    private static final ObjectReader InstallProfileReader = Jackson.JSON.readerFor(InstallProfile.class);
    private static final ObjectReader VersionInfoReader = Jackson.JSON.readerFor(VersionInfo.class);

    InstallProfile parse(String version) throws Exception {
        InputStream stream = ParserTests.class.getResourceAsStream("/install_profile_{version}.json".replace("{version}", version));
        return InstallProfileReader.readValue(stream);
    }

    @Test
    void parse1_7_10() {
        assertDoesNotThrow(() -> parse("1.7.10").validate());
    }

    @Test
    void parse1_8_9() {
        assertDoesNotThrow(() -> parse("1.8.9").validate());
    }

    @Test
    void parse1_9_4() {
        assertDoesNotThrow(() -> parse("1.9.4").validate());
    }

    @Test
    void parse1_10_2() {
        assertDoesNotThrow(() -> parse("1.10.2").validate());
    }

    @Test
    void parse1_11_2() {
        assertDoesNotThrow(() -> parse("1.11.2").validate());
    }

    @Test
    void parse1_12_2() {
        assertDoesNotThrow(() -> parse("1.12.2").validate());
    }

}

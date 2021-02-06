package uk.gemwire.installerconverter.v1_5;

import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.jupiter.api.Test;

import uk.gemwire.installerconverter.util.Jackson;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTests {

    private static final ObjectReader InstallProfileReader = Jackson.JSON.readerFor(InstallProfile.class);
    private static final ObjectReader VersionInfoReader = Jackson.JSON.readerFor(VersionInfo.class);

    InstallProfile parse(String version) throws Exception {
        InputStream stream = ParserTests.class.getResourceAsStream("/install_profile_{version}.json".replace("{version}", version));
        return InstallProfileReader.readValue(stream);
    }

    @Test
    void parse1_7_10() {
        assertDoesNotThrow(() -> parse("1.7.10"));
    }

    @Test
    void parse1_8_9() {
        assertDoesNotThrow(() -> parse("1.8.9"));
    }

    @Test
    void parse1_9_4() {
        assertDoesNotThrow(() -> parse("1.9.4"));
    }

    @Test
    void parse1_10_2() {
        assertDoesNotThrow(() -> parse("1.10.2"));
    }

    @Test
    void parse1_11_2() {
        assertDoesNotThrow(() -> parse("1.11.2"));
    }

    @Test
    void parse1_12_2() {
        assertDoesNotThrow(() -> parse("1.12.2"));
    }

    @Test
    void parseVersionInfo() throws JsonProcessingException {
        VersionInfo info = VersionInfoReader.readValue("{\"inheritsFrom\":\"1.7.10\",\"jar\":\"1.7.10\"}");

        assertEquals("1.7.10", info.getInheritsFrom());
        assertEquals("1.7.10", info.getJar());
    }

}

package uk.gemwire.installerconverter.raw;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import uk.gemwire.installerconverter.util.Jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConverterTests {

    @Test
    void convertId() {
        assertEquals("a-forge-b", Converter.convertId("a-forgeextra-b"));
        assertEquals("1.12.2-forge-14.23.5.2847", Converter.convertId("1.12.2-forge1.12.2-14.23.5.2847"));

        assertThrows(IllegalArgumentException.class, () -> Converter.convertId(""));
        assertThrows(IllegalArgumentException.class, () -> Converter.convertId("a-b-c"));
    }

    @Test
    void convertInstall() throws IOException {
        compareConversion("install_profile.json", "install_profile_install.json", Converter::convertInstall);
    }

    @Test
    void convertVersionInfo() throws IOException {
        compareConversion("version_info.json", "install_profile_version_info.json", Converter::convertVersionInfo);
    }

    void compareConversion(String expected, String data, Function<ObjectNode, ObjectNode> converter) throws IOException {
        assertEquals(getTestData(expected), Jackson.write(converter.apply(Jackson.read(getTestData(data)))));
    }

    String getTestData(String name) throws IOException {
        return Files.readString(Path.of("src/test/resources/data/" + name));
    }

}

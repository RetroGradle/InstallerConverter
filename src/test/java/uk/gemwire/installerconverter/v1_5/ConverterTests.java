package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import uk.gemwire.installerconverter.util.IConvertable;
import uk.gemwire.installerconverter.util.Jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConverterTests {

    @Test
    void convertInstall() throws IOException {
        compareConversion("install_profile_obj.json", "install_profile_install.json", Install.class);
    }

    @Test
    void convertVersionInfo() throws IOException {
        compareConversion("version_info_obj.json", "install_profile_version_info.json", VersionInfo.class);
    }

    void compareConversion(String expected, String data, Class<? extends IConvertable<? extends JsonNode>> clazz) throws IOException {
        assertEquals(getTestData(expected), Jackson.write(Jackson.JSON.readValue(getTestData(data), clazz).convert(Jackson.JSON.getNodeFactory())));
    }

    String getTestData(String name) throws IOException {
        return Files.readString(Path.of("src/test/resources/data/" + name));
    }

}

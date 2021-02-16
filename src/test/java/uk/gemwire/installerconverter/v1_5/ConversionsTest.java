package uk.gemwire.installerconverter.v1_5;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConversionsTest {

    @Test
    void convertId() {
        assertEquals("a-forge-b", Conversions.convertId("a-forgeextra-b"));
        assertEquals("1.12.2-forge-14.23.5.2847", Conversions.convertId("1.12.2-forge1.12.2-14.23.5.2847"));

        assertThrows(IllegalArgumentException.class, () -> Conversions.convertId(""));
        assertThrows(IllegalArgumentException.class, () -> Conversions.convertId("a-b"));
        assertThrows(IllegalArgumentException.class, () -> Conversions.convertId("a-b-c"));
    }

}

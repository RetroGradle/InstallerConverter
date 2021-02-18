package uk.gemwire.installerconverter.v1_5;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConversionsTest {

    @Test
    void convertId() {
        assertEquals("a-forge-b", Conversions.convertId("a-forgeextra-b"));
        assertEquals("a-forge-b", Conversions.convertId("a-Forgeb-a"));
        assertEquals("a-forge-b", Conversions.convertId("a-Forgeb"));

        assertEquals("1.12.2-forge-14.23.5.2847", Conversions.convertId("1.12.2-forge1.12.2-14.23.5.2847"));
        assertEquals("1.11.2-forge-13.20.1.2588", Conversions.convertId("1.11.2-forge1.11.2-13.20.1.2588"));
        assertEquals("1.10.2-forge-12.18.3.2511", Conversions.convertId("1.10.2-forge1.10.2-12.18.3.2511"));
        assertEquals("1.9.4-forge-12.17.0.2051", Conversions.convertId("1.9.4-forge1.9.4-12.17.0.2051"));
        assertEquals("1.8.9-forge-11.15.1.2318", Conversions.convertId("1.8.9-forge1.8.9-11.15.1.2318-1.8.9"));
        assertEquals("1.7.10-forge-10.13.4.1614", Conversions.convertId("1.7.10-Forge10.13.4.1614-1.7.10"));
        assertEquals("1.6.4-forge-9.11.1.965", Conversions.convertId("1.6.4-Forge9.11.1.965"));
        assertEquals("1.5.2-forge-7.8.1.738", Conversions.convertId("1.5.2-Forge7.8.1.738"));

        assertThrows(IllegalArgumentException.class, () -> Conversions.convertId(""));
        assertThrows(IllegalArgumentException.class, () -> Conversions.convertId("a-b"));
        assertThrows(IllegalArgumentException.class, () -> Conversions.convertId("a-b-c"));
    }

}

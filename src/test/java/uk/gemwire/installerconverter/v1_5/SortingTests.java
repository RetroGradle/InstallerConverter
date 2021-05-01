package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import uk.gemwire.installerconverter.v1_5.conversion.Conversions;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.fail;

public class SortingTests {

    @Test
    void compareSorting() throws IOException {
        Stream<String> actual = getTestData("input.a.txt").sorted(Comparator.comparing(Conversions::asComparableVersion));
        assertLinesMatch(getTestData("expected.a.txt"), actual);
    }

    Stream<String> getTestData(String name) throws IOException {
        try {
            //noinspection UnstableApiUsage
            return Files.lines(Path.of(Resources.getResource("data/sorting/" + name).toURI()));
        } catch (URISyntaxException e) {
            fail("Failed to create URI for test data " + name);
            return null;
        }
    }

}

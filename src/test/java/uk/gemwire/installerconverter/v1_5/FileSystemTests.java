package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import uk.gemwire.installerconverter.InstallerConverter;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

public class FileSystemTests {

    private void setup(FileSystem fs) throws IOException {
        dummy(fs.getPath("forge-VERSION-installer-win.exe"));
        dummy(fs.getPath("forge-VERSION-installer.jar"));
        dummy(fs.getPath("forge-VERSION-changelog.txt"));
        dummy(fs.getPath("forge-VERSION-src.zip"));
        dummy(fs.getPath("forge-VERSION-javadoc.zip"));
        dummy(fs.getPath("forge-VERSION-universal.jar"));
        dummy(fs.getPath("forge-VERSION-userdev.jar"));
    }

    @Test
    public void testRemovalFilter() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            setup(fs);

            List<Path> paths = new ArrayList<>();
            InstallerConverter.forRemoval(fs.getPath(""), paths::add);

            assertLinesMatch(
                Stream.of("forge-VERSION-installer-win.exe", "forge-VERSION-installer.jar").sorted(),
                paths.stream().map(Path::toString).sorted()
            );
        }
    }

    private void dummy(Path output) throws IOException {
        copy("definitely a file", output);
    }

    private void copy(String input, Path output) throws IOException {
        try (InputStream stream = IOUtils.toInputStream(input, StandardCharsets.UTF_8)) {
            Files.copy(stream, output);
        }
    }

}

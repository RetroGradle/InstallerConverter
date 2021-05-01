package uk.gemwire.installerconverter.util.signing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class JarSignerInterop {

    public static void sign(SigningConfig config, Path input, Path output) throws IOException {
        sign(config.keystore(), config.storepass(), config.keypass(), config.alias(), input, output);
    }

    public static void sign(Path keystore, String storepass, String keypass, String alias, Path input, Path output) throws IOException {
        if (Files.exists(output)) Files.delete(output);

        Path temporary = null;
        if (!isStandardPath(input)) {
            temporary = Files.createTempFile("signing", ".tmp");
            Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING);
            input = temporary;
        }

        if (keypass.isBlank()) {
            sign(
                "-keystore", keystore.toAbsolutePath().toString(),
                "-storepass", storepass,
                "-signedjar", output.toAbsolutePath().toString(),
                input.toAbsolutePath().toString(),
                alias
            );
        } else {
            sign(
                "-keystore", keystore.toAbsolutePath().toString(),
                "-storepass", storepass,
                "-keypass", keypass,
                "-signedjar", output.toAbsolutePath().toString(),
                input.toAbsolutePath().toString(),
                alias
            );
        }


        if (temporary != null) {
            Files.delete(temporary);
        }
    }

    //TODO: Actually implement the logic this uses here so we don't have to do this reflection
    /** @see sun.security.tools.jarsigner.Main */
    private static void sign(String... args) throws IOException {
        try {
            Class.forName("sun.security.tools.jarsigner.Main").getDeclaredMethod("main", String[].class).invoke(null, (Object) args);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private static boolean isStandardPath(Path path) {
        try {
            /* toFile should throw an UnsupportedOperationException on anything but the default filesystem */
            path.toFile();
            return true;
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

}

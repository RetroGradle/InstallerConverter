package uk.gemwire.installerconverter.util.signing;

import java.io.File;

public class JarSignerInterop {

    //TODO: Actually implement the logic this uses here so we don't have to do this reflection
    /** @see sun.security.tools.jarsigner.Main */
    public static void sign(File keyStoreFile, String storepass, String keypass, String alias, File input, File output) {
        try {
            Class.forName("sun.security.tools.jarsigner.Main").getDeclaredMethod("main", String[].class).invoke(null,
                (Object) new String[] {
                    "-keystore", keyStoreFile.getAbsolutePath(),
                    "-storepass", storepass,
                    "-keypass", keypass,
                    "-signedjar", output.getAbsolutePath(),
                    input.getAbsolutePath(),
                    alias
                }
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

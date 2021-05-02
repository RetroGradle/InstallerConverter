package uk.gemwire.installerconverter.util.signing;

import java.nio.file.Path;
import javax.annotation.Nonnull;

import lombok.Value;
import lombok.With;
import lombok.experimental.Tolerate;

@Value
@With
public class SigningConfig {

    @Nonnull Path keystore;
    @Nonnull String storepass;
    @Nonnull String keypass;
    @Nonnull String alias;

    @Tolerate
    public SigningConfig withKeystore(String keystore) {
        return new SigningConfig(Path.of(keystore), storepass, keypass, alias);
    }

    public static SigningConfig withDefaults() {
        return new SigningConfig(Path.of("keystore.jks"), "", "", "forge");
    }

}

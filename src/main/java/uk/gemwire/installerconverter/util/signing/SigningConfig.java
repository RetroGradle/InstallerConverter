package uk.gemwire.installerconverter.util.signing;

import java.nio.file.Path;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public class SigningConfig {

    private final Path keystore;
    private final String storepass;
    private final String keypass;
    private final String alias;

    protected SigningConfig(Path keystore, String storepass, String keypass, String alias) {
        this.keystore = keystore;
        this.storepass = storepass;
        this.keypass = keypass;
        this.alias = alias;
    }

    public Path keystore() {
        return keystore;
    }

    public String storepass() {
        return storepass;
    }

    public String keypass() {
        return keypass;
    }

    public String alias() {
        return alias;
    }

    public SigningConfig withKeyStore(String keystore) {
        return new SigningConfig(Path.of(keystore), storepass, keypass, alias);
    }

    public SigningConfig withKeyStore(Path keystore) {
        return new SigningConfig(keystore, storepass, keypass, alias);
    }

    public SigningConfig withStorePass(String storepass) {
        return new SigningConfig(keystore, storepass, keypass, alias);
    }

    public SigningConfig withKeyPass(String keypass) {
        return new SigningConfig(keystore, storepass, keypass, alias);
    }

    public SigningConfig withAlias(String alias) {
        return new SigningConfig(keystore, storepass, keypass, alias);
    }

    public static SigningConfig withDefaults() {
        return new SigningConfig(Path.of("keystore.jks"), "", "", "forge");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SigningConfig that = (SigningConfig) o;
        return Objects.equals(keystore, that.keystore) && Objects.equals(storepass, that.storepass) && Objects.equals(keypass, that.keypass) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keystore, storepass, keypass, alias);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("keystore", keystore)
            .add("storepass", storepass)
            .add("keypass", keypass)
            .add("alias", alias)
            .toString();
    }
}

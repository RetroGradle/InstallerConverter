package uk.gemwire.installerconverter.v1_5.processor;

import java.util.Objects;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.MoreObjects;
import uk.gemwire.installerconverter.resolver.IResolver;
import uk.gemwire.installerconverter.util.JacksonUsed;
import uk.gemwire.installerconverter.util.maven.ArtifactKey;

public class DataFile {

    private final String client;
    private final String server;

    protected DataFile(String client, String server) {
        this.client = client;
        this.server = server;
    }

    @JacksonUsed
    public String getClient() {
        return client;
    }

    @JacksonUsed
    public String getServer() {
        return server;
    }

    public static DataFile of(String value) {
        return of(value, value);
    }

    public static DataFile ofLiteral(String value) {
        return of("'" + value + "'", "'" + value + "'");
    }

    public static DataFile of(String client, String server) {
        return new DataFile(client, server);
    }

    public static void inject(ObjectNode data, String key, String value) {
        data.putPOJO(key, DataFile.of(value));
    }

    public static void injectLiteral(ObjectNode data, String key, String value) {
        data.putPOJO(key, DataFile.ofLiteral(value));
    }

    public static void injectSha1(ObjectNode data, String key, ArtifactKey artifact, IResolver resolver) {
        injectLiteral(data, key, Objects.requireNonNull(resolver.resolve(artifact)).sha1Hash());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataFile dataFile = (DataFile) o;
        return Objects.equals(getClient(), dataFile.getClient()) && Objects.equals(getServer(), dataFile.getServer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClient(), getServer());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("client", client)
            .add("server", server)
            .toString();
    }
}

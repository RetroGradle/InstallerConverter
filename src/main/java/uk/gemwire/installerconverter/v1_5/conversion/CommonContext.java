package uk.gemwire.installerconverter.v1_5.conversion;

import java.util.Objects;
import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

public class CommonContext { //TODO: Change from CachedArtifactInfo?

    private final Config config;
    private final String minecraft;
    @Nullable private final CachedArtifactInfo client;
    @Nullable private final CachedArtifactInfo server;

    public CommonContext(Config config, String minecraft, @Nullable CachedArtifactInfo client, @Nullable CachedArtifactInfo server) {
        this.config = config;
        this.minecraft = minecraft;
        this.client = client;
        this.server = server;
    }

    public Config config() {
        return config;
    }

    public String minecraft() {
        return minecraft;
    }

    @Nullable
    public CachedArtifactInfo client() {
        return client;
    }

    @Nullable
    public CachedArtifactInfo server() {
        return server;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommonContext that = (CommonContext) o;
        return Objects.equals(config, that.config) && Objects.equals(minecraft, that.minecraft) && Objects.equals(client, that.client) && Objects.equals(server, that.server);
    }

    @Override
    public int hashCode() {
        return Objects.hash(config, minecraft, client, server);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("config", config)
            .add("minecraft", minecraft)
            .add("client", client)
            .add("server", server)
            .toString();
    }

    public static CommonContext of(Config config, String minecraft) {
        return new CommonContext(config, minecraft, null, null);
    }

    public static CommonContext of(Config config, String minecraft, @Nullable CachedArtifactInfo client, @Nullable CachedArtifactInfo server) {
        return new CommonContext(config, minecraft, client, server);
    }

}

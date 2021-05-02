package uk.gemwire.installerconverter.v1_5.processor;

import java.util.Objects;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Value;
import uk.gemwire.installerconverter.resolver.IResolver;
import uk.gemwire.installerconverter.util.maven.ArtifactKey;

@Value(staticConstructor = "of")
@Getter(onMethod_={@JsonProperty})
public class DataFile {

    @Nonnull String client;
    @Nonnull String server;

    public static DataFile of(String value) {
        return of(value, value);
    }

    public static DataFile ofLiteral(String value) {
        return of("'" + value + "'", "'" + value + "'");
    }

    public static void inject(ObjectNode data, String key, String value) {
        data.putPOJO(key, of(value));
    }

    public static void injectLiteral(ObjectNode data, String key, String value) {
        data.putPOJO(key, ofLiteral(value));
    }

    public static void injectSha1(ObjectNode data, String key, ArtifactKey artifact, IResolver resolver) {
        injectLiteral(data, key, Objects.requireNonNull(resolver.resolve(artifact)).sha1Hash());
    }

}

package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.legacy.LibraryTransformers;
import uk.gemwire.installerconverter.util.JacksonUsed;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;
import uk.gemwire.installerconverter.util.maven.Maven;

public final class LibraryInfo implements IConvertable<ObjectNode, CommonContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryInfo.class);

    private Artifact gav;
    private List<String> checksums;
    private boolean clientreq = false;
    private boolean serverreq = false;
    private String url = Maven.MOJANG;

    private ObjectNode extract;
    private ArrayNode rules;
    private ObjectNode natives;

    @JacksonUsed
    public void setName(String name) {
        this.gav = Artifact.of(name);
    }

    @JacksonUsed
    public void setChecksums(List<String> checksums) {
        this.checksums = checksums;
    }

    @JacksonUsed
    public void setClientreq(boolean clientreq) {
        this.clientreq = clientreq;
    }

    @JacksonUsed
    public void setServerreq(boolean serverreq) {
        this.serverreq = serverreq;
    }

    @JacksonUsed
    public void setUrl(String url) {
        this.url = url.replaceFirst("^http:", "https:") + (url.endsWith("/") ? "" : "/");
    }

    @JacksonUsed
    public void setExtract(ObjectNode extract) {
        this.extract = extract;
    }

    @JacksonUsed
    public void setRules(ArrayNode rules) {
        this.rules = rules;
    }

    @JacksonUsed
    public void setNatives(ObjectNode natives) {
        this.natives = natives;
    }

    @JacksonUsed("Used only in 1.6.x for scala")
    public void setComment(String comment) {

    }

    public String getUrl() {
        return url;
    }

    public Artifact getGav() {
        return gav;
    }

    public void setGav(Artifact gav) {
        this.gav = gav;
    }

    @Override
    public void validate() throws IllegalStateException {
        if (gav == null) throw new IllegalStateException("No Name for LibraryInfo");
    }

    @Override
    public ObjectNode convert(CommonContext context, JsonNodeFactory factory) throws IOException {
        // Legacy Forge Conversion (1.6.x / 1.5.x)
        upgradeLegacyForge(context.minecraft());

        //TODO: Handle clientreq / serverreq - What do we need to do with them

        ObjectNode downloads = factory.objectNode();

        //TODO: Testing
        if (gav.classifier() != null) {
            ObjectNode classifiers = factory.objectNode();
            classifiers.set(gav.classifier(), convertArtifact(context.config(), factory));

            downloads.set("classifiers", classifiers);
        } else {
            downloads.set("artifact", convertArtifact(context.config(), factory));
        }

        ObjectNode node = factory.objectNode();
        node.put("name", gav.asString());
        node.set("downloads", downloads);

        if (natives != null) node.set("natives", natives);
        if (rules != null) node.set("rules", rules);
        if (extract != null) node.set("extract", extract);

        return node;
    }

    private ObjectNode convertArtifact(Config config, JsonNodeFactory factory) throws IOException {
        String path = gav.asPath();
        String finalURL = url + path;

        LOGGER.debug("Resolving: " + finalURL);

        ObjectNode artifact = factory.objectNode();
        artifact.put("path", path);

        CachedArtifactInfo data = config.resolver().resolve(url, gav);

        if (data == null) throw new IOException(String.format("Couldn't get Sha1 or Size for '%s' from '%s'", gav.asStringWithClassifier(), url));

        artifact.put("url", data.url());
        artifact.put("sha1", data.sha1Hash());
        artifact.put("size", data.expectedSize());

        return artifact;
    }

    private void upgradeLegacyForge(String minecraft) {
        if (!Objects.equals(gav.artifact(), "minecraftforge")) return;

        gav = new Artifact(gav.group(), "forge", minecraft + "-" + gav.version(), null);
    }

    public void standardise(String minecraft) {
        LibraryTransformers.execute(minecraft, this);
    }

}

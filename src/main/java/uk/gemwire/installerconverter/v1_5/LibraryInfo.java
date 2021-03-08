package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.legacy.LibraryTransformers;
import uk.gemwire.installerconverter.util.JacksonUsed;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.ArtifactKey;
import uk.gemwire.installerconverter.util.maven.Maven;
import uk.gemwire.installerconverter.v1_5.conversion.CommonContext;
import uk.gemwire.installerconverter.v1_5.conversion.IConvertable;

public final class LibraryInfo implements IConvertable<ObjectNode, CommonContext> {

    private Artifact gav;
    private List<String> checksums;
    private boolean clientreq = false;
    private boolean serverreq = false;
    private String url = Maven.MOJANG;

    private ObjectNode extract;
    private ArrayNode rules;
    private ObjectNode natives;

    @JacksonUsed
    public void setName(Artifact gav) {
        this.gav = gav;
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
        //TODO: Handle clientreq / serverreq - What do we need to do with them

        ObjectNode node = gav.classifier() != null
            ? wrapClassifier(ArtifactKey.of(url, gav), context.config(), factory)
            : wrapArtifact(gav.asString(), ArtifactKey.of(url, gav), context.config(), factory);

        if (natives != null) node.set("natives", natives);
        if (rules != null) node.set("rules", rules);
        if (extract != null) node.set("extract", extract);

        return node;
    }

    public void standardise(String minecraft) {
        LibraryTransformers.execute(minecraft, this);
    }

    public static ObjectNode wrapArtifact(String name, ArtifactKey artifact, Config config, JsonNodeFactory factory) throws IOException {
        ObjectNode downloads = factory.objectNode();
        downloads.set("artifact", artifact.convert(config, factory));

        ObjectNode node = factory.objectNode();
        node.put("name", name);
        node.set("downloads", downloads);

        return node;
    }

    public static ObjectNode wrapClassifier(ArtifactKey artifact, Config config, JsonNodeFactory factory) throws IOException {
        ObjectNode classifiers = factory.objectNode();
        classifiers.set(artifact.artifact().classifier(), artifact.convert(config, factory));

        ObjectNode downloads = factory.objectNode();
        downloads.set("classifiers", classifiers);

        ObjectNode node = factory.objectNode();
        node.put("name", artifact.artifact().asString());
        node.set("downloads", downloads);

        return node;
    }

    public static LibraryInfo of(ArtifactKey key) {
        LibraryInfo info = new LibraryInfo();
        info.setUrl(key.host());
        info.setGav(key.artifact());
        return info;
    }

    public boolean isServerReq() {
        return serverreq;
    }
}

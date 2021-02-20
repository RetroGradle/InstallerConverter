package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.util.JacksonUsed;
import uk.gemwire.installerconverter.util.common.Pair;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;
import uk.gemwire.installerconverter.util.maven.Maven;

public final class InstallProfile implements IConvertable<Pair<ObjectNode, ObjectNode>, Config> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstallProfile.class);

    private Install install;
    private VersionInfo versionInfo;
    private List<OptionalLibrary> optionals;

    @JacksonUsed
    public void setInstall(Install install) {
        this.install = install;
    }

    @JacksonUsed
    public void setVersionInfo(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    @JacksonUsed
    public void setOptionals(List<OptionalLibrary> optionals) {
        this.optionals = optionals;
    }

    @Override
    public void validate() throws IllegalStateException {
        if (optionals != null && optionals.size() != 0) {
            LOGGER.warn("Skipping OptionalLibraries [{}]", optionals.stream().map(OptionalLibrary::getDescription).collect(Collectors.joining(",")));
        }

        if (install == null) throw new IllegalStateException("No Installer info");
        install.validate();

        if (versionInfo == null) throw new IllegalStateException("No VersionInfo info");
        versionInfo.validate();
    }

    @Override
    public Pair<ObjectNode, ObjectNode> convert(Config config, JsonNodeFactory factory) throws IOException {
        String minecraft = install.getMinecraft();
        CachedArtifactInfo client = null;
        CachedArtifactInfo server = null;
        if (minecraft.startsWith("1.5")) {
            client = config.resolver().resolve(Maven.FAKE, Artifact.of("net.minecraft:client:{version}:stripped".replace("{version}", minecraft))); // CachedArtifactInfo.of("90f9a7e7e651990b2dc81bfeb10f2b01f6956165", 0, "");
            server = config.resolver().resolve(Maven.FAKE, Artifact.of("net.minecraft:server:{version}:stripped".replace("{version}", minecraft)));
        }

        return Pair.of(install.convert(CommonContext.of(config, minecraft, client, server), factory), versionInfo.convert(CommonContext.of(config, minecraft, client, server), factory));
    }
}

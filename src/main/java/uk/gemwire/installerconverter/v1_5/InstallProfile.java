package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.util.IConvertable;
import uk.gemwire.installerconverter.util.JacksonUsed;
import uk.gemwire.installerconverter.util.Log;
import uk.gemwire.installerconverter.util.Pair;

public final class InstallProfile implements IConvertable<Pair<ObjectNode, ObjectNode>> {

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
            Log.warn(String.format("Skipping OptionalLibraries [%s]", optionals.stream().map(OptionalLibrary::getDescription).collect(Collectors.joining(","))));
        }

        if (install == null) throw new IllegalStateException("No Installer info");
        install.validate();

        if (versionInfo == null) throw new IllegalStateException("No VersionInfo info");
        versionInfo.validate();
    }

    @Override
    public Pair<ObjectNode, ObjectNode> convert(JsonNodeFactory factory) throws IOException {
        return Pair.of(install.convert(factory), versionInfo.convert(factory));
    }
}

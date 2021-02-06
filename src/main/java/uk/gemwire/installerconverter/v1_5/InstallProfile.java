package uk.gemwire.installerconverter.v1_5;

import java.util.List;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.util.IConvertable;
import uk.gemwire.installerconverter.util.Pair;

public final class InstallProfile implements IConvertable<Pair<ObjectNode, ObjectNode>> {

    private Install install;
    private VersionInfo versionInfo;
    private List<OptionalLibrary> optionals;

    public void setInstall(Install install) {
        this.install = install;
    }

    public Install getInstall() {
        return install;
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    public void setOptionals(List<OptionalLibrary> optionals) {
        this.optionals = optionals;
    }

    public List<OptionalLibrary> getOptionals() {
        return optionals;
    }

    @Override
    public void validate() throws IllegalStateException {
        //TODO: optionals

        if (install == null) throw new IllegalStateException("No Installer info");
        install.validate();

        if (versionInfo == null) throw new IllegalStateException("No VersionInfo info");
        versionInfo.validate();
    }

    @Override
    public Pair<ObjectNode, ObjectNode> convert(JsonNodeFactory factory) {
        validate(); //TODO: Call before convert

        //TODO: optionals

        return Pair.of(install.convert(factory), versionInfo.convert(factory));
    }
}

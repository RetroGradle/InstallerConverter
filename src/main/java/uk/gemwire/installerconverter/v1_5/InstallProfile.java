package uk.gemwire.installerconverter.v1_5;

import java.util.List;

public final class InstallProfile {

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
}

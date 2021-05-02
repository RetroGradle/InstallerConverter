package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.util.JacksonUsed;
import uk.gemwire.installerconverter.v1_5.conversion.CommonContext;
import uk.gemwire.installerconverter.v1_5.conversion.Converted;
import uk.gemwire.installerconverter.v1_5.conversion.IConvertable;

public final class InstallProfile implements IConvertable<Converted, Config> {

    private static final Logger LOGGER = LoggerFactory.getLogger("InstallProfile");

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
    public Converted convert(Config config, JsonNodeFactory factory) throws IOException {
        String minecraft = install.getMinecraft();

        return Converted.of(install.convert(CommonContext.of(config, minecraft), factory), versionInfo.convert(CommonContext.of(config, minecraft), factory));
    }
}

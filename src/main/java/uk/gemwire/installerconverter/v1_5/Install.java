package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.util.JacksonUsed;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.ArtifactKey;
import uk.gemwire.installerconverter.util.maven.Maven;
import uk.gemwire.installerconverter.v1_5.conversion.CommonContext;
import uk.gemwire.installerconverter.v1_5.conversion.Conversions;
import uk.gemwire.installerconverter.v1_5.conversion.IConvertable;
import uk.gemwire.installerconverter.v1_5.processor.DataFile;
import uk.gemwire.installerconverter.v1_5.processor.Processor;
import uk.gemwire.installerconverter.v1_5.processor.ProcessorStep;

public final class Install implements IConvertable<ObjectNode, CommonContext> {

    private String profileName = "forge";
    private String target;
    private String path;
    private String modList;
    private String version;
    private String welcome = "Welcome to the simple Forge installer.";
    private String logo = "/big_logo.png";
    private String urlIcon;
    private boolean stripMeta = false;
    private String filePath;
    private String minecraft;
    private String mirrorList;
    private boolean hideClient = false;
    private boolean hideServer = false;
    private boolean hideExtract = false;

    private ObjectNode data;
    private ArrayNode processors;
    private ArrayNode libraries;

    @JacksonUsed
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    @JacksonUsed
    public void setTarget(String target) {
        this.target = target;
    }

    @JacksonUsed
    public void setPath(String path) {
        this.path = path;
    }

    @JacksonUsed
    public void setModList(@Nullable String modList) {
        this.modList = modList;
    }

    @JacksonUsed
    public void setVersion(String version) {
        this.version = version;
    }

    @JacksonUsed
    public void setWelcome(String welcome) {
        this.welcome = welcome;
    }

    @JacksonUsed
    public void setLogo(String logo) {
        this.logo = logo;
    }

    @JacksonUsed
    public void setUrlIcon(String urlIcon) {
        this.urlIcon = urlIcon;
    }

    @JacksonUsed
    public void setStripMeta(boolean stripMeta) {
        this.stripMeta = stripMeta;
    }

    @JacksonUsed
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @JacksonUsed
    public void setMinecraft(String minecraft) {
        this.minecraft = minecraft;
    }

    @JacksonUsed
    public void setMirrorList(String mirrorList) {
        this.mirrorList = mirrorList;
    }

    @JacksonUsed
    public void setHideClient(boolean hideClient) {
        this.hideClient = hideClient;
    }

    @JacksonUsed
    public void setHideServer(boolean hideServer) {
        this.hideServer = hideServer;
    }

    @JacksonUsed
    public void setHideExtract(boolean hideExtract) {
        this.hideExtract = hideExtract;
    }

    public String getMinecraft() {
        return minecraft;
    }

    @Override
    public void validate() throws IllegalStateException {
        // TODO: More Validation
        if (minecraft == null) throw new IllegalStateException("No Minecraft Version Specified");
        if (path == null)      throw new IllegalStateException("No Path Specified");

        // Validate that the Id Conversion works for the target
        try {
            Conversions.convertId(target);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Override
    public ObjectNode convert(CommonContext context, JsonNodeFactory factory) throws IOException {
        ObjectNode node = factory.objectNode();

        version = Conversions.convertId(target);

        LibraryInfo forge = new LibraryInfo();
        forge.setName(Artifact.of(path));
        forge.setUrl(Maven.FORGE);
        forge.standardise(minecraft);

        /* Skip MirrorList if it's forges - TODO: Check this is correct */
        if (Objects.equals(mirrorList, "http://files.minecraftforge.net/mirror-brand.list")) mirrorList = null;

        node.set("_comment_", Conversions.createCommentNode(factory));
        node.put("spec", 0);
        node.put("profile", Conversions.convertProfile(profileName));
        node.put("version", version);
        node.put("icon", context.config().icon()); //TODO: Conversion?
        node.put("json", "/version.json");
        node.put("path", forge.getGav().asStringWithClassifier());
        node.put("logo", logo);
        node.put("minecraft", minecraft);

        if (urlIcon != null)
            node.put("urlIcon", urlIcon);

        node.put("welcome", Conversions.convertWelcome(welcome));

        if (mirrorList != null)
            node.put("mirrorList", mirrorList);

        if (hideClient)
            node.put("hideClient", true);
        if (hideServer)
            node.put("hideServer", true);
        if (hideExtract)
            node.put("hideExtract", true);

        if (data == null) data = factory.objectNode();
        if (processors == null) processors = factory.arrayNode();
        if (libraries == null) libraries = factory.arrayNode();

        if (minecraft.startsWith("1.7.")) transform_1_7_or_1_6(context, factory);

        node.set("data", data);
        node.set("processors", processors);

        // Add the forge library.
        node.set("libraries", libraries.add(forge.convert(context, factory)));

        return node;
    }

    //TODO: MIGRATE TO AN OFFICIAL FORGE BUILD
    private static final Processor INSTALLER_TOOLS = Processor.of(Maven.ATERANIMAVIS,
        "net.minecraftforge:installertools:1.2.1-local",
        ArtifactKey.of(Maven.FORGE, "com.google.code.gson:gson:2.8.6"),
        ArtifactKey.of(Maven.FORGE, "com.google.guava:guava:20.0"),
        ArtifactKey.of(Maven.FORGE, "de.siegmar:fastcsv:2.0.0"),
        ArtifactKey.of(Maven.FORGE, "net.md-5:SpecialSource:1.8.5"),
        ArtifactKey.of(Maven.FORGE, "net.minecraftforge:srgutils:0.4.1"),
        ArtifactKey.of(Maven.FORGE, "net.sf.jopt-simple:jopt-simple:5.0.4"),
        ArtifactKey.of(Maven.FORGE, "net.sf.opencsv:opencsv:2.3"),
        ArtifactKey.of(Maven.FORGE, "org.ow2.asm:asm:6.1.1"),
        ArtifactKey.of(Maven.FORGE, "org.ow2.asm:asm-analysis:6.1.1"),
        ArtifactKey.of(Maven.FORGE, "org.ow2.asm:asm-commons:6.1.1"),
        ArtifactKey.of(Maven.FORGE, "org.ow2.asm:asm-tree:6.1.1")
    );

    /*
     * Can be in the following formats:
     * [value] - An absolute path to an artifact located in the target maven style repo.
     * 'value' - A string literal, remove the 's and use this value
     * value - A file in the installer package, to be extracted to a temp folder, and then have the absolute path in replacements.
     */

    private void transform_1_7_or_1_6(CommonContext context, JsonNodeFactory factory) throws IOException {
        INSTALLER_TOOLS.injectLibraries(libraries, context.config(), factory);
        ArtifactKey LEGACY_JAVA_FIXER = ArtifactKey.of(Maven.FORGE, "net.minecraftforge.lex:legacyjavafixer:1.0");
        Processor.inject(LEGACY_JAVA_FIXER, libraries, context.config(), factory);

        DataFile.inject(data, "LEGACYJAVAFIXER", "|mods/legacyjavafixer-1.0.jar|");
        DataFile.injectSha1(data, "LEGACYJAVAFIXER_HASH", LEGACY_JAVA_FIXER, context.config().resolver());

        List<ProcessorStep> steps = List.of(
            // Both: Copy LegacyJavaFixer to Mods folder
            ProcessorStep.both(
                INSTALLER_TOOLS,
                Map.of("{LEGACYJAVAFIXER}", "{LEGACYJAVAFIXER_HASH}"),
                "--task", "COPY",
                "--input", "[net.minecraftforge.lex:legacyjavafixer:1.0]",
                "--output", "{LEGACYJAVAFIXER}"
            )
        );

        steps.forEach(step -> processors.add(step.toNode(factory)));
    }

}

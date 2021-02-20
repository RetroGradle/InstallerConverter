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
import uk.gemwire.installerconverter.util.maven.Maven;

public final class Install implements IConvertable<ObjectNode, CommonContext> {

    private String profileName;
    private String target;
    private String path;
    private String modList;
    private String version;
    private String welcome;
    private String logo;
    private String urlIcon;
    private boolean stripMeta = false;
    private String filePath;
    private String minecraft;
    private String mirrorList;
    private boolean hideClient = false;
    private boolean hideServer = false;
    private boolean hideExtract = false;

    private ObjectNode data;
    private ArrayNode processor;
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

        // Validate that the Id Conversion works for the target
        try {
            Conversions.convertId(target);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Override
    public ObjectNode convert(CommonContext context, JsonNodeFactory factory) throws IOException {
        ObjectNode node = factory.objectNode();

        version = Conversions.convertId(target);

        /* Skip MirrorList if it's forges - TODO: Check this is correct */
        if (Objects.equals(mirrorList, "http://files.minecraftforge.net/mirror-brand.list")) mirrorList = null;

        node.set("_comment_", Conversions.createCommentNode(factory));
        node.put("spec", 0);
        node.put("profile", Conversions.convertProfile(profileName));
        node.put("version", version);
        node.put("icon", context.config().icon()); //TODO: Conversion?
        node.put("json", "/version.json");
        node.put("path", path);
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
        if (processor == null) processor = factory.arrayNode();
        if (libraries == null) libraries = factory.arrayNode();

        if (minecraft.startsWith("1.5.")) {
            /*
             * Can be in the following formats:
             * [value] - An absolute path to an artifact located in the target maven style repo.
             * 'value' - A string literal, remove the 's and use this value
             * value - A file in the installer package, to be extracted to a temp folder, and then have the absolute path in replacements.
             */
            data.set("STRIPPED", factory.objectNode()
                .put("client", "[net.minecraft:client:" + minecraft + ":stripped]")
                .put("server", "[net.minecraft:server:" + minecraft + ":stripped]")
            );
            data.set("STRIPPED_SHA1", factory.objectNode()
                .put("client", "'{SHA1}'".replace("{SHA1}", context.client().sha1Hash()))
                .put("server", "'{SHA1}'".replace("{SHA1}", context.server().sha1Hash()))
            );

            processor.add(Processor.of(
                Artifact.of("uk.gemwire:RetroInstallerTools:0.1:fatjar"),
                new Artifact[0],
                new String[] { "--task=STRIP_SIGNATURES", "--input", "{MINECRAFT_JAR}", "--output", "{STRIPPED}" },
                Map.of("{STRIPPED}", "{STRIPPED_SHA1}"),
                List.of()
            ).toNode(factory));

            processor.add(Processor.of(
                Artifact.of("uk.gemwire:RetroInstallerTools:0.1:fatjar"),
                new Artifact[0],
                new String[] { "--task=COPY_AS_CLIENT_JAR", "--input", "{STRIPPED}", "--reference", "{MINECRAFT_JAR}", "--version", version },
                Map.of(),
                List.of("client")
            ).toNode(factory));

            LibraryInfo tools = new LibraryInfo();
            tools.setName("uk.gemwire:RetroInstallerTools:0.1:fatjar");
            tools.setUrl(Maven.FORGE);
            libraries.add(tools.convert(context, factory));
        }

        node.set("data", data);
        node.set("processors", processor);

        // Add the forge library. (TODO: Check if path is the correct value for all cases we care about)
        LibraryInfo forge = new LibraryInfo();
        forge.setName(path);
        forge.setUrl(Maven.FORGE);
        node.set("libraries", libraries.add(forge.convert(context, factory)));

        return node;
    }
}

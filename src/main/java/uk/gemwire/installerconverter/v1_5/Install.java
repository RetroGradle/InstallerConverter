package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.resolver.retrotools.ClasspathJar;
import uk.gemwire.installerconverter.util.JacksonUsed;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.ArtifactKey;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;
import uk.gemwire.installerconverter.util.maven.Maven;
import uk.gemwire.installerconverter.v1_5.conversion.CommonContext;
import uk.gemwire.installerconverter.v1_5.conversion.Conversions;
import uk.gemwire.installerconverter.v1_5.conversion.IConvertable;
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
        if (minecraft.startsWith("1.6.")) transform_1_7_or_1_6(context, factory);
        if (minecraft.startsWith("1.5.")) transform_1_5(context, factory);

        node.set("data", data);
        node.set("processors", processors);

        // Add the forge library.
        node.set("libraries", libraries.add(forge.convert(context, factory)));

        return node;
    }

    private static final Processor RETRO_TOOLS = Processor.of(Maven.ATERANIMAVIS, "uk.gemwire:RetroInstallerTools:0.1:fatjar");

    /*
     * Can be in the following formats:
     * [value] - An absolute path to an artifact located in the target maven style repo.
     * 'value' - A string literal, remove the 's and use this value
     * value - A file in the installer package, to be extracted to a temp folder, and then have the absolute path in replacements.
     */

    /*
     * SIDE           - 'client'                       | 'server'
     * MINECRAFT_JAR  - [versions/1.5.2/1.5.2.jar]     | [minecraft_server.1.5.2.jar]
     * TARGET_JAR     - [versions/1.5.2-#/1.5.2-#.jar] | [forge-1.5.2-#.jar]
     * BASE_DIRECTORY -
     */

    private void transform_1_7_or_1_6(CommonContext context, JsonNodeFactory factory) throws IOException {
        RETRO_TOOLS.injectLibraries(libraries, context.config(), factory);
        Processor.inject(ArtifactKey.of(Maven.ATERANIMAVIS, "net.minecraftforge.lex:legacyjavafixer:1.0"), libraries, context.config(), factory);

        //TODO: 1.6 need a minecraft_server with ClassPath (1.6.3-9.11.0.873)

        List<ProcessorStep> steps = List.of(
            // Both: Copy LegacyJavaFixer to Mods folder
            ProcessorStep.both(
                RETRO_TOOLS,
                "--task", "COPY_RELATIVE",
                "--input", "[net.minecraftforge.lex:legacyjavafixer:1.0]",
                "--base", "{BASE_DIRECTORY}",
                "--path", "mods"
            )
        );

        steps.forEach(step -> processors.add(step.toNode(factory)));
    }

    private void transform_1_5(CommonContext context, JsonNodeFactory factory) throws IOException {
        //TODO: Test a rename of minecraft_server.1.12.2 to minecraft_server
        //TODO: A rename of minecraft_server.1.12.2 to minecraft_server works...
        //TODO: Do something with injecting ClassPath into server instead of forge? we can then cache that as should be same for most versions

        RETRO_TOOLS.injectLibraries(libraries, context.config(), factory);
        Processor.inject(ArtifactKey.of(Maven.ATERANIMAVIS, "net.minecraftforge_temp.legacy:legacyfixer:1.0"), libraries, context.config(), factory);

        data.set("STRIPPED", factory.objectNode()
            .put("client", "[net.minecraft:client:" + minecraft + ":stripped]")
            .put("server", "[net.minecraft:server:" + minecraft + ":stripped]")
        );
        data.set("STRIPPED_SHA1", factory.objectNode()
            .put("client", "'{SHA1}'".replace("{SHA1}", context.client().sha1Hash()))
            .put("server", "'{SHA1}'".replace("{SHA1}", context.server().sha1Hash()))
        );

        String[] classpath_args = new String[] {
            "--task", "GENERATE_CLASSPATH_JAR",
            "--output", "{CLASSPATH}",
            "--classpath", "org.ow2.asm:asm-all:4.1",
            "--classpath", "org.scala-lang:scala-library:2.10.0-custom",
            "--classpath", "net.sourceforge.argo:argo:3.2-small",
            "--classpath", "org.bouncycastle:bcprov-jdk15on:1.47",
            "--classpath", "com.google.guava:guava:14.0",
            "--classpath", "minecraft_server.{version}.jar".replace("{version}", minecraft)
        };

        CachedArtifactInfo classpath = ClasspathJar.provide(minecraft, classpath_args);

        data.set("CLASSPATH", factory.objectNode()
            .put("client", "''")
            .put("server", "[net.minecraftforge:server:{version}:classpath]".replace("{version}", version))
        );

        data.set("CLASSPATH_SHA1", factory.objectNode()
            .put("client", "''")
            .put("server", "'" + classpath.sha1Hash() + "'")
        );

        //List<ProcessorStep> steps = List.of(
        //    // Client: Create a Stripped Jar
        //    ProcessorStep.client(
        //        RETRO_TOOLS,
        //        Map.of("{STRIPPED}", "{STRIPPED_SHA1}"),
        //        "--task", "STRIP_SIGNATURES",
        //        "--input", "{MINECRAFT_JAR}",
        //        "--output", "{STRIPPED}"
        //    ),
        //
        //    // Client: Copy the Client Jar into Place
        //    ProcessorStep.client(RETRO_TOOLS, "--task", "COPY", "--input", "{STRIPPED}", "--output", "{TARGET_JAR}"),
        //
        //    // Server: Inject Classpath Entry to Manifest
        //    ProcessorStep.server(
        //        RETRO_TOOLS,
        //        // Map.of("{CLASSPATH}", "{CLASSPATH_SHA1}"), //TODO: Should we calculate a SHA1 of this? Means we have to do so for all forge versions
        //        "--task", "INJECT_CLASSPATH",
        //        "--input", "{TARGET_JAR}",
        //        "--output", "{TARGET_JAR}",
        //        "--classpath", "org.ow2.asm:asm-all:4.1",
        //        "--classpath", "org.scala-lang:scala-library:2.10.0-custom",
        //        "--classpath", "net.sourceforge.argo:argo:3.2-small",
        //        "--classpath", "org.bouncycastle:bcprov-jdk15on:1.47",
        //        "--classpath", "com.google.guava:guava:14.0",
        //        "--classpath", "minecraft_server.{version}.jar".replace("{version}", minecraft)
        //    ),
        //
        //    // Server: Copy LegacyFixer to CoreMods folder, Client provides it via launcher json
        //    ProcessorStep.server(RETRO_TOOLS,
        //        "--task", "COPY_RELATIVE",
        //        "--input", "[net.minecraftforge_temp.legacy:legacyfixer:1.0]",
        //        "--base", "{BASE_DIRECTORY}",
        //        "--path", "coremods"
        //    )
        //);

        List<ProcessorStep> steps = List.of(
            // Client: Create a Stripped Jar
            ProcessorStep.client(
                RETRO_TOOLS,
                Map.of("{STRIPPED}", "{STRIPPED_SHA1}"),
                "--task", "STRIP_SIGNATURES",
                "--input", "{MINECRAFT_JAR}",
                "--output", "{STRIPPED}"
            ),

            // Client: Copy the Client Jar into Place
            ProcessorStep.client(RETRO_TOOLS, "--task", "COPY", "--input", "{STRIPPED}", "--output", "{TARGET_JAR}"),

            // Server: Inject Classpath Entry to Manifest
            ProcessorStep.server(
                RETRO_TOOLS,
                 Map.of("{CLASSPATH}", "{CLASSPATH_SHA1}"),
                classpath_args
            ),

            // Server: Copy Generated ClassPath Jar as minecraft_server.jar
            ProcessorStep.server(
                RETRO_TOOLS,
                "--task", "COPY_RELATIVE",
                "--input", "{CLASSPATH}",
                "--base", "{BASE_DIRECTORY}",
                "--path", "minecraft_server.jar"
            ),

            // Server: Copy LegacyFixer to CoreMods folder, Client provides it via launcher json
            ProcessorStep.server(
                RETRO_TOOLS,
                "--task", "COPY_RELATIVE",
                "--input", "[net.minecraftforge_temp.legacy:legacyfixer:1.0]",
                "--base", "{BASE_DIRECTORY}",
                "--path", "coremods"
            )
        );

        steps.forEach(step -> processors.add(step.toNode(factory)));
    }

}

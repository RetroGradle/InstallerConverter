package uk.gemwire.installerconverter;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import uk.gemwire.installerconverter.util.Jackson;
import uk.gemwire.installerconverter.util.maven.ArtifactKey;
import uk.gemwire.installerconverter.util.maven.Maven;
import uk.gemwire.installerconverter.v1_5.Install;
import uk.gemwire.installerconverter.v1_5.InstallProfile;
import uk.gemwire.installerconverter.v1_5.VersionInfo;
import uk.gemwire.installerconverter.v1_5.conversion.Conversions;

public class InstallerGenerator {

    public static InstallProfile generate(String version) {
        JsonNodeFactory factory = Jackson.factory();

        String standardisedVersion = Conversions.convertVersion(version);
        String minecraft = standardisedVersion.split("-", 2)[0];
        String target    = minecraft + "-forge-" + standardisedVersion.split("-", 2)[1];
        String forge     = "net.minecraftforge:forge:" + standardisedVersion;

        //==================================================================================================================================

        Install install = new Install();
        install.setMinecraft(minecraft);
        install.setTarget(target);
        install.setPath(forge);

        //==================================================================================================================================

        VersionInfo info = new VersionInfo();
        info.setId(target);

        info.setAdditionalData("inheritsFrom", factory.textNode(minecraft));
        info.setAdditionalData("minecraftArguments", factory.textNode("--username ${auth_player_name} --session ${auth_session} --version ${version_name} --gameDir ${game_directory} --assetsDir ${game_assets} --tweakClass cpw.mods.fml.common.launcher.FMLTweaker"));
        info.setAdditionalData("processArguments", factory.textNode("username_session_version"));

        info.addLibrary(ArtifactKey.of(Maven.FORGE, forge));
        info.addLibrary(ArtifactKey.of("net.minecraft:launchwrapper:1.8"));
        info.addLibrary(ArtifactKey.of("org.ow2.asm:asm-all:4.1"));
        info.addLibrary(ArtifactKey.of(Maven.FORGE, "org.scala-lang:scala-library:2.10.2"));
        info.addLibrary(ArtifactKey.of(Maven.FORGE, "org.scala-lang:scala-compiler:2.10.2"));
        info.addLibrary(ArtifactKey.of("lzma:lzma:0.0.1"));
        info.addLibrary(ArtifactKey.of("net.sf.jopt-simple:jopt-simple:4.5"));

        /*
            TODO: Base Artifacts off the Manifest.MF or update Class-Path

            1.6.4-8.11.0.879 requires launchwrapper 1.3 instead of 1.8

            libraries/net/minecraft/launchwrapper/1.3/launchwrapper-1.
             3.jar libraries/org/ow2/asm/asm-all/4.1/asm-all-4.1.jar libraries/lzm
             a/lzma/0.0.1/lzma-0.0.1.jar libraries/net/sf/jopt-simple/jopt-simple/
             4.5/jopt-simple-4.5.jar libraries/org/scala-lang/scala-library/2.10.2
             /scala-library-2.10.2.jar libraries/org/scala-lang/scala-compiler/2.1
             0.2/scala-compiler-2.10.2.jar minecraft_server.1.6.4.jar

         */

        //==================================================================================================================================

        InstallProfile profile = new InstallProfile();
        profile.setInstall(install);
        profile.setVersionInfo(info);

        return profile;
    }

}

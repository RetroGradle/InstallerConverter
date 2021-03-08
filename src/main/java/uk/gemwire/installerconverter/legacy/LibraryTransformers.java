package uk.gemwire.installerconverter.legacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.ArtifactKey;
import uk.gemwire.installerconverter.util.maven.Maven;
import uk.gemwire.installerconverter.v1_5.LibraryInfo;

public class LibraryTransformers {
    //TODO: CLEANUP + SIMPLIFY

    static List<BiFunction<String, ArtifactKey, ArtifactKey>> TRANSFORMERS = new ArrayList<>();

    static Predicate<Artifact> FILTER_MINECRAFTFORGE = filterGroup("net.minecraftforge").and(filterArtifact("minecraftforge"));

    static Artifact OBJECT_WEB_ASM_OLD = Artifact.of("org.ow2.asm:asm:4.1-all");
    static Artifact OBJECT_WEB_ASM     = Artifact.of("org.ow2.asm:asm-all:4.1");

    static Artifact GUAVA_14_RC3       = Artifact.of("com.google.guava:guava:14.0-rc3");
    static Artifact GUAVA_14           = Artifact.of("com.google.guava:guava:14.0");

    static Artifact BOUNCY_148         = Artifact.of("org.bouncycastle:bcprov-jdk15on:148");
    static Artifact BOUNCY_1_47        = Artifact.of("org.bouncycastle:bcprov-jdk15on:1.47");

    //TODO: LEGACYFIXER needs to be updated so it handles options.txt causing a crash
    public static Artifact LEGACY_FIXER       = Artifact.of("net.minecraftforge_temp.legacy:legacyfixer:1.0");
    static Artifact SCALA_CUSTOM       = Artifact.of("org.scala-lang:scala-library:2.10.0-custom");

    static {
        TRANSFORMERS.add(LibraryTransformers::transformForge);
        TRANSFORMERS.add(LibraryTransformers::transformAsm);
        TRANSFORMERS.add(LibraryTransformers::transformGuava14);
        TRANSFORMERS.add(LibraryTransformers::transformBouncy);
        TRANSFORMERS.add(LibraryTransformers::transformLegacyFixer);
        TRANSFORMERS.add(LibraryTransformers::transformScalaCustom);
    }

    public static ArtifactKey execute(String minecraft, ArtifactKey key) {
        for (BiFunction<String, ArtifactKey, ArtifactKey> transformer : TRANSFORMERS) {
            key = transformer.apply(minecraft, key);
        }

        return key;
    }

    public static void execute(String minecraft, LibraryInfo libraryInfo) {
        ArtifactKey info = execute(minecraft, ArtifactKey.of(libraryInfo));

        libraryInfo.setUrl(info.host());
        libraryInfo.setGav(info.artifact());
    }

    private static ArtifactKey transformForge(String minecraft, ArtifactKey info) {
        return transform(info, FILTER_MINECRAFTFORGE, (gav) -> new Artifact(gav.group(), "forge", minecraft + "-" + gav.version(), null));
    }

    private static ArtifactKey transformAsm(String minecraft, ArtifactKey info) {
        return transform(info, filterArtifact(OBJECT_WEB_ASM_OLD), gav -> OBJECT_WEB_ASM, url -> Maven.MOJANG);
    }

    private static ArtifactKey transformGuava14(String minecraft, ArtifactKey info) {
        return transform(info, filterArtifact(GUAVA_14_RC3), gav -> GUAVA_14, url -> Maven.MOJANG);
    }

    private static ArtifactKey transformBouncy(String minecraft, ArtifactKey info) {
        return transform(info, filterArtifact(BOUNCY_148), gav -> BOUNCY_1_47, url -> Maven.MOJANG);
    }

    private static ArtifactKey transformLegacyFixer(String minecraft, ArtifactKey info) {
        return transform(info, filterArtifact(LEGACY_FIXER), gav -> gav, url -> Maven.ATERANIMAVIS);
    }

    private static ArtifactKey transformScalaCustom(String minecraft, ArtifactKey info) {
        return transform(info, filterArtifact(SCALA_CUSTOM), gav -> gav, url -> Maven.ATERANIMAVIS);
    }

    private static ArtifactKey transform(ArtifactKey info, Predicate<ArtifactKey> predicate, Function<Artifact, Artifact> transformArtifact, Function<String, String> transformUrl) {
        if (!predicate.test(info)) return info;

        return ArtifactKey.of(transformUrl.apply(info.host()), transformArtifact.apply(info.artifact()));
    }

    private static ArtifactKey transform(ArtifactKey info, Predicate<Artifact> predicate, Function<Artifact, Artifact> transform) {
        Artifact gav = info.artifact();

        if (!predicate.test(gav)) return info;

        return info.with(transform.apply(gav));
    }

    private static Predicate<ArtifactKey> filterArtifact(Artifact artifact) {
        return info -> Objects.equals(artifact, info.artifact());
    }

    private static Predicate<Artifact> filterGroup(String group) {
        return gav -> Objects.equals(gav.group(), group);
    }

    private static Predicate<Artifact> filterArtifact(String artifact) {
        return gav -> Objects.equals(gav.artifact(), artifact);
    }

}

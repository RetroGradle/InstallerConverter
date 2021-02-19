package uk.gemwire.installerconverter.legacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.Maven;
import uk.gemwire.installerconverter.v1_5.LibraryInfo;

public class LibraryTransformers {
    //TODO: CLEANUP + SIMPLIFY

    static List<BiFunction<String, MinimalLibraryInfo, MinimalLibraryInfo>> TRANSFORMERS = new ArrayList<>();

    static Predicate<Artifact> FILTER_MINECRAFTFORGE = filterGroup("net.minecraftforge").and(filterArtifact("minecraftforge"));

    static Artifact OBJECT_WEB_ASM_OLD = Artifact.of("org.ow2.asm:asm:4.1-all");
    static Artifact OBJECT_WEB_ASM     = Artifact.of("org.ow2.asm:asm-all:4.1");

    static Artifact GUAVA_14_RC3       = Artifact.of("com.google.guava:guava:14.0-rc3");
    static Artifact GUAVA_14           = Artifact.of("com.google.guava:guava:14.0");

    static Artifact BOUNCY_148         = Artifact.of("org.bouncycastle:bcprov-jdk15on:148");
    static Artifact BOUNCY_1_47        = Artifact.of("org.bouncycastle:bcprov-jdk15on:1.47");

    public static Artifact LEGACY_FIXER       = Artifact.of("net.minecraftforge_temp.legacy:legacyfixer:1.0");

    static {
        TRANSFORMERS.add(LibraryTransformers::transformForge);
        TRANSFORMERS.add(LibraryTransformers::transformAsm);
        TRANSFORMERS.add(LibraryTransformers::transformGuava14);
        TRANSFORMERS.add(LibraryTransformers::transformBouncy);
        TRANSFORMERS.add(LibraryTransformers::transformLegacyFixer);
    }

    public static void execute(String minecraft, LibraryInfo libraryInfo) {
        MinimalLibraryInfo info = MinimalLibraryInfo.of(libraryInfo);

        for (BiFunction<String, MinimalLibraryInfo, MinimalLibraryInfo> transformer : TRANSFORMERS) {
            info = transformer.apply(minecraft, info);
        }

        libraryInfo.setUrl(info.url());
        libraryInfo.setGav(info.gav());
    }

    private static MinimalLibraryInfo transformForge(String minecraft, MinimalLibraryInfo info) {
        return transform(info, FILTER_MINECRAFTFORGE, (gav) -> new Artifact(gav.group(), "forge", minecraft + "-" + gav.version(), null));
    }

    private static MinimalLibraryInfo transformAsm(String minecraft, MinimalLibraryInfo info) {
        return transform(info, filterArtifact(OBJECT_WEB_ASM_OLD), gav -> OBJECT_WEB_ASM, url -> Maven.MOJANG);
    }

    private static MinimalLibraryInfo transformGuava14(String minecraft, MinimalLibraryInfo info) {
        return transform(info, filterArtifact(GUAVA_14_RC3), gav -> GUAVA_14, url -> Maven.MOJANG);
    }

    private static MinimalLibraryInfo transformBouncy(String minecraft, MinimalLibraryInfo info) {
        return transform(info, filterArtifact(BOUNCY_148), gav -> BOUNCY_1_47, url -> Maven.MOJANG);
    }

    private static MinimalLibraryInfo transformLegacyFixer(String minecraft, MinimalLibraryInfo info) {
        //TODO:
        return info;
    }

    private static MinimalLibraryInfo transform(MinimalLibraryInfo info, Predicate<MinimalLibraryInfo> predicate, Function<Artifact, Artifact> transformArtifact, Function<String, String> transformUrl) {
        if (!predicate.test(info)) return info;

        return new MinimalLibraryInfo(transformUrl.apply(info.url()), transformArtifact.apply(info.gav()));
    }

    private static MinimalLibraryInfo transform(MinimalLibraryInfo info, Predicate<Artifact> predicate, Function<Artifact, Artifact> transform) {
        Artifact gav = info.gav();

        if (!predicate.test(gav)) return info;

        return info.with(transform.apply(gav));
    }

    private static Predicate<MinimalLibraryInfo> filterArtifact(Artifact artifact) {
        return info -> Objects.equals(artifact, info.gav());
    }

    private static Predicate<Artifact> filterGroup(String group) {
        return gav -> Objects.equals(gav.group(), group);
    }

    private static Predicate<Artifact> filterArtifact(String artifact) {
        return gav -> Objects.equals(gav.artifact(), artifact);
    }

}

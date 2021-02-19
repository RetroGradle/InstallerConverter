package uk.gemwire.installerconverter.legacy;

import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.v1_5.LibraryInfo;

public record MinimalLibraryInfo(String url, Artifact gav) {

    public static MinimalLibraryInfo of(LibraryInfo info) {
        return new MinimalLibraryInfo(info.getUrl(), info.getGav());
    }

    public MinimalLibraryInfo with(String url) {
        return new MinimalLibraryInfo(url, gav);
    }

    public MinimalLibraryInfo with(Artifact gav) {
        return new MinimalLibraryInfo(url, gav);
    }

}

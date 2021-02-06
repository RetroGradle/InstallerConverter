package uk.gemwire.installerconverter.v1_5;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public final class VersionInfo {

    private final Map<String, Object> data = new HashMap<>();

    private String inheritsFrom;
    private String jar;

    private List<LibraryInfo> libraries;

    @Nullable
    public String getInheritsFrom() {
        return inheritsFrom;
    }

    public void setInheritsFrom(@Nullable String inheritsFrom) {
        this.inheritsFrom = inheritsFrom;
    }

    @Nullable
    public String getJar() {
        return jar;
    }

    public void setJar(@Nullable String jar) {
        this.jar = jar;
    }

    public List<LibraryInfo> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<LibraryInfo> libraries) {
        this.libraries = libraries;
    }

    public Map<String, Object> getAdditionalData() {
        return data;
    }

    @JsonAnySetter
    public void setAdditionalData(String key, Object value) {
        data.put(key, value);
    }

}
package uk.gemwire.installerconverter.v1_5.conversion;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.MoreObjects;

public class Converted {

    private final ObjectNode install;
    private final ObjectNode version;

    protected Converted(ObjectNode install, ObjectNode version) {
        this.install = install;
        this.version = version;
    }

    public static Converted of(ObjectNode install, ObjectNode version) {
        return new Converted(install, version);
    }

    public JsonNode install() {
        return install;
    }

    public JsonNode version() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Converted converted = (Converted) o;
        return Objects.equals(install, converted.install) && Objects.equals(version, converted.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(install, version);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("install", install)
            .add("version", version)
            .toString();
    }
}

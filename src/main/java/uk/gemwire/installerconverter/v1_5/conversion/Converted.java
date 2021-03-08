package uk.gemwire.installerconverter.v1_5.conversion;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record Converted(ObjectNode install, ObjectNode version) {

    public static Converted of(ObjectNode install, ObjectNode version) {
        return new Converted(install, version);
    }

}

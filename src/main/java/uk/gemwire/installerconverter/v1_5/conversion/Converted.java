package uk.gemwire.installerconverter.v1_5.conversion;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Value;

@Value(staticConstructor = "of")
public class Converted {

    @Nonnull ObjectNode install;
    @Nonnull ObjectNode version;

}

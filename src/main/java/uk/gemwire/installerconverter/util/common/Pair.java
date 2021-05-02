package uk.gemwire.installerconverter.util.common;

import lombok.Data;

@Data(staticConstructor = "of")
public class Pair<L, R> {

    private final L left;
    private final R right;

}

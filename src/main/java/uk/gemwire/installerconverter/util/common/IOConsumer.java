package uk.gemwire.installerconverter.util.common;

import java.io.IOException;

public interface IOConsumer<T> {

    void accept(T obj) throws IOException;

}

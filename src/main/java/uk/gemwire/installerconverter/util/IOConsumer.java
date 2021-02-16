package uk.gemwire.installerconverter.util;

import java.io.IOException;

public interface IOConsumer<T> {

    void accept(T obj) throws IOException;

}

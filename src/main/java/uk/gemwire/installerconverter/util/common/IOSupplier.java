package uk.gemwire.installerconverter.util.common;

import java.io.IOException;

public interface IOSupplier<T> {

    T get() throws IOException;

}

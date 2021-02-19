package uk.gemwire.installerconverter.util.common;

import java.io.IOException;

public class Lazy<T> implements IOSupplier<T> {

    private IOSupplier<T> supplier;
    private T value;

    public Lazy(IOSupplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() throws IOException {
        if (supplier != null) {
            value = supplier.get();
            supplier = null;
        }

        return value;
    }

    public static <T> Lazy<T> of(IOSupplier<T> supplier) {
        return new Lazy<>(supplier);
    }

}

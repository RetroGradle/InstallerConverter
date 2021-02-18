package uk.gemwire.installerconverter.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public interface IConvertable<T, Context> {

    void validate() throws IllegalStateException;

    T convert(Context context, JsonNodeFactory factory) throws IOException;

}

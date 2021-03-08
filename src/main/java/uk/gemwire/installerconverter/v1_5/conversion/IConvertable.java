package uk.gemwire.installerconverter.v1_5.conversion;

import java.io.IOException;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public interface IConvertable<T, Context> {

    void validate() throws IllegalStateException;

    T convert(Context context, JsonNodeFactory factory) throws IOException;

}

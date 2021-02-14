package uk.gemwire.installerconverter.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public interface IConvertable<T> {

    void validate() throws IllegalStateException;

    T convert(JsonNodeFactory factory) throws IOException;

}
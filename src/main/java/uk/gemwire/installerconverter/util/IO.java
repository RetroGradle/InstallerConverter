package uk.gemwire.installerconverter.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.google.common.base.Charsets;

public abstract class IO {

    private static final int BUFFER_SIZE = 0x10000;

    public static long exhaust(InputStream stream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];

        long size = 0;

        int read = 1;
        while (read >= 1)  {
            read = stream.read(buffer);
            if (read > 0) size += read;
        }

        return size;
    }

    public static String toString(InputStream stream) throws IOException {
        return toString(stream, Charsets.UTF_8);
    }

    public static String toString(InputStream stream, Charset charset) throws IOException {
        char[] buffer = new char[BUFFER_SIZE];

        StringBuilder builder = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(stream, charset);

        int read = 1;
        while (read >= 1)  {
            read = reader.read(buffer);
            if (read >= 1) builder.append(buffer, 0, read);
        }

        return builder.toString();
    }
}

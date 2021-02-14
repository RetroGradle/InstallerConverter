package uk.gemwire.installerconverter.util;

import java.io.IOException;
import java.io.InputStream;

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

}

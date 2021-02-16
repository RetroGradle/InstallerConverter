package uk.gemwire.installerconverter.util;

import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class Hashing {

    public static CachedArtifactInfo calculateSHA1andSize(InputStream stream) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            long size = IO.exhaust(new DigestInputStream(stream, digest));
            return new CachedArtifactInfo(asSha1Hash(digest), size);
        } catch (NoSuchAlgorithmException exception) {
            throw new IOException(exception);
        }
    }

    private static String asSha1Hash(MessageDigest digest) {
        return String.format("%1$0" + 40 + "x", new BigInteger(1, digest.digest()));
    }

}

package uk.gemwire.installerconverter.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.google.common.base.Charsets;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

public abstract class Maven {

    public static InputStream download(URL url) throws IOException {
        URLConnection connection = makeConnection(url);

        if (connection instanceof HttpURLConnection httpConn && (httpConn.getResponseCode() < 200 || httpConn.getResponseCode() >= 300))
            throw new IOException("Couldn't connect to server (responded with %s)".formatted(httpConn.getResponseCode()));

        return connection.getInputStream();
    }

    public static CachedArtifactInfo calculateSHA1andSize(URL url) throws IOException {
        try (InputStream stream = download(url)) {
            return Hashing.calculateSHA1andSize(stream);
        }
    }

    public static String downloadSha1(URL url) throws IOException {
        try (InputStream stream = download(url)) {
            return IO.toString(stream, Charsets.UTF_8).trim();
        }
    }

    public static URLConnection makeConnection(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);
        connection.setDefaultUseCaches(false);
        connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
        connection.setRequestProperty("Expires", "0");
        connection.setRequestProperty("Pragma", "no-cache");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(30000);
        return connection;
    }
}

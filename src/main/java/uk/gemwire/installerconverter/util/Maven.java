package uk.gemwire.installerconverter.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import com.google.common.base.Charsets;

public abstract class Maven {

    public static InputStream download(URL url) throws IOException {
        HttpURLConnection connection = makeConnection(url);

        if (connection.getResponseCode() / 100 != 2)
            throw new IOException(String.format("Couldn't connect to server (responded with %s)", connection.getResponseCode()));

        return connection.getInputStream();
    }

    public static Pair<String, Long> calculateSHA1andSize(URL url) throws IOException {
        try (InputStream stream = download(url)) {
            return Hashing.calculateSHA1andSize(stream);
        }
    }

    public static String downloadSha1(URL url) throws IOException {
        try (InputStream stream = download(url)) {
            return IO.toString(stream, Charsets.UTF_8).trim();
        }
    }

    public static HttpURLConnection makeConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection(Proxy.NO_PROXY); //TODO: Proxy?
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

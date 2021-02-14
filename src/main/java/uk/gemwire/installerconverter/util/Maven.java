package uk.gemwire.installerconverter.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

public abstract class Maven {

    public static String asPath(String artifact) {
        return asPath(artifact, false);
    }

    // "org.scala-lang:scala-xml_2.11:1.0.2"
    // "org/scala-lang/scala-xml_2.11/1.0.2/scala-xml_2.11-1.0.2.jar"
    public static String asPath(String artifact, boolean allowClassifier) {
        String[] parts = artifact.split(":", 4);

        if (parts.length < 3) throw new IllegalStateException("Invalid Maven Artifact: " + artifact);
        if (parts.length == 4 && !allowClassifier) throw new IllegalStateException("Invalid Maven Artifact (Has Classifier): " + artifact);

        String group = parts[0].replaceAll("\\.", "/");
        String name = parts[1];
        String version = parts[2];
        String classifier = parts.length > 3 ? "-" + parts[3] : "";

        return group + "/" + name + "/" + version + "/" + name + "-" + version + classifier + ".jar";
    }

    public static Pair<String, Long> calculateSHA1andSize(URL url) throws IOException {
        HttpURLConnection connection = makeConnection(url);

        if (connection.getResponseCode() / 100 != 2)
            throw new IOException(String.format("Couldn't connect to server (responded with %s)", connection.getResponseCode()));

        try (InputStream stream = connection.getInputStream()) {
            return Hashing.calculateSHA1andSize(stream); // Pair.of(calculateSHA1andSize(stream), connection.getContentLengthLong());
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

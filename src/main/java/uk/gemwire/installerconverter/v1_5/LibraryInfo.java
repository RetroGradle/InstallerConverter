package uk.gemwire.installerconverter.v1_5;

import java.util.List;

public final class LibraryInfo {
    private String name;
    private List<String> checksums;
    private boolean clientreq = false;
    private boolean serverreq = false;
    private String url = "https://libraries.minecraft.net/";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getChecksums() {
        return checksums;
    }

    public void setChecksums(List<String> checksums) {
        this.checksums = checksums;
    }

    public boolean isClientreq() {
        return clientreq;
    }

    public void setClientreq(boolean clientreq) {
        this.clientreq = clientreq;
    }

    public boolean isServerreq() {
        return serverreq;
    }

    public void setServerreq(boolean serverreq) {
        this.serverreq = serverreq;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

package uk.gemwire.installerconverter.v1_5;

import java.util.List;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.util.IConvertable;

public final class LibraryInfo implements IConvertable<ObjectNode> {
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

    @Override
    public void validate() throws IllegalStateException {
        //TODO:
    }

    @Override
    public ObjectNode convert(JsonNodeFactory factory) {
        //TODO:
        return factory.objectNode();
    }
}

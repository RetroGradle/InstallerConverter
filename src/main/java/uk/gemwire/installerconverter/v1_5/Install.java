package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.util.IConvertable;
import uk.gemwire.installerconverter.util.JacksonUsed;

public final class Install implements IConvertable<ObjectNode> {

    private String profileName;
    private String target;
    private String path;
    private String modList;
    private String version;
    private String welcome;
    private String logo;
    private String urlIcon;
    private boolean stripMeta = false;
    private String filePath;
    private String minecraft;
    private String mirrorList;
    private boolean hideClient = false;
    private boolean hideServer = false;
    private boolean hideExtract = false;

    @JacksonUsed
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    @JacksonUsed
    public void setTarget(String target) {
        this.target = target;
    }

    @JacksonUsed
    public void setPath(String path) {
        this.path = path;
    }

    @JacksonUsed
    public void setModList(@Nullable String modList) {
        this.modList = modList;
    }

    @JacksonUsed
    public void setVersion(String version) {
        this.version = version;
    }

    @JacksonUsed
    public void setWelcome(String welcome) {
        this.welcome = welcome;
    }

    @JacksonUsed
    public void setLogo(String logo) {
        this.logo = logo;
    }

    @JacksonUsed
    public void setUrlIcon(String urlIcon) {
        this.urlIcon = urlIcon;
    }

    @JacksonUsed
    public void setStripMeta(boolean stripMeta) {
        this.stripMeta = stripMeta;
    }

    @JacksonUsed
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @JacksonUsed
    public void setMinecraft(String minecraft) {
        this.minecraft = minecraft;
    }

    @JacksonUsed
    public void setMirrorList(String mirrorList) {
        this.mirrorList = mirrorList;
    }

    @JacksonUsed
    public void setHideClient(boolean hideClient) {
        this.hideClient = hideClient;
    }

    @JacksonUsed
    public void setHideServer(boolean hideServer) {
        this.hideServer = hideServer;
    }

    @JacksonUsed
    public void setHideExtract(boolean hideExtract) {
        this.hideExtract = hideExtract;
    }

    @Override
    public void validate() throws IllegalStateException {
        // TODO: Validate

        // Validate that the Id Conversion works for the target
        try {
            Conversions.convertId(target);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Override
    public ObjectNode convert(JsonNodeFactory factory) throws IOException {
        ObjectNode node = factory.objectNode();

        /* Skip MirrorList if it's forges - TODO: Check this is correct */
        if (Objects.equals(mirrorList, "http://files.minecraftforge.net/mirror-brand.list")) mirrorList = null;

        node.set("_comment_", Conversions.createCommentNode(factory));
        node.put("spec", 0);
        node.put("profile", profileName);
        node.put("version", Conversions.convertId(target));
        node.put("icon", Config.ICON); //TODO: Conversion?
        node.put("json", "/version.json");
        node.put("path", path);
        node.put("logo", logo);
        node.put("minecraft", minecraft);

        if (urlIcon != null)
            node.put("urlIcon", urlIcon);

        node.put("welcome", Conversions.convertWelcome(welcome));

        if (mirrorList != null)
            node.put("mirrorList", mirrorList);

        if (hideClient)
            node.put("hideClient", true);
        if (hideServer)
            node.put("hideServer", true);
        if (hideExtract)
            node.put("hideExtract", true);

        node.set("data", factory.objectNode());
        node.set("processors", factory.arrayNode());

        //TODO: Libraries (Needs to provide the forge-universal (as forge))
        LibraryInfo forge = new LibraryInfo();
        forge.setName(path);
        node.set("libraries", factory.arrayNode().add(forge.convert(factory)));

        return node;
    }
}

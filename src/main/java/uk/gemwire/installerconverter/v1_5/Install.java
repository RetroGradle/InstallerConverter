package uk.gemwire.installerconverter.v1_5;

import java.util.Objects;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.util.IConvertable;

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

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Nullable
    public String getModList() {
        return modList;
    }

    public void setModList(@Nullable String modList) {
        this.modList = modList;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getWelcome() {
        return welcome;
    }

    public void setWelcome(String welcome) {
        this.welcome = welcome;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getUrlIcon() {
        return urlIcon;
    }

    public void setUrlIcon(String urlIcon) {
        this.urlIcon = urlIcon;
    }

    public boolean isStripMeta() {
        return stripMeta;
    }

    public void setStripMeta(boolean stripMeta) {
        this.stripMeta = stripMeta;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMinecraft() {
        return minecraft;
    }

    public void setMinecraft(String minecraft) {
        this.minecraft = minecraft;
    }

    public String getMirrorList() {
        return mirrorList;
    }

    public void setMirrorList(String mirrorList) {
        this.mirrorList = mirrorList;
    }

    public boolean isHideClient() {
        return hideClient;
    }

    public void setHideClient(boolean hideClient) {
        this.hideClient = hideClient;
    }

    public boolean isHideServer() {
        return hideServer;
    }

    public void setHideServer(boolean hideServer) {
        this.hideServer = hideServer;
    }

    public boolean isHideExtract() {
        return hideExtract;
    }

    public void setHideExtract(boolean hideExtract) {
        this.hideExtract = hideExtract;
    }

    @Override
    public void validate() throws AssertionError {
        // TODO: Validate
    }

    @Override
    public ObjectNode convert(JsonNodeFactory factory) {
        ObjectNode node = factory.objectNode();

        /* Skip MirrorList if it's forges - TODO: Check this is correct */
        if (Objects.equals(mirrorList, "http://files.minecraftforge.net/mirror-brand.list")) mirrorList = null;

        node.set("_comment_", Conversions.createCommentNode(factory));
        node.put("spec", 0);
        node.put("profile", profileName);
        node.put("version", Conversions.convertId(target));
        node.put("icon", "{ICON}");
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

        //TODO: Libraries

        return node;
    }
}

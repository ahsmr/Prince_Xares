package Currency;

import java.util.ArrayList;
import java.util.List;

public class GuildInfo {
    private final String guildId;
    private final String shopChannelId;
    private final String introChannelId;
    private final List<String> roleIds;

    public GuildInfo(String guildId, String shopChannelId, String introChannelId, List<String> roleIds) {
        this.guildId = guildId;
        this.shopChannelId = shopChannelId;
        this.introChannelId = introChannelId;
        this.roleIds = new ArrayList<String>(roleIds);
    }

    public String getGuildId() { return guildId; }
    public String getShopChannelId() { return shopChannelId; }
    public String getIntroChannelId() { return introChannelId; }
    public List<String> getRoleIds() { return new ArrayList<String>(roleIds); }
}

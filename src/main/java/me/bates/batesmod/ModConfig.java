package me.bates.batesmod;

import java.util.*;

public class ModConfig {
    public String batesModGradient = "[<gradient:#7b41f0:#57bff2>BatesMod</gradient>]";
    public String joinMessage = "<gradient:41ff6d:72baff>%display-name% hopped on</gradient>";
    public String leaveMessage = "<gradient:#ff5e5e:#ffbc72>%display-name% hopped off</gradient>";
    public String chatMessageFormat = "\\<%display-name%\\> %message%";
    public String notWhitelistedMessage = "You are not white-listed on this server!";
    public List<String> filterLoggers = new ArrayList<>();
    public List<String> filterPhrases = new ArrayList<>();
    public List<String> filterRegexes = new ArrayList<>();
    public String protectedRegionMessage = "%bates% This region is protected!";
    public String protectedRegionMessageAdmin = "%bates% This region is protected! Region name: \"%region-name%\"";
    public String timeMessage = "%bates% The time is currently %ticks% ticks, or roughly %timeColored%.";
    public String timeMessageNoClock = "%bates% <c>You must have a clock in your inventory!</c>";
    public String motd = "<gray>A Minecraft Server</gray>";
    public boolean seedCommandAllowed = false;
    public List<Region> protectedRegions = new ArrayList<>();
    public Map<UUID, String> displayNames = Map.of();
    public Map<String, MobGriefOverride> mobGriefMap = new HashMap<>();
}

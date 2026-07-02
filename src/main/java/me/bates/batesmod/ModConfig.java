package me.bates.batesmod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModConfig {
    public String batesModGradient = "<gradient:#7b41f0:#57bff2>BatesMod</gradient>";
    public String joinMessage = "<gradient:41ff6d:72baff>%player% hopped on</gradient>";
    public String leaveMessage = "<gradient:#ff5e5e:#ffbc72>%player% hopped off</gradient>";
    public String chatMessageFormat = "\\<%name%\\> %message%";
    public List<String> filterLoggers = new ArrayList<>();
    public List<String> filterPhrases = new ArrayList<>();
    public List<String> filterRegexes = new ArrayList<>();
    public String protectedRegionMessage = "[%bates%] This region is protected!";
    public String protectedRegionMessageAdmin = "[%bates%] This region is protected! Region name: \"%region-name%\"";
    public String timeMessage = "[%bates%] The time is currently %ticks% ticks, or roughly %time%.";
    public List<Region> protectedRegions = new ArrayList<>();
    public Map<String, String> displayNames = Map.of();
}

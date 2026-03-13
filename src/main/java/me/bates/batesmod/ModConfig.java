package me.bates.batesmod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModConfig {
    public String joinMessage = "<gradient:41ff6d:72baff>%player% hopped on</gradient>";
    public String leaveMessage = "<gradient:#ff5e5e:#ffbc72>%player% hopped off</gradient>";
    public String chatMessageFormat = "\\<%name%\\> %message%";
    public List<String> filterLoggers = new ArrayList<>();
    public List<String> filterPhrases = new ArrayList<>();
    public List<String> filterRegexes = new ArrayList<>();
    public Map<String, String> displayNames = Map.of();
}

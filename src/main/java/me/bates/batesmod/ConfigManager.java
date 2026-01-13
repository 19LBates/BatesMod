package me.bates.batesmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("batesmod.json");
    private static ModConfig config;

    public static void load() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                config = new ModConfig();
                save();
                return;
            }

            config = GSON.fromJson(Files.readString(CONFIG_PATH), ModConfig.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load config:", e);
        }
    }

    public static void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config:", e);
        }
    }

    public static ModConfig get() {
        if (config == null) {
            throw new IllegalStateException("Config not loaded!");
        }
        return config;
    }
}

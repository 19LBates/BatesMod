package me.bates.batesmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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

            ModConfig defaults = new ModConfig();
            config = GSON.fromJson(Files.readString(CONFIG_PATH), ModConfig.class);
            merge(config, defaults);
            save();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load config:", e);
        }
    }

    public static void save() {
        try {
            Path temp = CONFIG_PATH.resolveSibling("config.json.tmp");
            Files.writeString(temp, GSON.toJson(config));
            Files.move(temp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

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

    private static void merge (ModConfig base, ModConfig overlay) {
        try {
            for (var field : ModConfig.class.getFields()) {
                Object value = field.get(overlay);
                if (field.get(base) == null && value != null) {
                    field.set(base, value);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to migrate config:", e);
        }
    }
}

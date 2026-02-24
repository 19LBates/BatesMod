package me.bates.batesmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

public class BatesMod implements ModInitializer {

    public static final String MOD_ID = "bates";
    public static final String VERSION = getModVersion(MOD_ID);
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final LogFilter LOG_FILTER = new LogFilter();

    @Override
    public void onInitialize() {
        LOGGER.info("Loaded version {}", VERSION);
        registerCommands();
        ModGameRules.init();
        ConfigManager.load();
        org.apache.logging.log4j.core.Logger root = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        root.addFilter(BatesMod.LOG_FILTER);
    }

    public static String getModVersion(String modId) {
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(modId);
        return container.map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            BatesCommand.register(dispatcher);
            SkibCommand.register(dispatcher);
        });
    }
}

package me.bates.batesmod;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.LogManager;

public class BatesPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger())
                .addFilter(BatesMod.LOG_FILTER);
    }
}

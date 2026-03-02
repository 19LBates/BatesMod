package me.bates.batesmod;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LogFilter extends AbstractFilter implements Filter {
    public Result filter(@NotNull LogEvent event) {
        if (shouldBlockEvent(event))
            return Result.DENY;

        if (shouldFilter("[" + event.getLoggerName() + "]: " +
                event.getMessage().getFormattedMessage()))
            return Result.DENY;

        return Result.NEUTRAL;
    }

    private boolean shouldBlockEvent(LogEvent event) {
        List<String> events = ConfigManager.get().filterLoggers;
        return events.contains(event.getLoggerName());
    }

    private boolean shouldFilter(String s) {
        List<String> phrases = ConfigManager.get().filterPhrases;
        List<String> regexes = ConfigManager.get().filterRegexes;
        if (s == null) return false;

        if (phrases != null) {
            for (String phrase : phrases) {
                if (s.contains(phrase)) return true;
            }
        }

        if (regexes != null) {
            for (String regex : regexes) {
                if (s.matches(regex)) return true;
            }
        }

        return false;
    }
}

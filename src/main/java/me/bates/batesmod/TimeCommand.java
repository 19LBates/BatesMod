package me.bates.batesmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.List;

import static net.minecraft.commands.Commands.literal;

public class TimeCommand {

    private static final int TICKS_IN_DAY = 24000;
    private static final int TICKS_IN_HOUR = 1000;
    private static final int TICK_SHIFT = 6 * TICKS_IN_HOUR; //Minecraft days start at 0 ticks, which corresponds to roughly 6.a.m
    private static final double TICKS_IN_MINUTE = (double) 1000 / 60;
    private static final int MIDDAY_TICKS = 6000;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        CommandNode<CommandSourceStack> timeNode = dispatcher.getRoot().getChild("time");
        if (timeNode == null) return;

        setRequirement(timeNode, _ -> true);
        dispatcher.register(literal("time").executes(TimeCommand::baseCommand));

        for (String sub : List.of("add", "set", "query", "of", "pause", "resume", "rate")) {
            CommandNode<CommandSourceStack> subNode = timeNode.getChild(sub);
            if (subNode != null) {
                setRequirement(subNode, source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
            }
        }
    }

    private static int baseCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = context.getSource().getServer();
        ServerClockManager clockManager = server.clockManager();

        Registry<WorldClock> clocksRegistry = server.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK);

        Holder<WorldClock> clock = clocksRegistry.getOrThrow(WorldClocks.OVERWORLD);

        int currentTicksIntoDay = (int) (clockManager.getTotalTicks(clock)) % TICKS_IN_DAY;
        int currentTicksIntoDayShifted = (currentTicksIntoDay + TICK_SHIFT) % TICKS_IN_DAY;
        String time = String.format("%02d", currentTicksIntoDayShifted / TICKS_IN_HOUR) + ":" + String.format("%02d", (int) ((currentTicksIntoDayShifted % TICKS_IN_HOUR) / TICKS_IN_MINUTE));

        String message;
        if (Objects.requireNonNull(source.getPlayer()).getInventory().contains(new ItemStack(Items.CLOCK))) {
            message = ConfigManager.get().timeMessage;
        } else {
            message = ConfigManager.get().timeMessageNoClock;
        }

        source.sendSystemMessage(TextTools.builder().input(message)
                .placeholder("bates", ConfigManager.get().batesModGradient)
                .placeholder("ticks", String.valueOf(currentTicksIntoDay))
                .placeholder("time", time)
                .placeholder("timeColored", "<lerp:5555FF:55FFFF:" + closenessToMidday(currentTicksIntoDay) + ">" + time + "</lerp>")
                .build()
        );

        return 1;
    }

    private static void setRequirement(CommandNode<CommandSourceStack> node, Predicate<CommandSourceStack> requirement) {
        try {
            Field field = CommandNode.class.getDeclaredField("requirement");
            field.setAccessible(true);
            field.set(node, requirement);
            field.setAccessible(false);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to override command requirement", e);
        }
    }

    private static float closenessToMidday(int curTicks) {
        //Check to see if it is closer to midday of the current day, or of the next day
        int differenceToCurrentMidday = Math.abs(MIDDAY_TICKS - curTicks);
        int differenceToNextMidday = Math.abs(MIDDAY_TICKS + TICKS_IN_DAY - curTicks);
        return 1 - 2 * (float) Math.min(differenceToCurrentMidday, differenceToNextMidday) / (float) TICKS_IN_DAY;
    }
}
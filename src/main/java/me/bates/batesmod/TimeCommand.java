package me.bates.batesmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.dimension.DimensionType;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.List;

import static net.minecraft.commands.Commands.literal;

public class TimeCommand {

    private static final DynamicCommandExceptionType ERROR_NO_DEFAULT_CLOCK = new DynamicCommandExceptionType(
            dimension -> Component.translatableEscape("commands.time.no_default_clock", dimension)
    );

    private static final int ticksInDay = 24000;
    private static final int ticksInHour = 1000;
    private static final int tickShift = 6 * ticksInHour; //Minecraft days start at 0 ticks, which corresponds to roughly 6.a.m
    private static final double tickInMinute = (double) 1000 / 60;

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

    private static int baseCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerClockManager clockManager = source.getServer().clockManager();
        Holder<DimensionType> dimensionType = source.getLevel().dimensionTypeRegistration();
        Holder<WorldClock> clock = dimensionType.value().defaultClock().orElseThrow(() -> ERROR_NO_DEFAULT_CLOCK.create(dimensionType.getRegisteredName()));
        int currentTicksIntoDay = (int) (clockManager.getTotalTicks(clock)) % ticksInDay;
        int currentTicksIntoDayShifted = (currentTicksIntoDay + tickShift) % ticksInDay;
        java.lang.String time = String.format("%02d", currentTicksIntoDayShifted / ticksInHour) + ":" + String.format("%02d", (int) ((currentTicksIntoDayShifted % ticksInHour) / tickInMinute));
        source.sendSystemMessage(TextTools.builder().input(ConfigManager.get().timeMessage)
                .placeholder("bates", ConfigManager.get().batesModGradient)
                .placeholder("ticks", String.valueOf(currentTicksIntoDay))
                .placeholder("time", time)
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
}
package me.bates.batesmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;

import java.lang.reflect.Field;
import java.util.function.Predicate;

public class SeedCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        CommandNode<CommandSourceStack> seedNode = dispatcher.getRoot().getChild("seed");
        if (seedNode == null) return;
        setRequirement(seedNode, source -> {
            if (source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) return true;
            return ConfigManager.get().seedCommandAllowed;
        });
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

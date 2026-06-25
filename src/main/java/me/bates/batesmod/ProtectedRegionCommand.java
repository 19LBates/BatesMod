package me.bates.batesmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.permissions.Permissions;

import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ProtectedRegionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("protected-region")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                .then(
                        literal("add")
                                .then(argument("name", StringArgumentType.string())
                                        .then(argument("from", BlockPosArgument.blockPos())
                                                .then(argument("to", BlockPosArgument.blockPos())
                                                        .executes(ProtectedRegionCommand::addRegion)
                                                )
                                        )
                                )
                )
                .then(
                        literal("remove")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests((_, builder) ->
                                                SharedSuggestionProvider.suggest(ConfigManager.get().protectedRegions
                                                        .stream().map(Region::getName).collect(Collectors.toList()), builder
                                                ))
                                        .executes(ProtectedRegionCommand::removeRegion)
                                )
                )
        );
    }

    private static int addRegion(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name").toLowerCase();
        BlockPos from = BlockPosArgument.getBlockPos(context, "from");
        BlockPos to = BlockPosArgument.getBlockPos(context, "to");

        if (ConfigManager.get().protectedRegions.stream().anyMatch(region -> region.getName().toLowerCase().equals(name))) {
            context.getSource().sendSystemMessage(TextTools.builder().input("[%bates%] <c>A Protected Region called \"%name%\" already exists!</c>")
                    .placeholder("bates", ConfigManager.get().batesModGradient)
                    .placeholder("name", name)
                    .build()
            );
            return 0;
        }

        ConfigManager.get().protectedRegions.add(new Region(
                        name,
                        from.getX(),
                        from.getY(),
                        from.getZ(),
                        to.getX(),
                        to.getY(),
                        to.getZ()
                )
        );
        context.getSource().sendSystemMessage(TextTools.builder().input("[%bates%] Protected Region \"%name%\" added.")
                .placeholder("bates", ConfigManager.get().batesModGradient)
                .placeholder("name", name)
                .build()
        );
        return 1;
    }

    private static int removeRegion(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        ConfigManager.get().protectedRegions.removeIf(r -> r.getName().equals(name));
        context.getSource().sendSystemMessage(TextTools.builder().input("[%bates%] Protected Region \"%name%\" removed.")
                .placeholder("bates", ConfigManager.get().batesModGradient)
                .placeholder("name", name)
                .build()
        );
        return 1;
    }
}

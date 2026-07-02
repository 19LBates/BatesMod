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

                .then(
                        literal("info")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests((_, builder) ->
                                                SharedSuggestionProvider.suggest(ConfigManager.get().protectedRegions
                                                        .stream().map(Region::getName).collect(Collectors.toList()), builder
                                                ))
                                        .executes(ProtectedRegionCommand::infoOfRegion)
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
        ConfigManager.save();
        context.getSource().sendSystemMessage(TextTools.builder().input("[%bates%] Protected Region \"%name%\" added.")
                .placeholder("bates", ConfigManager.get().batesModGradient)
                .placeholder("name", name)
                .build()
        );
        return 1;
    }

    private static int removeRegion(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        Region region = ConfigManager.get().protectedRegions.stream().filter(r -> r.getName().equals(name)).findFirst().orElse(null);

        if (region == null) {
            context.getSource().sendSystemMessage(TextTools.builder().input("[%bates%] <c>No Protected Region with name \"%name%\" exists!</c>")
                    .placeholder("bates", ConfigManager.get().batesModGradient)
                    .placeholder("name", name)
                    .build()
            );
            return 0;
        }

        ConfigManager.get().protectedRegions.remove(region);
        ConfigManager.save();
        context.getSource().sendSystemMessage(TextTools.builder().input("[%bates%] Protected Region \"%name%\" removed.")
                .placeholder("bates", ConfigManager.get().batesModGradient)
                .placeholder("name", name)
                .build()
        );
        return 1;
    }

    private static int infoOfRegion(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        Region region = ConfigManager.get().protectedRegions.stream().filter(r -> r.getName().equals(name)).findFirst().orElse(null);

        if (region == null) {
            context.getSource().sendSystemMessage(TextTools.builder().input("[%bates%] <c>No Protected Region with name \"%name%\" exists!</c>")
                    .placeholder("bates", ConfigManager.get().batesModGradient)
                    .placeholder("name", name)
                    .build()
            );
            return 0;
        }

        context.getSource().sendSystemMessage(TextTools.builder().input("[%bates%] Protected Region \"%name%\": From <b>%x1% %y1% %z1%</b>, To <e>%x2% %y2% %z2%</e>")
                .placeholder("bates", ConfigManager.get().batesModGradient)
                .placeholder("name", name)
                .placeholder("x1", String.valueOf(region.getLowerX()))
                .placeholder("y1", String.valueOf(region.getLowerY()))
                .placeholder("z1", String.valueOf(region.getLowerZ()))
                .placeholder("x2", String.valueOf(region.getUpperX()))
                .placeholder("y2", String.valueOf(region.getUpperY()))
                .placeholder("z2", String.valueOf(region.getUpperZ()))
                .build()
        );
        return 1;
    }

}

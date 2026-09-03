package me.bates.batesmod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class CustomNameTag extends Display.TextDisplay {

    public CustomNameTag(Player player) {
        Level level = player.level();

        super(EntityType.TEXT_DISPLAY, level);
        setBillboardConstraints(Display.BillboardConstraints.CENTER);
        setText(Component.literal("yo"));

        setPos(player.getX(), player.getY(), player.getZ());
        setNoGravity(true);
        setPosRotInterpolationDuration(3);

        level.addFreshEntity(this);
    }

}

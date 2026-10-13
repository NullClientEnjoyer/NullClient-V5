package dev.nullclient.module.modules;

import dev.nullclient.module.Category;
import dev.nullclient.module.Module;
import dev.nullclient.setting.NumSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
    private final NumSetting distance = add(new NumSetting("Min Distance", 2.5, 1.0, 10.0, 0.5));

    public NoFall() { super("NoFall", "Cancels fall damage.", Category.PLAYER); }

    @Override public void onTick() {
        if (mc.player.isGliding()) return; // never touch elytra flight
        if (mc.player.fallDistance > distance.get()) {
            mc.player.networkHandler.sendPacket(
                new PlayerMoveC2SPacket.OnGroundOnly(true, mc.player.horizontalCollision));
        }
    }
}

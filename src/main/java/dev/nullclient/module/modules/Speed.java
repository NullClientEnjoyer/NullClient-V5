package dev.nullclient.module.modules;

import dev.nullclient.module.Category;
import dev.nullclient.module.Module;
import dev.nullclient.setting.BoolSetting;
import dev.nullclient.setting.NumSetting;
import dev.nullclient.util.MoveUtil;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {
    private final NumSetting speed = add(new NumSetting("Speed", 0.30, 0.10, 1.00, 0.01));
    private final BoolSetting onlyGround = add(new BoolSetting("Only On Ground", false));

    public Speed() { super("Speed", "Increases horizontal movement speed.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (!MoveUtil.isMoving()) return;
        if (onlyGround.get() && !mc.player.isOnGround()) return;
        double[] h = MoveUtil.horizontal(speed.get());
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(h[0], v.y, h[1]);
    }
}

package dev.nullclient.module.modules;

import dev.nullclient.module.Category;
import dev.nullclient.module.Module;
import dev.nullclient.setting.BoolSetting;
import dev.nullclient.setting.NumSetting;
import dev.nullclient.util.MoveUtil;

public class Flight extends Module {
    private final NumSetting speed = add(new NumSetting("Speed", 1.0, 0.1, 5.0, 0.1));
    private final NumSetting vertical = add(new NumSetting("Vertical", 0.5, 0.1, 3.0, 0.1));
    private final BoolSetting antiKick = add(new BoolSetting("Anti-Kick", true));
    private int ticks;

    public Flight() { super("Flight", "Fly freely (velocity based).", Category.MOVEMENT); }

    @Override public void onEnable() { ticks = 0; }

    @Override public void onTick() {
        ticks++;
        double[] h = MoveUtil.horizontal(speed.get());
        double y = 0;
        if (mc.options.jumpKey.isPressed()) y += vertical.get();
        if (mc.options.sneakKey.isPressed()) y -= vertical.get();
        // Vanilla "flying too long" check: dip slightly every second when hovering.
        if (antiKick.get() && y == 0 && ticks % 20 == 0) y = -0.04;
        mc.player.setVelocity(h[0], y, h[1]);
    }
}

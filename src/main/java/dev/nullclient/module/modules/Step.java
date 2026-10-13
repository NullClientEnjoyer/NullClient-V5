package dev.nullclient.module.modules;

import dev.nullclient.module.Category;
import dev.nullclient.module.Module;
import dev.nullclient.setting.NumSetting;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

public class Step extends Module {
    private final NumSetting height = add(new NumSetting("Height", 1.0, 0.6, 2.5, 0.1));

    public Step() { super("Step", "Step up blocks instantly.", Category.MOVEMENT); }

    private void apply(double v) {
        if (mc.player == null) return;
        EntityAttributeInstance a = mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
        if (a != null) a.setBaseValue(v);
    }

    @Override public void onTick() { apply(height.get()); }
    @Override public void onDisable() { apply(0.6); }
}

package dev.nullclient.module.modules;

import dev.nullclient.module.Category;
import dev.nullclient.module.Module;

public class Sprint extends Module {
    public Sprint() { super("Sprint", "Always sprint while moving forward.", Category.MOVEMENT); }

    @Override public void onTick() {
        if (mc.options.forwardKey.isPressed() && !mc.player.horizontalCollision && !mc.player.isSneaking()) {
            mc.player.setSprinting(true);
        }
    }
}

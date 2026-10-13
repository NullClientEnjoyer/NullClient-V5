package dev.nullclient.module.modules;

import dev.nullclient.module.Category;
import dev.nullclient.module.Module;
import dev.nullclient.module.ModuleManager;
import dev.nullclient.setting.NumSetting;
import dev.nullclient.util.CameraState;
import net.minecraft.client.option.Perspective;

public class Freelook extends Module {
    private final NumSetting distance = add(new NumSetting("Distance", 4, 1, 20, 1));
    private Perspective prev;

    public Freelook() { super("Freelook", "Orbit the camera around you without turning. Camera clips through blocks.", Category.RENDER); }

    @Override public void onEnable() {
        if (mc.player == null) { enabled = false; return; }
        for (var m : ModuleManager.modules) {
            if (m instanceof Freecam && m.enabled) m.setEnabled(false);
        }
        CameraState.yaw = mc.player.getYaw();
        CameraState.pitch = mc.player.getPitch();
        CameraState.freelook = true;
        prev = mc.options.getPerspective();
        mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
    }

    @Override public void onDisable() {
        CameraState.freelook = false;
        if (prev != null) mc.options.setPerspective(prev);
    }

    @Override public void onTick() { CameraState.freelookDist = distance.get(); }
}

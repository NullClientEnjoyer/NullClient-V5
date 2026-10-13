package dev.nullclient.module.modules;

import dev.nullclient.module.Category;
import dev.nullclient.module.Module;
import dev.nullclient.setting.NumSetting;
import dev.nullclient.util.CameraState;
import net.minecraft.client.option.Perspective;

public class Freecam extends Module {
    private final NumSetting speed = add(new NumSetting("Speed (b/s)", 15, 2, 80, 1));
    private Perspective prev;

    public Freecam() { super("Freecam", "Detach the camera and fly around while your body stays put.", Category.RENDER); }

    @Override public void onEnable() {
        if (mc.player == null) { enabled = false; return; }
        for (var m : dev.nullclient.module.ModuleManager.modules) {
            if (m instanceof Freelook && m.enabled) m.setEnabled(false);
        }
        CameraState.fx = CameraState.captured ? CameraState.cx : mc.player.getX();
        CameraState.fy = CameraState.captured ? CameraState.cy : mc.player.getEyeY();
        CameraState.fz = CameraState.captured ? CameraState.cz : mc.player.getZ();
        CameraState.yaw = mc.player.getYaw();
        CameraState.pitch = mc.player.getPitch();
        CameraState.resetClock();
        CameraState.freecam = true;
        prev = mc.options.getPerspective();
        mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
    }

    @Override public void onDisable() {
        CameraState.freecam = false;
        if (prev != null) mc.options.setPerspective(prev);
    }

    @Override public void onTick() { CameraState.freecamSpeed = speed.get(); }
}

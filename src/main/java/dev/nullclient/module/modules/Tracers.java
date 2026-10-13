package dev.nullclient.module.modules;

import dev.nullclient.module.Category;
import dev.nullclient.module.Module;
import dev.nullclient.setting.BoolSetting;
import dev.nullclient.setting.EnumSetting;
import dev.nullclient.setting.NumSetting;
import dev.nullclient.util.CameraState;
import dev.nullclient.util.Draw3D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

/** Lines from your crosshair to players, StorageESP blocks and SusChunkFinder chunks. */
public class Tracers extends Module {
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting storage = add(new BoolSetting("Storage (needs StorageESP)", true));
    private final BoolSetting chunks = add(new BoolSetting("Sus Chunks (needs SusChunkFinder)", true));
    private final EnumSetting origin = add(new EnumSetting("Origin", 0, "Crosshair", "Bottom"));
    private final NumSetting maxDist = add(new NumSetting("Max Distance", 256, 32, 1024, 16));

    public Tracers() { super("Tracers", "Draws lines to players, storage and flagged chunks.", Category.RENDER); }

    @Override public void onHud(DrawContext ctx, float partial) {
        double maxSq = maxDist.get() * maxDist.get();
        double[] o = Draw3D.tracerOrigin(origin.index() == 1);

        if (players.get()) {
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (p == mc.player && !CameraState.freecam) continue;
                double x = p.prevX + (p.getX() - p.prevX) * partial;
                double y = p.prevY + (p.getY() - p.prevY) * partial + p.getHeight() / 2;
                double z = p.prevZ + (p.getZ() - p.prevZ) * partial;
                if (Draw3D.distSq(x, y, z) > maxSq) continue;
                Draw3D.lineCam(ctx, o, Draw3D.toCam(x, y, z), 0xFFFFFFFF);
            }
        }

        StorageESP s = StorageESP.INSTANCE;
        if (storage.get() && s != null && s.enabled) {
            for (StorageESP.Entry e : s.entries) {
                BlockPos p = e.pos();
                double x = p.getX() + 0.5, y = p.getY() + 0.5, z = p.getZ() + 0.5;
                if (Draw3D.distSq(x, y, z) > maxSq) continue;
                Draw3D.lineCam(ctx, o, Draw3D.toCam(x, y, z), e.color());
            }
        }

        SusChunkFinder f = SusChunkFinder.INSTANCE;
        if (chunks.get() && f != null && f.enabled) {
            for (Map.Entry<Long, SusChunkFinder.Info> e : f.flagged.entrySet()) {
                int cx = (int) (e.getKey() >> 32), cz = (int) (long) e.getKey();
                double x = cx * 16 + 8, z = cz * 16 + 8, y = (e.getValue().minY() + e.getValue().maxY()) / 2.0;
                if (Draw3D.distSq(x, y, z) > maxSq) continue;
                Draw3D.lineCam(ctx, o, Draw3D.toCam(x, y, z), 0xFFB066FF);
            }
        }
    }
}

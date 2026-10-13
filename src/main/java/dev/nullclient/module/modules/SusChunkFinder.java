package dev.nullclient.module.modules;

import dev.nullclient.module.Category;
import dev.nullclient.module.Module;
import dev.nullclient.setting.BoolSetting;
import dev.nullclient.setting.NumSetting;
import dev.nullclient.util.Draw3D;
import net.minecraft.block.entity.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Heuristic base finder: scores each loaded chunk by player-made block entities
 * (storage, hoppers, furnaces, signs...). Higher sensitivity = lower score needed to flag.
 * Flagged chunks are boxed through walls.
 */
public class SusChunkFinder extends Module {
    public static SusChunkFinder INSTANCE;

    public record Info(int score, int minY, int maxY) { }

    private final NumSetting sensitivity = add(new NumSetting("Sensitivity", 5, 1, 10, 1));
    private final NumSetting range = add(new NumSetting("Range (chunks)", 8, 2, 32, 1));
    private final BoolSetting esp = add(new BoolSetting("Chunk ESP", true));
    private final BoolSetting chat = add(new BoolSetting("Chat Alerts", true));

    public volatile Map<Long, Info> flagged = new HashMap<>();
    private final Set<Long> alerted = new HashSet<>();
    private int ticks;

    public SusChunkFinder() {
        super("SusChunkFinder", "Flags chunks that look player-built and boxes them.", Category.WORLD);
        INSTANCE = this;
    }

    @Override public void onEnable() { flagged = new HashMap<>(); alerted.clear(); ticks = 39; }
    @Override public void onDisable() { flagged = new HashMap<>(); alerted.clear(); }

    private static long key(int x, int z) { return ((long) x << 32) | (z & 0xFFFFFFFFL); }

    private static int weight(BlockEntity be) {
        if (be instanceof EnderChestBlockEntity) return 3;
        if (be instanceof ShulkerBoxBlockEntity) return 3;
        if (be instanceof BarrelBlockEntity) return 2;
        if (be instanceof HopperBlockEntity) return 2;
        if (be instanceof AbstractFurnaceBlockEntity) return 2;
        if (be instanceof DispenserBlockEntity) return 2;
        if (be instanceof ChestBlockEntity) return 1;
        if (be instanceof SignBlockEntity) return 1;
        return 0;
    }

    @Override public void onTick() {
        if (++ticks % 40 != 0) return;
        int threshold = 12 - sensitivity.getInt();
        int r = range.getInt();
        int pcx = mc.player.getBlockX() >> 4, pcz = mc.player.getBlockZ() >> 4;

        Map<Long, Info> next = new HashMap<>();
        for (int cx = pcx - r; cx <= pcx + r; cx++) {
            for (int cz = pcz - r; cz <= pcz + r; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;
                int score = 0, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    int w = weight(be);
                    if (w == 0) continue;
                    score += w;
                    minY = Math.min(minY, be.getPos().getY());
                    maxY = Math.max(maxY, be.getPos().getY());
                }
                if (score < threshold) continue;

                long k = key(cx, cz);
                next.put(k, new Info(score, minY, maxY));
                if (alerted.add(k) && chat.get()) {
                    mc.player.sendMessage(Text.literal("\u00a75[Null] \u00a7fSus chunk at "
                        + (cx * 16 + 8) + ", " + (cz * 16 + 8) + " (score " + score + ")"), false);
                }
            }
        }
        flagged = next;
    }

    private static int colorFor(int score) {
        if (score >= 20) return 0xFFFF3030;   // very sus
        if (score >= 10) return 0xFFFFA030;
        return 0xFFFFE040;                    // mildly sus
    }

    @Override public void onHud(DrawContext ctx, float partial) {
        if (!esp.get()) return;
        for (Map.Entry<Long, Info> e : flagged.entrySet()) {
            int cx = (int) (e.getKey() >> 32), cz = (int) (long) e.getKey();
            Info i = e.getValue();
            int x = cx * 16, z = cz * 16;
            int color = colorFor(i.score());
            Draw3D.box(ctx, x, i.minY() - 3, z, x + 16, i.maxY() + 4, z + 16, color);

            double dist = Math.sqrt(Draw3D.distSq(x + 8, i.maxY() + 8, z + 8));
            float scale = net.minecraft.util.math.MathHelper.clamp(120f / (float) Math.max(dist, 1), 1.0f, 3.0f);
            Draw3D.label(ctx, String.valueOf(i.score()), x + 8, i.maxY() + 8, z + 8, color, scale);
        }
    }
}

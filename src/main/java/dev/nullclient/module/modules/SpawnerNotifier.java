package dev.nullclient.module.modules;

import dev.nullclient.module.Category;
import dev.nullclient.module.Module;
import dev.nullclient.setting.BoolSetting;
import dev.nullclient.setting.NumSetting;
import dev.nullclient.util.Draw3D;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Finds spawners in loaded chunks, posts their coordinates in chat once, and boxes them in red. */
public class SpawnerNotifier extends Module {
    private final NumSetting range = add(new NumSetting("Range (chunks)", 10, 1, 32, 1));
    private final BoolSetting chat = add(new BoolSetting("Chat Alerts", true));
    private final BoolSetting esp = add(new BoolSetting("ESP", true));

    private volatile List<BlockPos> spawners = List.of();
    private final Set<Long> alerted = new HashSet<>();
    private World lastWorld;
    private int ticks;

    public SpawnerNotifier() { super("SpawnerNotifier", "Alerts you to spawners with coords in chat and boxes them in red.", Category.WORLD); }

    @Override public void onEnable() { alerted.clear(); spawners = List.of(); ticks = 19; }
    @Override public void onDisable() { alerted.clear(); spawners = List.of(); }

    @Override public void onTick() {
        if (mc.world != lastWorld) { lastWorld = mc.world; alerted.clear(); spawners = List.of(); }
        if (++ticks % 20 != 0) return;

        int r = range.getInt();
        int pcx = mc.player.getBlockX() >> 4, pcz = mc.player.getBlockZ() >> 4;
        List<BlockPos> found = new ArrayList<>();
        for (int cx = pcx - r; cx <= pcx + r; cx++) {
            for (int cz = pcz - r; cz <= pcz + r; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof MobSpawnerBlockEntity)) continue;
                    BlockPos p = be.getPos();
                    found.add(p);
                    if (alerted.add(p.asLong()) && chat.get()) {
                        mc.player.sendMessage(Text.literal("\u00a7c[Null] \u00a7fSpawner at "
                            + p.getX() + ", " + p.getY() + ", " + p.getZ()), false);
                    }
                }
            }
        }
        spawners = found;
    }

    @Override public void onHud(DrawContext ctx, float partial) {
        if (!esp.get()) return;
        for (BlockPos p : spawners) {
            Draw3D.box(ctx, p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1, 0xFFFF2020);
        }
    }
}

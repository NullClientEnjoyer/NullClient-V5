package dev.nullclient.module.modules;

import dev.nullclient.module.Category;
import dev.nullclient.module.Module;
import dev.nullclient.setting.EnumSetting;
import dev.nullclient.setting.NumSetting;
import dev.nullclient.util.Draw3D;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.function.Predicate;

/** Scans loaded chunks (a couple per tick) for one chosen block type and boxes every hit. */
public class BlockESP extends Module {
    private static final String[] NAMES = {"Diamond", "Ancient Debris", "Emerald", "Gold", "Iron", "Spawner", "Obsidian"};
    private static final int[] COLORS = {0xFF40E8FF, 0xFFB0603A, 0xFF30E060, 0xFFFFD020, 0xFFE0C0A0, 0xFFFF3030, 0xFF8040C0};
    private static final List<Set<Block>> SETS = List.of(
        Set.of(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE),
        Set.of(Blocks.ANCIENT_DEBRIS),
        Set.of(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE),
        Set.of(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE),
        Set.of(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE),
        Set.of(Blocks.SPAWNER),
        Set.of(Blocks.OBSIDIAN));

    private final EnumSetting block = add(new EnumSetting("Block", 0, NAMES));
    private final NumSetting range = add(new NumSetting("Range (chunks)", 3, 1, 8, 1));

    private final Map<Long, List<BlockPos>> cache = new HashMap<>();
    private List<int[]> order = new ArrayList<>();
    private int cursor, lastRange = -1, lastBlock = -1, ticks;

    public BlockESP() { super("BlockESP", "Highlights a chosen block type through walls. Right click to change the block.", Category.RENDER); }

    @Override public void onEnable() { cache.clear(); lastRange = -1; lastBlock = -1; }
    @Override public void onDisable() { cache.clear(); }

    private static long key(int x, int z) { return ((long) x << 32) | (z & 0xFFFFFFFFL); }

    @Override public void onTick() {
        int r = range.getInt(), b = block.index();
        if (b != lastBlock) { cache.clear(); lastBlock = b; }
        if (r != lastRange) {
            lastRange = r;
            order = new ArrayList<>();
            for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) order.add(new int[]{x, z});
            order.sort(Comparator.comparingInt(o -> o[0] * o[0] + o[1] * o[1]));
            cursor = 0;
        }
        int pcx = mc.player.getBlockX() >> 4, pcz = mc.player.getBlockZ() >> 4;

        for (int i = 0; i < 2 && !order.isEmpty(); i++) {
            int[] o = order.get(cursor++ % order.size());
            scanChunk(pcx + o[0], pcz + o[1], SETS.get(b));
        }

        if (++ticks % 40 == 0) {
            cache.keySet().removeIf(k -> {
                int cx = (int) (k >> 32), cz = (int) (long) k;
                return Math.abs(cx - pcx) > lastRange + 1 || Math.abs(cz - pcz) > lastRange + 1;
            });
        }
    }

    private void scanChunk(int cx, int cz, Set<Block> targets) {
        WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
        if (chunk == null) { cache.remove(key(cx, cz)); return; }
        Predicate<BlockState> pred = s -> targets.contains(s.getBlock());
        List<BlockPos> found = new ArrayList<>();
        ChunkSection[] sections = chunk.getSectionArray();
        for (int i = 0; i < sections.length; i++) {
            ChunkSection s = sections[i];
            if (s == null || s.isEmpty() || !s.hasAny(pred)) continue;
            int baseY = (chunk.getBottomSectionCoord() + i) << 4;
            for (int x = 0; x < 16; x++)
                for (int y = 0; y < 16; y++)
                    for (int z = 0; z < 16; z++)
                        if (pred.test(s.getBlockState(x, y, z)))
                            found.add(new BlockPos(cx * 16 + x, baseY + y, cz * 16 + z));
        }
        if (found.isEmpty()) cache.remove(key(cx, cz)); else cache.put(key(cx, cz), found);
    }

    @Override public void onHud(DrawContext ctx, float partial) {
        int color = COLORS[block.index()];
        double max = lastRange * 16 + 16, maxSq = max * max;
        int drawn = 0;
        for (List<BlockPos> list : cache.values()) {
            for (BlockPos p : list) {
                if (drawn >= 400) return;
                if (Draw3D.distSq(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5) > maxSq) continue;
                Draw3D.box(ctx, p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1, color);
                drawn++;
            }
        }
    }
}

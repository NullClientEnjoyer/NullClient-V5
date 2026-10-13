package dev.nullclient.module.modules;

import dev.nullclient.module.Category;
import dev.nullclient.module.Module;
import dev.nullclient.setting.BoolSetting;
import dev.nullclient.setting.NumSetting;
import dev.nullclient.util.Draw3D;
import net.minecraft.block.entity.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class StorageESP extends Module {
    public static StorageESP INSTANCE;

    public record Entry(BlockPos pos, int color) { }

    private final NumSetting range = add(new NumSetting("Range (chunks)", 6, 1, 16, 1));
    private final BoolSetting chests = add(new BoolSetting("Chests", true));
    private final BoolSetting trapped = add(new BoolSetting("Trapped Chests", true));
    private final BoolSetting ender = add(new BoolSetting("Ender Chests", true));
    private final BoolSetting barrels = add(new BoolSetting("Barrels", true));
    private final BoolSetting shulkers = add(new BoolSetting("Shulkers", true));
    private final BoolSetting hoppers = add(new BoolSetting("Hoppers", false));

    public volatile List<Entry> entries = List.of();
    private int ticks;

    public StorageESP() {
        super("StorageESP", "Highlights storage blocks through walls.", Category.RENDER);
        INSTANCE = this;
    }

    @Override public void onEnable() { ticks = 9; }
    @Override public void onDisable() { entries = List.of(); }

    @Override public void onTick() {
        if (++ticks % 10 != 0) return;
        int r = range.getInt();
        int pcx = mc.player.getBlockX() >> 4, pcz = mc.player.getBlockZ() >> 4;
        List<Entry> out = new ArrayList<>();
        for (int cx = pcx - r; cx <= pcx + r; cx++) {
            for (int cz = pcz - r; cz <= pcz + r; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    int color = colorFor(be);
                    if (color != 0) out.add(new Entry(be.getPos(), color));
                }
            }
        }
        entries = out;
    }

    private int colorFor(BlockEntity be) {
        if (be instanceof TrappedChestBlockEntity) return trapped.get() ? 0xFFFF4040 : 0;
        if (be instanceof ChestBlockEntity) return chests.get() ? 0xFFFFA020 : 0;
        if (be instanceof EnderChestBlockEntity) return ender.get() ? 0xFFB060FF : 0;
        if (be instanceof BarrelBlockEntity) return barrels.get() ? 0xFFC08040 : 0;
        if (be instanceof ShulkerBoxBlockEntity) return shulkers.get() ? 0xFFFF70E0 : 0;
        if (be instanceof HopperBlockEntity) return hoppers.get() ? 0xFFA0A0A0 : 0;
        return 0;
    }

    @Override public void onHud(DrawContext ctx, float partial) {
        double max = range.get() * 16 + 16;
        double maxSq = max * max;
        for (Entry e : entries) {
            BlockPos p = e.pos();
            if (Draw3D.distSq(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5) > maxSq) continue;
            Draw3D.box(ctx, p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1, e.color());
        }
    }
}

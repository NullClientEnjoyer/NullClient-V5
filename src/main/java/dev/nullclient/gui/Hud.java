package dev.nullclient.gui;

import dev.nullclient.module.Module;
import dev.nullclient.module.ModuleManager;
import dev.nullclient.util.Draw3D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Hud {
    public static final String WATERMARK = "Null client";
    private static final int ACCENT = 0xFFB066FF;

    public static void render(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        TextRenderer tr = mc.textRenderer;
        float partial = tick.getTickProgress(false);

        Draw3D.begin(mc, ctx);
        for (Module m : ModuleManager.modules) {
            if (!m.enabled) continue;
            try { m.onHud(ctx, partial); } catch (Throwable t) { ModuleManager.fail(m, t); }
        }

        int w = ctx.getScaledWindowWidth();

        // watermark, always visible
        int tw = tr.getWidth(WATERMARK);
        int x = w - tw - 10;
        ctx.fill(x - 4, 3, w - 3, 15, 0x90000000);
        ctx.fill(w - 5, 3, w - 3, 15, ACCENT);
        ctx.drawTextWithShadow(tr, WATERMARK, x, 5, 0xFFFFFFFF);

        if (mc.options.hudHidden) return;

        List<Module> on = new ArrayList<>();
        for (Module m : ModuleManager.modules) if (m.enabled) on.add(m);
        on.sort(Comparator.comparingInt((Module m) -> tr.getWidth(m.name)).reversed());

        int y = 19;
        for (Module m : on) {
            int mw = tr.getWidth(m.name);
            ctx.fill(w - mw - 8, y - 1, w - 3, y + 9, 0x60000000);
            ctx.fill(w - 4, y - 1, w - 3, y + 9, ACCENT);
            ctx.drawTextWithShadow(tr, m.name, w - mw - 6, y, ACCENT);
            y += 10;
        }
    }
}

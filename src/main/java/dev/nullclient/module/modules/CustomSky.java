package dev.nullclient.module.modules;

import dev.nullclient.module.Category;
import dev.nullclient.module.Module;
import dev.nullclient.setting.BoolSetting;
import dev.nullclient.setting.EnumSetting;
import dev.nullclient.setting.NumSetting;
import dev.nullclient.util.CameraState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

/**
 * Animated aurora curtains anchored to compass directions, so they slide across the screen as
 * you turn. Drawn as a soft overlay in the upper part of the view.
 */
public class CustomSky extends Module {
    private static final int[][][] PALETTES = {
        {{0x30, 0xFF, 0x80}, {0x20, 0xE0, 0xC0}, {0x90, 0x60, 0xFF}},  // Aurora
        {{0x40, 0xFF, 0xD0}, {0x30, 0xB0, 0xFF}, {0x60, 0x50, 0xFF}},  // Arctic
        {{0x60, 0xFF, 0xA0}, {0xC0, 0x70, 0xFF}, {0xFF, 0x60, 0xB0}},  // Borealis
    };

    private final EnumSetting palette = add(new EnumSetting("Palette", 0, "Aurora", "Arctic", "Borealis"));
    private final NumSetting intensity = add(new NumSetting("Intensity", 0.7, 0.1, 1.0, 0.05));
    private final NumSetting speed = add(new NumSetting("Speed", 1.0, 0.1, 4.0, 0.1));
    private final BoolSetting outdoors = add(new BoolSetting("Only Outdoors", true));

    public CustomSky() { super("Custom Sky", "Green aurora across the sky.", Category.NULL); }

    private static int screenY(double relDeg, int h, double th) {
        relDeg = MathHelper.clamp(relDeg, -80, 80);
        return (int) (h / 2.0 * (1 - Math.tan(Math.toRadians(relDeg)) / th));
    }

    private static int mix(int[][] p, double u) {
        int[] a, b; double t;
        if (u < 0.5) { a = p[0]; b = p[1]; t = u * 2; } else { a = p[1]; b = p[2]; t = (u - 0.5) * 2; }
        int r = (int) (a[0] + (b[0] - a[0]) * t), g = (int) (a[1] + (b[1] - a[1]) * t), bl = (int) (a[2] + (b[2] - a[2]) * t);
        return (r << 16) | (g << 8) | bl;
    }

    @Override public void onHud(DrawContext ctx, float partial) {
        if (mc.world == null || mc.player == null) return;
        if (!mc.world.getDimension().hasSkyLight()) return;
        if (outdoors.get() && !mc.world.isSkyVisible(mc.player.getBlockPos())) return;

        int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
        double th = Math.tan(Math.toRadians(CameraState.fov) / 2.0), asp = (double) w / h;
        double camElev = -CameraState.cpitch;
        double t = System.currentTimeMillis() / 1000.0 * speed.get();
        double inten = intensity.get();
        int[][] pal = PALETTES[palette.index()];
        int step = 3, segs = 10;

        for (int x = 0; x < w; x += step) {
            double ndc = ((x + step / 2.0) / w) * 2 - 1;
            double az = CameraState.cyaw + Math.toDegrees(Math.atan(ndc * th * asp));
            double band = Math.sin(az * 0.05 + t * 0.20) * 0.5
                        + Math.sin(az * 0.13 - t * 0.31) * 0.3
                        + Math.sin(az * 0.29 + t * 0.50) * 0.2;
            double a = MathHelper.clamp(0.5 + 0.6 * band, 0, 1);
            if (a < 0.08) continue;

            double base = 10 + 7 * Math.sin(az * 0.07 + t * 0.15);
            double height = 16 + 26 * a;
            for (int s = 0; s < segs; s++) {
                double e0 = base + height * s / segs, e1 = base + height * (s + 1) / segs;
                int yLow = screenY(e0 - camElev, h, th), yHigh = screenY(e1 - camElev, h, th);
                if (yLow <= 0 || yHigh >= h) continue;
                yHigh = Math.max(yHigh, 0);
                yLow = Math.min(yLow, h);

                double u = (s + 0.5) / segs;
                double fade = Math.pow(Math.sin(Math.PI * u), 0.8);
                int alpha = (int) (255 * inten * a * fade * 0.5);
                if (alpha < 4) continue;
                ctx.fill(x, yHigh, x + step, yLow, (alpha << 24) | mix(pal, u));
            }
        }
    }
}

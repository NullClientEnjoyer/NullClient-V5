package dev.nullclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

/** Shared state for Freecam / Freelook and the final camera used by the overlays. */
public final class CameraState {
    public static boolean freecam, freelook;
    public static double fx, fy, fz;          // freecam position
    public static float yaw, pitch;           // freecam / freelook angles
    public static double freecamSpeed = 15;   // blocks per second
    public static double freelookDist = 4;

    // final camera, written every frame by CameraMixin
    public static double cx, cy, cz;
    public static float cyaw, cpitch;
    public static float fov = 70f;
    public static boolean captured;

    private static long last;

    public static boolean active() { return freecam || freelook; }

    public static void resetClock() { last = 0; }

    public static void look(double dx, double dy) {
        yaw += (float) dx * 0.15f;
        pitch = MathHelper.clamp(pitch + (float) dy * 0.15f, -90f, 90f);
    }

    public static void stepFreecam() {
        MinecraftClient mc = MinecraftClient.getInstance();
        long now = System.nanoTime();
        double dt = last == 0 ? 0 : Math.min(0.1, (now - last) / 1e9);
        last = now;
        if (dt <= 0 || mc.currentScreen != null) return;

        double v = freecamSpeed * dt;
        double[] h = MoveUtil.horizontal(v, yaw);
        double vy = 0;
        if (mc.options.jumpKey.isPressed()) vy += v;
        if (mc.options.sneakKey.isPressed()) vy -= v;
        fx += h[0];
        fy += vy;
        fz += h[1];
    }
}

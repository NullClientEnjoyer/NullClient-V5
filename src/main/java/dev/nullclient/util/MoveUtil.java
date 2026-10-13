package dev.nullclient.util;

import net.minecraft.client.MinecraftClient;

public class MoveUtil {
    public static double[] horizontal(double speed) {
        return horizontal(speed, MinecraftClient.getInstance().player.getYaw());
    }

    /** Horizontal {x, z} for the held movement keys relative to the given yaw. */
    public static double[] horizontal(double speed, double yawDeg) {
        MinecraftClient mc = MinecraftClient.getInstance();
        double f = (mc.options.forwardKey.isPressed() ? 1 : 0) - (mc.options.backKey.isPressed() ? 1 : 0);
        double s = (mc.options.leftKey.isPressed() ? 1 : 0) - (mc.options.rightKey.isPressed() ? 1 : 0);
        if (f == 0 && s == 0) return new double[]{0, 0};
        double len = Math.sqrt(f * f + s * s);
        f /= len; s /= len;
        double yaw = Math.toRadians(yawDeg);
        double sin = Math.sin(yaw), cos = Math.cos(yaw);
        return new double[]{(-sin * f + cos * s) * speed, (cos * f + sin * s) * speed};
    }

    public static boolean isMoving() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed()
                || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
    }
}

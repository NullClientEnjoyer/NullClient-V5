package dev.nullclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Projects world coordinates onto the screen and draws lines / wireframe boxes with the GUI
 * renderer. Because it is a screen overlay, everything is visible through walls.
 */
public final class Draw3D {
    private static double rx, ry, rz, ux, uy, uz, fx, fy, fz, cx, cy, cz, th, asp;
    private static int sw, sh;
    private static final double NEAR = 0.05;

    private Draw3D() { }

    public static void begin(MinecraftClient mc, DrawContext ctx) {
        sw = ctx.getScaledWindowWidth();
        sh = ctx.getScaledWindowHeight();
        asp = (double) sw / sh;

        double yawDeg, pitchDeg;
        if (CameraState.captured) {
            cx = CameraState.cx; cy = CameraState.cy; cz = CameraState.cz;
            yawDeg = CameraState.cyaw; pitchDeg = CameraState.cpitch;
        } else {
            cx = mc.player.getX(); cy = mc.player.getEyeY(); cz = mc.player.getZ();
            yawDeg = mc.player.getYaw(); pitchDeg = mc.player.getPitch();
        }
        double yaw = Math.toRadians(yawDeg), pit = Math.toRadians(pitchDeg);
        double cp = Math.cos(pit), sp = Math.sin(pit), sy = Math.sin(yaw), cyw = Math.cos(yaw);
        fx = -sy * cp; fy = -sp; fz = cyw * cp;
        rx = -cyw; ry = 0; rz = -sy;
        ux = ry * fz - rz * fy;
        uy = rz * fx - rx * fz;
        uz = rx * fy - ry * fx;
        th = Math.tan(Math.toRadians(CameraState.fov) / 2.0);
    }

    public static double[] toCam(double x, double y, double z) {
        double dx = x - cx, dy = y - cy, dz = z - cz;
        return new double[]{
            dx * rx + dy * ry + dz * rz,
            dx * ux + dy * uy + dz * uz,
            dx * fx + dy * fy + dz * fz};
    }

    public static double distSq(double x, double y, double z) {
        double dx = x - cx, dy = y - cy, dz = z - cz;
        return dx * dx + dy * dy + dz * dz;
    }

    /** Screen position {x, y} of a world point, or null if it is behind the camera. */
    public static double[] project(double x, double y, double z) {
        double[] c = toCam(x, y, z);
        if (c[2] < NEAR) return null;
        return new double[]{sw / 2.0 * (1 + c[0] / (c[2] * th * asp)), sh / 2.0 * (1 - c[1] / (c[2] * th))};
    }

    /** Text anchored to a world point (e.g. the sus score above a chunk). */
    public static void label(DrawContext ctx, String text, double x, double y, double z, int color, float scale) {
        double[] p = project(x, y, z);
        if (p == null || p[0] < -60 || p[0] > sw + 60 || p[1] < -60 || p[1] > sh + 60) return;
        var tr = MinecraftClient.getInstance().textRenderer;
        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate((float) p[0], (float) p[1]);
        m.scale(scale, scale);
        int w = tr.getWidth(text);
        ctx.fill(-w / 2 - 3, -2, w / 2 + 3, 10, 0xB0000000);
        ctx.drawTextWithShadow(tr, text, -w / 2, 0, color);
        m.popMatrix();
    }

    /** Point in camera space that lands at the screen centre or bottom centre. */
    public static double[] tracerOrigin(boolean bottom) {
        return bottom ? new double[]{0, -th, 1} : new double[]{0, 0, 1};
    }

    public static void line(DrawContext ctx, double x1, double y1, double z1,
                            double x2, double y2, double z2, int color) {
        lineCam(ctx, toCam(x1, y1, z1), toCam(x2, y2, z2), color);
    }

    public static void lineCam(DrawContext ctx, double[] a, double[] b, int color) {
        double ax = a[0], ay = a[1], az = a[2], bx = b[0], by = b[1], bz = b[2];
        if (az < NEAR && bz < NEAR) return;
        if (az < NEAR) {
            double t = (NEAR - az) / (bz - az);
            ax += (bx - ax) * t; ay += (by - ay) * t; az = NEAR;
        } else if (bz < NEAR) {
            double t = (NEAR - bz) / (az - bz);
            bx += (ax - bx) * t; by += (ay - by) * t; bz = NEAR;
        }
        double sx1 = sw / 2.0 * (1 + ax / (az * th * asp)), sy1 = sh / 2.0 * (1 - ay / (az * th));
        double sx2 = sw / 2.0 * (1 + bx / (bz * th * asp)), sy2 = sh / 2.0 * (1 - by / (bz * th));
        draw2D(ctx, sx1, sy1, sx2, sy2, color);
    }

    public static void box(DrawContext ctx, double x1, double y1, double z1,
                           double x2, double y2, double z2, int color) {
        double[][] c = {
            toCam(x1, y1, z1), toCam(x2, y1, z1), toCam(x2, y1, z2), toCam(x1, y1, z2),
            toCam(x1, y2, z1), toCam(x2, y2, z1), toCam(x2, y2, z2), toCam(x1, y2, z2)};
        int[][] e = {{0,1},{1,2},{2,3},{3,0},{4,5},{5,6},{6,7},{7,4},{0,4},{1,5},{2,6},{3,7}};
        for (int[] ed : e) lineCam(ctx, c[ed[0]], c[ed[1]], color);
    }

    private static void draw2D(DrawContext ctx, double x1, double y1, double x2, double y2, int color) {
        // clip to the screen (Liang-Barsky) so huge coordinates near the camera stay sane
        double xmin = -50, xmax = sw + 50, ymin = -50, ymax = sh + 50;
        double dx = x2 - x1, dy = y2 - y1, t0 = 0, t1 = 1;
        double[] p = {-dx, dx, -dy, dy};
        double[] q = {x1 - xmin, xmax - x1, y1 - ymin, ymax - y1};
        for (int i = 0; i < 4; i++) {
            if (p[i] == 0) {
                if (q[i] < 0) return;
            } else {
                double r = q[i] / p[i];
                if (p[i] < 0) { if (r > t1) return; if (r > t0) t0 = r; }
                else { if (r < t0) return; if (r < t1) t1 = r; }
            }
        }
        float nx1 = (float) (x1 + t0 * dx), ny1 = (float) (y1 + t0 * dy);
        float nx2 = (float) (x1 + t1 * dx), ny2 = (float) (y1 + t1 * dy);
        float ddx = nx2 - nx1, ddy = ny2 - ny1;
        float len = (float) Math.hypot(ddx, ddy);
        if (len < 1f) return;

        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(nx1, ny1);
        m.rotate((float) Math.atan2(ddy, ddx));
        ctx.fill(0, 0, (int) Math.ceil(len), 1, color);
        m.popMatrix();
    }
}

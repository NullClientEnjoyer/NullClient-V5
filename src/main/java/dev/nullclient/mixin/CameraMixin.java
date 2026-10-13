package dev.nullclient.mixin;

import dev.nullclient.util.CameraState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private Vec3d pos;
    @Shadow private float pitch;
    @Shadow private float yaw;
    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPos(double x, double y, double z);

    @Inject(method = "update", at = @At("TAIL"), require = 0)
    private void nullclient$update(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            if (CameraState.freecam) {
                CameraState.stepFreecam();
                setRotation(CameraState.yaw, CameraState.pitch);
                setPos(CameraState.fx, CameraState.fy, CameraState.fz);
            } else if (CameraState.freelook) {
                float pt = mc.getRenderTickCounter().getTickProgress(false);
                double ex = MathHelper.lerp(pt, mc.player.prevX, mc.player.getX());
                double ey = MathHelper.lerp(pt, mc.player.prevY, mc.player.getY()) + mc.player.getStandingEyeHeight();
                double ez = MathHelper.lerp(pt, mc.player.prevZ, mc.player.getZ());
                double yr = Math.toRadians(CameraState.yaw), pr = Math.toRadians(CameraState.pitch);
                double fx = -Math.sin(yr) * Math.cos(pr), fy = -Math.sin(pr), fz = Math.cos(yr) * Math.cos(pr);
                double d = CameraState.freelookDist;
                setRotation(CameraState.yaw, CameraState.pitch);
                // no collision check on purpose: the camera passes straight through blocks
                setPos(ex - fx * d, ey - fy * d, ez - fz * d);
            }
        }
        CameraState.cx = this.pos.x;
        CameraState.cy = this.pos.y;
        CameraState.cz = this.pos.z;
        CameraState.cyaw = this.yaw;
        CameraState.cpitch = this.pitch;
        CameraState.captured = true;
    }
}

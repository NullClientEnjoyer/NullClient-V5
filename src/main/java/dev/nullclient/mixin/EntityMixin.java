package dev.nullclient.mixin;

import dev.nullclient.util.CameraState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    // While Freecam / Freelook is on, the mouse moves the camera instead of the player.
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true, require = 0)
    private void nullclient$look(double dx, double dy, CallbackInfo ci) {
        if (CameraState.active() && (Object) this == MinecraftClient.getInstance().player) {
            CameraState.look(dx, dy);
            ci.cancel();
        }
    }
}

package dev.nullclient.mixin;

import dev.nullclient.util.CameraState;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    // Records the real FOV (sprinting / potions change it) so overlays line up with the world.
    @Inject(method = "getFov", at = @At("RETURN"), require = 0)
    private void nullclient$fov(CallbackInfoReturnable<?> cir) {
        Object v = cir.getReturnValue();
        if (v instanceof Number n) CameraState.fov = n.floatValue();
    }
}

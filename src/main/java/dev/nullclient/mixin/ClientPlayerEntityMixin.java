package dev.nullclient.mixin;

import dev.nullclient.util.CameraState;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    // Freecam: keep the real player standing still.
    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true, require = 0)
    private void nullclient$freeze(CallbackInfo ci) {
        if (CameraState.freecam) ci.cancel();
    }
}

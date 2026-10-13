package dev.nullclient.mixin;

import dev.nullclient.gui.NullTitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    // Swap the vanilla title screen for our black "Null Client" screen.
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen nullclient$replaceTitle(Screen screen) {
        if (screen != null && screen.getClass() == TitleScreen.class) {
            return new NullTitleScreen();
        }
        return screen;
    }
}

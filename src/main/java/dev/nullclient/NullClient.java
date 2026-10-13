package dev.nullclient;

import dev.nullclient.gui.Hud;
import dev.nullclient.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.util.Identifier;

public class NullClient implements ClientModInitializer {
    public static final String NAME = "Null Client";

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        ClientTickEvents.END_CLIENT_TICK.register(ModuleManager::tick);
        // current Fabric HUD API (HudRenderCallback is deprecated)
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT, Identifier.of("nullclient", "overlay"), Hud::render);
    }
}

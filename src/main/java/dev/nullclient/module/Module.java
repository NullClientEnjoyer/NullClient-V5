package dev.nullclient.module;

import dev.nullclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name, description;
    public final Category category;
    public boolean enabled;
    public int key = -1;
    public final List<Setting<?>> settings = new ArrayList<>();
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    protected Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }

    protected <T extends Setting<?>> T add(T s) { settings.add(s); return s; }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean e) {
        if (e == enabled) return;
        enabled = e;
        if (e) onEnable(); else onDisable();
    }

    public void onEnable() { }
    public void onDisable() { }
    public void onTick() { }
    /** Called every frame while enabled; draw overlays here (see Draw3D). */
    public void onHud(DrawContext ctx, float partial) { }
}

package dev.nullclient.module;

import dev.nullclient.gui.ClickGuiScreen;
import dev.nullclient.module.modules.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final List<Module> modules = new ArrayList<>();
    public static int guiKey = GLFW.GLFW_KEY_RIGHT_SHIFT;
    private static final boolean[] wasDown = new boolean[512];

    public static void init() {
        // sky first so its overlay is drawn underneath everything else
        modules.add(new CustomSky());

        modules.add(new Sprint());
        modules.add(new Flight());
        modules.add(new Speed());
        modules.add(new Step());

        modules.add(new NoFall());

        modules.add(new Fullbright());
        modules.add(new Freecam());
        modules.add(new Freelook());
        modules.add(new StorageESP());
        modules.add(new BlockESP());
        modules.add(new Tracers());

        modules.add(new SusChunkFinder());
        modules.add(new SpawnerNotifier());
    }

    public static List<Module> inCategory(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : modules) if (m.category == c) out.add(m);
        return out;
    }

    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;

        if (mc.currentScreen == null) {
            long win = mc.getWindow().getHandle();
            pollKey(win, guiKey, () -> mc.setScreen(new ClickGuiScreen()));
            for (Module m : modules) {
                if (m.key >= 0) pollKey(win, m.key, m::toggle);
            }
        }
        for (Module m : new ArrayList<>(modules)) {
            if (!m.enabled) continue;
            try { m.onTick(); } catch (Throwable t) { fail(m, t); }
        }
    }

    /** Turns a misbehaving module off and tells you why, instead of crashing the game. */
    public static void fail(Module m, Throwable t) {
        try { m.enabled = false; m.onDisable(); } catch (Throwable ignored) { }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("\u00a7c[Null] " + m.name + " crashed and was turned off: " + t), false);
        }
    }

    private static void pollKey(long win, int key, Runnable action) {
        if (key < 0 || key >= wasDown.length) return;
        boolean down = GLFW.glfwGetKey(win, key) == GLFW.GLFW_PRESS;
        if (down && !wasDown[key]) action.run();
        wasDown[key] = down;
    }

    public static String keyName(int key) {
        if (key < 0) return "None";
        return switch (key) {
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RShift";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LShift";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCtrl";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCtrl";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RAlt";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LAlt";
            case GLFW.GLFW_KEY_SPACE -> "Space";
            case GLFW.GLFW_KEY_TAB -> "Tab";
            case GLFW.GLFW_KEY_UP -> "Up";
            case GLFW.GLFW_KEY_DOWN -> "Down";
            case GLFW.GLFW_KEY_LEFT -> "Left";
            case GLFW.GLFW_KEY_RIGHT -> "Right";
            default -> {
                String n = GLFW.glfwGetKeyName(key, 0);
                yield n != null ? n.toUpperCase() : "Key" + key;
            }
        };
    }
}

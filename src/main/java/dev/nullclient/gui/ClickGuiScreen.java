package dev.nullclient.gui;

import dev.nullclient.module.Category;
import dev.nullclient.module.Module;
import dev.nullclient.module.ModuleManager;
import dev.nullclient.setting.NumSetting;
import dev.nullclient.setting.Setting;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * One black square with a search bar on top. Type to filter, click a tab to filter by category.
 * Left click a module = toggle. Right click a module = settings + keybind. Mouse wheel scrolls.
 */
public class ClickGuiScreen extends Screen {
    private static final int ACCENT = 0xFFB066FF;

    private final StringBuilder query = new StringBuilder();
    private Category tab;                       // null = All
    private final Set<Module> expanded = new HashSet<>();
    private final List<Row> rows = new ArrayList<>();
    private Module listening;
    private NumSetting sliding;
    private int slideX, slideW;
    private int scroll, listTop, listBottom;

    private enum Kind { TAB, MODULE, SETTING, BIND }
    private record Row(int x, int y, int w, int h, Kind kind, Category cat, Module mod, Setting<?> set) {
        boolean hit(int mx, int my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    public ClickGuiScreen() { super(Text.literal("Null Client")); }

    @Override public boolean shouldPause() { return false; }

    @Override public void render(DrawContext ctx, int mx, int my, float delta) {
        try { draw(ctx, mx, my); } catch (Throwable ignored) { }
    }

    private void draw(DrawContext ctx, int mx, int my) {
        int pw = Math.min(340, width - 16), ph = Math.min(320, height - 16);
        int px = (width - pw) / 2, py = (height - ph) / 2;

        ctx.fill(0, 0, width, height, 0x60000000);
        ctx.fill(px - 1, py - 1, px + pw + 1, py + ph + 1, 0xFF2E2E2E);
        ctx.fill(px, py, px + pw, py + ph, 0xFF000000);
        rows.clear();

        long win = client.getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (sliding != null) {
            if (down) {
                double t = MathHelper.clamp((mx - slideX) / (double) slideW, 0, 1);
                sliding.set(sliding.min + t * (sliding.max - sliding.min));
            } else sliding = null;
        }

        // search bar
        int sx = px + 8, sy = py + 8, sw = pw - 16, sh = 18;
        ctx.fill(sx - 1, sy - 1, sx + sw + 1, sy + sh + 1, 0xFF3A2A5A);
        ctx.fill(sx, sy, sx + sw, sy + sh, 0xFF111111);
        String q = query.toString();
        if (q.isEmpty()) ctx.drawTextWithShadow(textRenderer, "Search modules...", sx + 6, sy + 5, 0xFF666666);
        else ctx.drawTextWithShadow(textRenderer, q, sx + 6, sy + 5, 0xFFFFFFFF);
        if ((System.currentTimeMillis() / 500) % 2 == 0) {
            int cx = sx + 6 + textRenderer.getWidth(q);
            ctx.fill(cx, sy + 4, cx + 1, sy + 14, ACCENT);
        }

        // category tabs
        int ty = py + 32, tx = px + 8;
        Category[] cats = Category.values();
        for (int i = -1; i < cats.length; i++) {
            Category c = i < 0 ? null : cats[i];
            String label = c == null ? "All" : c.label;
            int w = textRenderer.getWidth(label) + 10;
            if (tx + w > px + pw - 4) break;
            boolean sel = tab == c;
            boolean hov = mx >= tx && mx < tx + w && my >= ty && my < ty + 14;
            ctx.fill(tx, ty, tx + w, ty + 14, sel ? 0xFF22123F : (hov ? 0xFF181818 : 0xFF0C0C0C));
            if (sel) ctx.fill(tx, ty + 13, tx + w, ty + 14, ACCENT);
            ctx.drawTextWithShadow(textRenderer, label, tx + 5, ty + 3, sel ? 0xFFFFFFFF : 0xFF999999);
            rows.add(new Row(tx, ty, w, 14, Kind.TAB, c, null, null));
            tx += w + 2;
        }

        // module list
        listTop = py + 52;
        listBottom = py + ph - 18;
        int lx = px + 6, lw = pw - 12;
        ctx.fill(px + 4, listTop - 1, px + pw - 4, listTop, 0xFF222222);
        ctx.enableScissor(px + 1, listTop, px + pw - 1, listBottom);

        String ql = q.toLowerCase(Locale.ROOT);
        int y = listTop - scroll, total = 0;
        String hoverDesc = null;
        for (Module m : ModuleManager.modules) {
            if (tab != null && m.category != tab) continue;
            if (!ql.isEmpty() && !m.name.toLowerCase(Locale.ROOT).contains(ql)
                    && !m.description.toLowerCase(Locale.ROOT).contains(ql)) continue;

            int h = 17;
            boolean hover = mx >= lx && mx < lx + lw && my >= y && my < y + h && my >= listTop && my < listBottom;
            if (hover) hoverDesc = m.description;
            ctx.fill(lx, y, lx + lw, y + h - 1, m.enabled ? 0xFF22123F : (hover ? 0xFF151515 : 0xFF0B0B0B));
            if (m.enabled) ctx.fill(lx, y, lx + 2, y + h - 1, ACCENT);
            ctx.drawTextWithShadow(textRenderer, m.name, lx + 8, y + 4, m.enabled ? 0xFFFFFFFF : 0xFFAAAAAA);
            String right = m.key >= 0 ? "[" + ModuleManager.keyName(m.key) + "]" : m.category.label;
            ctx.drawTextWithShadow(textRenderer, right, lx + lw - 6 - textRenderer.getWidth(right), y + 4,
                m.key >= 0 ? 0xFF9A8AC8 : 0xFF555555);
            if (y + h > listTop && y < listBottom) rows.add(new Row(lx, y, lw, h, Kind.MODULE, m.category, m, null));
            y += h; total += h;

            if (expanded.contains(m)) {
                int sxx = lx + 8, sww = lw - 8, sh2 = 15;
                for (Setting<?> s : m.settings) {
                    ctx.fill(sxx, y, sxx + sww, y + sh2 - 1, 0xFF101010);
                    if (s instanceof NumSetting n) {
                        int fill = (int) (sww * (n.get() - n.min) / (n.max - n.min));
                        ctx.fill(sxx, y, sxx + fill, y + sh2 - 1, 0xFF3A1E70);
                    } else if (s.value instanceof Boolean b && b) {
                        ctx.fill(sxx, y, sxx + 3, y + sh2 - 1, ACCENT);
                    }
                    ctx.drawTextWithShadow(textRenderer, s.name + ": " + s.display(), sxx + 7, y + 4, 0xFFDDDDDD);
                    if (y + sh2 > listTop && y < listBottom) rows.add(new Row(sxx, y, sww, sh2, Kind.SETTING, m.category, m, s));
                    y += sh2; total += sh2;
                }
                ctx.fill(sxx, y, sxx + sww, y + sh2 - 1, 0xFF101010);
                String bind = listening == m ? "press a key... (Esc = clear)" : "Keybind: " + ModuleManager.keyName(m.key);
                ctx.drawTextWithShadow(textRenderer, bind, sxx + 7, y + 4, listening == m ? 0xFFFFD060 : 0xFFDDDDDD);
                if (y + sh2 > listTop && y < listBottom) rows.add(new Row(sxx, y, sww, sh2, Kind.BIND, m.category, m, null));
                y += sh2; total += sh2;
            }
        }
        ctx.disableScissor();

        int maxScroll = Math.max(0, total - (listBottom - listTop));
        scroll = MathHelper.clamp(scroll, 0, maxScroll);
        if (maxScroll > 0) {
            int trackH = listBottom - listTop;
            int barH = Math.max(12, trackH * trackH / (trackH + maxScroll));
            int barY = listTop + (int) ((trackH - barH) * (scroll / (double) maxScroll));
            ctx.fill(px + pw - 4, barY, px + pw - 2, barY + barH, 0xFF4A3A7A);
        }

        String foot = hoverDesc != null ? hoverDesc : "Right click a module for settings + keybind  |  Wheel to scroll";
        ctx.drawTextWithShadow(textRenderer, foot, px + 8, py + ph - 13, 0xFF777777);
    }

    @Override public boolean mouseClicked(Click click, boolean doubled) {
        int mx = (int) click.x(), my = (int) click.y(), btn = click.button();
        for (int i = rows.size() - 1; i >= 0; i--) {
            Row r = rows.get(i);
            if (!r.hit(mx, my)) continue;
            if (r.kind != Kind.TAB && (my < listTop || my >= listBottom)) continue;
            switch (r.kind) {
                case TAB -> { if (btn == 0) { tab = r.cat; scroll = 0; } }
                case MODULE -> {
                    if (btn == 0) r.mod.toggle();
                    else if (btn == 1) { if (!expanded.remove(r.mod)) expanded.add(r.mod); }
                }
                case SETTING -> {
                    if (r.set instanceof NumSetting n) {
                        if (btn == 0) {
                            sliding = n; slideX = r.x; slideW = r.w;
                            double t = MathHelper.clamp((mx - r.x) / (double) r.w, 0, 1);
                            n.set(n.min + t * (n.max - n.min));
                        }
                    } else r.set.click(btn == 0);
                }
                case BIND -> {
                    if (btn == 0) listening = r.mod;
                    else if (btn == 1) r.mod.key = -1;
                }
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll -= (int) (verticalAmount * 20);
        return true;
    }

    @Override public boolean keyPressed(KeyInput input) {
        int key = input.getKeycode();
        if (listening != null) {
            listening.key = (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_BACKSPACE
                || key == GLFW.GLFW_KEY_DELETE) ? -1 : key;
            listening = null;
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE || key == ModuleManager.guiKey) { close(); return true; }
        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (query.length() > 0) query.setLength(query.length() - 1);
            scroll = 0;
            return true;
        }
        if (key == GLFW.GLFW_KEY_UP) { scroll -= 20; return true; }
        if (key == GLFW.GLFW_KEY_DOWN) { scroll += 20; return true; }
        if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            query.append((char) ('a' + key - GLFW.GLFW_KEY_A)); scroll = 0; return true;
        }
        if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
            query.append((char) ('0' + key - GLFW.GLFW_KEY_0)); scroll = 0; return true;
        }
        if (key == GLFW.GLFW_KEY_SPACE) { query.append(' '); scroll = 0; return true; }
        return super.keyPressed(input);
    }
}

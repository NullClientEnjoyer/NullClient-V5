package dev.nullclient.gui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;

import java.util.Random;

public class NullTitleScreen extends Screen {
    private static final int BTN_W = 210, BTN_H = 22, GAP = 6;
    private static final int ACCENT = 0xB066FF;
    private static final String[] LABELS = {"Singleplayer", "Multiplayer", "Options", "Quit"};

    private final float[] sx = new float[110], sy = new float[110], sph = new float[110], sspd = new float[110];
    private final long start = System.currentTimeMillis();

    public NullTitleScreen() {
        super(Text.literal("Null Client"));
        Random r = new Random(42);
        for (int i = 0; i < sx.length; i++) {
            sx[i] = r.nextFloat(); sy[i] = r.nextFloat();
            sph[i] = r.nextFloat() * 6.28f; sspd[i] = 0.002f + r.nextFloat() * 0.006f;
        }
    }

    private int btnX() { return width / 2 - BTN_W / 2; }
    private int btnY(int i) { return (int) (height * 0.54) + i * (BTN_H + GAP); }

    private void spaced(DrawContext ctx, String s, float cx, float y, float scale, int spacing, int color) {
        int total = 0;
        for (char c : s.toCharArray()) total += textRenderer.getWidth(String.valueOf(c)) + spacing;
        total -= spacing;
        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(cx, y);
        m.scale(scale, scale);
        int x = -total / 2;
        for (char c : s.toCharArray()) {
            String t = String.valueOf(c);
            ctx.drawTextWithShadow(textRenderer, t, x, 0, color);
            x += textRenderer.getWidth(t) + spacing;
        }
        m.popMatrix();
    }

    @Override public void render(DrawContext ctx, int mx, int my, float delta) {
        try { draw(ctx, mx, my); } catch (Throwable ignored) { ctx.fill(0, 0, width, height, 0xFF000000); }
    }

    private void draw(DrawContext ctx, int mx, int my) {
        float t = (System.currentTimeMillis() - start) / 1000f;
        ctx.fill(0, 0, width, height, 0xFF000000);

        // slow drifting stars
        for (int i = 0; i < sx.length; i++) {
            float yy = (sy[i] - t * sspd[i]) % 1f;
            if (yy < 0) yy += 1f;
            float tw = 0.5f + 0.5f * (float) Math.sin(t * 1.5f + sph[i]);
            int a = (int) (40 + 150 * tw);
            int px = (int) (sx[i] * width), py = (int) (yy * height);
            ctx.fill(px, py, px + 1, py + 1, (a << 24) | 0xC8BEFF);
        }

        float cx = width / 2f, ty = height * 0.17f;
        // soft glow behind the wordmark
        for (int i = 0; i < 4; i++) {
            float ox = (i % 2 == 0 ? -1 : 1) * 0.8f, oy = (i < 2 ? -1 : 1) * 0.8f;
            spaced(ctx, "NULL", cx + ox * 7, ty + oy * 7, 7f, 4, 0x30000000 | ACCENT);
        }
        spaced(ctx, "NULL", cx, ty, 7f, 4, 0xFFFFFFFF);
        spaced(ctx, "C L I E N T", cx, ty + 66, 2f, 2, 0xFF000000 | ACCENT);

        // breathing line
        int la = (int) (90 + 100 * (0.5 + 0.5 * Math.sin(t * 1.2)));
        ctx.fill((int) cx - 40, (int) (ty + 88), (int) cx + 40, (int) (ty + 89), (la << 24) | ACCENT);
        String tag = "utility client for anarchy servers";
        ctx.drawTextWithShadow(textRenderer, tag, (int) cx - textRenderer.getWidth(tag) / 2, (int) (ty + 96), 0xFF666666);

        for (int i = 0; i < LABELS.length; i++) {
            int x = btnX(), y = btnY(i);
            boolean hover = mx >= x && mx < x + BTN_W && my >= y && my < y + BTN_H;
            int border = hover ? (0xFF000000 | ACCENT) : 0xFF2A2A2A;
            ctx.fill(x, y, x + BTN_W, y + BTN_H, border);
            ctx.fill(x + 1, y + 1, x + BTN_W - 1, y + BTN_H - 1, hover ? 0xFF16101F : 0xFF0C0C0C);
            int tw = textRenderer.getWidth(LABELS[i]);
            ctx.drawTextWithShadow(textRenderer, LABELS[i], x + (BTN_W - tw) / 2, y + 7, hover ? 0xFFFFFFFF : 0xFFBBBBBB);
        }

        ctx.drawTextWithShadow(textRenderer, "Null Client 0.1.0  |  Minecraft 1.21.11", 6, height - 12, 0xFF444444);
    }

    @Override public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            int mx = (int) click.x(), my = (int) click.y();
            for (int i = 0; i < LABELS.length; i++) {
                int x = btnX(), y = btnY(i);
                if (mx >= x && mx < x + BTN_W && my >= y && my < y + BTN_H) {
                    switch (i) {
                        case 0 -> client.setScreen(new SelectWorldScreen(this));
                        case 1 -> client.setScreen(new MultiplayerScreen(this));
                        case 2 -> client.setScreen(new OptionsScreen(this, client.options));
                        default -> client.scheduleStop();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override public boolean shouldCloseOnEsc() { return false; }
}

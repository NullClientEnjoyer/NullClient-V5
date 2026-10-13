package dev.nullclient.setting;

import java.util.Locale;

public class NumSetting extends Setting<Double> {
    public final double min, max, step;

    public NumSetting(String name, double def, double min, double max, double step) {
        super(name, def);
        this.min = min; this.max = max; this.step = step;
    }

    public double get() { return value; }
    public int getInt() { return (int) Math.round(value); }

    public void set(double v) {
        v = Math.max(min, Math.min(max, v));
        v = Math.round(v / step) * step;
        value = Math.max(min, Math.min(max, v));
    }

    @Override public String display() {
        return step >= 1 ? String.valueOf((int) Math.round(value)) : String.format(Locale.ROOT, "%.2f", value);
    }
    @Override public void click(boolean forward) { }
}

package dev.nullclient.setting;

public class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String name, boolean def) { super(name, def); }
    public boolean get() { return value; }
    @Override public String display() { return value ? "On" : "Off"; }
    @Override public void click(boolean forward) { value = !value; }
}

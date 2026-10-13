package dev.nullclient.setting;

public class EnumSetting extends Setting<Integer> {
    public final String[] options;

    public EnumSetting(String name, int def, String... options) {
        super(name, def);
        this.options = options;
    }

    public int index() { return value; }
    public String get() { return options[value]; }
    @Override public String display() { return options[value]; }
    @Override public void click(boolean forward) {
        value = (value + (forward ? 1 : options.length - 1)) % options.length;
    }
}

package dev.nullclient.setting;

public abstract class Setting<T> {
    public final String name;
    public T value;

    protected Setting(String name, T value) { this.name = name; this.value = value; }

    public abstract String display();
    /** Left click = forward, right click = backward. */
    public abstract void click(boolean forward);
}

package dev.nullclient.module;

public enum Category {
    MOVEMENT("Movement"), RENDER("Render"), PLAYER("Player"), WORLD("World"), NULL("Null Client");
    public final String label;
    Category(String label) { this.label = label; }
}

package com.clyro;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;

/** Source unique de vérité pour le thème clair/sombre. */
public final class ThemeManager {

    private static final BooleanProperty DARK = new SimpleBooleanProperty(false);

    private ThemeManager() {}

    public static BooleanProperty darkProperty() { return DARK; }

    public static boolean isDark() { return DARK.get(); }

    public static void toggle(Scene scene) {
        DARK.set(!DARK.get());
        apply(scene.getRoot());
    }

    /** Applique le thème courant à la racine d'une page. */
    public static void apply(Parent root) {
        var classes = root.getStyleClass();
        if (DARK.get()) {
            if (!classes.contains("dark")) classes.add("dark");
        } else {
            classes.remove("dark");
        }
    }
}
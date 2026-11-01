package com.clyro;

import javafx.animation.FadeTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Gère les pages : chaque page est créée une seule fois puis réutilisée. */
public final class Navigator {

    private static final Map<String, Supplier<Parent>> FACTORIES = new HashMap<>();
    private static final Map<String, Parent> CACHE = new HashMap<>();
    private static Scene scene;
    private static boolean switching = false;

    private Navigator() {}

    public static void register(String name, Supplier<Parent> factory) {
        FACTORIES.put(name, factory);
    }

    /** Retourne la page (la crée si besoin). */
    public static Parent load(String name) {
        return CACHE.computeIfAbsent(name, n -> FACTORIES.get(n).get());
    }

    public static void init(Scene s) { scene = s; }

    /** Affiche une page avec un fondu enchaîné. */
    public static void go(String name) {
        Parent next = load(name);
        Parent current = scene.getRoot();
        if (switching || next == current) return;
        switching = true;

        FadeTransition out = new FadeTransition(Duration.millis(180), current);
        out.setToValue(0);
        out.setOnFinished(e -> {
            current.setOpacity(1);          // on la remet visible pour son prochain affichage
            ThemeManager.apply(next);       // la page prend le thème courant
            next.setOpacity(0);
            scene.setRoot(next);
            FadeTransition in = new FadeTransition(Duration.millis(260), next);
            in.setToValue(1);
            in.setOnFinished(x -> switching = false);
            in.play();
        });
        out.play();
    }
}
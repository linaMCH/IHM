package com.clyro;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/** Petits utilitaires d'images et d'animations partagés par les pages. */
public final class Fx {

    private Fx() {}

    public static Image loadImage(String path) {
        var stream = Fx.class.getResourceAsStream(path);
        if (stream == null) throw new IllegalStateException("Image introuvable : " + path);
        return new Image(stream);
    }

    public static ImageView img(String path, double height) {
        ImageView iv = new ImageView(loadImage(path));
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setFitHeight(height);
        return iv;
    }

    /** Flottement vertical infini. */
    public static void bob(Node n, double dist, double seconds) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(seconds), n);
        tt.setFromY(0);
        tt.setToY(-dist);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    /** Léger agrandissement au survol. */
    public static void hoverPop(Node n) {
        n.setOnMouseEntered(e -> animateTo(n, 0, 1.06));
        n.setOnMouseExited(e -> animateTo(n, 0, 1));
    }

    public static void animateTo(Node n, double y, double s) {
        new Timeline(new KeyFrame(Duration.millis(180),
                new KeyValue(n.translateYProperty(), y, Interpolator.EASE_OUT),
                new KeyValue(n.scaleXProperty(), s, Interpolator.EASE_OUT),
                new KeyValue(n.scaleYProperty(), s, Interpolator.EASE_OUT))).play();
    }

    /** Secousse horizontale (champ invalide). */
    public static void shake(Node n) {
        new Timeline(
                new KeyFrame(Duration.millis(0),   new KeyValue(n.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(60),  new KeyValue(n.translateXProperty(), -8)),
                new KeyFrame(Duration.millis(120), new KeyValue(n.translateXProperty(), 8)),
                new KeyFrame(Duration.millis(180), new KeyValue(n.translateXProperty(), -6)),
                new KeyFrame(Duration.millis(240), new KeyValue(n.translateXProperty(), 6)),
                new KeyFrame(Duration.millis(300), new KeyValue(n.translateXProperty(), 0))
        ).play();
    }
}
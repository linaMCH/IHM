package com.clyro;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public final class RolePage {

    private static String selectedRole = null;

    private final BorderPane root = new BorderPane();
    private final VBox content = new VBox(34);

    /** Point d'entrée : crée la page. */
    public static Parent create() {
        return new RolePage().root;
    }

    /** Rôle choisi ("professeur" ou "etudiant"), null si aucun. */
    public static String selectedRole() {
        return selectedRole;
    }

    private RolePage() {
        root.getStyleClass().add("role-page");
        root.getStylesheets().add(RolePage.class.getResource("/css/role.css").toExternalForm());
        root.setCenter(buildContent());
        root.setBottom(buildFooter());
        // L'animation d'entrée se rejoue à chaque affichage de la page
        root.sceneProperty().addListener((o, old, s) -> { if (s != null) playIntro(); });
    }

    // ------------------------------------------------------------ Contenu
    private Node buildContent() {
        Image light = Fx.loadImage("/images/logo.png");
        Image dark = Fx.loadImage("/images/logo-dark.png");
        ImageView logo = new ImageView(ThemeManager.isDark() ? dark : light);
        logo.setPreserveRatio(true);
        logo.setSmooth(true);
        logo.setFitHeight(120);
        logo.setCursor(Cursor.HAND);
        logo.setOnMouseClicked(e -> Navigator.go("home"));
        ThemeManager.darkProperty().addListener((o, was, isDark) -> logo.setImage(isDark ? dark : light));

        Label text = new Label("Afin de personnaliser votre expérience,\nindiquez le type de votre profil");
        text.getStyleClass().add("role-text");
        text.setTextAlignment(TextAlignment.CENTER);
        text.setAlignment(Pos.CENTER);

        Button prof = roleButton("Professeur", "far-user", "professeur");
        Button etud = roleButton("Étudiant", "fas-graduation-cap", "etudiant");
        HBox buttons = new HBox(40, prof, etud);
        buttons.setAlignment(Pos.CENTER);

        content.getChildren().addAll(logo, text, buttons);
        content.setAlignment(Pos.CENTER);

        StackPane center = new StackPane(content);
        center.setPadding(new Insets(20));
        return center;
    }

    private Button roleButton(String label, String icon, String role) {
        Button b = new Button(label);
        b.setGraphic(new FontIcon(icon));
        b.getStyleClass().add("role-btn");
        b.setCursor(Cursor.HAND);
        Fx.hoverPop(b);
        b.setOnAction(e -> choose(role));
        return b;
    }

    private Node buildFooter() {
        Label l = new Label("© 2026 Clyro || Tout droits réservés");
        l.getStyleClass().add("role-footer");
        StackPane footer = new StackPane(l);
        footer.setPadding(new Insets(0, 0, 14, 0));
        return footer;
    }

    // ------------------------------------------------------------ Logique
    private void choose(String role) {
        selectedRole = role;
        // TODO : décommente quand la page "dashboard" sera enregistrée dans le Navigator
        // Navigator.go("dashboard");
    }

    private void playIntro() {
        content.setOpacity(0);
        content.setTranslateY(30);
        FadeTransition f = new FadeTransition(Duration.millis(500), content);
        f.setToValue(1);
        TranslateTransition t = new TranslateTransition(Duration.millis(500), content);
        t.setToY(0);
        t.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(f, t).play();
    }
}
package com.clyro;

import javafx.animation.*;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.regex.Pattern;

public final class LoginPage {

    private static final double TAB_W = 190, TAB_H = 44;
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final PseudoClass FOCUSED = PseudoClass.getPseudoClass("focused-field");
    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    private final BorderPane root = new BorderPane();
    private final HBox card = new HBox();
    private final StackPane formHolder = new StackPane();
    private final StackPane robotWrap = new StackPane();
    private final Region pill = new Region();
    private final Button tabLogin = new Button("Se connecter");
    private final Button tabSignup = new Button("S'inscrire");
    private boolean signupMode = false;

    /** Point d'entrée : crée la page. */
    public static Parent create() {
        return new LoginPage().root;
    }

    private LoginPage() {
        root.getStyleClass().add("login-page");
        root.setTop(buildTopBar());
        root.setCenter(buildCard());
        // L'animation d'entrée se rejoue à chaque affichage de la page
        root.sceneProperty().addListener((o, old, s) -> { if (s != null) playIntro(); });
    }

    // ------------------------------------------------------------ Barre du haut
    private Node buildTopBar() {
        Image light = Fx.loadImage("/images/logo.png");
        Image dark = Fx.loadImage("/images/logo-dark.png");
        ImageView logo = new ImageView(ThemeManager.isDark() ? dark : light);
        logo.setPreserveRatio(true);
        logo.setSmooth(true);
        logo.setFitHeight(42);
        logo.setCursor(Cursor.HAND);
        logo.setOnMouseClicked(e -> Navigator.go("home"));   // clic sur le logo = retour accueil
        Fx.hoverPop(logo);
        ThemeManager.darkProperty().addListener((o, was, isDark) -> logo.setImage(isDark ? dark : light));

        Node tabs = buildTabs();
        StackPane top = new StackPane(logo, tabs);
        StackPane.setAlignment(logo, Pos.CENTER_LEFT);
        StackPane.setAlignment(tabs, Pos.CENTER);
        top.getStyleClass().add("login-top");
        return top;
    }

    /** Sélecteur « Se connecter | S'inscrire » avec pastille qui glisse. */
    private Node buildTabs() {
        pill.getStyleClass().add("tab-pill");
        pill.setMinSize(TAB_W, TAB_H);
        pill.setPrefSize(TAB_W, TAB_H);
        pill.setMaxSize(TAB_W, TAB_H);

        for (Button b : new Button[]{tabLogin, tabSignup}) {
            b.getStyleClass().add("tab-btn");
            b.setPrefSize(TAB_W, TAB_H);
        }
        tabLogin.getStyleClass().add("active");
        tabLogin.setOnAction(e -> setMode(false));
        tabSignup.setOnAction(e -> setMode(true));

        StackPane tabs = new StackPane(pill, new HBox(tabLogin, tabSignup));
        StackPane.setAlignment(pill, Pos.CENTER_LEFT);
        tabs.getStyleClass().add("tabs");
        tabs.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        return tabs;
    }

    private void setMode(boolean signup) {
        if (signup == signupMode) return;
        signupMode = signup;

        tabLogin.getStyleClass().remove("active");
        tabSignup.getStyleClass().remove("active");
        (signup ? tabSignup : tabLogin).getStyleClass().add("active");

        TranslateTransition slide = new TranslateTransition(Duration.millis(260), pill);
        slide.setToX(signup ? TAB_W : 0);
        slide.setInterpolator(Interpolator.EASE_BOTH);
        slide.play();

        // Le formulaire s'efface, change, puis réapparaît
        FadeTransition out = new FadeTransition(Duration.millis(130), formHolder);
        out.setToValue(0);
        out.setOnFinished(e -> {
            formHolder.getChildren().setAll(buildForm(signup));
            FadeTransition in = new FadeTransition(Duration.millis(220), formHolder);
            in.setToValue(1);
            in.play();
        });
        out.play();
    }

    // ------------------------------------------------------------ Carte
    private Node buildCard() {
        formHolder.getChildren().add(buildForm(false));
        formHolder.setMinWidth(340);
        formHolder.setPrefWidth(340);
        formHolder.setMaxWidth(340);

        Circle glow = new Circle(170);
        glow.getStyleClass().add("glow");
        ImageView robot = Fx.img("/images/robot-login.png", 330);
        StackPane robotBox = new StackPane(glow, robot);
        Fx.bob(robotBox, 10, 2.4);
        robotWrap.getChildren().setAll(robotBox);

        // Effet parallaxe : le robot suit légèrement la souris
        card.setOnMouseMoved(e -> {
            double dx = (e.getX() - card.getWidth() / 2) / card.getWidth();
            double dy = (e.getY() - card.getHeight() / 2) / card.getHeight();
            robot.setTranslateX(dx * 28);
            robot.setTranslateY(dy * 18);
        });
        card.setOnMouseExited(e -> new Timeline(new KeyFrame(Duration.millis(400),
                new KeyValue(robot.translateXProperty(), 0),
                new KeyValue(robot.translateYProperty(), 0))).play());

        StackPane left = new StackPane(formHolder);
        StackPane right = new StackPane(robotWrap);
        left.setPrefWidth(0);
        right.setPrefWidth(0);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        card.getChildren().addAll(left, right);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("login-card");
        card.setPrefHeight(470);
        card.setMaxWidth(1000);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        StackPane center = new StackPane(card);
        center.setPadding(new Insets(30, 50, 50, 50));
        return center;
    }

    private void playIntro() {
        card.setOpacity(0);
        card.setTranslateY(40);
        FadeTransition cf = new FadeTransition(Duration.millis(550), card);
        cf.setToValue(1);
        TranslateTransition ct = new TranslateTransition(Duration.millis(550), card);
        ct.setToY(0);
        ct.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(cf, ct).play();

        robotWrap.setOpacity(0);
        robotWrap.setScaleX(0.8);
        robotWrap.setScaleY(0.8);
        FadeTransition rf = new FadeTransition(Duration.millis(500), robotWrap);
        rf.setToValue(1);
        ScaleTransition rs = new ScaleTransition(Duration.millis(500), robotWrap);
        rs.setToX(1);
        rs.setToY(1);
        rs.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition robotIn = new ParallelTransition(rf, rs);
        robotIn.setDelay(Duration.millis(250));
        robotIn.play();
    }

    // ------------------------------------------------------------ Formulaire
    private Node buildForm(boolean signup) {
        Label title = new Label(signup ? "Créer votre compte Clyro" : "Bienvenue chez Clyro");
        title.getStyleClass().add("form-title");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        VBox.setMargin(title, new Insets(0, 0, 10, 0));

        Field name = signup ? new Field("Entrer votre nom complet", "far-user", "Votre nom", false) : null;
        Field email = new Field("Entrer votre adresse mail", "far-envelope", "test@gmail.com", false);
        Field pass = new Field("Entrer votre mot de passe", "fas-lock", "••••••••••••", true);

        Label ask = new Label(signup ? "Vous avez déjà un compte ?" : "Vous n'avez pas de compte ?");
        ask.getStyleClass().add("form-hint");
        Label link = new Label(signup ? "Se connecter" : "S'inscrire");
        link.getStyleClass().add("form-link");
        link.setOnMouseClicked(e -> setMode(!signup));
        HBox switchLine = new HBox(5, ask, link);
        switchLine.setAlignment(Pos.CENTER);

        Button submit = new Button(signup ? "Créer le compte" : "Se connecter");
        submit.setGraphic(new FontIcon("fas-rocket"));
        submit.getStyleClass().add("btn-submit");
        submit.setMaxWidth(Double.MAX_VALUE);
        Fx.hoverPop(submit);
        submit.setOnAction(e -> submit(submit, name, email, pass, signup));

        // Touche Entrée dans un champ = clic sur le bouton
        email.input.setOnAction(e -> submit.fire());
        pass.input.setOnAction(e -> submit.fire());
        if (name != null) name.input.setOnAction(e -> submit.fire());

        VBox form = new VBox(18);
        form.setAlignment(Pos.CENTER_LEFT);
        form.getChildren().add(title);
        if (name != null) form.getChildren().add(name.node);
        form.getChildren().addAll(email.node, pass.node, new VBox(10, switchLine, submit));
        return form;
    }

    private void submit(Button btn, Field name, Field email, Field pass, boolean signup) {
        boolean ok = true;
        if (name != null && name.text().isBlank()) {
            name.fail("Veuillez saisir votre nom.");
            ok = false;
        }
        if (!EMAIL.matcher(email.text().trim()).matches()) {
            email.fail("Adresse mail invalide.");
            ok = false;
        }
        if (pass.text().length() < (signup ? 8 : 1)) {
            pass.fail(signup ? "8 caractères minimum." : "Veuillez saisir votre mot de passe.");
            ok = false;
        }
        if (!ok) return;

        // Simulation : à remplacer par ton vrai appel d'authentification.
        // Plus tard : Navigator.go("dashboard") après une connexion réussie.
        String original = btn.getText();
        btn.setDisable(true);
        btn.setText("Veuillez patienter…");
        PauseTransition load = new PauseTransition(Duration.millis(1200));
        load.setOnFinished(e -> {
            btn.setText("✓ Réussi (démo)");
            PauseTransition reset = new PauseTransition(Duration.millis(1500));
            reset.setOnFinished(x -> {
                btn.setText(original);
                btn.setDisable(false);
            });
            reset.play();
        });
        load.play();
    }

    // ------------------------------------------------------------ Champ de saisie
    /** Un champ : label + zone arrondie avec icône + message d'erreur. */
    private static final class Field {
        final VBox node = new VBox(8);
        final TextField input;
        private final HBox box = new HBox(10);
        private final Label error = new Label();

        Field(String label, String icon, String prompt, boolean password) {
            Label l = new Label(label);
            l.getStyleClass().add("field-label");

            FontIcon ic = new FontIcon(icon);
            ic.setIconSize(14);

            input = password ? new PasswordField() : new TextField();
            input.setPromptText(prompt);
            input.getStyleClass().add("input-field");

            box.getStyleClass().add("input-box");
            box.getChildren().add(ic);

            if (password) {
                // Version « texte visible » du mot de passe, synchronisée avec le PasswordField
                TextField shown = new TextField();
                shown.getStyleClass().add("input-field");
                shown.setPromptText(prompt);
                shown.setVisible(false);
                shown.textProperty().bindBidirectional(input.textProperty());

                StackPane editor = new StackPane(input, shown);
                HBox.setHgrow(editor, Priority.ALWAYS);

                FontIcon eye = new FontIcon("fas-eye");
                eye.setIconSize(14);
                Button eyeBtn = new Button();
                eyeBtn.setGraphic(eye);
                eyeBtn.getStyleClass().add("eye-btn");
                eyeBtn.setFocusTraversable(false);
                eyeBtn.setOnAction(e -> {
                    boolean show = !shown.isVisible();
                    shown.setVisible(show);
                    input.setVisible(!show);
                    eye.setIconLiteral(show ? "fas-eye-slash" : "fas-eye");
                    (show ? shown : input).requestFocus();
                });
                box.getChildren().addAll(editor, eyeBtn);
                trackFocus(input, shown);
            } else {
                HBox.setHgrow(input, Priority.ALWAYS);
                box.getChildren().add(input);
                trackFocus(input);
            }

            error.getStyleClass().add("field-error");
            error.setVisible(false);
            error.setManaged(false);
            input.textProperty().addListener((o, a, b) -> clearError());

            node.getChildren().addAll(l, box, error);
        }

        String text() { return input.getText() == null ? "" : input.getText(); }

        void fail(String message) {
            error.setText(message);
            error.setVisible(true);
            error.setManaged(true);
            box.pseudoClassStateChanged(ERROR, true);
            Fx.shake(box);
        }

        private void clearError() {
            if (!error.isVisible()) return;
            error.setVisible(false);
            error.setManaged(false);
            box.pseudoClassStateChanged(ERROR, false);
        }

        /** Allume le halo violet quand l'un des champs a le focus. */
        private void trackFocus(TextInputControl... fields) {
            for (TextInputControl f : fields) {
                f.focusedProperty().addListener((o, was, now) -> {
                    boolean any = false;
                    for (TextInputControl g : fields) any |= g.isFocused();
                    box.pseudoClassStateChanged(FOCUSED, any);
                });
            }
        }
    }
}
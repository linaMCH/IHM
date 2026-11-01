package com.clyro;

import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainApp extends Application {

    /** Un élément qui apparaît (fondu + glissement) quand il entre dans la fenêtre. */
    private record Reveal(Node node, double delay, Runnable after) {}

    private final List<Reveal> pending = new ArrayList<>();
    private Scene scene;
    private ScrollPane scroll;
    private VBox page;
    private boolean dark = false;
    private FontIcon themeIcon;
    private ImageView logoView;
    private Image logoLight, logoDark;
    private Node heroSection, codeSection, mentorSection, impactSection;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        loadFonts();

        heroSection = buildHero();
        codeSection = buildCodeSection();
        mentorSection = buildMentorSection();
        impactSection = buildImpactSection();
        Node footer = buildFooter();

        page = new VBox(heroSection, codeSection, mentorSection, impactSection, footer);
        page.getStyleClass().add("page");

        scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Défilement plus rapide à la molette
        page.setOnScroll(e -> {
            double range = page.getHeight() - scroll.getViewportBounds().getHeight();
            if (range <= 0) return;
            double d = e.getDeltaY() * 3 / range;
            scroll.setVvalue(Math.max(0, Math.min(1, scroll.getVvalue() - d)));
            e.consume();
        });

        BorderPane root = new BorderPane();
        root.setTop(buildNavbar());
        root.setCenter(scroll);

        scene = new Scene(root, 1100, 760);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setTitle("Clyro");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();

        scroll.vvalueProperty().addListener((o, a, b) -> checkReveals());
        scene.heightProperty().addListener((o, a, b) -> checkReveals());
        PauseTransition start = new PauseTransition(Duration.millis(150));
        start.setOnFinished(e -> checkReveals());
        start.play();
    }

    // ------------------------------------------------------------------ Polices
    private void loadFonts() {
        String[] fonts = {"Poppins-Regular", "Poppins-Bold", "JetBrainsMono-Regular", "JetBrainsMono-Bold"};
        for (String f : fonts) {
            Font.loadFont(getClass().getResourceAsStream("/fonts/" + f + ".ttf"), 12);
        }
    }

    // ------------------------------------------------------------------ Navbar
    private Node buildNavbar() {
        logoLight = loadImage("/images/logo.png");
        logoDark = loadImage("/images/logo-dark.png");
        logoView = new ImageView(logoLight);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);
        logoView.setFitHeight(42);

        Button accueil = navLink("Accueil", () -> scrollTo(heroSection));
        Button decouvrir = navLink("Découvrir", () -> scrollTo(codeSection));
        Button impact = navLink("Impact", () -> scrollTo(impactSection));

        themeIcon = new FontIcon("fas-moon");
        themeIcon.setIconSize(16);
        Button themeBtn = new Button();
        themeBtn.setGraphic(themeIcon);
        themeBtn.getStyleClass().add("icon-btn");
        themeBtn.setOnAction(e -> {
            toggleTheme();
            RotateTransition rt = new RotateTransition(Duration.millis(450), themeBtn);
            rt.setByAngle(360);
            rt.play();
        });

        Button login = new Button("Se connecter");
        login.getStyleClass().add("btn-outline-small");
        hoverPop(login);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox nav = new HBox(22, logoView, spacer, accueil, decouvrir, impact, themeBtn, login);
        nav.getStyleClass().add("navbar");
        return nav;
    }

    private Button navLink(String text, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-link");
        b.setOnAction(e -> action.run());
        return b;
    }

    private Image loadImage(String path) {
        var stream = getClass().getResourceAsStream(path);
        if (stream == null) throw new IllegalStateException("Image introuvable : " + path);
        return new Image(stream);
    }

    private void toggleTheme() {
        dark = !dark;
        var classes = scene.getRoot().getStyleClass();
        if (dark) classes.add("dark"); else classes.remove("dark");
        themeIcon.setIconLiteral(dark ? "fas-sun" : "fas-moon");
        logoView.setImage(dark ? logoDark : logoLight);
    }

    // ------------------------------------------------------------------ Hero
    private Node buildHero() {
        Text a = new Text("Deviens ");
        Text b = new Text("vraiment");
        Text c = new Text(" bon en programmation C !");
        a.getStyleClass().add("hero-title");
        b.getStyleClass().addAll("hero-title", "underlined");
        c.getStyleClass().add("hero-title");
        TextFlow title = new TextFlow(a, b, c);
        title.setMaxWidth(540);

        Label sub = new Label("Donner aux étudiants et aux enseignants les moyens d'apprendre, "
                + "d'enseigner et de collaborer autour de la programmation en C en utilisant l'IA.");
        sub.getStyleClass().add("hero-sub");
        sub.setWrapText(true);
        sub.setMaxWidth(500);

        Button signup = new Button("S'inscrire gratuitement");
        signup.getStyleClass().add("btn-primary");
        Button discover = new Button("Découvrir la plateforme");
        discover.getStyleClass().add("btn-secondary");
        discover.setOnAction(e -> scrollTo(codeSection));
        hoverPop(signup);
        hoverPop(discover);

        VBox left = new VBox(18, title, sub, new HBox(14, signup, discover));
        left.setAlignment(Pos.CENTER_LEFT);

        Circle glow = new Circle(190);
        glow.getStyleClass().add("glow");
        ImageView robot = img("/images/robot-wave.png", 330);
        StackPane robotBox = new StackPane(glow, robot);
        bob(robotBox, 12, 2.2);                       // flottement continu
        StackPane robotWrap = new StackPane(robotBox); // wrapper pour l'animation d'apparition

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox hero = new HBox(left, spacer, robotWrap);
        hero.setAlignment(Pos.CENTER);
        hero.getStyleClass().add("hero");

        // Effet parallaxe : le robot suit légèrement la souris
        hero.setOnMouseMoved(e -> {
            double dx = (e.getX() - hero.getWidth() / 2) / hero.getWidth();
            double dy = (e.getY() - hero.getHeight() / 2) / hero.getHeight();
            robot.setTranslateX(dx * 30);
            robot.setTranslateY(dy * 20);
        });
        hero.setOnMouseExited(e -> new Timeline(new KeyFrame(Duration.millis(400),
                new KeyValue(robot.translateXProperty(), 0),
                new KeyValue(robot.translateYProperty(), 0))).play());

        reveal(left, 0, null);
        reveal(robotWrap, 250, null);
        return hero;
    }

    // ------------------------------------------------------------------ Section code
    private Node buildCodeSection() {
        Text t = new Text("Deviens à l'aise avec le langage C");
        t.getStyleClass().add("section-title");
        TextFlow title = new TextFlow(t);
        title.setTextAlignment(TextAlignment.CENTER);

        List<TextFlow> lines = List.of(
                line(code("#INCLUDE ", "kw"), code("<STDIO.H>", "fn")),
                line(code("INT ", "fn"), code("MAIN ()", "plain")),
                line(code("{", "plain")),
                line(code("    INT ", "fn"), code("N= 0;", "kw")),
                line(code("    PRINTF", "fn"), code("(“HELLO CONNECT”);", "kw")),
                line(code("    RETURN 0;", "kw")),
                line(code("}", "plain")));
        lines.forEach(l -> l.setOpacity(0));

        Text file = new Text(">-algorithme.c");
        file.getStyleClass().add("code-title");
        VBox codeBox = new VBox(8);
        codeBox.getChildren().addAll(lines);
        VBox card = new VBox(20, file, codeBox);
        card.getStyleClass().add("code-card");
        card.setMaxWidth(520);

        FontIcon codeIcon = new FontIcon("fas-code");
        codeIcon.setIconSize(26);
        StackPane badgeCode = new StackPane(codeIcon);
        badgeCode.getStyleClass().add("badge-blue");

        Text brace = new Text("{}");
        brace.getStyleClass().add("brace-text");
        StackPane badgeBrace = new StackPane(brace);
        badgeBrace.getStyleClass().add("badge-dark");

        StackPane stack = new StackPane(card, badgeCode, badgeBrace);
        stack.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane.setAlignment(badgeCode, Pos.TOP_RIGHT);
        StackPane.setMargin(badgeCode, new Insets(-30, -30, 0, 0));
        StackPane.setAlignment(badgeBrace, Pos.BOTTOM_LEFT);
        StackPane.setMargin(badgeBrace, new Insets(0, 0, -30, -30));
        bob(badgeCode, 8, 1.8);
        bob(badgeBrace, 8, 2.4);

        VBox section = new VBox(50, title, stack);
        section.setAlignment(Pos.CENTER);
        section.getStyleClass().addAll("section", "section-alt");

        reveal(title, 0, null);
        // Les lignes de code apparaissent l'une après l'autre
        reveal(stack, 150, () -> {
            for (int i = 0; i < lines.size(); i++) {
                FadeTransition ft = new FadeTransition(Duration.millis(400), lines.get(i));
                ft.setToValue(1);
                ft.setDelay(Duration.millis(500 + i * 220L));
                ft.play();
            }
        });
        return section;
    }

    private Text code(String s, String cls) {
        Text x = new Text(s);
        x.getStyleClass().addAll("code-text", cls);
        return x;
    }

    private TextFlow line(Text... parts) {
        return new TextFlow(parts);
    }

    // ------------------------------------------------------------------ Section mentorat
    private Node buildMentorSection() {
        ImageView robot = img("/images/robot-book.png", 270);
        StackPane robotBox = new StackPane(robot);
        bob(robotBox, 10, 2.8);
        StackPane robotWrap = new StackPane(robotBox);

        Text l1 = new Text("Approfondis tes connaissances grâce au mentorat de l'IA, ");
        Text l2 = new Text("gratuitement.");
        l1.getStyleClass().add("mentor-title");
        l2.getStyleClass().addAll("mentor-title", "underlined");
        TextFlow title = new TextFlow(l1, l2);
        title.setMaxWidth(430);

        Label p = new Label("Découvre des façons nouvelles et enthousiastes d'aborder un exercice "
                + "en te faisant mentorer par l'intelligence artificielle");
        p.getStyleClass().add("mentor-text");
        p.setWrapText(true);
        p.setMaxWidth(430);

        VBox right = new VBox(18, title, p);
        right.setAlignment(Pos.CENTER_LEFT);

        HBox section = new HBox(70, robotWrap, right);
        section.setAlignment(Pos.CENTER);
        section.getStyleClass().add("section");

        reveal(robotWrap, 0, null);
        reveal(right, 200, null);
        return section;
    }

    // ------------------------------------------------------------------ Section chiffres + cartes
    private Node buildImpactSection() {
        Text i1 = new Text("Notre impact en ");
        Text i2 = new Text("chiffres");
        i1.getStyleClass().add("section-title");
        i2.getStyleClass().addAll("section-title", "underlined");
        TextFlow title = new TextFlow(i1, i2);
        title.setTextAlignment(TextAlignment.CENTER);

        HBox stats = new HBox(20,
                stat(1200, "Étudiants enregistrés", 0),
                stat(500, "Professeurs", 1),
                stat(5000, "Leçons étudiées", 2),
                stat(1500, "Exercices réalisés", 3));
        stats.setAlignment(Pos.CENTER);

        HBox features = new HBox(30,
                feature("fas-book-open", "COURS STRUCTURÉS", false, 0),
                feature("fas-chart-line", "SUIVI PERSONNALISÉ", true, 1),
                feature("fas-users", "ACCESSIBLE À TOUS", false, 2));
        features.setAlignment(Pos.CENTER);

        VBox section = new VBox(45, title, stats, features);
        section.setAlignment(Pos.CENTER);
        section.getStyleClass().addAll("section", "section-alt");

        reveal(title, 0, null);
        return section;
    }

    private Node stat(int target, String caption, int index) {
        Label num = new Label("+0");
        num.getStyleClass().add("stat-number");
        Label cap = new Label(caption);
        cap.getStyleClass().add("stat-label");
        VBox box = new VBox(6, num, cap);
        box.getStyleClass().add("stat-card");
        hover(box, 6, 1.04);
        reveal(box, index * 120, () -> countUp(num, target));
        return box;
    }

    private void countUp(Label label, int target) {
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.US);
        IntegerProperty v = new SimpleIntegerProperty(0);
        v.addListener((o, a, b) -> label.setText("+" + nf.format(b.intValue())));
        new Timeline(new KeyFrame(Duration.millis(1600),
                new KeyValue(v, target, Interpolator.EASE_OUT))).play();
    }

    private Node feature(String icon, String text, boolean main, int index) {
        FontIcon fi = new FontIcon(icon);
        fi.setIconSize(20);
        StackPane circle = new StackPane(fi);
        circle.getStyleClass().add("feature-icon");
        Label l = new Label(text);
        l.getStyleClass().add("feature-text");
        VBox card = new VBox(14, circle, l);
        card.getStyleClass().add("feature-card");
        if (main) card.getStyleClass().add("feature-card-main");
        hover(card, 10, 1.05);
        reveal(card, 300 + index * 140, null);
        return card;
    }

    // ------------------------------------------------------------------ Footer
    private Node buildFooter() {
        Label l = new Label("© 2026 Clyro || Tous droits réservés");
        l.getStyleClass().add("footer-text");
        StackPane footer = new StackPane(l);
        footer.getStyleClass().add("footer");
        return footer;
    }

    // ------------------------------------------------------------------ Utilitaires d'animation
    private ImageView img(String path, double height) {
        var stream = getClass().getResourceAsStream(path);
        if (stream == null) throw new IllegalStateException("Image introuvable : " + path);
        ImageView iv = new ImageView(new Image(stream));
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setFitHeight(height);
        return iv;
    }

    /** Flottement vertical infini. */
    private void bob(Node n, double dist, double seconds) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(seconds), n);
        tt.setFromY(0);
        tt.setToY(-dist);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    /** Léger agrandissement au survol (boutons). */
    private void hoverPop(Node n) {
        n.setOnMouseEntered(e -> animateTo(n, 0, 1.06));
        n.setOnMouseExited(e -> animateTo(n, 0, 1));
    }

    /** Soulèvement + agrandissement au survol (cartes). Le halo est géré en CSS (:hover). */
    private void hover(Node n, double lift, double scale) {
        n.setOnMouseEntered(e -> animateTo(n, -lift, scale));
        n.setOnMouseExited(e -> animateTo(n, 0, 1));
    }

    private void animateTo(Node n, double y, double s) {
        new Timeline(new KeyFrame(Duration.millis(180),
                new KeyValue(n.translateYProperty(), y, Interpolator.EASE_OUT),
                new KeyValue(n.scaleXProperty(), s, Interpolator.EASE_OUT),
                new KeyValue(n.scaleYProperty(), s, Interpolator.EASE_OUT))).play();
    }

    /** Prépare un élément invisible, il s'animera quand il sera visible à l'écran. */
    private void reveal(Node n, double delayMs, Runnable after) {
        n.setOpacity(0);
        n.setTranslateY(40);
        pending.add(new Reveal(n, delayMs, after));
    }

    private void checkReveals() {
        if (scene == null) return;
        Iterator<Reveal> it = pending.iterator();
        while (it.hasNext()) {
            Reveal r = it.next();
            Bounds b = r.node().localToScene(r.node().getBoundsInLocal());
            if (b.getMinY() < scene.getHeight() - 40) {
                it.remove();
                playReveal(r);
            }
        }
    }

    private void playReveal(Reveal r) {
        FadeTransition ft = new FadeTransition(Duration.millis(700), r.node());
        ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(700), r.node());
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition pt = new ParallelTransition(ft, tt);
        pt.setDelay(Duration.millis(r.delay()));
        pt.play();

        if (r.after() != null) {
            PauseTransition d = new PauseTransition(Duration.millis(r.delay()));
            d.setOnFinished(e -> r.after().run());
            d.play();
        }
    }

    /** Défilement fluide vers une section. */
    private void scrollTo(Node section) {
        double range = page.getHeight() - scroll.getViewportBounds().getHeight();
        if (range <= 0) return;
        double target = Math.max(0, Math.min(1, section.getBoundsInParent().getMinY() / range));
        new Timeline(new KeyFrame(Duration.millis(600),
                new KeyValue(scroll.vvalueProperty(), target, Interpolator.EASE_BOTH))).play();
    }
}

package myproject.icarus;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeroApp extends Application
{
    // ── State ────────────────────────────────────────────────────────
    private boolean darkMode = false;
    private ObservableList<VideoData> allResults = FXCollections.observableArrayList();
    private Stage primaryStage;

    // ── UI fields ────────────────────────────────────────────────────
    private TextField searchField;
    private Button    searchButton;
    private Button    darkModeBtn;
    private Button    exportBtn;
    private Label     statusLabel;
    private Label     titleLabel;
    private Label     subtitleLabel;
    private ProgressBar progressBar;
    private TableView<VideoData> resultsTable;
    private VBox      root;

    // Filter bar fields
    private TextField minViewsField;
    private ComboBox<String> sentimentFilter;
    private Button    applyFilterBtn;
    private Button    clearFilterBtn;
    private Label     resultCountLabel;

    // ── Colour constants ─────────────────────────────────────────────
    // Light theme
    private static final String LIGHT_BG        = "#F8F9FF";
    private static final String LIGHT_CARD      = "#FFFFFF";
    private static final String LIGHT_TEXT      = "#2E4A7A";
    private static final String LIGHT_SUBTEXT   = "#555555";
    private static final String LIGHT_BORDER    = "#DCE3F5";

    // Dark theme
    private static final String DARK_BG         = "#1A1A2E";
    private static final String DARK_CARD       = "#16213E";
    private static final String DARK_TEXT       = "#E0E0FF";
    private static final String DARK_SUBTEXT    = "#9090BB";
    private static final String DARK_BORDER     = "#2E3A5A";

    @Override
    public void start(Stage stage)
    {
        this.primaryStage = stage;

        // ── Title section ────────────────────────────────────────────
        titleLabel = new Label("Lumis");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 38));
        titleLabel.setTextFill(Color.web(LIGHT_TEXT));
        titleLabel.setOpacity(0);   // start invisible for fade-in

        subtitleLabel = new Label("Intelligent YouTube Video Recommendation System");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setTextFill(Color.web(LIGHT_SUBTEXT));
        subtitleLabel.setOpacity(0);

        // Dark mode toggle button
        darkModeBtn = new Button("🌙 Dark Mode");
        darkModeBtn.setStyle(buttonStyle("#6C757D"));
        darkModeBtn.setOnAction(e -> toggleDarkMode());

        HBox topRight = new HBox(darkModeBtn);
        topRight.setAlignment(Pos.CENTER_RIGHT);
        topRight.setPadding(new Insets(10, 20, 0, 0));

        VBox titleBox = new VBox(4, titleLabel, subtitleLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(10, 0, 10, 0));

        // ── Search bar ───────────────────────────────────────────────
        searchField = new TextField();
        searchField.setPromptText("Enter search topic  e.g.  Clash of Clans");
        searchField.setPrefWidth(440);
        searchField.setPrefHeight(42);
        searchField.setFont(Font.font("Arial", 15));
        searchField.setStyle(
                "-fx-background-color: white; -fx-border-color: #DCE3F5; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4 10 4 10;"
        );
        searchField.setOnAction(e -> onSearchClicked());

        searchButton = new Button("🔍  Search");
        searchButton.setPrefHeight(42);
        searchButton.setPrefWidth(120);
        searchButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        searchButton.setStyle(buttonStyle("#2E4A7A"));
        searchButton.setOnAction(e -> onSearchClicked());

        exportBtn = new Button("📥  Export CSV");
        exportBtn.setPrefHeight(42);
        exportBtn.setFont(Font.font("Arial", 13));
        exportBtn.setStyle(buttonStyle("#0F6E56"));
        exportBtn.setDisable(true);
        exportBtn.setOnAction(e -> onExportClicked());

        HBox searchBox = new HBox(10, searchField, searchButton, exportBtn);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setPadding(new Insets(0, 20, 8, 20));

        // ── Filter bar ───────────────────────────────────────────────
        Label filterLabel = new Label("Filters:");
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        filterLabel.setTextFill(Color.web(LIGHT_SUBTEXT));

        Label minViewsLabel = new Label("Min views:");
        minViewsLabel.setFont(Font.font("Arial", 12));
        minViewsLabel.setTextFill(Color.web(LIGHT_SUBTEXT));

        minViewsField = new TextField();
        minViewsField.setPromptText("e.g. 100000");
        minViewsField.setPrefWidth(110);
        minViewsField.setPrefHeight(32);
        minViewsField.setFont(Font.font("Arial", 12));
        minViewsField.setStyle(
                "-fx-border-color: #DCE3F5; -fx-border-radius: 4; " +
                        "-fx-background-radius: 4; -fx-padding: 2 6 2 6;"
        );

        Label sentimentLabel = new Label("Sentiment:");
        sentimentLabel.setFont(Font.font("Arial", 12));
        sentimentLabel.setTextFill(Color.web(LIGHT_SUBTEXT));

        sentimentFilter = new ComboBox<>();
        sentimentFilter.getItems().addAll("All", "Positive (≥1.2)", "Neutral (0.8–1.2)", "Negative (≤0.8)");
        sentimentFilter.setValue("All");
        sentimentFilter.setPrefHeight(32);
        sentimentFilter.setStyle("-fx-font-size: 12px;");

        applyFilterBtn = new Button("Apply");
        applyFilterBtn.setPrefHeight(32);
        applyFilterBtn.setFont(Font.font("Arial", 12));
        applyFilterBtn.setStyle(buttonStyle("#2E4A7A"));
        applyFilterBtn.setOnAction(e -> applyFilters());

        clearFilterBtn = new Button("Clear");
        clearFilterBtn.setPrefHeight(32);
        clearFilterBtn.setFont(Font.font("Arial", 12));
        clearFilterBtn.setStyle(buttonStyle("#6C757D"));
        clearFilterBtn.setOnAction(e -> clearFilters());

        resultCountLabel = new Label("");
        resultCountLabel.setFont(Font.font("Arial", 12));
        resultCountLabel.setTextFill(Color.web(LIGHT_SUBTEXT));

        HBox filterBar = new HBox(10,
                filterLabel,
                minViewsLabel, minViewsField,
                sentimentLabel, sentimentFilter,
                applyFilterBtn, clearFilterBtn,
                resultCountLabel
        );
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(0, 20, 6, 20));

        // ── Status + progress bar ────────────────────────────────────
        statusLabel = new Label("Enter a search term and press Search.");
        statusLabel.setFont(Font.font("Arial", 13));
        statusLabel.setTextFill(Color.web(LIGHT_SUBTEXT));

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(560);
        progressBar.setVisible(false);

        VBox statusBox = new VBox(6, statusLabel, progressBar);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(0, 20, 8, 20));

        // ── Results table ────────────────────────────────────────────
        resultsTable = buildResultsTable();
        VBox tableBox = new VBox(resultsTable);
        VBox.setVgrow(resultsTable, Priority.ALWAYS);
        tableBox.setPadding(new Insets(0, 20, 20, 20));

        // ── Root layout ──────────────────────────────────────────────
        root = new VBox(topRight, titleBox, searchBox, filterBar, statusBox, tableBox);
        VBox.setVgrow(tableBox, Priority.ALWAYS);
        root.setStyle("-fx-background-color: " + LIGHT_BG + ";");

        Scene scene = new Scene(root, 1150, 720);
        stage.setTitle("Lumis — YouTube Recommendation System");
        stage.setScene(scene);
        stage.show();

        // ── Animate title in after window opens ──────────────────────
        animateTitle();
    }

    // ─────────────────────────────────────────────────────────────────
    // ANIMATED TITLE
    // ─────────────────────────────────────────────────────────────────
    private void animateTitle()
    {
        // Title slides down and fades in
        TranslateTransition slide = new TranslateTransition(Duration.millis(600), titleLabel);
        slide.setFromY(-20);
        slide.setToY(0);

        FadeTransition fade = new FadeTransition(Duration.millis(800), titleLabel);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        slide.play();
        fade.play();

        // Subtitle fades in slightly after
        FadeTransition fadeSub = new FadeTransition(Duration.millis(800), subtitleLabel);
        fadeSub.setFromValue(0.0);
        fadeSub.setToValue(1.0);
        fadeSub.setDelay(Duration.millis(400));
        fadeSub.play();
    }

    // ─────────────────────────────────────────────────────────────────
    // DARK MODE TOGGLE
    // ─────────────────────────────────────────────────────────────────
    private void toggleDarkMode()
    {
        darkMode = !darkMode;

        if (darkMode)
        {
            root.setStyle("-fx-background-color: " + DARK_BG + ";");
            titleLabel.setTextFill(Color.web(DARK_TEXT));
            subtitleLabel.setTextFill(Color.web(DARK_SUBTEXT));
            statusLabel.setTextFill(Color.web(DARK_SUBTEXT));
            resultCountLabel.setTextFill(Color.web(DARK_SUBTEXT));
            darkModeBtn.setText("☀️ Light Mode");
            darkModeBtn.setStyle(buttonStyle("#E0A020"));
            searchField.setStyle(
                    "-fx-background-color: " + DARK_CARD + "; -fx-text-fill: " + DARK_TEXT + "; " +
                            "-fx-border-color: " + DARK_BORDER + "; -fx-border-radius: 6; " +
                            "-fx-background-radius: 6; -fx-padding: 4 10 4 10;"
            );
            resultsTable.setStyle(
                    "-fx-background-color: " + DARK_CARD + "; " +
                            "-fx-control-inner-background: " + DARK_CARD + "; " +
                            "-fx-table-cell-border-color: " + DARK_BORDER + "; " +
                            "-fx-text-fill: " + DARK_TEXT + ";"
            );
            // filter labels
            for (javafx.scene.Node n : ((HBox) root.getChildren().get(3)).getChildren())
            {
                if (n instanceof Label)
                    ((Label) n).setTextFill(Color.web(DARK_SUBTEXT));
            }
        }
        else
        {
            root.setStyle("-fx-background-color: " + LIGHT_BG + ";");
            titleLabel.setTextFill(Color.web(LIGHT_TEXT));
            subtitleLabel.setTextFill(Color.web(LIGHT_SUBTEXT));
            statusLabel.setTextFill(Color.web(LIGHT_SUBTEXT));
            resultCountLabel.setTextFill(Color.web(LIGHT_SUBTEXT));
            darkModeBtn.setText("🌙 Dark Mode");
            darkModeBtn.setStyle(buttonStyle("#6C757D"));
            searchField.setStyle(
                    "-fx-background-color: white; -fx-text-fill: black; " +
                            "-fx-border-color: #DCE3F5; -fx-border-radius: 6; " +
                            "-fx-background-radius: 6; -fx-padding: 4 10 4 10;"
            );
            resultsTable.setStyle("");
            for (javafx.scene.Node n : ((HBox) root.getChildren().get(3)).getChildren())
            {
                if (n instanceof Label)
                    ((Label) n).setTextFill(Color.web(LIGHT_SUBTEXT));
            }
        }

        // Refresh table rows so colour-coded cells repaint
        resultsTable.refresh();
    }

    // ─────────────────────────────────────────────────────────────────
    // FILTER BAR
    // ─────────────────────────────────────────────────────────────────
    private void applyFilters()
    {
        if (allResults.isEmpty()) return;

        ObservableList<VideoData> filtered = FXCollections.observableArrayList();

        // Parse min views
        int minViews = 0;
        try
        {
            String v = minViewsField.getText().trim().replace(",", "");
            if (!v.isEmpty()) minViews = Integer.parseInt(v);
        }
        catch (NumberFormatException ignored) {}

        String sentiment = sentimentFilter.getValue();

        for (VideoData v : allResults)
        {
            if (v.viewCount < minViews) continue;

            if (sentiment.equals("Positive (≥1.2)") && v.sentimentScore < 1.2) continue;
            if (sentiment.equals("Neutral (0.8–1.2)") &&
                    (v.sentimentScore < 0.8 || v.sentimentScore > 1.2)) continue;
            if (sentiment.equals("Negative (≤0.8)") && v.sentimentScore > 0.8) continue;

            filtered.add(v);
        }

        resultsTable.setItems(filtered);
        resultCountLabel.setText("Showing " + filtered.size() + " of " + allResults.size() + " videos");
    }

    private void clearFilters()
    {
        minViewsField.clear();
        sentimentFilter.setValue("All");
        resultsTable.setItems(allResults);
        resultCountLabel.setText("Showing " + allResults.size() + " videos");
    }

    // ─────────────────────────────────────────────────────────────────
    // EXPORT CSV
    // ─────────────────────────────────────────────────────────────────
    private void onExportClicked()
    {
        if (allResults.isEmpty()) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Save Results as CSV");
        fc.setInitialFileName("lumis_results.csv");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        File file = fc.showSaveDialog(primaryStage);
        if (file == null) return;

        try (FileWriter fw = new FileWriter(file))
        {
            fw.write("Rank,Video Title,Channel,Video URL,Views,Likes,Comment Count,Sentiment Score,Recommendation Score,Published At\n");
            int rank = 1;
            for (VideoData v : allResults)
            {
                fw.write(String.format("%d,\"%s\",\"%s\",%s,%d,%d,%d,%.4f,%.4f,%s\n",
                        rank++,
                        v.videoTitle.replace("\"", "'"),
                        v.channel.channelName.replace("\"", "'"),
                        v.videoUrl,
                        v.viewCount,
                        v.likeCount,
                        v.commentCount,
                        v.sentimentScore,
                        v.recommendationScore,
                        v.publishedAt
                ));
            }
            statusLabel.setText("✅ Exported " + allResults.size() + " results to " + file.getName());
        }
        catch (IOException e)
        {
            statusLabel.setText("Export failed: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // SCORE BREAKDOWN PIE CHART POPUP
    // ─────────────────────────────────────────────────────────────────
    private void showScoreBreakdown(VideoData v)
    {
        // Normalise each component using the same weights as RecommendationScorer
        // We show the weighted contribution of each factor
        double maxViews = allResults.stream().mapToDouble(x -> x.viewCount).max().orElse(1);
        double maxLikes = allResults.stream().mapToDouble(x -> x.likeCount).max().orElse(1);
        double maxComments = allResults.stream().mapToDouble(x -> x.commentCount).max().orElse(1);

        double viewContrib     = 0.35 * (v.viewCount    / maxViews);
        double likeContrib     = 0.25 * (v.likeCount < 0 ? 0 : v.likeCount / maxLikes);
        double commentContrib  = 0.15 * (v.commentCount / maxComments);
        double sentimentContrib = 0.25 * (v.sentimentScore / 2.0);

        PieChart chart = new PieChart();
        chart.getData().addAll(
                new PieChart.Data(String.format("Views (%.1f%%)", viewContrib * 100),      viewContrib),
                new PieChart.Data(String.format("Likes (%.1f%%)", likeContrib * 100),      likeContrib),
                new PieChart.Data(String.format("Comments (%.1f%%)", commentContrib * 100), commentContrib),
                new PieChart.Data(String.format("Sentiment (%.1f%%)", sentimentContrib * 100), sentimentContrib)
        );
        chart.setTitle("Score Breakdown");
        chart.setLabelsVisible(true);
        chart.setPrefSize(400, 340);

        Label titleLbl = new Label(v.videoTitle);
        titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(400);

        Label scoreLbl = new Label(String.format(
                "Total Score: %.3f  |  Sentiment: %.2f  |  Views: %,d  |  Likes: %s",
                v.recommendationScore,
                v.sentimentScore,
                v.viewCount,
                v.likeCount == -1 ? "hidden" : String.format("%,d", v.likeCount)
        ));
        scoreLbl.setFont(Font.font("Arial", 12));
        scoreLbl.setTextFill(Color.web("#555555"));
        scoreLbl.setWrapText(true);

        VBox content = new VBox(10, titleLbl, chart, scoreLbl);
        content.setPadding(new Insets(20));
        content.setStyle(darkMode ?
                "-fx-background-color: " + DARK_CARD + ";" :
                "-fx-background-color: white;"
        );

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(primaryStage);
        popup.setTitle("Score Breakdown — " + v.channel.channelName);
        popup.setScene(new Scene(content, 440, 460));
        popup.show();
    }

    // ─────────────────────────────────────────────────────────────────
    // TABLE BUILDER
    // ─────────────────────────────────────────────────────────────────
    private TableView<VideoData> buildResultsTable()
    {
        TableView<VideoData> table = new TableView<>();
        table.setPlaceholder(new Label("Results will appear here after search."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ── Colour-coded rows by sentiment ───────────────────────────
        table.setRowFactory(tv ->
        {
            TableRow<VideoData> row = new TableRow<>()
            {
                @Override
                protected void updateItem(VideoData item, boolean empty)
                {
                    super.updateItem(item, empty);
                    if (empty || item == null)
                    {
                        setStyle("");
                        return;
                    }
                    if (item.sentimentScore >= 1.2)
                        setStyle("-fx-background-color: #F0FAF0;");   // soft green
                    else if (item.sentimentScore <= 0.8)
                        setStyle("-fx-background-color: #FFF5F5;");   // soft red
                    else
                        setStyle("");                                   // neutral white
                }
            };

            // Click row to show score breakdown popup
            row.setOnMouseClicked(e ->
            {
                if (e.getClickCount() == 2 && !row.isEmpty())
                    showScoreBreakdown(row.getItem());
            });

            return row;
        });

        // ── Rank column ──────────────────────────────────────────────
        TableColumn<VideoData, Void> rankCol = new TableColumn<>("#");
        rankCol.setMaxWidth(40);
        rankCol.setMinWidth(40);
        rankCol.setCellFactory(col -> new TableCell<>()
        {
            @Override
            protected void updateItem(Void item, boolean empty)
            {
                super.updateItem(item, empty);
                setText(empty ? "" : String.valueOf(getIndex() + 1));
                setAlignment(Pos.CENTER);
                setFont(Font.font("Arial", FontWeight.BOLD, 13));
            }
        });

        // ── Title column ─────────────────────────────────────────────
        TableColumn<VideoData, String> titleCol = new TableColumn<>("Video Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("videoTitle"));
        titleCol.setMinWidth(300);
        titleCol.setCellFactory(col -> new TableCell<>()
        {
            @Override
            protected void updateItem(String item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(""); return; }
                setText(item);
                setWrapText(true);
                setFont(Font.font("Arial", 13));
            }
        });

        // ── Channel column ───────────────────────────────────────────
        TableColumn<VideoData, String> channelCol = new TableColumn<>("Channel");
        channelCol.setMinWidth(150);
        channelCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().channel.channelName));

        // ── Views column ─────────────────────────────────────────────
        TableColumn<VideoData, Integer> viewsCol = new TableColumn<>("Views");
        viewsCol.setCellValueFactory(new PropertyValueFactory<>("viewCount"));
        viewsCol.setMinWidth(90);
        viewsCol.setCellFactory(col -> new TableCell<>()
        {
            @Override
            protected void updateItem(Integer item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty || item == null || item == -1) { setText("—"); return; }
                setText(String.format("%,d", item));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        // ── Likes column ─────────────────────────────────────────────
        TableColumn<VideoData, Integer> likesCol = new TableColumn<>("Likes");
        likesCol.setCellValueFactory(new PropertyValueFactory<>("likeCount"));
        likesCol.setMinWidth(80);
        likesCol.setCellFactory(col -> new TableCell<>()
        {
            @Override
            protected void updateItem(Integer item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty || item == null || item == -1) { setText("—"); return; }
                setText(String.format("%,d", item));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        // ── Sentiment column — colour coded text + emoji ─────────────
        TableColumn<VideoData, Double> sentimentCol = new TableColumn<>("Sentiment");
        sentimentCol.setCellValueFactory(new PropertyValueFactory<>("sentimentScore"));
        sentimentCol.setMinWidth(100);
        sentimentCol.setCellFactory(col -> new TableCell<>()
        {
            @Override
            protected void updateItem(Double item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(""); setStyle(""); return; }

                String emoji = item >= 1.2 ? " 😊" : item <= 0.8 ? " 😟" : " 😐";
                setText(String.format("%.2f", item) + emoji);
                setAlignment(Pos.CENTER);

                if (item >= 1.2)
                    setStyle("-fx-text-fill: #1a7a1a; -fx-font-weight: bold;");
                else if (item <= 0.8)
                    setStyle("-fx-text-fill: #cc2200; -fx-font-weight: bold;");
                else
                    setStyle("-fx-text-fill: #555555;");
            }
        });

        // ── Score column — colour coded ──────────────────────────────
        TableColumn<VideoData, Double> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("recommendationScore"));
        scoreCol.setMinWidth(80);
        scoreCol.setCellFactory(col -> new TableCell<>()
        {
            @Override
            protected void updateItem(Double item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(""); setStyle(""); return; }
                setText(String.format("%.3f", item));
                setAlignment(Pos.CENTER);
                if (item >= 0.5)
                    setStyle("-fx-text-fill: #1a7a1a; -fx-font-weight: bold;");
                else if (item >= 0.2)
                    setStyle("-fx-text-fill: #2E4A7A; -fx-font-weight: bold;");
                else
                    setStyle("-fx-text-fill: #888888;");
            }
        });

        // ── Breakdown column — opens pie chart ───────────────────────
        TableColumn<VideoData, Void> breakdownCol = new TableColumn<>("Breakdown");
        breakdownCol.setMinWidth(90);
        breakdownCol.setMaxWidth(90);
        breakdownCol.setCellFactory(col -> new TableCell<>()
        {
            private final Button btn = new Button("📊 View");
            {
                btn.setStyle(
                        "-fx-background-color: #2E4A7A; -fx-text-fill: white; " +
                                "-fx-font-size: 11px; -fx-background-radius: 4; " +
                                "-fx-cursor: hand; -fx-padding: 3 6 3 6;"
                );
                btn.setOnAction(e ->
                {
                    VideoData v = getTableView().getItems().get(getIndex());
                    showScoreBreakdown(v);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty)
            {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // ── Link column — opens YouTube ──────────────────────────────
        TableColumn<VideoData, String> linkCol = new TableColumn<>("Link");
        linkCol.setMinWidth(80);
        linkCol.setMaxWidth(80);
        linkCol.setCellValueFactory(new PropertyValueFactory<>("videoUrl"));
        linkCol.setCellFactory(col -> new TableCell<>()
        {
            private final Button btn = new Button("▶ Open");
            {
                btn.setStyle(
                        "-fx-background-color: #FF0000; -fx-text-fill: white; " +
                                "-fx-font-size: 11px; -fx-background-radius: 4; " +
                                "-fx-cursor: hand; -fx-padding: 3 6 3 6;"
                );
                btn.setOnAction(e ->
                {
                    String url = getItem();
                    if (url != null && !url.equals("null"))
                    {
                        try
                        {
                            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                        }
                        catch (Exception ex)
                        {
                            System.out.println("Could not open browser: " + ex.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String url, boolean empty)
            {
                super.updateItem(url, empty);
                setGraphic(empty || url == null || url.equals("null") ? null : btn);
            }
        });

        table.getColumns().addAll(
                rankCol, titleCol, channelCol, viewsCol,
                likesCol, sentimentCol, scoreCol, breakdownCol, linkCol
        );

        // Hint label at bottom
        Label hint = new Label("💡 Double-click any row to view score breakdown");
        hint.setFont(Font.font("Arial", 11));
        hint.setTextFill(Color.web("#888888"));
        table.setPlaceholder(hint);

        return table;
    }

    // ─────────────────────────────────────────────────────────────────
    // SEARCH
    // ─────────────────────────────────────────────────────────────────
    private void onSearchClicked()
    {
        String query = searchField.getText().trim();
        if (query.isEmpty())
        {
            statusLabel.setText("Please enter a search term.");
            return;
        }

        searchButton.setDisable(true);
        searchField.setDisable(true);
        exportBtn.setDisable(true);
        resultsTable.getItems().clear();
        allResults.clear();
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        statusLabel.setText("🔍 Collecting videos from YouTube...");
        resultCountLabel.setText("");

        ExecutorService bgThread = Executors.newSingleThreadExecutor();
        bgThread.submit(() ->
        {
            try
            {
                Platform.runLater(() ->
                        statusLabel.setText("🧠 Running NLP sentiment analysis — please wait...")
                );

                List<VideoData> videos = YoutubeApiClient.search(query);

                Platform.runLater(() ->
                {
                    allResults.setAll(videos);
                    resultsTable.setItems(allResults);
                    progressBar.setVisible(false);
                    statusLabel.setText("✅ Done — showing top " + videos.size() +
                            " recommendations for \"" + query + "\"   |   " +
                            "Double-click any row for score breakdown");
                    resultCountLabel.setText("Showing " + videos.size() + " videos");
                    searchButton.setDisable(false);
                    searchField.setDisable(false);
                    exportBtn.setDisable(false);
                });
            }
            catch (Exception e)
            {
                Platform.runLater(() ->
                {
                    statusLabel.setText("❌ Error: " + e.getMessage());
                    progressBar.setVisible(false);
                    searchButton.setDisable(false);
                    searchField.setDisable(false);
                });
            }
        });
        bgThread.shutdown();
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────
    private String buttonStyle(String hex)
    {
        return "-fx-background-color: " + hex + "; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 14 6 14;";
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
package myproject.icarus;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeroApp extends Application
{
    // UI components we need to access across methods
    private TextField searchField;
    private Button searchButton;
    private Label statusLabel;
    private ProgressBar progressBar;
    private TableView<VideoData> resultsTable;
    private Label titleLabel;

    @Override
    public void start(Stage stage)
    {
        // ── Top bar: title ───────────────────────────────────────────
        titleLabel = new Label("Tero");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web("#2E4A7A"));

        Label subtitleLabel = new Label("Intelligent YouTube Video Recommendation System");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setTextFill(Color.web("#555555"));

        VBox titleBox = new VBox(4, titleLabel, subtitleLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(20, 0, 10, 0));

        // ── Search bar ───────────────────────────────────────────────
        searchField = new TextField();
        searchField.setPromptText("Enter search topic  e.g. Clash of Clans");
        searchField.setPrefWidth(420);
        searchField.setPrefHeight(40);
        searchField.setFont(Font.font("Arial", 15));

        searchButton = new Button("Search");
        searchButton.setPrefHeight(40);
        searchButton.setPrefWidth(100);
        searchButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        searchButton.setStyle(
                "-fx-background-color: #2E4A7A; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-cursor: hand;"
        );
        searchButton.setOnAction(e -> onSearchClicked());

        // Allow pressing Enter in the search field
        searchField.setOnAction(e -> onSearchClicked());

        HBox searchBox = new HBox(10, searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setPadding(new Insets(0, 20, 10, 20));

        // ── Status + progress bar ────────────────────────────────────
        statusLabel = new Label("Enter a search term and press Search.");
        statusLabel.setFont(Font.font("Arial", 13));
        statusLabel.setTextFill(Color.web("#555555"));

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(540);
        progressBar.setVisible(false);

        VBox statusBox = new VBox(6, statusLabel, progressBar);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(0, 20, 10, 20));

        // ── Results table ────────────────────────────────────────────
        resultsTable = buildResultsTable();

        VBox tableBox = new VBox(resultsTable);
        VBox.setVgrow(resultsTable, Priority.ALWAYS);
        tableBox.setPadding(new Insets(0, 20, 20, 20));

        // ── Root layout ──────────────────────────────────────────────
        VBox root = new VBox(titleBox, searchBox, statusBox, tableBox);
        VBox.setVgrow(tableBox, Priority.ALWAYS);
        root.setStyle("-fx-background-color: #F8F9FF;");

        Scene scene = new Scene(root, 1100, 680);
        stage.setTitle("Tero — YouTube Recommendation System");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Builds the results TableView with all columns.
     */
    private TableView<VideoData> buildResultsTable()
    {
        TableView<VideoData> table = new TableView<>();
        table.setPlaceholder(new Label("Results will appear here after search."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Rank column
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
            }
        });

        // Title column
        TableColumn<VideoData, String> titleCol = new TableColumn<>("Video Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("videoTitle"));
        titleCol.setMinWidth(320);
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

        // Channel column
        TableColumn<VideoData, String> channelCol = new TableColumn<>("Channel");
        channelCol.setMinWidth(160);
        channelCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().channel.channelName));

        // Views column
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
                // Format with commas e.g. 1,234,567
                setText(String.format("%,d", item));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        // Likes column
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

        // Sentiment column — colour coded
        TableColumn<VideoData, Double> sentimentCol = new TableColumn<>("Sentiment");
        sentimentCol.setCellValueFactory(new PropertyValueFactory<>("sentimentScore"));
        sentimentCol.setMinWidth(90);
        sentimentCol.setCellFactory(col -> new TableCell<>()
        {
            @Override
            protected void updateItem(Double item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(""); setStyle(""); return; }
                setText(String.format("%.2f", item));
                setAlignment(Pos.CENTER);
                // Green for positive, red for negative, grey for neutral
                if (item >= 1.2)
                    setStyle("-fx-text-fill: #1a7a1a; -fx-font-weight: bold;");
                else if (item <= 0.8)
                    setStyle("-fx-text-fill: #cc2200; -fx-font-weight: bold;");
                else
                    setStyle("-fx-text-fill: #555555;");
            }
        });

        // Recommendation score column — colour coded
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
                    setStyle("-fx-text-fill: #555555;");
            }
        });

        TableColumn<VideoData, String> linkCol = new TableColumn<>("Link");
        linkCol.setMinWidth(70);
        linkCol.setMaxWidth(70);
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
                            java.awt.Desktop.getDesktop().browse(
                                    new java.net.URI(url)
                            );
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
                likesCol, sentimentCol, scoreCol, linkCol   // ← linkCol added here
        );

        return table;
    }

    /**
     * Called when the Search button is pressed.
     * Runs the pipeline on a background thread so the UI stays responsive.
     */
    private void onSearchClicked()
    {
        String query = searchField.getText().trim();
        if (query.isEmpty())
        {
            statusLabel.setText("Please enter a search term.");
            return;
        }

        // Lock UI during search
        searchButton.setDisable(true);
        searchField.setDisable(true);
        resultsTable.getItems().clear();
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        statusLabel.setText("Fetching videos from YouTube...");

        // Run on background thread — never block the JavaFX thread
        ExecutorService bgThread = Executors.newSingleThreadExecutor();
        bgThread.submit(() ->
        {
            try
            {
                Platform.runLater(() -> statusLabel.setText("Collecting videos..."));

                List<VideoData> videos = YoutubeApiClient.search(query);

                Platform.runLater(() -> statusLabel.setText("Running NLP sentiment analysis..."));

                // Update UI on JavaFX thread
                Platform.runLater(() ->
                {
                    ObservableList<VideoData> data = FXCollections.observableArrayList(videos);
                    resultsTable.setItems(data);
                    progressBar.setVisible(false);
                    statusLabel.setText(
                            "Done — showing top " + videos.size() +
                                    " recommendations for \"" + query + "\""
                    );
                    searchButton.setDisable(false);
                    searchField.setDisable(false);
                });
            }
            catch (Exception e)
            {
                Platform.runLater(() ->
                {
                    statusLabel.setText("Error: " + e.getMessage());
                    progressBar.setVisible(false);
                    searchButton.setDisable(false);
                    searchField.setDisable(false);
                });
            }
        });
        bgThread.shutdown();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
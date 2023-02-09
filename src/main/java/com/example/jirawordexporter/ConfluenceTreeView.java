package com.example.jirawordexporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ConfluenceTreeView extends Application {
    private static final String spaceKey = "TIKA";
    private static final int limit = 500;

    @Override
    public void start(Stage stage) {
        TreeView<String> treeView = new TreeView<>();
        treeView.setRoot(getPages(spaceKey));
        treeView.setShowRoot(false);

        StackPane root = new StackPane();
        root.getChildren().add(treeView);

        Scene scene = new Scene(root, 800, 600);

        stage.setScene(scene);
        stage.setTitle("Confluence Pages");
        stage.show();
    }

    private TreeItem<String> getPages(String spaceKey) {
        int start = 0;
        List<JsonObject> pages = new ArrayList<>();

        while (true) {
            try {
                URL url = new URL("https://cwiki.apache.org/confluence/rest/api/content?spaceKey=" + spaceKey + "&start=" + start + "&limit=" + limit);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();

                JsonObject response = new JsonParser().parse(content.toString()).getAsJsonObject();
                JsonArray pageArray = response.getAsJsonArray("results");

                if (pageArray.size() == 0) {
                    break;
                }

                for (int i = 0; i < pageArray.size(); i++) {
                    JsonObject page = pageArray.get(i).getAsJsonObject();
                    pages.add(page);
                }

                start += limit;
            } catch (IOException e) {
                System.err.println("An error occurred while retrieving pages: " + e.getMessage());
                break;
            }
        }

        Map<String, TreeItem<String>> parentMap = new TreeMap<>();
        TreeItem<String> root = new TreeItem<>("Pages");
        parentMap.put("", root);

        for (JsonObject page : pages) {
            JsonElement parentElement = page.get("_links").getAsJsonObject().get("parent");
            String parentId = "";
            if (parentElement != null && parentElement.isJsonObject()) {
                parentId = parentElement.getAsJsonObject().get("id").getAsString();
            }
            String title = page.get("title").getAsString();

            TreeItem<String> parent = parentMap.get(parentId);
            if (parent == null) {
                parent = root;
            }

            TreeItem<String> child = new TreeItem<>(title);
            parent.getChildren().add(child);
            parentMap.put(page.get("id").getAsString(), child);
        }

        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }
}


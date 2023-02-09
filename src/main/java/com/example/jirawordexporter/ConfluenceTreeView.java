package com.example.jirawordexporter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.google.gson.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ConfluenceTreeView extends Application {
    private static final String spaceKey = "TIKA";
    private Map<String, TreeItem<String>> parentMap = new TreeMap<>();
    private TreeItem<String> root = new TreeItem<>("Pages");

    @Override
    public void start(Stage stage) throws IOException {
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

    private TreeItem<String> getPages(String spaceKey) throws IOException {
        int start = 0;
        List<JsonObject> pages = new ArrayList<>();

        URL url = new URL("https://cwiki.apache.org/confluence/pages/children.action?spaceKey="+spaceKey+"&node=root");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        InputStreamReader reader = new InputStreamReader(con.getInputStream());
        JsonArray pageArray = new Gson().fromJson(reader, JsonArray.class);
        for (int i = 0; i < pageArray.size(); i++) {
            JsonObject page = pageArray.get(i).getAsJsonObject();
            pages.add(page);
        }
        reader.close();
        con.disconnect();

        parentMap.put("", root);

        for (JsonObject page : pages) {
            processPages(page,"");
            processChildren(page.get("pageId").getAsString(), page.get("text").getAsString());
        }

        return root;
    }

    private void processPages(JsonObject page, String parentId) {
        String pageId = page.get("pageId").getAsString();
        String title = page.get("text").getAsString();
        TreeItem<String> parent = parentMap.get(parentId);
        if (parent == null) {
            parent = root;
        }
        TreeItem<String> child = new TreeItem<>(title);
        parent.getChildren().add(child);
        parentMap.put(page.get("text").getAsString(), child);
    }


    public static void main(String[] args) {
        launch(args);
    }
    private void processChildren(String parentId, String parentName) throws IOException {
        List<JsonObject> pages;
        pages = getChildIds(parentId);
        for (JsonObject page : pages) {
            processPages(page,parentName);
            processChildren(page.get("pageId").getAsString(), page.get("text").getAsString());
        }
    }

    private List<JsonObject> getChildIds(String parentId) throws IOException {
        List<JsonObject> pages = new ArrayList<>();
        URL url = new URL("https://cwiki.apache.org/confluence/pages/children.action?pageId="+parentId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        InputStreamReader reader = new InputStreamReader(con.getInputStream());
        JsonArray pageArray = new Gson().fromJson(reader, JsonArray.class);
        for (int i = 0; i < pageArray.size(); i++) {
            JsonObject page = pageArray.get(i).getAsJsonObject();
            pages.add(page);
        }
        reader.close();
        con.disconnect();

        return pages;
    }
}


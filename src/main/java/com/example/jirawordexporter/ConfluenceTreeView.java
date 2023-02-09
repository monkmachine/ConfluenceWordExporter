package com.example.jirawordexporter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import org.controlsfx.control.CheckTreeView;

public class ConfluenceTreeView extends Application {
    private static final String spaceKey = "TIKA";
    private Map<String, CheckBoxTreeItem<String>> parentMap = new TreeMap<>();
    private CheckBoxTreeItem<String> root = new CheckBoxTreeItem<>("Pages");
    private HashMap<String,String> pagesIds = new HashMap<>();

    @Override
    public void start(Stage stage) throws IOException {
        CheckTreeView<String> treeView = new CheckTreeView<>(getPages(spaceKey));
        //treeView.setRoot(getPages(spaceKey));

        treeView.setShowRoot(false);
        VBox vBox = new VBox();
        Button playButton = new Button("Export");
        ObservableList list = vBox.getChildren();
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
            ConfluenceProcessor cp = new ConfluenceProcessor();
                ObservableList<TreeItem<String>> ls = treeView.getCheckModel().getCheckedItems();
                System.out.println(ls);
                for(TreeItem t : ls){
                    String pageId = pagesIds.get(t.getValue());

                try {
                    cp.ExportFromConfluence(pageId);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                }
            }
        });
        list.addAll( playButton);
        VBox vBox1 = new VBox();
        ObservableList list1 = vBox.getChildren();
        list1.add(treeView);
        Pane root = new Pane();
        root.getChildren().addAll(vBox,vBox1);
        Scene scene = new Scene(root, 800, 600);

        stage.setScene(scene);
        stage.setTitle("Confluence Pages");
        stage.show();
    }

    private CheckBoxTreeItem<String> getPages(String spaceKey) throws IOException {
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
        CheckBoxTreeItem<String> parent = parentMap.get(parentId);
        if (parent == null) {
            parent = root;
        }
        CheckBoxTreeItem<String> child = new CheckBoxTreeItem<>(title);
        parent.getChildren().add(child);
        pagesIds.put(title,pageId);
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
        URL url = new URL("https://cwiki.apache.org/confluence/pages/children.action?pageId=" + parentId);
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


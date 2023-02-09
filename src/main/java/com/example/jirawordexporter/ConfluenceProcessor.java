package com.example.jirawordexporter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ConfluenceProcessor {
    public void ExportFromConfluence(String pageId) throws IOException {
        URL url = new URL("https://cwiki.apache.org/confluence/exportword?pageId="+pageId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String fileName = java.net.URLDecoder.decode(con.getHeaderField("Content-Disposition").substring(con.getHeaderField("Content-Disposition").indexOf("''")+2), "UTF-8");
        Files.copy(con.getInputStream(), Path.of("g:/temp/"+fileName.replace(";","")), new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING});

    }
}

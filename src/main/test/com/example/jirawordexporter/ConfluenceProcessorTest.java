package com.example.jirawordexporter;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ConfluenceProcessorTest {

    ConfluenceProcessor cp = new ConfluenceProcessor();
    @Test
    void test() throws IOException {
        cp.ExportFromConfluence("109454039");
    }

}
package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() throws IOException {
        Path path = Paths.get("src/main/resources/apartments.xlsx");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        assertTrue(Files.exists(path));
        Files.delete(path);

    }
}

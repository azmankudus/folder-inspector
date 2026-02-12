package dev.ayam.folderinspector.scanner.dummy;

import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.core.plugin.Scanner;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class DummyScanner implements Scanner {

  private final Random random = new Random();

  @Override
  public Stream<Item> scan(Path rootPath) throws IOException {
    List<Item> items = new ArrayList<>();
    // Generate some dummy data
    // Root folder
    items.add(createDummyItem(rootPath.toString(), ".", "directory", 0));

    // Some subfolders
    for (int i = 0; i < 5; i++) {
      String subFolderName = "folder_" + i;
      items.add(createDummyItem(rootPath.resolve(subFolderName).toString(), subFolderName, "directory", 0));

      // Files in subfolder
      for (int j = 0; j < random.nextInt(10) + 5; j++) {
        String fileName = "file_" + i + "_" + j + ".txt";
        long size = random.nextInt(1024 * 1024 * 10); // up to 10MB
        items.add(createDummyItem(rootPath.resolve(subFolderName).resolve(fileName).toString(),
            subFolderName + "/" + fileName, "file", size));
      }
    }

    // Files in root
    for (int i = 0; i < 10; i++) {
      String fileName = "root_file_" + i + ".log";
      long size = random.nextInt(1024 * 50); // up to 50KB
      items.add(createDummyItem(rootPath.resolve(fileName).toString(), fileName, "file", size));
    }

    return items.stream();
  }

  private Item createDummyItem(String absolutePath, String relativePath, String type, long size) {
    Instant now = Instant.now();
    // Randomize dates slightly
    Instant modified = now.minusSeconds(random.nextInt(60 * 60 * 24 * 30)); // up to 30 days ago
    Instant created = modified.minusSeconds(random.nextInt(60 * 60 * 24));

    return new Item(
        absolutePath,
        relativePath,
        type,
        size,
        modified,
        created,
        "dummy-user",
        "dummy-group",
        "rw-r--r--",
        null);
  }

  @Override
  public String getName() {
    return "dummy";
  }
}

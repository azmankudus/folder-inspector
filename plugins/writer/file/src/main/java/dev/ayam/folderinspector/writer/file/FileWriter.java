package dev.ayam.folderinspector.writer.file;

import dev.ayam.folderinspector.core.plugin.Writer;
import dev.ayam.folderinspector.writer.file.utility.StringResource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWriter implements Writer {

  private static final Logger logger = LoggerFactory.getLogger(FileWriter.class);
  private static final String DEFAULT_FILE_NAME = "report.txt";

  @Override
  public String write(java.util.stream.Stream<String> content) {
    if (content == null) {
      return "No content to write"; // Or use utility resource
    }

    try {
      java.io.BufferedWriter writer = Files.newBufferedWriter(Paths.get(DEFAULT_FILE_NAME),
          StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

      try (writer) {
        content.forEach(line -> {
          try {
            writer.write(line);
            writer.newLine();
          } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
          }
        });
      }
      return "Written to " + DEFAULT_FILE_NAME;
    } catch (IOException | java.io.UncheckedIOException e) {
      logger.error("Failed to write to file", e);
      return "Failed to write to " + DEFAULT_FILE_NAME + ": " + e.getMessage();
    }
  }

  @Override
  public String getName() {
    return StringResource.NAME;
  }
}

package dev.ayam.folderinspector.core.plugin;

import dev.ayam.folderinspector.core.model.Item;
import java.util.List;

/**
 * Interface for formatting a list of items into a string representation.
 */
public interface Formatter {
  /**
   * Formats a stream of items.
   *
   * @param items The items to format.
   * @return The formatted output stream.
   */
  java.util.stream.Stream<String> format(java.util.stream.Stream<Item> items);

  /**
   * @return The name of this formatter implementation (e.g., "text", "csv").
   */
  String getName();
}

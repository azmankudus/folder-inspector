package dev.ayam.folderinspector.core.plugin;

import dev.ayam.folderinspector.core.model.Item;

/**
 * Interface for formatting a stream of {@link Item}s into a stream of strings.
 * Implementations can provide different formats like CSV, Text, JSON, etc.
 */
public interface Formatter {
  /**
   * Formats a stream of items into a stream of formatted strings.
   *
   * @param items The items to format.
   * @return A stream of formatted strings.
   */
  java.util.stream.Stream<String> format(java.util.stream.Stream<Item> items);

  /**
   * Returns the unique name of this formatter implementation.
   *
   * @return The formatter name (e.g., "csv", "text").
   */
  String getName();
}

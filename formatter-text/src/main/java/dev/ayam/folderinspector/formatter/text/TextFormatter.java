package dev.ayam.folderinspector.formatter.text;

import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.core.model.AclItem;
import dev.ayam.folderinspector.core.plugin.Formatter;
import dev.ayam.folderinspector.core.utility.DateTimeUtil;
import dev.ayam.folderinspector.formatter.text.utility.StringResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link Formatter} that produces a formatted ASCII table.
 */
public class TextFormatter implements Formatter {

  private static final List<String> HEADERS = StringResource.TABLE_HEADERS;

  /**
   * Formats a stream of items into a stream of lines representing an ASCII table.
   *
   * @param items The stream of items to format.
   * @return A stream of formatted table lines.
   */
  @Override
  public java.util.stream.Stream<String> format(java.util.stream.Stream<Item> items) {
    if (items == null) {
      return java.util.stream.Stream.of(StringResource.NO_ITEMS_FOUND);
    }
    List<Item> itemList = items.collect(Collectors.toList());
    if (itemList.isEmpty()) {
      return java.util.stream.Stream.of(StringResource.NO_ITEMS_FOUND);
    }

    return formatTableStream(itemList);
  }

  /**
   * Helper to format a list of items into an ASCII table stream.
   * Note: This buffers all items to calculate column widths.
   */
  private java.util.stream.Stream<String> formatTableStream(List<Item> items) {
    List<List<String>> rows = new ArrayList<>();
    rows.add(HEADERS);

    for (Item item : items) {
      rows.add(Arrays.asList(
          item.name(),
          item.parent(),
          item.type() != null ? item.type().name : "",
          String.valueOf(item.size()),
          DateTimeUtil.format(item.lastModified()),
          DateTimeUtil.format(item.created()),
          item.owner() != null ? item.owner().name() : "",
          item.group() != null ? item.group().name() : "",
          formatPermissions(item.permissions()),
          formatAcls(item.acls())));
    }

    int[] colWidths = new int[HEADERS.size()];
    for (List<String> row : rows) {
      for (int i = 0; i < row.size(); i++) {
        colWidths[i] = Math.max(colWidths[i], row.get(i).length());
      }
    }

    List<String> outputLines = new ArrayList<>();

    // Print Header
    outputLines.add(formatRow(rows.get(0), colWidths));

    // Print Separator
    outputLines.add(formatSeparator(colWidths));

    // Print Data
    for (int i = 1; i < rows.size(); i++) {
      outputLines.add(formatRow(rows.get(i), colWidths));
    }
    return outputLines.stream();
  }

  /**
   * Formats a single row of the table.
   */
  private String formatRow(List<String> row, int[] colWidths) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < row.size(); i++) {
      String format = "%-" + (colWidths[i] + 2) + "s";
      sb.append(String.format(format, row.get(i)));
    }
    return sb.toString();
  }

  /**
   * Generates a separator line for the table.
   */
  private String formatSeparator(int[] colWidths) {
    StringBuilder sb = new StringBuilder();
    for (int width : colWidths) {
      for (int i = 0; i < width + 2; i++) {
        sb.append("-");
      }
    }
    return sb.toString();
  }

  /**
   * Returns the name of this formatter.
   *
   * @return "text"
   */
  @Override
  public String getName() {
    return "text";
  }

  /**
   * Formats a set of permissions into a string.
   */
  private String formatPermissions(java.util.Set<dev.ayam.folderinspector.core.model.PermissionType> permissions) {
    if (permissions == null || permissions.isEmpty()) {
      return "";
    }
    return permissions.stream()
        .map(p -> p.name)
        .collect(Collectors.joining(","));
  }

  /**
   * Formats a list of ACL items into a string.
   */
  private String formatAcls(List<AclItem> acls) {
    if (acls == null || acls.isEmpty())
      return "";
    return acls.stream()
        .map(acl -> String.format("%s:[%s]:[%s]", acl.name(),
            acl.permissions().stream().map(p -> p.name).collect(Collectors.joining(",")),
            acl.inheritFlags().stream().map(f -> f.name).collect(Collectors.joining(","))))
        .collect(Collectors.joining("; "));
  }
}

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link Formatter} that produces a formatted ASCII table.
 */
public class TextFormatter implements Formatter {

  private static final Logger logger = LoggerFactory.getLogger(TextFormatter.class);
  private static final List<String> HEADERS = StringResource.TABLE_HEADERS;

  @Override
  public java.util.stream.Stream<String> format(java.util.stream.Stream<Item> items) {
    if (items == null) {
      return java.util.stream.Stream.of(StringResource.NO_ITEMS_FOUND);
    }
    List<Item> itemList = items.collect(Collectors.toList());
    if (itemList.isEmpty()) {
      return java.util.stream.Stream.of(StringResource.NO_ITEMS_FOUND);
    }

    // We can't easily stream the table creation locally line-by-line without
    // buffering
    // because column widths depend on all rows.
    // So we buffer, calculate widths, and *then* stream the output strings.

    // Logic extraction from formatTable to avoid duplicate buffering if possible,
    // but formatTable uses list.
    // We will keep formatTable logic but make it return a List<String> lines or
    // Stream<String>.

    // Refactoring formatTable to return Stream<String> would be better.
    return formatTableStream(itemList);
  }

  // Helper to bridge the logic
  private java.util.stream.Stream<String> formatTableStream(List<Item> items) {
    List<List<String>> rows = new ArrayList<>();
    rows.add(HEADERS);

    for (Item item : items) {
      rows.add(Arrays.asList(
          item.absolutePath(),
          item.relativePath(),
          item.type(),
          String.valueOf(item.size()),
          DateTimeUtil.format(item.lastModified()),
          DateTimeUtil.format(item.created()),
          item.owner(),
          item.group(),
          item.permissions(),
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

  private String formatRow(List<String> row, int[] colWidths) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < row.size(); i++) {
      String format = "%-" + (colWidths[i] + 2) + "s";
      sb.append(String.format(format, row.get(i)));
    }
    return sb.toString();
  }

  private String formatSeparator(int[] colWidths) {
    StringBuilder sb = new StringBuilder();
    for (int width : colWidths) {
      for (int i = 0; i < width + 2; i++) {
        sb.append("-");
      }
    }
    return sb.toString();
  }

  @Override
  public String getName() {
    return "text";
  }

  // Old methods removed/refactored into formatTableStream

  private String formatAcls(List<AclItem> acls) {
    if (acls == null || acls.isEmpty())
      return "";
    return acls.stream()
        .map(acl -> String.format("%s:[%s]:[%s]", acl.name(),
            String.join(",", acl.permissions()),
            String.join(",", acl.flags())))
        .collect(Collectors.joining("; "));
  }
}

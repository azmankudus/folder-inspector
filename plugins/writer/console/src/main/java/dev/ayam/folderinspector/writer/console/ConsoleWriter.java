package dev.ayam.folderinspector.writer.console;

import dev.ayam.folderinspector.core.Writer;
import dev.ayam.folderinspector.core.model.Item;
import java.nio.file.attribute.AclEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link Writer} that produces a formatted ASCII table.
 */
public class ConsoleWriter implements Writer {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleWriter.class);

    private static final List<String> HEADERS = Arrays.asList(
            "Absolute Path", "Relative Path", "Type", "Size",
            "Last Modified", "Created", "Owner", "Group", "Permissions", "ACLs");

    @Override
    public String write(List<Item> items) {
        logger.debug("Formatting {} items to Console table", items.size());
        if (items.isEmpty()) {
            return "No items found.";
        }
        return formatTable(items);
    }

    @Override
    public String getName() {
        return "console";
    }

    private String formatTable(List<Item> items) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(HEADERS);

        for (Item item : items) {
            rows.add(Arrays.asList(
                    item.absolutePath(),
                    item.relativePath(),
                    item.type(),
                    String.valueOf(item.size()),
                    String.valueOf(item.lastModified()),
                    String.valueOf(item.created()),
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

        StringBuilder sb = new StringBuilder();

        // Print Header
        printRow(sb, rows.get(0), colWidths);

        // Print Separator
        printSeparator(sb, colWidths);

        // Print Data
        for (int i = 1; i < rows.size(); i++) {
            printRow(sb, rows.get(i), colWidths);
        }

        return sb.toString();
    }

    private void printRow(StringBuilder sb, List<String> row, int[] colWidths) {
        for (int i = 0; i < row.size(); i++) {
            String format = "%-" + (colWidths[i] + 2) + "s";
            sb.append(String.format(format, row.get(i)));
        }
        sb.append(System.lineSeparator());
    }

    private void printSeparator(StringBuilder sb, int[] colWidths) {
        for (int width : colWidths) {
            for (int i = 0; i < width + 2; i++) {
                sb.append("-");
            }
        }
        sb.append(System.lineSeparator());
    }

    private String formatAcls(List<AclEntry> acls) {
        if (acls == null || acls.isEmpty())
            return "";
        return acls.stream()
                .map(AclEntry::toString)
                .collect(Collectors.joining("; "));
    }
}

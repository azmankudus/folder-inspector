package dev.ayam.folderinspector.writer.csv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import dev.ayam.folderinspector.core.Writer;
import dev.ayam.folderinspector.core.model.Item;
import java.io.StringWriter;
import java.nio.file.attribute.AclEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link Writer} that produces CSV output.
 */
public class CsvWriter implements Writer {

    private static final Logger logger = LoggerFactory.getLogger(CsvWriter.class);

    @Override
    public String write(List<Item> items) {
        logger.debug("Formatting {} items to CSV", items.size());
        try {
            List<CsvItem> csvItems = new ArrayList<>();
            for (Item item : items) {
                csvItems.add(new CsvItem(
                        item.absolutePath(),
                        item.relativePath(),
                        item.type(),
                        item.size(),
                        String.valueOf(item.lastModified()),
                        String.valueOf(item.created()),
                        item.owner(),
                        item.group(),
                        item.permissions(),
                        formatAcls(item.acls())));
            }

            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = mapper.schemaFor(CsvItem.class).withHeader();

            StringWriter writer = new StringWriter();
            mapper.writer(schema).writeValue(writer, csvItems);
            return writer.toString();

        } catch (Exception e) {
            logger.error("Error writing CSV", e);
            return "Error generating CSV output: " + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "csv";
    }

    private String formatAcls(List<AclEntry> acls) {
        if (acls == null || acls.isEmpty())
            return "";
        return acls.stream()
                .map(AclEntry::toString)
                .collect(Collectors.joining("; "));
    }

    @JsonPropertyOrder({
            "Absolute Path", "Relative Path", "Type", "Size",
            "Last Modified", "Created", "Owner", "Group", "Permissions", "ACLs"
    })
    private static class CsvItem {
        @JsonProperty("Absolute Path")
        public String absolutePath;
        @JsonProperty("Relative Path")
        public String relativePath;
        @JsonProperty("Type")
        public String type;
        @JsonProperty("Size")
        public long size;
        @JsonProperty("Last Modified")
        public String lastModified;
        @JsonProperty("Created")
        public String created;
        @JsonProperty("Owner")
        public String owner;
        @JsonProperty("Group")
        public String group;
        @JsonProperty("Permissions")
        public String permissions;
        @JsonProperty("ACLs")
        public String acls;

        public CsvItem(String absolutePath, String relativePath, String type, long size, String lastModified,
                String created, String owner, String group, String permissions, String acls) {
            this.absolutePath = absolutePath;
            this.relativePath = relativePath;
            this.type = type;
            this.size = size;
            this.lastModified = lastModified;
            this.created = created;
            this.owner = owner;
            this.group = group;
            this.permissions = permissions;
            this.acls = acls;
        }
    }
}

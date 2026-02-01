package dev.ayam.folderinspector.writer.csv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import dev.ayam.folderinspector.writer.csv.utility.StringResource;
import dev.ayam.folderinspector.core.plugin.Writer;
import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.core.utility.DateTimeUtil;
import java.io.StringWriter;
import dev.ayam.folderinspector.core.model.AclItem;
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
                        DateTimeUtil.format(item.lastModified()),
                        DateTimeUtil.format(item.created()),
                        item.owner(),
                        item.group(),
                        item.permissions(),
                        formatAcls(item.acls())));
            }

            StringWriter writer = new StringWriter();

            // Write Header manually
            writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    StringResource.HEADER_ABSOLUTE_PATH,
                    StringResource.HEADER_RELATIVE_PATH,
                    StringResource.HEADER_TYPE,
                    StringResource.HEADER_SIZE,
                    StringResource.HEADER_LAST_MODIFIED,
                    StringResource.HEADER_CREATED,
                    StringResource.HEADER_OWNER,
                    StringResource.HEADER_GROUP,
                    StringResource.HEADER_PERMISSIONS,
                    StringResource.HEADER_ACLS));

            CsvMapper mapper = new CsvMapper();
            // Use schema from class but disable header output (since we wrote it manually)
            CsvSchema schema = mapper.schemaFor(CsvItem.class).withoutHeader();

            mapper.writer(schema).writeValue(writer, csvItems);
            return writer.toString();

        } catch (Exception e) {
            logger.error(StringResource.ERROR_WRITING_CSV, e);
            return StringResource.ERROR_GENERATING_OUTPUT + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "csv";
    }

    private String formatAcls(List<AclItem> acls) {
        if (acls == null || acls.isEmpty())
            return "";
        return acls.stream()
                .map(acl -> String.format("%s:[%s]:[%s]", acl.name(),
                        String.join(",", acl.permissions()),
                        String.join(",", acl.flags())))
                .collect(Collectors.joining("; "));
    }

    @JsonPropertyOrder({
            "absolutePath", "relativePath", "type", "size",
            "lastModified", "created", "owner", "group", "permissions", "acls"
    })
    private static class CsvItem {
        @JsonProperty
        public String absolutePath;
        @JsonProperty
        public String relativePath;
        @JsonProperty
        public String type;
        @JsonProperty
        public String size;
        @JsonProperty
        public String lastModified;
        @JsonProperty
        public String created;
        @JsonProperty
        public String owner;
        @JsonProperty
        public String group;
        @JsonProperty
        public String permissions;
        @JsonProperty
        public String acls;

        public CsvItem(String absolutePath, String relativePath, String type, long size, String lastModified,
                String created, String owner, String group, String permissions, String acls) {
            this.absolutePath = absolutePath;
            this.relativePath = relativePath;
            this.type = type;
            this.size = String.valueOf(size);
            this.lastModified = lastModified;
            this.created = created;
            this.owner = owner;
            this.group = group;
            this.permissions = permissions;
            this.acls = acls;
        }
    }
}

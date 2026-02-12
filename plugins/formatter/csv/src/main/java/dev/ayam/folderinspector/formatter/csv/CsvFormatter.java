package dev.ayam.folderinspector.formatter.csv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.core.model.AclItem;
import dev.ayam.folderinspector.core.plugin.Formatter;
import dev.ayam.folderinspector.core.utility.DateTimeUtil;
import dev.ayam.folderinspector.formatter.csv.utility.StringResource;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link Formatter} that produces CSV output.
 */
public class CsvFormatter implements Formatter {

  private static final Logger logger = LoggerFactory.getLogger(CsvFormatter.class);
  private final CsvMapper mapper = new CsvMapper();
  private final CsvSchema schema = mapper.schemaFor(CsvItem.class).withoutHeader();

  @Override
  public java.util.stream.Stream<String> format(java.util.stream.Stream<Item> items) {
    String header = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
        StringResource.HEADER_ABSOLUTE_PATH,
        StringResource.HEADER_RELATIVE_PATH,
        StringResource.HEADER_TYPE,
        StringResource.HEADER_SIZE,
        StringResource.HEADER_LAST_MODIFIED,
        StringResource.HEADER_CREATED,
        StringResource.HEADER_OWNER,
        StringResource.HEADER_GROUP,
        StringResource.HEADER_PERMISSIONS,
        StringResource.HEADER_ACLS);

    return java.util.stream.Stream.concat(
        java.util.stream.Stream.of(header),
        items.map(this::formatItem));
  }

  @Override
  public String getName() {
    return "csv";
  }

  private String formatItem(Item item) {
    try {
      CsvItem csvItem = new CsvItem(
          item.absolutePath(),
          item.relativePath(),
          item.type(),
          item.size(),
          DateTimeUtil.format(item.lastModified()),
          DateTimeUtil.format(item.created()),
          item.owner(),
          item.group(),
          item.permissions(),
          formatAcls(item.acls()));

      return mapper.writer(schema).writeValueAsString(csvItem).trim(); // trim to remove newline added by jackson if
                                                                       // any, or ensuring single line
    } catch (Exception e) {
      logger.error("Error formatting item", e);
      return "";
    }
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

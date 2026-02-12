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

  /**
   * Formats a stream of items into a stream of CSV strings.
   *
   * @param items The stream of items to format.
   * @return A stream of CSV strings, starting with a header line.
   */
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

  /**
   * Returns the name of this formatter.
   *
   * @return "csv"
   */
  @Override
  public String getName() {
    return "csv";
  }

  /**
   * Helper to format a single item into a CSV string.
   */
  private String formatItem(Item item) {
    try {
      CsvItem csvItem = new CsvItem(
          item.name(),
          item.parent(),
          item.type() != null ? item.type().name : null,
          item.size(),
          DateTimeUtil.format(item.lastModified()),
          DateTimeUtil.format(item.created()),
          item.owner() != null ? item.owner().name() : null,
          item.group() != null ? item.group().name() : null,
          formatPermissions(item.permissions()),
          formatAcls(item.acls()));

      return mapper.writer(schema).writeValueAsString(csvItem).trim();
    } catch (Exception e) {
      logger.error("Error formatting item", e);
      return "";
    }
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

  /**
   * DTO for Jackson CSV mapping.
   */
  @JsonPropertyOrder({
      "name", "parent", "type", "size",
      "lastModified", "created", "owner", "group", "permissions", "acls"
  })
  private static class CsvItem {
    @JsonProperty
    public String name;
    @JsonProperty
    public String parent;
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

    public CsvItem(String name, String parent, String type, long size, String lastModified,
        String created, String owner, String group, String permissions, String acls) {
      this.name = name;
      this.parent = parent;
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

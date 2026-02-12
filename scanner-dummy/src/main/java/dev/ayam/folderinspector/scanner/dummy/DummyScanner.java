package dev.ayam.folderinspector.scanner.dummy;

import dev.ayam.folderinspector.core.model.AclItem;
import dev.ayam.folderinspector.core.model.InheritFlag;
import dev.ayam.folderinspector.core.model.Item;
import dev.ayam.folderinspector.core.model.ItemType;
import dev.ayam.folderinspector.core.model.PermissionType;
import dev.ayam.folderinspector.core.model.Principal;
import dev.ayam.folderinspector.core.model.PrincipalType;
import dev.ayam.folderinspector.core.plugin.Scanner;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Implementation of {@link Scanner} that generates dummy file system data.
 * Useful for testing and demonstration purposes.
 */
public class DummyScanner implements Scanner {

  private static final List<String> ADJECTIVES = Arrays.asList("secret", "internal", "draft", "final", "temp", "system",
      "backup", "legacy", "new", "old", "secure", "shared", "private", "public", "urgent", "pending");
  private static final List<String> NOUNS = Arrays.asList("reports", "projects", "notes", "assets", "configs", "logs",
      "scripts", "data", "archive", "docs", "images", "videos", "src", "tests", "resources", "analysis");
  private static final List<String> OWNERS = Arrays.asList(
      // Malay
      "ahmad", "siti", "farhan", "nurul", "zulkifli",
      // Chinese
      "wei", "li", "chen", "xiuying", "jian", "mei",
      // Indian
      "arjun", "priya", "sanjay", "deepa", "rahul", "ananya",
      // English / American
      "james", "emma", "william", "olivia", "robert", "sophia",
      // Hispanic
      "alejandro", "maria", "diego", "gabriela", "mateo", "isabella",
      // Japanese
      "kenji", "yuki", "hiroshi", "akari", "takashi", "sakura",
      // Korean
      "minjun", "jiwon", "seo-yeon", "hyun-woo", "da-eun", "joon-ho",
      // Russian
      "dmitry", "olga", "ivan", "elena", "sergei", "natasha",
      // Middle Eastern
      "omar", "fatima", "zayd", "layla", "youssef", "amina",
      // African
      "kwame", "amani", "ngozi", "oluchi", "jabari", "zola",
      // Others
      "hans", "monique", "scotto", "claire", "luca", "giulia");
  private static final List<String> GROUPS = Arrays.asList("admins", "developers", "users", "staff", "guests",
      "managers", "auditors", "ops", "hr", "finance", "legal", "marketing", "sales", "support", "engineers",
      "security");

  private final Random random = new Random();

  /**
   * Generates a dummy stream of {@link Item} objects.
   *
   * @param rootPath The root path (used as a prefix for dummy paths).
   * @return A stream of dummy items.
   */
  @Override
  public Stream<Item> scan(Path rootPath) {
    List<Item> items = new ArrayList<>();
    items.add(createDummyItem(".", "directory", 0));

    // Some subfolders
    for (int i = 0; i < 8; i++) {
      String subFolderName = randomName();
      items.add(createDummyItem(subFolderName, "directory", 0));

      // Files in subfolder
      int fileCount = random.nextInt(12) + 3;
      for (int j = 0; j < fileCount; j++) {
        String fileName = randomName() + (random.nextBoolean() ? ".txt" : ".log");
        long size = random.nextInt(1024 * 1024 * 5);
        items.add(createDummyItem(subFolderName + "/" + fileName, "file", size));
      }
    }

    // Files in root
    for (int i = 0; i < 15; i++) {
      String fileName = randomName() + ".shared";
      long size = random.nextInt(1024 * 100);
      items.add(createDummyItem(fileName, "file", size));
    }

    return items.stream();
  }

  /**
   * Generates a random name by combining an adjective and a noun.
   */
  private String randomName() {
    return ADJECTIVES.get(random.nextInt(ADJECTIVES.size())) + "_" + NOUNS.get(random.nextInt(NOUNS.size()));
  }

  /**
   * Creates a single dummy {@link Item}.
   */
  private Item createDummyItem(String relativePath, String type, long size) {
    Instant now = Instant.now();
    Instant modified = now.minusSeconds(random.nextInt(60 * 60 * 24 * 365)); // up to 1 year ago
    Instant created = modified.minusSeconds(random.nextInt(60 * 60 * 24 * 30));

    return new Item(
        null, // No ID for dummy items
        relativePath.substring(relativePath.lastIndexOf('/') + 1),
        relativePath.contains("/") ? relativePath.substring(0, relativePath.lastIndexOf('/')) : ".",
        "directory".equals(type) ? ItemType.DIRECTORY : ItemType.FILE,
        size,
        modified,
        created,
        new Principal(null, null, OWNERS.get(random.nextInt(OWNERS.size())), PrincipalType.USER),
        new Principal(null, null, GROUPS.get(random.nextInt(GROUPS.size())), PrincipalType.GROUP),
        randomPermissions(),
        generateRandomAcls());
  }

  /**
   * Generates a random set of Unix permissions.
   */
  private Set<PermissionType> randomPermissions() {
    Set<PermissionType> perms = EnumSet.noneOf(PermissionType.class);
    if (random.nextBoolean())
      perms.add(PermissionType.UNIX_READ);
    if (random.nextBoolean())
      perms.add(PermissionType.UNIX_WRITE);
    if (random.nextBoolean())
      perms.add(PermissionType.UNIX_EXECUTE);
    return perms;
  }

  /**
   * Generates a random list of {@link AclItem} objects.
   */
  private List<AclItem> generateRandomAcls() {
    int count = random.nextInt(4);
    if (count == 0)
      return null;
    List<AclItem> acls = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      String name = random.nextBoolean() ? OWNERS.get(random.nextInt(OWNERS.size()))
          : GROUPS.get(random.nextInt(GROUPS.size()));
      Set<PermissionType> perms = EnumSet.of(PermissionType.NTFS_READ);
      if (random.nextBoolean())
        perms.add(PermissionType.NTFS_WRITE);

      acls.add(new AclItem(name, perms, EnumSet.of(InheritFlag.OBJECT_INHERIT)));
    }
    return acls;
  }

  /**
   * Returns the name of this scanner.
   *
   * @return "dummy"
   */
  @Override
  public String getName() {
    return "dummy";
  }
}

package backend.service;

import backend.model.ItemAcl;
import backend.model.Item;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class ExportServiceTest {

    @Inject
    ExportService exportService;

    @Test
    void testCsvExport() throws IOException {
        List<Item> nodes = new ArrayList<>();
        nodes.add(new Item(1L, 1L, null, "test.txt", "/test.txt", "FILE", 1024L, LocalDateTime.now(), "S-1-5-18",
                "SYSTEM", "S-1-5-18", "SYSTEM", 1L));

        Map<Long, List<ItemAcl>> aclsMap = new HashMap<>();
        List<ItemAcl> acls = new ArrayList<>();
        acls.add(new ItemAcl(1L, 1L, "Everyone", "DIRECT", true, false, false, false, 1L));
        aclsMap.put(1L, acls);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exportService.generateCsv(nodes, aclsMap, out);

        String result = out.toString();
        assertTrue(result.contains("test.txt"));
        assertTrue(result.contains("Everyone"));
        assertTrue(result.contains("READ"));
    }

    @Test
    void testXlsxExport() throws IOException {
        List<Item> nodes = new ArrayList<>();
        nodes.add(new Item(1L, 1L, null, "test.txt", "/test.txt", "FILE", 1024L, LocalDateTime.now(), "S-1-5-18",
                "SYSTEM", "S-1-5-18", "SYSTEM", 1L));

        Map<Long, List<ItemAcl>> aclsMap = new HashMap<>();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exportService.generateXlsx(nodes, aclsMap, out);

        assertTrue(out.size() > 0);
        byte[] bytes = out.toByteArray();
        // Check for ZIP magic number (XLSX is a ZIP file)
        assertTrue(bytes[0] == 0x50 && bytes[1] == 0x4B);
    }
}

package backend.service;

import backend.model.FileNode;
import backend.model.FileAcl;
import com.opencsv.CSVWriter;
import jakarta.inject.Singleton;
import org.apache.poi.ss.usermodel.*;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating data exports in CSV and XLSX formats.
 * Utilizes streaming to maintain a constant memory profile during large exports.
 */
@Singleton
public class ExportService {

    private static final String[] HEADERS = {
            "ID", "Name", "Path", "Type", "Size (Bytes)", "Last Modified", "Owner Name", "Group Name", "Permissions (ACL)"
    };

    /**
     * Generates a CSV export of the provided nodes and their ACLs.
     * @param nodes Iterable of file nodes to export
     * @param aclMap Map of node IDs to their associated ACL entries
     * @param outputStream The stream to write CSV data to
     * @throws IOException If writing to the stream fails
     */
    public void generateCsv(Iterable<FileNode> nodes, Map<Long, List<FileAcl>> aclMap, OutputStream outputStream) throws IOException {
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
            writer.writeNext(HEADERS);
            for (FileNode node : nodes) {
                List<FileAcl> acls = aclMap.getOrDefault(node.id(), List.of());
                writer.writeNext(new String[] {
                        String.valueOf(node.id()),
                        node.name(),
                        node.path(),
                        node.nodeType(),
                        node.sizeBytes() != null ? String.valueOf(node.sizeBytes()) : "0",
                        node.lastModified() != null ? node.lastModified().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                : "",
                        node.ownerName() != null ? node.ownerName() : "",
                        node.groupName() != null ? node.groupName() : "",
                        formatAcls(acls)
                });
            }
        }
    }

    /**
     * Generates an XLSX export using SXSSF for streaming.
     * @param nodes Iterable of file nodes to export
     * @param aclMap Map of node IDs to their associated ACL entries
     * @param outputStream The stream to write XLSX data to
     * @throws IOException If writing to the stream fails
     */
    public void generateXlsx(Iterable<FileNode> nodes, Map<Long, List<FileAcl>> aclMap, OutputStream outputStream) throws IOException {
        // Use SXSSFWorkbook for streaming (default window size 100)
        try (org.apache.poi.xssf.streaming.SXSSFWorkbook workbook = new org.apache.poi.xssf.streaming.SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("Files");

            // Header
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            int rowIdx = 1;
            for (FileNode node : nodes) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(node.id());
                row.createCell(1).setCellValue(node.name());
                row.createCell(2).setCellValue(node.path());
                row.createCell(3).setCellValue(node.nodeType());
                row.createCell(4).setCellValue(node.sizeBytes() != null ? node.sizeBytes() : 0);
                row.createCell(5)
                        .setCellValue(node.lastModified() != null
                                ? node.lastModified().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                : "");
                row.createCell(6).setCellValue(node.ownerName() != null ? node.ownerName() : "");
                row.createCell(7).setCellValue(node.groupName() != null ? node.groupName() : "");

                List<FileAcl> acls = aclMap.getOrDefault(node.id(), List.of());
                row.createCell(8).setCellValue(formatAcls(acls));
            }

            workbook.write(outputStream);
            workbook.dispose(); // Delete temp files
        }
    }

    private String formatAcls(List<FileAcl> acls) {
        if (acls == null || acls.isEmpty()) return "";
        return acls.stream().map(acl -> {
            StringBuilder sb = new StringBuilder(acl.userOrGroup());
            if (acl.inheritanceType() != null && !acl.inheritanceType().equals("NONE")) {
                sb.append(" (").append(acl.inheritanceType()).append(")");
            }
            sb.append(": ");
            List<String> perms = new ArrayList<>();
            if (Boolean.TRUE.equals(acl.canView())) perms.add("READ");
            if (Boolean.TRUE.equals(acl.canAdd())) perms.add("WRITE");
            if (Boolean.TRUE.equals(acl.canEdit())) perms.add("EDIT");
            if (Boolean.TRUE.equals(acl.canRemove())) perms.add("DELETE");
            sb.append(String.join(", ", perms));
            return sb.toString();
        }).collect(Collectors.joining(" | "));
    }
}

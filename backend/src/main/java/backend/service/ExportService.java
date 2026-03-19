package backend.service;

import backend.model.Item;
import backend.model.ItemAcl;
import backend.model.Job;
import backend.model.JobException;
import backend.model.JobLog;
import backend.model.ScanConfig;
import backend.model.ServerConfig;
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
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import java.time.Duration;

/**
* Service for generating data exports in CSV and XLSX formats.
* Utilizes streaming to maintain a constant memory profile during large exports.
*/
@Singleton
public class ExportService {

  private static final String[] HEADERS = {
      "ID", "Name", "Parent Path", "Type", "Size (Bytes)", "Last Modified", "Owner Name", "Group Name",
      "Permissions (ACL)"
  };

  private static final String[] EXCEPTION_HEADERS = {
      "Timestamp", "Level", "Path", "Issue", "Reason", "Note"
  };


  /**
  * Generates a CSV export of the provided nodes and their ACLs.
  * @param nodes Iterable of file nodes to export
  * @param aclMap Map of node IDs to their associated ACL entries
  * @param outputStream The stream to write CSV data to
  * @throws IOException If writing to the stream fails
  */
  public void generateCsv(Iterable<Item> nodes, Map<Long, List<ItemAcl>> aclMap, OutputStream outputStream)
      throws IOException {
    try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
      writer.writeNext(HEADERS);
      for (Item node : nodes) {
        List<ItemAcl> acls = aclMap.getOrDefault(node.id(), List.of());
        writer.writeNext(new String[] {
            String.valueOf(node.id()),
            node.name(),
            getParentPath(node.path(), node.name()),
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
  public void generateXlsx(Iterable<Item> nodes, Map<Long, List<ItemAcl>> aclMap, OutputStream outputStream)
      throws IOException {
    generateXlsxWithReport(null, null, null, List.of(), List.of(), nodes, aclMap, outputStream);
  }

  public void generateXlsxWithReport(Job history, ScanConfig profile, ServerConfig connection,
      List<JobLog> logs, List<JobException> exceptions, Iterable<Item> nodes,
      Map<Long, List<ItemAcl>> aclMap, OutputStream outputStream) throws IOException {
    try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
      CellStyle dateStyle = workbook.createCellStyle();
      dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss AM/PM"));

      // Summary Sheet (Report)
      if (history != null && profile != null) {
        Sheet reportSheet = workbook.createSheet("Scan Summary");
        if (reportSheet instanceof SXSSFSheet) {
          ((SXSSFSheet) reportSheet).trackAllColumnsForAutoSizing();
        }

        int r = 0;
        createSummaryRow(reportSheet, r++, "Server Hostname / IP Address",
            connection != null ? connection.host() : "N/A");
        createSummaryRow(reportSheet, r++, "Root path", profile.rootPath());
        createSummaryRow(reportSheet, r++, "Username", connection != null ? connection.username() : "N/A");
        createSummaryRow(reportSheet, r++, "Start time",
            history.startTime() != null ? history.startTime() : "N/A", dateStyle);
        createSummaryRow(reportSheet, r++, "Finish time",
            history.finishTime() != null ? history.finishTime() : "N/A", dateStyle);

        String duration = "N/A";
        if (history.startTime() != null && history.finishTime() != null) {
          Duration d = Duration.between(history.startTime(), history.finishTime());
          duration = String.format("%d min, %d sec", d.toMinutes(),
              d.minusMinutes(d.toMinutes()).getSeconds());
        }
        createSummaryRow(reportSheet, r++, "Duration", duration);
        createSummaryRow(reportSheet, r++, "Status", history.status());
        createSummaryRow(reportSheet, r++, "Total files",
            String.valueOf(history.totalFiles() != null ? history.totalFiles() : 0));
        createSummaryRow(reportSheet, r++, "Total folders",
            String.valueOf(history.totalFolders() != null ? history.totalFolders() : 0));
        createSummaryRow(reportSheet, r++, "Total exception",
            String.valueOf(history.totalExceptions() != null ? history.totalExceptions() : 0));

        if (exceptions != null && !exceptions.isEmpty()) {
          r++;
          Row exTitleRow = reportSheet.createRow(r++);
          Cell titleCell = exTitleRow.createCell(0);
          titleCell.setCellValue("Scan Exceptions");

          CellStyle titleStyle = workbook.createCellStyle();
          Font titleFont = workbook.createFont();
          titleFont.setBold(true);
          titleStyle.setFont(titleFont);
          titleCell.setCellStyle(titleStyle);

          // Styles for Level column
          CellStyle warnStyle = workbook.createCellStyle();
          warnStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
          warnStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
          warnStyle.setFont(titleFont); // making it bold too

          CellStyle errorStyle = workbook.createCellStyle();
          errorStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
          errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
          Font whiteFont = workbook.createFont();
          whiteFont.setColor(IndexedColors.WHITE.getIndex());
          whiteFont.setBold(true);
          errorStyle.setFont(whiteFont);

          Row exHeaderRow = reportSheet.createRow(r++);
          for (int i = 0; i < EXCEPTION_HEADERS.length; i++) {
            Cell cell = exHeaderRow.createCell(i);
            cell.setCellValue(EXCEPTION_HEADERS[i]);
            cell.setCellStyle(titleStyle);
          }

          for (JobException ex : exceptions) {
            Row row = reportSheet.createRow(r++);
            Cell cell = row.createCell(0);
            if (ex.timestamp() != null) {
              cell.setCellValue(ex.timestamp().toLocalDateTime());
              cell.setCellStyle(dateStyle);
            } else {
              cell.setCellValue("");
            }

            Cell levelCell = row.createCell(1);
            levelCell.setCellValue(ex.level());
            if ("ERROR".equalsIgnoreCase(ex.level()))
              levelCell.setCellStyle(errorStyle);
            else if ("WARN".equalsIgnoreCase(ex.level()))
              levelCell.setCellStyle(warnStyle);

            row.createCell(2).setCellValue(ex.path());
            row.createCell(3).setCellValue(ex.message());
            row.createCell(4).setCellValue(ex.reason());
            row.createCell(5).setCellValue(ex.note() != null ? ex.note() : "");
          }
        }

        reportSheet.autoSizeColumn(0);
        reportSheet.autoSizeColumn(1);
      }

      // Data Sheet
      if (nodes != null) {
        Sheet dataSheet = workbook.createSheet("Security Data");
        if (dataSheet instanceof SXSSFSheet) {
          ((SXSSFSheet) dataSheet).trackAllColumnsForAutoSizing();
        }
        // Header
        Row headerRow = dataSheet.createRow(0);
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
        CellStyle aclStyle = workbook.createCellStyle();
        aclStyle.setWrapText(true);
        aclStyle.setVerticalAlignment(VerticalAlignment.TOP);

        int rowIdx = 1;
        for (Item node : nodes) {
          Row row = dataSheet.createRow(rowIdx++);
          row.createCell(0).setCellValue(node.id());
          row.createCell(1).setCellValue(node.name());
          row.createCell(2).setCellValue(getParentPath(node.path(), node.name()));
          row.createCell(3).setCellValue(node.nodeType());
          row.createCell(4).setCellValue(node.sizeBytes() != null ? node.sizeBytes() : 0);
          Cell dateCell = row.createCell(5);
          if (node.lastModified() != null) {
            dateCell.setCellValue(node.lastModified().toLocalDateTime());
            dateCell.setCellStyle(dateStyle);
          } else {
            dateCell.setCellValue("");
          }
          row.createCell(6).setCellValue(node.ownerName() != null ? node.ownerName() : "");
          row.createCell(7).setCellValue(node.groupName() != null ? node.groupName() : "");

          List<ItemAcl> acls = aclMap.getOrDefault(node.id(), List.of());
          Cell aclCell = row.createCell(8);
          aclCell.setCellValue(formatAcls(acls));
          aclCell.setCellStyle(aclStyle);
        }

        for (int i = 0; i < HEADERS.length; i++) {
          dataSheet.autoSizeColumn(i);
        }
      }

      workbook.write(outputStream);
      workbook.dispose();
    }
  }

  private void createSummaryRow(Sheet sheet, int rowNum, String key, Object value) {
    createSummaryRow(sheet, rowNum, key, value, null);
  }

  private void createSummaryRow(Sheet sheet, int rowNum, String key, Object value, CellStyle dateStyle) {
    Row row = sheet.createRow(rowNum);
    row.createCell(0).setCellValue(key);
    Cell valueCell = row.createCell(1);
    if (value instanceof java.time.OffsetDateTime && dateStyle != null) {
      valueCell.setCellValue(((java.time.OffsetDateTime) value).toLocalDateTime());
      valueCell.setCellStyle(dateStyle);
    } else {
      valueCell.setCellValue(value != null ? value.toString() : "N/A");
    }
  }

  private String getParentPath(String fullPath, String name) {
    if (fullPath == null || name == null || fullPath.equals(name))
      return "";
    if (fullPath.endsWith(name)) {
      String parent = fullPath.substring(0, fullPath.length() - name.length());
      if (parent.length() > 1 && (parent.endsWith("\\") || parent.endsWith("/"))) {
        parent = parent.substring(0, parent.length() - 1);
      }
      return parent;
    }
    return fullPath;
  }

  private String formatAcls(List<ItemAcl> acls) {
    if (acls == null || acls.isEmpty())
      return "";

    // Group and merge permissions for the same user/group
    Map<String, List<ItemAcl>> grouped = acls.stream()
        .collect(Collectors.groupingBy(acl -> acl.principal(), Collectors.toList()));

    return grouped.entrySet().stream().map(entry -> {
      String principal = entry.getKey();
      List<ItemAcl> groupAcls = entry.getValue();

      boolean canView = groupAcls.stream().anyMatch(a -> Boolean.TRUE.equals(a.canView()));
      boolean canAdd = groupAcls.stream().anyMatch(a -> Boolean.TRUE.equals(a.canAdd()));
      boolean canEdit = groupAcls.stream().anyMatch(a -> Boolean.TRUE.equals(a.canEdit()));
      boolean canRemove = groupAcls.stream().anyMatch(a -> Boolean.TRUE.equals(a.canRemove()));

      String inheritance = groupAcls.stream()
          .map(ItemAcl::inheritanceType)
          .filter(t -> t != null && !t.equalsIgnoreCase("NONE"))
          .distinct()
          .sorted()
          .collect(Collectors.joining(", "));

      StringBuilder sb = new StringBuilder(principal);
      if (!inheritance.isEmpty()) {
        sb.append(" (").append(inheritance).append(")");
      }
      sb.append(": ");

      List<String> perms = new ArrayList<>();
      if (canView)
        perms.add("READ");
      if (canAdd)
        perms.add("WRITE");
      if (canEdit)
        perms.add("EDIT");
      if (canRemove)
        perms.add("DELETE");
      sb.append(String.join(", ", perms));
      return sb.toString();
    }).sorted().collect(Collectors.joining("\n"));
  }
}

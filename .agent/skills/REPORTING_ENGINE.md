---
description: Streaming Architecture for Exporting Enormous Datasets
---

# Skill: Generating Memory-Safe Reports

## Pattern Rationale
Folder Inspector maps servers hosting potentially millions of files. Loading millions of `tb_item` objects into an `ArrayList` directly prior to HTTP response transmission causes direct `OutOfMemoryError` heap crashes.

## Execution Rules
Avoid memory traps when working with `ReportController` and `ExportService`.
1. **XLSX Exports**: Always utilize Apache POI's `SXSSFWorkbook`. It relies on sliding disk-windows (default parameter of 100 rows in-memory before flushing to the temp drive). 
2. Do not use legacy `XSSFWorkbook`.
3. **Iterative SQL Sweeps**: Rather than grabbing `.findAll()`, use `Pageable` constructs (`io.micronaut.data.model.Pageable`) to sweep the dataset block by block.
4. **Direct Stream Publishing**: Always bind the finalized workbook stream directly into Micronaut's `SystemFile` or `StreamedFile` HTTP construct instead of saving it natively on the API server.

Follow these rules unconditionally whenever expanding the reporting capabilities.

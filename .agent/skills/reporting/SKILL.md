---
name: Reporting Export Generation
description: Streaming execution boundaries for processing multi-million row reports.
---
# Execution Skill: Streaming Report Generation

**Usage Conditions:** When tasked with modifying the `ReportController` or configuring custom extraction models targeting database clusters containing thousands of entities.

## Procedure Rules
1. Never execute unbound nested queries into native Arrays! Agents must define `Pageable` database iteration loops.
2. Generating EXCEL payloads natively with `XSSFWorkbook` throws OOM heap constraints! All AI generation must implement the Apache POI Streaming model via `new SXSSFWorkbook(100)` configured dynamically to stream raw disk dumps concurrently with database ingestion parameters.
3. Automatically finalize memory resources by executing `workbook.dispose()` in the `finally` execution branch.

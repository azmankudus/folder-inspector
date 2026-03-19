# Agent Discovery Workflow

This document describes the design and implementation of the Folder Inspector **Discovery Report Engine**, built by the Antigravity agent.

## 📊 Discovery Report Module

The system now includes a standalone reporting module that allows for detailed analysis of historical scan jobs.

### Architecture
- **Controller**: `ReportController` (v1)
- **Data Source**: Fetches from `tb_job`, `tb_item`, and `tb_item_acl` using partitioned queries.
- **Frontend**: A dedicated `/report` route mirroring the explorer view but optimized for historical data analysis.

### Features
- **Summary Metrics**: High-level totals for files, folders, and exceptions including performance benchmarks.
- **Incremental Loading**: Paginated item views for historical datasets.
- **Excel Analytics**: Streaming XLSX generation that builds a multi-sheet audit report with:
  1.  **Scan Summary**: Configuration, duration, and high-level stats.
  2.  **Discovered Data**: Comprehensive listing of all files and folders.
  3.  **Permissions Map**: Full resolution of ACLs for audited objects.
  4.  **Exceptions & Logs**: Detailed debugging data.

### Security Model
The module uses granular API permissions to differentiate between full audit access and restricted discovery views:
-   `API_REPORT_ALL`: Full export and summary visibility.
-   `API_REPORT_RESTRICTED`: Limited visibility to discovered items.

## 🛠️ Infrastructure Updates

The following system-wide changes were implemented during the report engine build:
-   **Model Normalization**: Unified table prefixes (`tb_config_server`, `tb_config_directory`, `tb_config_scan`) to follow a standardized configuration schema.
-   **RBAC Alignment**: Cleaned up all `@Secured` annotations to rely solely on system-level `API_` tokens, separating UI visibility logic from backend authorization.

---
description: RBAC and JWT Security Architecture
---

# Security & Permissions Context

## Overview
Folder Inspector's security module relies exclusively on stateless JWT tokens and Spring-Security inspired `@Secured` RBAC mechanisms natively provided by Micronaut Security.

## Backend Verification
- Use `@Secured({ "API_TOKEN_NAME" })` rigidly on every method endpoint in the Controller layer.
- Never use legacy UI-based roles (e.g., "MANAGER", "USER"). All roles must correspond directly to granular application features mapped in `DataInit.java`.

### Known Authorities Map:
- `API_JOB_READ`: Read-only access to `/job` and scan histories.
- `API_JOB_START` / `API_JOB_STOP`: Execution rights.
- `API_LIST_ALL` / `API_LIST_RESTRICTED`: Controls pagination footprints in `ListController`. RESTRICTED prevents toggling full tenant visibility.
- `API_REPORT_ALL` / `API_REPORT_RESTRICTED`: Governs full dataset exporting vs aggregated summary exports in `ReportController`.

## Dynamic Proxy/Impersonation
When components need to "View As" another user (e.g., Dashboard Manager Views), the system receives `?targetUser=xxx`. Controller logic MUST manually assert the authentication token to ensure the requesting identity holds `API_REPORT_ALL` or `API_LIST_ALL` before overriding the underlying database query paths.

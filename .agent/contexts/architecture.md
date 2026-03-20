---
description: Architecture overview, routing, and stack map
---
# Application Architecture State

Folder Inspector is a robust enterprise utility serving discovery streams from massive File Share networks directly into a responsive UI structure.

- **Frontend**: Solid Start single-page application orchestrating ECharts graphics. Found in `/frontend`.
- **Backend**: Micronaut REST services compiled natively. Found in `/backend`.
- **Database Architecture**: PostgreSQL utilizing extensive table partitioning on legacy ACL footprints. 
- **Migration Engine**: Liquibase orchestrations inside `changelog.yaml`. Agents must assert `preConditions` dynamically to safely bypass or rename schema discrepancies (e.g., legacy `tb_server` vs current `tb_config_server` implementations) without modifying hardcoded historical steps.

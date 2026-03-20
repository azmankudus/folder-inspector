---
description: Database Schema Conventions and Migration Standards
---

# Database Schema & Liquibase Constraints

## Table Schemas
All tables align with standardized prefixes for cross-module identification:
- `tb_config_server` -> Configurations denoting Active Directory LDAP endpoints
- `tb_config_directory` -> Configurations dictating network scan settings
- `tb_config_scan` -> Primary definitions for scanning schedules

## Partitioning Patterns
To guarantee instantaneous discovery tracking scalability, we use **PostgreSQL Table Partitioning**.
- Tables like `tb_item` and `tb_item_acl` partition natively by `job_id` or `scan_profile_id`. 
- Indexes must always respect the partitioned primary keys.

## Caching Strategy (Dashboard Analytics)
- Dashboard profile visualizations cache active aggregation tracking footprints inside `tb_user_job_stats`.
- Do not run aggregation sweeps over `tb_item` dynamically! Instead, use `DashboardService.computeAndCacheStat` for lazy-evaluation insertion to secure performance metrics natively.

---
description: Application Architecture and Technology Stack
---

# Folder Inspector Architecture

## Core Technology Stack
- **Backend**: Java 21 powered by Micronaut Framework
- **Frontend**: SolidJS + Solid Start framework
- **Database**: PostgreSQL 16
- **Schema Management**: Liquibase

## Business Logic
- **Service Layer**: Decoupled domain implementations inside `backend.service.*`
- **Security**: JWT-based stateless authentication (`spring-security` principles) relying exclusively on system-level `API_` tokens for strict RBAC decoupling.
- **Reporting Engine**: Implements the Apache POI streaming pattern (`SXSSFWorkbook`) to compile unlimited size Auditing exports dynamically into an overarching multi-sheet canvas.

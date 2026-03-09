# 📁 Folder Inspector

[![Micronaut](https://img.shields.io/badge/Micronaut-4.6.2-blue.svg)](https://micronaut.io/)
[![SolidJS](https://img.shields.io/badge/SolidJS-1.8.17-navy.svg)](https://www.solidjs.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![SMBJ](https://img.shields.io/badge/SMBJ-0.13.0-green.svg)](https://github.com/hierynomus/smbj)

**Folder Inspector** is a high-performance, enterprise-grade file discovery and auditing tool. It enables IT administrators to scan massive SMB shares, resolve complex NTFS permissions via Active Directory, and generate scalable reports without memory bottlenecks.

---

## 🚀 Key Features

- **Scalable Discovery**: Iterative, non-recursive directory traversal that handles millions of files and deep nesting.
- **Resilient Identity Resolution**: Resolves SIDs to human-readable names via LDAP (Active Directory) with built-in failover support.
- **Streaming Export Engine**: Generate CSV and multi-million row Excel (XLSX) reports with constant memory usage.
- **Dynamic Partitioning**: Uses PostgreSQL table partitioning (by Scan Profile) for high-performance data isolation and rapid cleanup.
- **Premium UI**: Reactive dashboard built with SolidJS and Solid Store for a smooth, state-of-the-art user experience.
- **Secure by Design**: JWT-based authentication, automatic secret generation, and role-based access control (RBAC).

---

## 🏗️ Architecture Design

The application is built on a modular Micronaut backend and a reactive Solid Start frontend.

### Backend (Java 21)
- **Service Layer Pattern**: Decoupled business logic for scanning, exporting, and schema management.
- **Job Orchestration**: Background scan jobs are launched as isolated processes with real-time log tracking.
- **Streaming Pipeline**: Leveraging `SXSSFWorkbook` and `CSVWriter` with `Writable` interfaces to pipe data directly from DB to HTTP response.
- **Schema Management**: Liquibase-driven versioning with dynamic runtime partition creation.

### Database (PostgreSQL)
- **Composite Primary Keys**: Optimized for partitioned queries.
- **Partitioned Tables**: `file_node` and `file_acl` are partitioned by `scan_profile_id`, ensuring that index sizes remain manageable.

---

## 🛠️ Tech Stack

| Component | Technology |
| :--- | :--- |
| **Backend** | Micronaut Framework (Java 21) |
| **Frontend** | SolidJS, Solid Start, Vanilla CSS |
| **Database** | PostgreSQL 16 |
| **Migration** | Liquibase |
| **Protocols** | SMB/CIFS (SMBJ), LDAP/AD (JNDI) |
| **Security** | JWT, SAML (Pac4j), BCrypt |
| **Reporting** | Apache POI (Streaming XLSX), OpenCSV |

---

## 📋 Requirements & Implementation

The project satisfies the following technical requirements:

1. **[REQ-01] Memory Efficiency**: All heavy operations (scanning, exporting) must use streaming or iterative patterns.
2. **[REQ-02] Depth Resilience**: Directory traversal must avoid recursion to prevent StackOverflow errors.
3. **[REQ-03] Identity Accuracy**: SIDs must be resolved via LDAP with support for multiple AD controllers (failover).
4. **[REQ-04] Data Isolation**: Each scan profile's data must be easily manageable and isolatable (implemented via Partitions).
5. **[REQ-05] Security Defaults**: Automatic generation of crypto secrets if not provided by the environment.

---

## 🚦 Getting Started

### Prerequisites
- Java 21+
- Node.js 18+
- PostgreSQL instance

### Setup
1. **Clone the repository**:
   ```bash
   git clone https://github.com/azmankudus/folder-inspector.git
   ```
2. **Backend Configuration**:
   Update `backend/src/main/resources/application.properties` with your DB credentials.
3. **Frontend Configuration**:
   The frontend expects the backend at `http://localhost:8080`.

### Running Locally
- **Backend**: `./gradlew run`
- **Frontend**: `cd frontend && npm run dev`

---

## 🧪 Documentation
- **Javadocs**: All classes and methods are documented following standard Javadoc practices.
- **Walkthroughs**: Detailed implementation logs can be found in the `.gemini/antigravity/brain` directory.

---

Developed with ❤️ for Advanced Agentic Coding.
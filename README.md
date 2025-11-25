# GriefLogger-MariaDB

This repository is a fork of GriefLogger with MariaDB support added.

## New configuration option

A new configuration entry was added under `database` to choose which remote SQL driver to use when `useMysql` is enabled.

- `database.useMysql` (boolean) — When `true`, GriefLogger will use a remote SQL database instead of the bundled SQLite file. When `false`, SQLite (`database.db`) is used.
- `database.sqlDriver` (string) — Selects the remote JDBC driver to use. Allowed values:
  - `"mysql"` — Use the official MySQL Connector/J (`com.mysql:mysql-connector-j`). This is the default.
  - `"mariadb"` — Use the MariaDB JDBC driver (`org.mariadb.jdbc:mariadb-java-client`).

When using `useMysql: true`, set `database.sqlDriver` accordingly and provide the usual connection settings:

- `database.mysqlHost` (string)
- `database.mysqlPort` (int)
- `database.mysqlDatabase` (string)
- `database.mysqlUsername` (string)
- `database.mysqlPassword` (string)
- `database.mysqlTimeout` (int: milliseconds)

Example (YAML):

```yaml
database:
  useMysql: true
  sqlDriver: "mariadb"
  mysqlHost: "db.example.org"
  mysqlPort: 3306
  mysqlDatabase: "grieflogger"
  mysqlUsername: "grief"
  mysqlPassword: "secret"
  mysqlTimeout: 5000
```

## Build notes

Both MySQL and MariaDB JDBC drivers are included in the relevant platform modules (`fabric` and `neoforge`) so you can switch drivers without changing the code. The Gradle build will produce shaded jars as configured in each subproject.

To build on Windows PowerShell (project root):

```powershell
.\gradlew.bat build
```

If you want me to run the build now and report results, confirm and I'll start it.
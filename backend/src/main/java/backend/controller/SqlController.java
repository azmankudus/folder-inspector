package backend.controller;

import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.transaction.Transactional;
import io.micronaut.serde.annotation.Serdeable;

@Controller("/sql")
public class SqlController {
  private static final Logger LOG = LoggerFactory.getLogger(SqlController.class);

  private final DataSource dataSource;

  public SqlController(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Get("/tables")
  @Secured({ "API_SQL_LIST_TABLES" })
  @Transactional
  public List<String> getTables() {
    LOG.trace("Fetching database tables list");
    List<String> tables = new ArrayList<>();
    try (Connection conn = dataSource.getConnection();
        ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[] { "TABLE" })) {
      while (rs.next()) {
        tables.add(rs.getString("TABLE_NAME"));
      }
    } catch (Exception e) {
      LOG.error("Failed to fetch database tables", e);
      throw new RuntimeException("Error fetching tables: " + e.getMessage(), e);
    }
    return tables;
  }

  @Post("/execute")
  @Secured({ "API_SQL_EXECUTE" })
  @Transactional
  public QueryResult executeQuery(@Body QueryRequest request) {
    LOG.info("Executing administrative SQL query");
    QueryResult result = new QueryResult();
    result.setColumns(new ArrayList<>());
    result.setRows(new ArrayList<>());

    if (request.query() == null || request.query().trim().isEmpty()) {
      LOG.warn("Received empty SQL query request");
      result.setError("Query cannot be empty.");
      return result;
    }

    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement()) {

      LOG.debug("Executing SQL statement: {}", request.query());
      boolean isResultSet = stmt.execute(request.query());

      if (isResultSet) {
        try (ResultSet rs = stmt.getResultSet()) {
          ResultSetMetaData metaData = rs.getMetaData();
          int columnCount = metaData.getColumnCount();

          for (int i = 1; i <= columnCount; i++) {
            result.getColumns().add(metaData.getColumnName(i));
          }

          while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
              row.put(metaData.getColumnName(i), rs.getObject(i));
            }
            result.getRows().add(row);
          }
        }
      } else {
        int updateCount = stmt.getUpdateCount();
        LOG.debug("SQL update completed, rows affected: {}", updateCount);
        result.setError("Query executed successfully. Rows affected: " + updateCount);
      }

    } catch (Exception e) {
      LOG.error("Exception during SQL execution", e);
      result.setError(e.getMessage());
    }

    return result;
  }

  @Serdeable.Deserializable
  public record QueryRequest(String query) {
  }

  @Serdeable
  public static class QueryResult {
    private List<String> columns;
    private List<Map<String, Object>> rows;
    private String error;

    public List<String> getColumns() {
      return columns;
    }

    public void setColumns(List<String> columns) {
      this.columns = columns;
    }

    public List<Map<String, Object>> getRows() {
      return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
      this.rows = rows;
    }

    public String getError() {
      return error;
    }

    public void setError(String error) {
      this.error = error;
    }
  }
}

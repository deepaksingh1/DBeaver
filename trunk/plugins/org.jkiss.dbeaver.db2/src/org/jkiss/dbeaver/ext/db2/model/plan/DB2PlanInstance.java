package org.jkiss.dbeaver.ext.db2.model.plan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
/**
 * DB2 EXPLAIN_INSTANCE table
 * 
 * @author Denis Forveille
 * 
 */
import java.util.List;

import org.jkiss.dbeaver.ext.db2.model.DB2DataSource;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;

/**
 * DB2 EXPLAIN_INSTANCE table
 * 
 * @author Denis Forveille
 * 
 */
public class DB2PlanInstance {

   private static String          SEL_EXP_STATEMENT; // See init below

   private List<DB2PlanStatement> listPlanStatements;

   private String                 statement_id;

   private String                 explainRequester;
   private Timestamp              explainTime;
   private String                 sourceName;
   private String                 sourceSchema;
   private String                 sourceVersion;

   // ------------
   // Constructors
   // ------------

   public DB2PlanInstance(DB2DataSource dataSource, JDBCExecutionContext context, ResultSet dbResult) throws SQLException {

      this.explainRequester = JDBCUtils.safeGetStringTrimmed(dbResult, "EXPLAIN_REQUESTER");
      this.explainTime = JDBCUtils.safeGetTimestamp(dbResult, "EXPLAIN_TIME");
      this.sourceName = JDBCUtils.safeGetStringTrimmed(dbResult, "SOURCE_NAME");
      this.sourceSchema = JDBCUtils.safeGetStringTrimmed(dbResult, "SOURCE_SCHEMA");
      this.sourceVersion = JDBCUtils.safeGetStringTrimmed(dbResult, "SOURCE_VERSION");

      this.listPlanStatements = loadListPlanStatements(context, dbResult);
   }

   // -------------
   // Load children
   // -------------
   private List<DB2PlanStatement> loadListPlanStatements(JDBCExecutionContext context, ResultSet dbResult) throws SQLException {

      List<DB2PlanStatement> listeRes = new ArrayList<DB2PlanStatement>();

      JDBCPreparedStatement sqlStmt = context.prepareStatement(SEL_EXP_STATEMENT);
      sqlStmt.setString(1, explainRequester);
      sqlStmt.setTimestamp(2, explainTime);
      sqlStmt.setString(3, sourceName);
      sqlStmt.setString(4, sourceSchema);
      sqlStmt.setString(5, sourceVersion);

      JDBCResultSet res = null;
      try {
         res = sqlStmt.executeQuery();
         while (dbResult.next()) {
            listeRes.add(new DB2PlanStatement(context, res, this));
         }
      } finally {
         if (res != null) {
            res.close();
         }
         if (sqlStmt != null) {
            sqlStmt.close();
         }
      }

      return listeRes;
   }

   // -------------
   // Standards Getters
   // -------------
   public List<DB2PlanStatement> getListPlanStatements() {
      return listPlanStatements;
   }

   public void setListPlanStatements(List<DB2PlanStatement> listPlanStatements) {
      this.listPlanStatements = listPlanStatements;
   }

   public String getStatement_id() {
      return statement_id;
   }

   public String getExplainRequester() {
      return explainRequester;
   }

   public Timestamp getExplainTime() {
      return explainTime;
   }

   public String getSourceName() {
      return sourceName;
   }

   public String getSourceSchema() {
      return sourceSchema;
   }

   public String getSourceVersion() {
      return sourceVersion;
   }

   // -------
   // Queries
   // -------
   static {
      StringBuilder sb = new StringBuilder(1024);
      sb.append("SELECT *");
      sb.append(" FROM EXPLAIN_STATEMENT");
      sb.append(" WHERE EXPLAIN_REQUESTER = ?");
      sb.append("   AND EXPLAIN_TIME = ?");
      sb.append("   AND SOURCE_NAME = ?");
      sb.append("   AND SOURCE_SCHEMA = ?");
      sb.append("   AND SOURCE_VERSION = ?");
      SEL_EXP_STATEMENT = sb.toString();
   }
}

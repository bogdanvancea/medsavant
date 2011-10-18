/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.util.query;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.Query;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.structure.CustomTables;
import org.ut.biolab.medsavant.db.model.structure.MedSavantDatabase.DefaultpatientTableSchema;
import org.ut.biolab.medsavant.db.model.structure.MedSavantDatabase.DefaultvariantTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.ConnectionController;

/**
 *
 * @author Andrew
 */
public class VariantQueryUtil {
    
    private static String queryToString(Query query){
        return query.toString().replaceAll("\\[", "(").replaceAll("\\]", ")");
    }
    
    private static String queryToString(String query){
        return query.replaceAll("\\[", "(").replaceAll("\\]", ")");
    }
    
    public static TableSchema getVariantTableSchema(int projectId, int referenceId) throws SQLException {
        return CustomTables.getVariantTableSchema(ProjectQueryUtil.getVariantTablename(projectId, referenceId));
    }
    
    public static Vector getVariants(int projectId, int referenceId, int limit) throws SQLException {       
        return getVariants(projectId, referenceId, new ArrayList(), limit);
    }
   
    public static Vector getVariants(int projectId, int referenceId, List<List<Condition>> conditions, int limit) throws SQLException {            
        
        TableSchema table = CustomTables.getVariantTableSchema(ProjectQueryUtil.getVariantTablename(projectId, referenceId));
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        for(int i = 0; i < conditions.size(); i++){
            query.addCondition(ComboCondition.and(conditions.get(i)));
        }
        
        /*String query = 
                "SELECT *" + 
                " FROM " + ProjectQueryUtil.getVariantTablename(projectId, referenceId) + " t0";  
        if(!conditions.isEmpty()){
            query += " WHERE ";
        }
        query += conditionsToStringOr(conditions);
        query += " LIMIT " + limit;*/

        Connection conn = ConnectionController.connect();
        ResultSet rs = conn.createStatement().executeQuery(queryToString(query) + " LIMIT " + limit);
        
        ResultSetMetaData rsMetaData = rs.getMetaData();
        int numberColumns = rsMetaData.getColumnCount();
        
        Vector result = new Vector();
        while(rs.next()){
            Vector v = new Vector();
            for(int i = 1; i <= numberColumns; i++){
                v.add(rs.getObject(i));
            }
            result.add(v);
        }
        
        return result;
    }
    
    /*private static String conditionsToStringOr(List<List> conditions){
        String s = "";
        for(int i = 0; i < conditions.size(); i++){
            List subset = conditions.get(i);
            s += "(";
            boolean somethingWritten = false;
            boolean andWritten = false;
            for(int j = 0; j < subset.size(); j++){
                String current = subset.get(j).toString();
                if(current.equals("")){
                    if(j == subset.size()-1 && andWritten){
                        s += "1=1"; //ensure no unclosed AND
                    }
                    continue;
                }
                somethingWritten = true;
                s += subset.get(j).toString();
                if(j != subset.size()-1){
                    s += " AND ";
                    andWritten = true;
                }
            }
            if(!somethingWritten){
                s += "1=2"; //ensure no unclosed OR
            }
            s += ")";
            if(i != conditions.size()-1){
                s += " OR ";
            }
        }
        return s;
    }*/
    
    public static double[] getExtremeValuesForColumn(String tablename, String columnname) throws SQLException { 
        
        TableSchema table = CustomTables.getVariantTableSchema(tablename);
        
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addCustomColumns(FunctionCall.min().addColumnParams(table.getDBColumn(columnname)));
        query.addCustomColumns(FunctionCall.max().addColumnParams(table.getDBColumn(columnname)));
      
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(queryToString(query));
        
        double[] result = new double[2];
        rs.next();
        result[0] = rs.getDouble(1);
        result[1] = rs.getDouble(2);
        
        return result;
    }
    
    public static List<String> getDistinctValuesForColumn(String tablename, String columnname) throws SQLException {
        
        TableSchema table = CustomTables.getVariantTableSchema(tablename);
        
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.setIsDistinct(true);
        query.addColumns(table.getDBColumn(columnname)); 
        
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(queryToString(query));
        
        List<String> result = new ArrayList<String>();
        while(rs.next()){
            String val = rs.getString(1);
            if(val == null){
                result.add("");
            } else {
                result.add(val);
            }
        }
        
        return result;
    }
    
    public static int getNumFilteredVariants(int projectId, int referenceId) throws SQLException {
        return getNumFilteredVariants(projectId, referenceId, new ArrayList());
    }
    
    public static int getNumFilteredVariants(int projectId, int referenceId, List<List<Condition>> conditions) throws SQLException {
        
        TableSchema table = CustomTables.getVariantTableSchema(ProjectQueryUtil.getVariantTablename(projectId, referenceId));
               
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        for(int i = 0; i < conditions.size(); i++){
            q.addCondition(ComboCondition.and(conditions.get(i)));
        }

        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(queryToString(q));
        
        rs.next();
        return rs.getInt(1);
    }
    
    public static int getFilteredFrequencyValuesForColumnInRange(int projectId, int referenceId, List<List<Condition>> conditions, String columnname, double min, double max) throws SQLException {
        
        TableSchema table = CustomTables.getVariantTableSchema(ProjectQueryUtil.getVariantTablename(projectId, referenceId));
               
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        q.addCondition(BinaryCondition.greaterThan(table.getDBColumn(columnname), min, true)); 
        q.addCondition(BinaryCondition.lessThan(table.getDBColumn(columnname), max, false)); 
        for(int i = 0; i < conditions.size(); i++){
            q.addCondition(ComboCondition.and(conditions.get(i)));
        }

        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(queryToString(q));
        
        rs.next();
        return rs.getInt(1);        
    }
    
    public static Map<String, Integer> getFilteredFrequencyValuesForColumn(int projectId, int referenceId, List<List<Condition>> conditions, String columnAlias) throws SQLException {
        
        TableSchema tableSchema = CustomTables.getVariantTableSchema(ProjectQueryUtil.getVariantTablename(projectId, referenceId));
        DbTable table = tableSchema.getTable();
        DbColumn col = tableSchema.getDBColumnByAlias(columnAlias);
          
        return getFilteredFrequencyValuesForColumn(table, conditions, col);
    }
    
    public static Map<String, Integer> getFilteredFrequencyValuesForColumn(DbTable table, List<List<Condition>> conditions, DbColumn column) throws SQLException {
                       
        SelectQuery q = new SelectQuery();
        q.addFromTable(table);
        q.addCustomColumns(FunctionCall.countAll());
        for(int i = 0; i < conditions.size(); i++){
            q.addCondition(ComboCondition.and(conditions.get(i)));
        }
        q.addGroupings(column);
        
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(queryToString(q));
        
        Map<String, Integer> map = new HashMap<String, Integer>();
        
        while (rs.next()) {
            map.put(rs.getString(1), rs.getInt(2));
        }

        return map;     
    }
    
    public static int getNumVariantsInRange(int projectId, int referenceId, List<List<Condition>> conditions, String chrom, long start, long end) throws SQLException, NonFatalDatabaseException {
        
        TableSchema table = CustomTables.getVariantTableSchema(ProjectQueryUtil.getVariantTablename(projectId, referenceId));
               
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(DefaultvariantTableSchema.COLUMNNAME_OF_CHROM), chrom));
        q.addCondition(BinaryCondition.greaterThan(table.getDBColumn(DefaultvariantTableSchema.COLUMNNAME_OF_POSITION), start, true));
        q.addCondition(BinaryCondition.lessThan(table.getDBColumn(DefaultvariantTableSchema.COLUMNNAME_OF_POSITION), end, false));
        for(int i = 0; i < conditions.size(); i++){
            q.addCondition(ComboCondition.and(conditions.get(i)));
        }
        
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(queryToString(q));
        
        rs.next();
        return rs.getInt(1);
    }
    
    public static int[] getNumVariantsForBins(int projectId, int referenceId, List<List<Condition>> conditions, String chrom, int binsize, int numbins) throws SQLException, NonFatalDatabaseException {
        
        TableSchema table = CustomTables.getVariantTableSchema(ProjectQueryUtil.getVariantTablename(projectId, referenceId));
        
        SelectQuery queryBase = new SelectQuery();
        queryBase.addFromTable(table.getTable());
        queryBase.addColumns(table.getDBColumn(DefaultvariantTableSchema.COLUMNNAME_OF_POSITION));
        queryBase.addCondition(BinaryCondition.equalTo(table.getDBColumn(DefaultvariantTableSchema.COLUMNNAME_OF_CHROM), chrom));
        for(int i = 0; i < conditions.size(); i++){
            queryBase.addCondition(ComboCondition.and(conditions.get(i)));
        }
        
        /*String queryBase = 
                "SELECT `" + VariantTable.FIELDNAME_POSITION + "`" +
                " FROM " + ProjectQueryUtil.getVariantTablename(projectId, referenceId) + " t0" + 
                " WHERE `" + VariantTable.FIELDNAME_CHROM + "`=\"" + chrom + "\"";
        if(!conditions.isEmpty()){
            queryBase += " AND ";
        }
        queryBase += conditionsToStringOr(conditions);*/
        
        
        //TODO
        String query = "select y.range as `range`, count(*) as `number of occurences` "
                + "from ("
                + "select case ";
        int pos = 0;
        for(int i = 0; i < numbins; i++){
            query += "when `" + DefaultvariantTableSchema.COLUMNNAME_OF_POSITION + "` between " + pos + " and " + (pos+binsize) + " then " + i + " ";
            pos += binsize;
        }
        
        query += "end as `range` "
                + "from (";
        query += queryBase.toString();
        query += ") x ) y "
                + "group by y.`range`";
        
        Connection conn = ConnectionController.connect();
        ResultSet rs = conn.createStatement().executeQuery(queryToString(query));
        
        int[] numRows = new int[numbins];
        for(int i = 0; i < numbins; i++) numRows[i] = 0;
        while(rs.next()){
            int index = rs.getInt(1);
            numRows[index] = rs.getInt(2);
        }
        return numRows;     
    }
    
    public static void uploadFileToVariantTable(File file, String tableName) throws SQLException{
        Connection c = ConnectionController.connect();
        c.createStatement().execute(
                "LOAD DATA LOCAL INFILE '" + file.getAbsolutePath().replaceAll("\\\\", "/") + "' "
                + "INTO TABLE " + tableName + " "
                + "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' "
                + "LINES TERMINATED BY '\\r\\n';");
    }
    
    public static int getNumPatientsWithVariantsInRange(int projectId, int referenceId, List<List<Condition>> conditions, String chrom, int start, int end) throws SQLException {
        
        TableSchema table = getVariantTableSchema(projectId, referenceId);
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns("COUNT(DISTINCT " + DefaultvariantTableSchema.COLUMNNAME_OF_DNA_ID + ")");
        for(int i = 0; i < conditions.size(); i++){
            q.addCondition(ComboCondition.and(conditions.get(i)));
        }
        
        Condition[] cond = new Condition[3];
        cond[0] = new BinaryCondition(BinaryCondition.Op.EQUAL_TO, table.getDBColumn(DefaultvariantTableSchema.COLUMNNAME_OF_CHROM), chrom);
        cond[1] = new BinaryCondition(BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO, table.getDBColumn(DefaultvariantTableSchema.COLUMNNAME_OF_POSITION), start);
        cond[2] = new BinaryCondition(BinaryCondition.Op.LESS_THAN, table.getDBColumn(DefaultvariantTableSchema.COLUMNNAME_OF_POSITION), end);       
        q.addCondition(ComboCondition.and(cond));        
        
        String query = queryToString(q);
        query = query.replaceFirst("'", "").replaceFirst("'", "");
        
        Statement s = ConnectionController.connect().createStatement();
        ResultSet rs = s.executeQuery(query);
        rs.next();

        int numrows = rs.getInt(1);
        s.close();
        
        return numrows;
    }


}

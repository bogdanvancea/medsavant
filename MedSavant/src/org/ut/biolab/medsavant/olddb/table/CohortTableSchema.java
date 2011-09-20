/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.olddb.table;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;

/**
 *
 * @author AndrewBrook
 */
public class CohortTableSchema extends TableSchema {
    
    public static final String TABLE_NAME = "cohort";
   
    public static int INDEX_COHORTID;
    public static int INDEX_COHORTNAME;
    
    public static final String ALIAS_COHORTID = "Cohort ID";
    public static final String ALIAS_COHORTNAME = "Cohort Name";

    public static final String DBFIELDNAME_COHORTID = "cohort_id";
    public static final String DBFIELDNAME_COHORTNAME = "name";

    public CohortTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
        setIndexes();
    }
   
    private void setIndexes() {
        INDEX_COHORTID = this.getFieldIndexInDB(DBFIELDNAME_COHORTID);
        INDEX_COHORTNAME = this.getFieldIndexInDB(DBFIELDNAME_COHORTNAME);
    }
 
    private void addColumns() {
        addColumn(DBFIELDNAME_COHORTID,ALIAS_COHORTID,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_COHORTNAME,ALIAS_COHORTNAME,TableSchema.ColumnType.VARCHAR,10);
    }
    
    
}

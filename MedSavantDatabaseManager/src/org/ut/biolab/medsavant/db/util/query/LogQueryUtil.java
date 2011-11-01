/*
 *    Copyright 2011 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.db.util.query;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;

import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.ProjectTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.ReferenceTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.ServerLogTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.VariantPendingUpdateTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.db.util.ConnectionController;

/**
 *
 * @author mfiume
 */
public class LogQueryUtil {

    public static ResultSet getClientLog() throws SQLException {
        
        TableSchema table = MedSavantDatabase.ServerlogTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryCondition.notEqualTo(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_USER), "server"));
        query.addOrdering(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP), Dir.DESCENDING);
        
        return ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
    }

    public static ResultSet getServerLog() throws SQLException {
        
        TableSchema table = MedSavantDatabase.ServerlogTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_USER), "server"));
        query.addOrdering(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP), Dir.DESCENDING);
        
        return ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
    }

    public static ResultSet getAnnotationLog() throws SQLException {
        
        TableSchema projectTable = MedSavantDatabase.ProjectTableSchema;
        TableSchema referenceTable = MedSavantDatabase.ReferenceTableSchema;
        TableSchema updateTable = MedSavantDatabase.VariantpendingupdateTableSchema;
        
        SelectQuery query = new SelectQuery();
        query.addFromTable(updateTable.getTable());
        query.addColumns(
                projectTable.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME),
                referenceTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME),
                updateTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_ACTION),
                updateTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_STATUS),
                updateTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_TIMESTAMP),
                updateTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPDATE_ID));
        query.addJoin(
                SelectQuery.JoinType.LEFT_OUTER, 
                updateTable.getTable(), 
                projectTable.getTable(), 
                BinaryConditionMS.equalTo(
                        updateTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_PROJECT_ID), 
                        projectTable.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID)));
        query.addJoin(
                SelectQuery.JoinType.LEFT_OUTER, 
                updateTable.getTable(), 
                referenceTable.getTable(), 
                BinaryConditionMS.equalTo(
                        updateTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_REFERENCE_ID), 
                        referenceTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID)));
        
        return ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
    }
}

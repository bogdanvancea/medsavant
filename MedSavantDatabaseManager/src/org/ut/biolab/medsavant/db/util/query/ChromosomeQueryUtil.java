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
import java.util.ArrayList;
import java.util.List;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;

import org.ut.biolab.medsavant.db.model.Chromosome;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.ChromosomeTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.db.util.ConnectionController;

/**
 *
 * @author Andrew
 */
public class ChromosomeQueryUtil {
    
    public static List<Chromosome> getContigs(int refid) throws SQLException{
        
        TableSchema table = MedSavantDatabase.ChromosomeTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid));
        query.addOrdering(table.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_ID), Dir.ASCENDING);
        
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());

        List<Chromosome> result = new ArrayList<Chromosome>();
        while(rs.next()){
            result.add(new Chromosome(
                    rs.getString(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_NAME), 
                    null, 
                    rs.getLong(ChromosomeTableSchema.COLUMNNAME_OF_CENTROMERE_POS), 
                    rs.getLong(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_LENGTH)));
        }
        return result;       
    }
    
}

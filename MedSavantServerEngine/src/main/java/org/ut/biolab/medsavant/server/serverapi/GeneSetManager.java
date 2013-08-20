/*
 *    Copyright 2012 University of Toronto
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

package org.ut.biolab.medsavant.server.serverapi;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.GeneSetColumns;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.model.Block;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.GeneSet;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManagerFactory;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.QueryManagerFactory;
import org.ut.biolab.medsavant.shared.serverapi.GeneSetManagerAdapter;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;


/**
 * Server-side implementation of class which managed GeneSets.
 *
 * @author tarkvara
 */
public class GeneSetManager extends MedSavantServerUnicastRemoteObject implements GeneSetManagerAdapter, GeneSetColumns {
    private static final Log LOG = LogFactory.getLog(GeneSetManager.class);

    private static GeneSetManager instance;
    private static QueryManager queryManager;
    private static EntityManager entityManager;

    public static synchronized GeneSetManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new GeneSetManager();
        }
        return instance;
    }

    private GeneSetManager() throws RemoteException, SessionExpiredException {
        entityManager = EntityManagerFactory.getEntityManager();
        queryManager = QueryManagerFactory.getQueryManager();
    }


    /**
     * Get a list of all available gene sets.
     * @param sessID
     * @return
     * @throws SQLException
     */
    @Override
    public GeneSet[] getGeneSets(String sessID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select g from GeneSet");
        List<GeneSet> result = query.execute();
        return result.toArray(new GeneSet[0]);

    }

    /**
     * Get the gene set for the given reference genome.
     * @param sessID session ID
     * @param refName reference name (not ID)
     * @return
     * @throws SQLException
     */
    @Override
    public GeneSet getGeneSet(String sessID, String refName) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select g from GeneSet where g.genome= :refName");
        query.setParameter("refName",refName);
        List<GeneSet> result = query.execute();

        if (result.size() > 0 ) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public static void main(String[] argv) {
        TableSchema table = MedSavantDatabase.GeneSetTableSchema;
        SelectQuery query = MedSavantDatabase.GeneSetTableSchema.where(GENOME, "hg19", TYPE, "RefSeq").groupBy(CHROM).groupBy(NAME).select(NAME, CHROM, "MIN(start)", "MAX(end)", "MIN(codingStart)", "MAX(codingEnd)");
        BinaryCondition dumbChrsCondition1 = BinaryConditionMS.notlike(table.getDBColumn(MedSavantDatabase.GeneSetColumns.CHROM), "%\\_%");
        query.addCondition(dumbChrsCondition1);
        BinaryCondition dumbChrsCondition2 = BinaryConditionMS.notlike(table.getDBColumn(MedSavantDatabase.GeneSetColumns.CHROM), "%\\-%");
        query.addCondition(dumbChrsCondition2);
        System.out.println(query.toString());
    }

    @Override
    public Gene[] getGenes(String sessID, GeneSet geneSet) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select g from Gene where g.reference = :reference and g.type= :type");
        query.setParameter("reference", geneSet.getReference());
        query.setParameter("type", geneSet.getType());
        List<Gene> result = query.execute();
        return result.toArray(new Gene[0]);
    }

    @Override
    public Gene[] getTranscripts(String sessID, GeneSet geneSet) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select g from Gene where g.reference = :reference and g.type= :type");
        query.setParameter("reference", geneSet.getReference());
        query.setParameter("type", geneSet.getType());
        List<Gene> result = query.execute();
        return result.toArray(new Gene[0]);
    }

    @Override
    public Block[] getBlocks(String sessID, Gene gene) throws SQLException, RemoteException, SessionExpiredException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

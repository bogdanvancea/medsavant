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

package org.ut.biolab.medsavant.ontology;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.healthmarketscience.sqlbuilder.Condition;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.geneset.GeneSetController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.GenomicRegion;
import org.ut.biolab.medsavant.model.OntologyTerm;
import org.ut.biolab.medsavant.model.OntologyType;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.region.RegionSetFilter;


/**
 *
 * @author tarkvara
 */
public class OntologyFilter extends RegionSetFilter {
    
    private static final Log LOG = LogFactory.getLog(OntologyFilter.class);

    private final List<OntologyTerm> appliedTerms;
    private final OntologyType ontology;

    public OntologyFilter(List<OntologyTerm> applied, OntologyType ont) {
        appliedTerms = applied;
        ontology = ont;
    }

    @Override
    public Condition[] getConditions() throws SQLException, RemoteException {
        Set<Gene> genes = new HashSet<Gene>();
        Map<OntologyTerm, String[]> allTermsGenes = MedSavantClient.OntologyManager.getGenesForTerms(LoginController.sessionId, appliedTerms.toArray(new OntologyTerm[0]), ReferenceController.getInstance().getCurrentReferenceName());
        for (String[] termGenes: allTermsGenes.values()) {
            for (String geneName: termGenes) {
                Gene g = GeneSetController.getInstance().getGene(geneName);
                if (g != null) {
                    genes.add(g);
                } else {
                    LOG.info("Non-existent gene " + geneName + " referenced by " + ontology);
                }
            }
        }
        GenomicRegion[] regions = new GenomicRegion[genes.size()];
        int i = 0;
        for (Gene g: genes) {
            regions[i++] = new GenomicRegion(g.getName(), g.getChrom(), g.getStart(), g.getEnd());
        }
        return getConditions(regions);
    }

    @Override
    public String getID() {
        return ontologyToFilterID(ontology);
    }

    @Override
    public String getName() {
        return ontologyToTitle(ontology);
    }

    public static String ontologyToTitle(OntologyType ontology) {
        switch (ontology) {
            case GO:
                return "GO – Gene Ontology";
            case HPO:
                return "HPO – Human Phenotype Ontology";
            case OMIM:
// The full title is too long, and screws up the layout of the filter view.
//              return "OMIM – Online Mendelian Inheritance in Man";
                return "OMIM";
            default:
                return null;
        }
    }
    
    public static String ontologyToFilterID(OntologyType ontology) {
        return ontology.toString();
    }
    
    public static OntologyType filterIDToOntology(String filterID) {
        return Enum.valueOf(OntologyType.class, filterID);
    }
}
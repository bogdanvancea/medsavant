/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.db;

import org.ut.biolab.medsavant.db.table.GeneListTableSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import org.ut.biolab.medsavant.db.table.AlignmentTableSchema;
import org.ut.biolab.medsavant.db.table.CohortTableSchema;
import org.ut.biolab.medsavant.db.table.CohortViewTableSchema;
import org.ut.biolab.medsavant.db.table.GeneListMembershipTableSchema;
import org.ut.biolab.medsavant.db.table.GeneListViewTableSchema;
import org.ut.biolab.medsavant.db.table.GenomeTableSchema;
import org.ut.biolab.medsavant.db.table.TableSchema;
import org.ut.biolab.medsavant.db.table.ModifiableTableSchema;
import org.ut.biolab.medsavant.db.table.PatientTableSchema;
import org.ut.biolab.medsavant.db.table.PhenotypeTableSchema;
import org.ut.biolab.medsavant.db.table.VariantAnnotationGatkTableSchema;
import org.ut.biolab.medsavant.db.table.VariantAnnotationPolyphenTableSchema;
import org.ut.biolab.medsavant.db.table.VariantAnnotationSiftTableSchema;
import org.ut.biolab.medsavant.db.table.VariantTableSchema;

/**
 *
 * @author mfiume
 */
public class MedSavantDatabase {

    private final DbSpec spec;
    private final DbSchema schema;
    private static MedSavantDatabase instance;

    private VariantTableSchema variantTableSchema;  
    private VariantAnnotationSiftTableSchema variantSiftTableSchema;
    private VariantAnnotationPolyphenTableSchema variantPolyphenTableSchema;
    private VariantAnnotationGatkTableSchema variantGatkTableSchema;
    
    private AlignmentTableSchema alignmentTableSchema;
    private CohortViewTableSchema cohortviewTableSchema;
    private CohortTableSchema cohortTableSchema;
    private GeneListTableSchema geneListTableSchema;
    private GeneListViewTableSchema geneListViewTableSchema;
    private GeneListMembershipTableSchema geneListMembershipTableSchema;
    private GenomeTableSchema genomeTableSchema;
    //private PatientTableSchema patientTableSchema;
    
    private PatientTableSchema patientTableSchema;
    private PhenotypeTableSchema phenotypeTableSchema;
    
    public static void main(String[] argv) {
        getInstance();
    }

    public static MedSavantDatabase getInstance() {
        if (instance == null) {
            instance = new MedSavantDatabase();
        }
        return instance;
    }
    
    public DbSchema getSchema() {
        return schema;
    }

    public MedSavantDatabase() {
        spec = new DbSpec();
        schema = spec.addDefaultSchema();
        initTableSchemas();
    }

    private void initTableSchemas() {
        
        variantTableSchema = new VariantTableSchema(schema);    
        variantSiftTableSchema = new VariantAnnotationSiftTableSchema(schema);
        variantPolyphenTableSchema = new VariantAnnotationPolyphenTableSchema(schema);
        variantGatkTableSchema = new VariantAnnotationGatkTableSchema(schema);
        
        cohortviewTableSchema = new CohortViewTableSchema(schema);
        cohortTableSchema = new CohortTableSchema(schema);
        geneListTableSchema = new GeneListTableSchema(schema);
        geneListViewTableSchema = new GeneListViewTableSchema(schema);
        geneListMembershipTableSchema = new GeneListMembershipTableSchema(schema);
        alignmentTableSchema = new AlignmentTableSchema(schema);
        genomeTableSchema = new GenomeTableSchema(schema);
        
        refreshModifiableTables();
    }

    public VariantTableSchema getVariantTableSchema() {
        return this.variantTableSchema;
    }
    
    public VariantAnnotationSiftTableSchema getVariantSiftTableSchema() {
        return this.variantSiftTableSchema;
    }
    
    public VariantAnnotationPolyphenTableSchema getVariantPolyphenTableSchema() {
        return this.variantPolyphenTableSchema;
    }
    
    public VariantAnnotationGatkTableSchema getVariantGatkTableSchema() {
        return this.variantGatkTableSchema;
    }
    
    public PatientTableSchema getPatientTableSchema() {
        return this.patientTableSchema;
    }
    
    public TableSchema getCohortViewTableSchema() {
        return this.cohortviewTableSchema;
    }
    
    public TableSchema getCohortTableSchema() {
        return this.cohortTableSchema;
    }
    
    public TableSchema getGeneListViewTableSchema() {
        return this.geneListViewTableSchema;
    }
    
    public TableSchema getGeneListTableSchema() {
        return this.geneListTableSchema;
    }

    public TableSchema getGeneListMembershipTableSchema() {
        return geneListMembershipTableSchema;
    }
    
    public TableSchema getAlignmentTableSchema() {
        return alignmentTableSchema;
    }
    
    public TableSchema getGenomeTableSchema() {
        return genomeTableSchema;
    }
    
    public PhenotypeTableSchema getPhenotypeTableSchema() {
        return phenotypeTableSchema;
    }
    
    public ModifiableTableSchema[] getModifiableTables(){
        return new ModifiableTableSchema[]{patientTableSchema,phenotypeTableSchema};
    }
   
    public void refreshModifiableTables(){
        patientTableSchema = new PatientTableSchema(schema);
        phenotypeTableSchema = new PhenotypeTableSchema(schema);
    }
    
}
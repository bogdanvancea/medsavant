/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.format;

import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.db.format.CustomField.Category;

/**
 *
 * @author Andrew
 */
public class AnnotationFormat {
    
    public static enum AnnotationType {POSITION, INTERVAL};
    
    public static AnnotationType intToAnnotationType(int type){
        switch(type){
            case 0:
                return AnnotationType.POSITION;
            case 1:
                return AnnotationType.INTERVAL;
            default:
                return null;
        }
    }
    
    public static int annotationTypeToInt(AnnotationType type){
        switch(type){
            case POSITION:
                return 0;
            case INTERVAL:
                return 1;
            default:
                return -1;
        }
    }
    
    public static int annotationTypeToInt(String type){
        return annotationTypeToInt(AnnotationType.valueOf(type.toUpperCase()));
    }
    
    private boolean hasRef;
    private boolean hasAlt;
    private String program;
    private String version;
    private int referenceId;
    private String path;
    private AnnotationType type;
    private List<CustomField> fields = new ArrayList<CustomField>();
    
    public AnnotationFormat(String program, String version, int referenceId, String path, boolean hasRef, boolean hasAlt, AnnotationType type, List<CustomField> fields){
        this.program = program;
        this.version = version;
        this.referenceId = referenceId;
        this.path = path;
        this.hasRef = hasRef;
        this.hasAlt = hasAlt;
        this.fields = fields;
        this.type = type;
    }
    
    public String generateSchema(){
        String result = "";
        
        //add custom columns
        for(int i = 0; i < fields.size(); i++){
            CustomField field = fields.get(i);
            String columnName = field.getColumnName();
            String columnType = field.getColumnType();
            result += "`" + columnName + "` " + columnType + " DEFAULT NULL,";
        }

        return result;
    }
    
    public int getNumNonDefaultFields(){
        return fields.size();
    }
    
    public boolean hasRef(){
        return hasRef;
    }
    
    public boolean hasAlt(){
        return hasAlt;
    }
    
    public List<CustomField> getCustomFields(){
        return fields;
    }
    
    public static AnnotationFormat getDefaultAnnotationFormat(){
        List<CustomField> fields = new ArrayList<CustomField>();
        fields.add(new CustomField("upload_id", "INT(11)", false, "Upload ID", ""));
        fields.add(new CustomField("file_id", "INT(11)", false, "File ID", ""));
        fields.add(new CustomField("variant_id", "INT(11)", false, "Variant ID", ""));
        fields.add(new CustomField("dna_id", "VARCHAR(10)", true, "DNA ID", "", Category.PATIENT));
        fields.add(new CustomField("chrom", "VARCHAR(5)", true, "Chromosome", ""));
        fields.add(new CustomField("position", "INT(11)", true, "Position", ""));
        fields.add(new CustomField("dbsnp_id", "(VARCHAR(45)", false, "dbSNP ID", ""));
        fields.add(new CustomField("ref", "VARCHAR(30)", true, "Reference", ""));
        fields.add(new CustomField("alt", "VARCHAR(30)", true, "Alternate", ""));
        fields.add(new CustomField("qual", "FLOAT(10,0)", true, "Quality", ""));
        fields.add(new CustomField("filter", "VARCHAR(500)", false, "Filter", ""));
        fields.add(new CustomField("aa", "VARCHAR(500)", true, "Ancestral Allele", ""));
        fields.add(new CustomField("ac", "VARCHAR(500)", true, "Allele Count", ""));
        fields.add(new CustomField("af", "VARCHAR(500)", true, "Allele Frequency", ""));
        fields.add(new CustomField("an", "INT(11)", true, "Number of alleles", ""));
        fields.add(new CustomField("bq", "FLOAT", true, "Base Quality", ""));
        fields.add(new CustomField("cigar", "VARCHAR(500)", false, "Cigar", ""));
        fields.add(new CustomField("db", "INT(1)", true, "dbSNP Membership", ""));
        fields.add(new CustomField("dp", "INT(11)", true, "Depth of Coverage", ""));
        fields.add(new CustomField("end", "VARCHAR(500)", true, "End Position", ""));
        fields.add(new CustomField("h2", "INT(1)", true, "HapMap2 Membership", ""));
        fields.add(new CustomField("mq", "VARCHAR(500)", true, "Mapping Quality", ""));
        fields.add(new CustomField("mq0", "VARCHAR(500)", true, "Number of MQ0s", ""));
        fields.add(new CustomField("ns", "INT(11)", true, "Number of samples", ""));
        fields.add(new CustomField("sb", "VARCHAR(500)", true, "Strand Bias", ""));
        fields.add(new CustomField("somatic", "INT(1)", true, "Somatic", ""));
        fields.add(new CustomField("validated", "INT(1)", true, "Validated", ""));
        fields.add(new CustomField("custom_info", "VARCHAR(500)", false, "Custom Info", ""));
        
        return new AnnotationFormat("default", "default", 0, "", true, true, AnnotationType.POSITION, fields);
    }
    
}

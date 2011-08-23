/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.ut.biolab.medsavant.db.table.VariantTableSchema;
import org.ut.biolab.medsavant.model.Range;

/**
 *
 * @author Nirvana Nursimulu
 */
public class ClassifiedPositionInfo {
    
    
    /**
     * Name of the column with chromosome info.
     */
    public final static String CHROM_COL = VariantTableSchema.ALIAS_CHROM;
    
    /**
     * Name of the column with position info.
     */
    public final static String POSITION_COL = VariantTableSchema.ALIAS_POSITION;

    
    /**
     * Map of chromosomes to a list of ranges for the corresponding chromosome)
     */
    private HashMap<String, List<Range>> mapChrToLocations;
    
    
    public ClassifiedPositionInfo(){
        
        // initialise the keys for the hashmap.
        initMap();
    }
    
    
    private void initMap(){
        mapChrToLocations = new HashMap<String, List<Range>>();
        for (int counter = 1; counter <= 22; counter++){
            mapChrToLocations.put("chr" + counter, new ArrayList<Range>());
        }
        mapChrToLocations.put("chrX", new ArrayList<Range>());
        mapChrToLocations.put("chrY", new ArrayList<Range>());
    }
    
    
    /**
     * Add a condition to this statement.
     * @param chromosome the chromosome in question
     * @param start the start position
     * @param end the end position
     */
    public void addCondition(String chromosome, double start, double end){
        List<Range> ranges = mapChrToLocations.get(chromosome);
        // for "invalid" chromosome IDs.
        if (ranges == null){
            return;
        }
        Range newRange = new Range(start, end);
        ranges.add(newRange);
        
    }
    
    public HashMap<String, List<Range>> getConditions(){
        
        HashMap<String, List<Range>> map = new HashMap<String, List<Range>>();
        
        for (String key: mapChrToLocations.keySet()){
//            List<Range> ranges = Range.merge(mapChrToLocations.get(key));
            List<Range> ranges = mapChrToLocations.get(key);
            if (ranges.isEmpty())
                ;
            else{
                map.put(key, Range.merge(ranges));
            }
        }
        return map;
    }
    
    public List<String> getAllMergedRanges(){
        
        List<String> mergedRanges = new ArrayList<String>();
        HashMap<String, List<Range>> mapRanges = this.getConditions();
        
        for (String key: mapRanges.keySet()){
            List<Range> ranges = mapRanges.get(key);
            for (Range currRange: ranges){
                
                String formatted = 
                        key.trim() + "\t" + (long)currRange.getMin() + "\t" + (long)currRange.getMax();
                mergedRanges.add(formatted);
//                System.out.println(formatted);
            }
        }
        return mergedRanges;
    }
    
}

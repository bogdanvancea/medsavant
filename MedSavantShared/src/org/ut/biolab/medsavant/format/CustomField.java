/*
 *    Copyright 2011-2012 University of Toronto
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
package org.ut.biolab.medsavant.format;

import java.io.Serializable;
import java.sql.Date;

import org.ut.biolab.medsavant.db.ColumnType;


/**
 *
 * @author Andrew
 */
public class CustomField implements Serializable {

    private String columnName;
    private String columnType;
    private boolean filterable;
    private String alias;
    private String description;
    private ColumnType fieldType;
    private Category category;

    public static enum Category {

        PATIENT {
            public String toString() {
                    return "Patient Conditions";
                }
            }
        ,
        GENOTYPE {
            public String toString() {
                    return "Undefined";
                }
            }
        ,
        VARIANT {
            public String toString() {
                    return "Variant Conditions";
                }
            }
        ,
        GENOME_COORDS {
            public String toString() {
                    return "Ontology and Region Conditions";
                }
            }
        ,
        PLUGIN {
            public String toString() {
                    return "Plugin Conditions";
                }
            }
    }


    /*
    public static String categoryToString(Category cat){
        switch(cat){
            case PATIENT:
                return "Patient & Phenotype";
            case VARIANT:
                return "Variant Annotation";
            case GENOME_COORDS:
                return "Genomic Coordinates";
            case PLUGIN:
                return "Plugins";
            default:
                return "undefined";
        }
    }
    *
    */

    public CustomField(String name, String type, boolean filterable, String alias, String description){
        this(name, type, filterable, alias, description, Category.VARIANT);
    }

    public CustomField(String name, String type, boolean filterable, String alias, String description, Category category){
        this.columnName = name;
        this.columnType = type;
        this.filterable = filterable;
        this.alias = alias;
        this.description = description;
        setColumnType(columnType);
        this.category = category;
    }

    public String getAlias() {
        return alias;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnTypeString() {
        return columnType;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFilterable() {
        return filterable;
    }

    private void setColumnType(String type){
        fieldType = ColumnType.fromString(type);
    }

    public ColumnType getColumnType(){
        return fieldType;
    }

    public Class getColumnClass(){
        switch(fieldType){
            case BOOLEAN:
                return Boolean.class;
            case INTEGER:
                return Integer.class;
            case DATE:
                return Date.class;
            case DECIMAL:
                return Double.class;
            case FLOAT:
                return Float.class;
            case VARCHAR:
            default:
                return String.class;
        }
    }

    public String getColumnLength(){
        int posLeft = columnType.indexOf("(");
        if(posLeft == -1){
            return "";
        }
        int posRight = columnType.indexOf(")");
        return columnType.substring(posLeft+1, posRight);
    }

    public String generateSchema(){
        return generateSchema(false);
    }

    public String generateSchema(boolean forceLowerCase){
        return "`" + (forceLowerCase ? columnName.toLowerCase() : columnName) + "` " + columnType + " DEFAULT NULL,";
    }

    public Category getCategory() {
        return category;
    }

    public boolean isNumeric() {
        return fieldType.isNumeric();
    }

    @Override
    public String toString(){
        return alias;
    }

    @Override
    public boolean equals(Object o){
        if(o == null || !o.getClass().equals(CustomField.class)) return false;
        CustomField other = (CustomField)o;
        return this.getColumnName().equals(other.getColumnName()) &&
                this.getColumnTypeString().equals(other.getColumnTypeString());
    }

}
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

package org.ut.biolab.medsavant.db.api;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;

/**
 * Plugin-friendly interface for exposing table schemata.
 *
 * @author tarkvara
 */
public interface TableSchemaAdapter {
    
    public DbColumn getDBColumn(String colName);

    /**
     * Enum which describes the recognised column data-types.
     */
    public enum ColumnType {
        VARCHAR,
        BOOLEAN,
        INTEGER,
        FLOAT,
        DECIMAL,
        DATE;

        public boolean isNumeric() {
            return this == INTEGER || this == FLOAT  || this == DECIMAL;
        }

        public boolean isInt() {
            return this == INTEGER;
        }

        public boolean isFloat() {
            return this == FLOAT || this == DECIMAL;
        }

        public boolean isBoolean() {
            return this == BOOLEAN;
        }

        @Override
        public String toString() {
            switch (this) {
                case INTEGER:
                    return "int";
                case FLOAT:
                    return "float";
                case BOOLEAN:
                    return "bool";
                case VARCHAR:
                    return "varchar";
                case DECIMAL:
                    return "decimal";
                case DATE:
                default:
                    return "date";
            }
        }
    }
}

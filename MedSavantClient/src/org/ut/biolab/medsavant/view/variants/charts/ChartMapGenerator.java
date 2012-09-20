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

package org.ut.biolab.medsavant.view.variants.charts;

import org.ut.biolab.medsavant.filter.WhichTable;

/**
 *
 * @author mfiume
 */
public interface ChartMapGenerator {

    public ChartFrequencyMap generateChartMap(boolean useFilterConditions, boolean isLogScaleX) throws Exception;

    public boolean isNumeric();

    public String getName();

    public WhichTable getTable();

    // usually the column name
    public String getFilterId();
}

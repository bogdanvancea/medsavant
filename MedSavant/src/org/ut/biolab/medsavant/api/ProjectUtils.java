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

package org.ut.biolab.medsavant.api;

import java.sql.SQLException;

import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.model.structure.CustomTables;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;


/**
 * API for project-related functionality exposed to plugins.  Expect this to be refactored at
 * some point.
 *
 * @author tarkvara
 */
public class ProjectUtils {
    /**
     * @return an integer which uniquely identifies the current project
     */
    public static int getCurrentProjectID() {
        return ProjectController.getInstance().getCurrentProjectId();
    }

    /**
     * @return an integer which uniquely identifies the current reference
     */
    public static int getCurrentReferenceID() {
        return ReferenceController.getInstance().getCurrentReferenceId();
    }
    
    public static TableSchema getCustomVariantTableSchema(int projectID, int refID) throws SQLException {
        return CustomTables.getVariantTableSchema(ProjectQueryUtil.getVariantTablename(projectID, refID));
    }
}

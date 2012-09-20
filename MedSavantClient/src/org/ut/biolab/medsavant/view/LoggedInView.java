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

package org.ut.biolab.medsavant.view;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.cohort.CohortsPage;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.UserLevel;
import org.ut.biolab.medsavant.patient.IndividualsPage;
import org.ut.biolab.medsavant.project.ProjectsSection;
import org.ut.biolab.medsavant.region.RegionPage;
import org.ut.biolab.medsavant.variant.VariantFilesPage;
import org.ut.biolab.medsavant.view.variants.GeneticsSection;
import org.ut.biolab.medsavant.view.manage.ManageSection;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;


/**
 *
 * @author mfiume
 */
public class LoggedInView extends JPanel {
    private ViewController viewController;

    public LoggedInView() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        viewController = ViewController.loggedIn(this);
        viewController.clearMenu();

        viewController.addSection(new ProjectsSection());
        viewController.addSection(new ListsSection());
        viewController.addSection(new GeneticsSection());

        if (LoginController.getInstance().getUserLevel() == UserLevel.ADMIN) {
            viewController.addSection(new ManageSection());
        }

        viewController.selectFirstItem();
    }

    private class ListsSection extends SectionView {
        private ListsSection() {
            super("Tables");
        }

        @Override
        public SubSectionView[] getSubSections() {
            return new SubSectionView[] {
                new IndividualsPage(this),
                new CohortsPage(this),
                new RegionPage(this),
                new VariantFilesPage(this)
            };
        }
    }
}

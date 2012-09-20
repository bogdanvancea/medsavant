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
package org.ut.biolab.medsavant.cohort;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Cohort;
import org.ut.biolab.medsavant.model.SimplePatient;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.component.StripyTable;
import org.ut.biolab.medsavant.view.variants.GeneticsFilterPage;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class CohortDetailedView extends DetailedView {

    private static final Log LOG = LogFactory.getLog(CohortDetailedView.class);

    private Cohort[] cohorts;
    private CohortDetailsWorker worker;
    private final JPanel details;
    private JTable list;
    private final CollapsiblePane membersPane;

    CohortDetailedView(String page) {
        super(page);

        JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
        viewContainer.setLayout(new BorderLayout());

        JPanel infoContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(infoContainer);

        viewContainer.add(ViewUtil.getClearBorderlessScrollPane(infoContainer), BorderLayout.CENTER);

        CollapsiblePanes panes = new CollapsiblePanes();
        panes.setOpaque(false);
        infoContainer.add(panes);

        membersPane = new CollapsiblePane();
        membersPane.setStyle(CollapsiblePane.TREE_STYLE);
        membersPane.setCollapsible(false);
        panes.add(membersPane);


        details = new JPanel();
        details.setLayout(new BorderLayout());
        membersPane.setLayout(new BorderLayout());
        membersPane.add(details, BorderLayout.CENTER);

        panes.addExpansion();
    }

    private class CohortDetailsWorker extends MedSavantWorker<List<SimplePatient>> {

        private final Cohort cohort;

        CohortDetailsWorker(Cohort coh) {
            super(getPageName());
            cohort = coh;
        }

        @Override
        protected List<SimplePatient> doInBackground() throws Exception {
            List<SimplePatient> patientList = MedSavantClient.CohortManager.getIndividualsInCohort(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectID(),
                    cohort.getId());
            return patientList;
        }

        @Override
        protected void showProgress(double ignored) {
        }

        @Override
        protected void showSuccess(List<SimplePatient> result) {
            setPatientList(result);
        }
    }

    public synchronized void setPatientList(List<SimplePatient> patients) {

        details.removeAll();

        Object[][] data = new Object[patients.size()][1];
        for (int i = 0; i < patients.size(); i++) {
            data[i][0] = patients.get(i);
        }

        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(ViewUtil.getBigBorder());
        ViewUtil.applyVerticalBoxLayout(p);

        list = new StripyTable(data, new String[]{"Member Hospital IDs"});
        list.setBorder(null);
        list.setShowGrid(false);
        list.setRowHeight(21);

        p.add(ViewUtil.alignLeft(new JLabel(ViewUtil.numToString(list.getRowCount()) + " members")));
        p.add(Box.createRigidArea(new Dimension(10, 10)));
        p.add(ViewUtil.getClearBorderedScrollPane(list));

        JPanel menu = ViewUtil.getClearPanel();
        menu.add(removeIndividualsButton());
        p.add(menu);

        details.add(p, BorderLayout.CENTER);

        details.updateUI();
    }

    @Override
    public void setSelectedItem(Object[] item) {
        cohorts = new Cohort[] { (Cohort)item[0] };
        membersPane.setTitle(cohorts[0].getName());

        details.removeAll();
        details.updateUI();

        if (worker != null) {
            worker.cancel(true);
        }
        worker = new CohortDetailsWorker(cohorts[0]);
        worker.execute();
    }

    @Override
    public void setMultipleSelections(List<Object[]> items) {
        cohorts = new Cohort[items.size()];
        for (int i = 0; i < items.size(); i++) {
            cohorts[i] = (Cohort)items.get(i)[0];
        }
        if (items.isEmpty()) {
            membersPane.setTitle("");
        } else {
            membersPane.setTitle("Multiple cohorts (" + items.size() + ")");
        }
        details.removeAll();
        details.updateUI();
    }

    private JLabel removeIndividualsButton() {

        JLabel button = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.REMOVE_ON_TOOLBAR));
        button.setToolTipText("Remove selected");
        //JButton button = new JButton("Remove individual(s) from cohort");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.setOpaque(false);
        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int[] rows = list.getSelectedRows();
                int[] patientIDs = new int[rows.length];
                for (int i = 0; i < rows.length; i++) {
                    patientIDs[i] = ((SimplePatient) list.getModel().getValueAt(rows[i], 0)).getId();
                }
                if (patientIDs != null && patientIDs.length > 0) {

                    if (DialogUtils.askYesNo("Confirm", "Are you sure you want to remove these individual(s)?") == DialogUtils.NO) {
                        return;
                    }

                    try {
                        MedSavantClient.CohortManager.removePatientsFromCohort(LoginController.sessionId, patientIDs, cohorts[0].getId());
                    } catch (Exception ex) {
                        LOG.error("Error removing patients from cohort.", ex);
                    }

                    worker = new CohortDetailsWorker(cohorts[0]);
                    worker.execute();
                }
            }
        });

        return button;
    }

    @Override
    public JPopupMenu createPopup() {
        JPopupMenu popupMenu = new JPopupMenu();

        if (ProjectController.getInstance().getCurrentVariantTableSchema() == null) {
            popupMenu.add(new JLabel("(You must choose a variant table before filtering)"));
        } else {

            //Filter by patient
            JMenuItem filter1Item = new JMenuItem(String.format("<html>Filter by %s</html>", cohorts.length == 1 ? "Cohort <i>" + cohorts[0] + "</i>" : "Selected Cohorts"));
            filter1Item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    GeneticsFilterPage.getSearchBar().loadFilters(CohortFilterView.wrapState(Arrays.asList(cohorts)));
                }
            });
            popupMenu.add(filter1Item);
        }

        return popupMenu;
    }
}

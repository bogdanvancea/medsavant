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
package org.ut.biolab.medsavant.view.variants;

import java.awt.BorderLayout;
import java.awt.Component;
import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.swing.JPanel;
import javax.swing.*;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.filter.FilterController;
import org.ut.biolab.medsavant.filter.FilterEvent;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Chromosome;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.reference.ReferenceEvent;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.ThreadController;
import org.ut.biolab.medsavant.view.variants.inspector.InspectorPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.PeekingPanel;


/**
 *
 * @author mfiume
 */
public class GeneticsTablePage extends SubSectionView {

    private JPanel panel;
    private TablePanel tablePanel;
    private Component[] settingComponents;
    private PeekingPanel detailView;

    public GeneticsTablePage(SectionView parent) {
        super(parent, "Spreadsheet");
        FilterController.getInstance().addListener(new Listener<FilterEvent>() {
            @Override
            public void handleEvent(FilterEvent event) {
                updateContents();
            }

        });
        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED) {
                    updateContents();
                }
            }
        });
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        if (settingComponents == null) {
            settingComponents = new Component[1];
            settingComponents[0] = PeekingPanel.getCheckBoxForPanel(detailView, "Inspector");
        }
        return settingComponents;
    }

    @Override
    public JPanel getView(boolean update) {
        try {
            if (panel == null || update) {
                ThreadController.getInstance().cancelWorkers(pageName);
                setPanel();
            } else {
                tablePanel.update();
            }
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error generating genome view: %s", ex);
        }
        return panel;
    }

    private void setPanel() throws SQLException, RemoteException {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JTabbedPane inspectorPanel = InspectorPanel.getInstance();

        detailView = new PeekingPanel("Detail", BorderLayout.WEST, inspectorPanel, false, 380);
        detailView.setToggleBarVisible(false);

        panel.add(detailView, BorderLayout.EAST);

        tablePanel = new TablePanel(pageName);
        panel.add(tablePanel, BorderLayout.CENTER);
    }



    @Override
    public void viewDidLoad() {
        super.viewDidLoad();
        tablePanel.update();
    }

    @Override
    public void viewDidUnload() {
        if (tablePanel != null && !tablePanel.isInit()) {
            setUpdateRequired(true);
        }
        super.viewDidUnload();
    }

    public void updateContents() {
        ThreadController.getInstance().cancelWorkers(pageName);
        if (tablePanel == null) {
            return;
        }
        tablePanel.setUpdateRequired(true);
        if (loaded) {
            tablePanel.update();
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.listener.ProjectListener;
import org.ut.biolab.medsavant.view.dialog.SavantExportForm;
import org.ut.biolab.medsavant.view.manage.ImportVariantsWizard;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.subview.SectionView;

/**
 *
 * @author mfiume
 */
public class GeneticsSection extends SectionView implements ProjectListener {

    private JPanel[] panels;
    private JComboBox referenceDropDown;
    public static boolean isInitialized = false;

    public GeneticsSection() {
        setPersistencePanels();
        getBanner(); // force banner to be active, in turn forcing default reference selection
    }

    @Override
    public String getName() {
        return "Genetic Variants";
    }

    @Override
    public SubSectionView[] getSubSections() {
        SubSectionView[] pages = new SubSectionView[4];
        pages[0] = new GeneticsFilterPage(this);
        pages[1] = new GeneticsTablePage(this);
        pages[2] = new GeneticsChartPage(this);
        pages[3] = new AggregatePage(this);
        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        return panels;
    }

    public JButton createVcfButton() {
        JButton button = new JButton("Import Variants");

        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                new ImportVariantsWizard();
                //new VCFUploadForm();
            }
        });

        return button;
    }

    private JButton addShowInSavantButton() {
        JButton button = new JButton("Show in Savant");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                new SavantExportForm();
            }
        });
        return button;
    }

    private JButton addSaveResultSetButton() {
        JButton button = new JButton("Save Variants");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //TODO
            }
        });
        return button;
    }

    @Override
    public Component[] getBanner() {

        Component[] result = new Component[4];
        result[0] = new JLabel("Reference:");
        if (referenceDropDown == null) {
            result[1] = getReferenceDropDown();
        } else {
            result[1] = referenceDropDown;
        }
        result[2] = addShowInSavantButton();
        result[3] = createVcfButton();
        //result[0] = addSaveResultSetButton();

        isInitialized = true;
        return result;
    }

    private void setPersistencePanels() {
        try {
            /*panels = new JPanel[2];
            panels[0] = new FilterPanel();
            panels[1] = new FilterProgressPanel();
             *
             */
            //TODO: account for exception in filter panel instead
        } catch (Exception ex) {
        }
    }

    private Component getReferenceDropDown() {
        referenceDropDown = new JComboBox();

        referenceDropDown.setMinimumSize(new Dimension(200, 23));
        referenceDropDown.setPreferredSize(new Dimension(200, 23));
        referenceDropDown.setMaximumSize(new Dimension(200, 23));
        try {
            refreshReferenceDropDown();
        } catch (RemoteException ex) {
            Logger.getLogger(GeneticsSection.class.getName()).log(Level.SEVERE, null, ex);
        }

        ProjectController.getInstance().addProjectListener(this);

        return referenceDropDown;
    }

    private void refreshReferenceDropDown() throws RemoteException {
        try {
            for (ActionListener l : referenceDropDown.getActionListeners()) {
                referenceDropDown.removeActionListener(l);
            }
            referenceDropDown.removeAllItems();

            List<String> references = MedSavantClient.ReferenceQueryUtilAdapter.getReferencesForProject(
                    LoginController.sessionId, 
                    ProjectController.getInstance().getCurrentProjectId());

            for (String refname : references) {

                int refid = ReferenceController.getInstance().getReferenceId(refname);

                int numVariantsInTable = ProjectController.getInstance().getNumVariantsInTable(ProjectController.getInstance().getCurrentProjectId(), refid);

                referenceDropDown.addItem(refname); // + " (" + numVariantsInTable + " variants)");
            }

            if (references.isEmpty()) {
                referenceDropDown.addItem("No References");
                referenceDropDown.setEnabled(false);
            } else {
                referenceDropDown.setEnabled(true);
                referenceDropDown.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String currentName = ReferenceController.getInstance().getCurrentReferenceName();
                        if (!ReferenceController.getInstance().setReference((String) referenceDropDown.getSelectedItem(), true)) {
                            referenceDropDown.setSelectedItem(currentName);
                        }
                    }
                });
                ReferenceController.getInstance().setReference((String) referenceDropDown.getSelectedItem());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    public void projectAdded(String projectName) {
        try {
            refreshReferenceDropDown();
        } catch (RemoteException ex) {
            Logger.getLogger(GeneticsSection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void projectRemoved(String projectName) {
        try {
            refreshReferenceDropDown();
        } catch (RemoteException ex) {
            Logger.getLogger(GeneticsSection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void projectChanged(String projectName) {
        try {
            refreshReferenceDropDown();
        } catch (RemoteException ex) {
            Logger.getLogger(GeneticsSection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void projectTableRemoved(int projid, int refid) {
        try {
            refreshReferenceDropDown();
        } catch (RemoteException ex) {
            Logger.getLogger(GeneticsSection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
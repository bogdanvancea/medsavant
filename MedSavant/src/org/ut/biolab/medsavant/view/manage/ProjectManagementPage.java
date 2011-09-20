/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import com.jidesoft.utils.SwingWorker;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.view.genetics.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ProjectController.ProjectListener;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.model.record.Chromosome;
import org.ut.biolab.medsavant.model.record.Genome;
import org.ut.biolab.medsavant.util.view.PeekingPanel;
import org.ut.biolab.medsavant.view.MainFrame;
import org.ut.biolab.medsavant.view.dialog.SavantExportForm;
import org.ut.biolab.medsavant.view.patients.DetailedListModel;
import org.ut.biolab.medsavant.view.patients.DetailedView;
import org.ut.biolab.medsavant.view.patients.SplitScreenView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ProjectManagementPage extends SubSectionView implements ProjectListener {


    public void projectAdded(String projectName) {
        System.out.println("Project added: " + projectName);
            panel.refresh();
        }

        public void projectRemoved(String projectName) {
            panel.refresh();
        }

        public void projectChanged(String projectName) {
            panel.refresh();
        }

    private static class ProjectsDetailedView extends DetailedView  {
        private final JPanel menu;
        private final JPanel details;
        private final JPanel content;
        private String projectName;
        private ProjectDetailsSW sw;

        public ProjectsDetailedView() {
        
        content = this.getContentPanel();
        
        details = ViewUtil.getClearPanel();
        menu = ViewUtil.getButtonPanel();
        
        menu.add(changeReferenceGenomeButton());
        menu.add(deleteProjectButton());

        menu.setVisible(false);
        
        content.setLayout(new BorderLayout());
        
        content.add(details,BorderLayout.CENTER);
        content.add(menu,BorderLayout.SOUTH);
    }
        
        public final JButton deleteProjectButton() {
            JButton b = new JButton("Delete Project");
            b.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    
                    int result = JOptionPane.showConfirmDialog(MainFrame.getInstance(), 
                             "Are you sure you want to delete \n" + projectName + "? This cannot be undone.",
                             "Confirm", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        ProjectController.getInstance().removeProject(projectName);
                    }
                }
                
            });
            return b;
        }
        
        public final JButton changeReferenceGenomeButton() {
            JButton b = new JButton("Change Reference");
            return b;
        }
    
    @Override
    public void setSelectedItem(Vector item) {
        projectName = (String) item.get(0);
        setTitle(projectName);
        
        details.removeAll();
        details.updateUI();
        
        if (sw != null) {
            sw.cancel(true);
        }
        sw = new ProjectDetailsSW(projectName);
        sw.execute();
        
        if(menu != null) menu.setVisible(true);
    }

        
    
    private static class ProjectDetailsSW extends SwingWorker {

        public ProjectDetailsSW(String projectName) {
        }

            @Override
            protected Object doInBackground() throws Exception {
                return null;
            }

    }
    
    @Override
    public void setMultipleSelections(List<Vector> items){
    }
    
    }

    private static class ProjectsListModel implements DetailedListModel {
        private ArrayList<String> cnames;
        private ArrayList<Class> cclasses;
        private ArrayList<Integer> chidden;

        public ProjectsListModel() {
        }

        public List<Vector> getList(int limit) throws Exception {
            List<String> projects = ProjectController.getInstance().getProjectNames();
            List<Vector> projectVector = new ArrayList<Vector>();
            for (String p : projects) {
                Vector v = new Vector();
                v.add(p);
                projectVector.add(v);
            }
            return projectVector;
        }

        public List<String> getColumnNames() {
            if (cnames == null) {
                cnames = new ArrayList<String>();
                cnames.add("Project");
            }
            return cnames;
        }

        public List<Class> getColumnClasses() {
            if (cclasses == null) {
                cclasses = new ArrayList<Class>();
                cclasses.add(String.class);
            }
            return cclasses;
        }

        public List<Integer> getHiddenColumns() {
            if (chidden == null) {
                chidden = new ArrayList<Integer>();
            }
            return chidden;
        }
    }

    private SplitScreenView panel;

    public ProjectManagementPage(SectionView parent) { 
        super(parent); 
        ProjectController.getInstance().addProjectListener(this);
    }

    public String getName() {
        return "Projects";
    }

    public JPanel getView() {
        if (panel == null) {
            setPanel();
        }
        return panel;
    }
    
    public void setPanel() { 
        panel = new SplitScreenView(
                new ProjectsListModel(), 
                new ProjectsDetailedView());
    }
    
    @Override
    public Component[] getBanner() {
        Component[] result = new Component[1];
        result[0] = getAddPatientsButton();
        return result;
    }
    
    private JButton getAddPatientsButton(){
        JButton button = new JButton("New Project");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                NewProjectDialog npd = new NewProjectDialog(MainFrame.getInstance(),true);
                npd.setVisible(true);
            }
        });
        return button;
    }
    
    @Override
    public void viewLoading() {
    }

    @Override
    public void viewDidUnload() {
    }
    
}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ChangeVariantDialog.java
 *
 * Created on 21-Sep-2011, 12:10:38 PM
 */
package org.ut.biolab.medsavant.view.manage;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import org.ut.biolab.medsavant.db.util.jobject.Annotation;
import org.ut.biolab.medsavant.db.util.jobject.AnnotationQueryUtil;
import org.ut.biolab.medsavant.db.util.jobject.ProjectQueryUtil;

/**
 *
 * @author Andrew
 */
public class ChangeVariantDialog extends javax.swing.JDialog {
    
    private int projectId;
    private int refId;

    /** Creates new form ChangeVariantDialog */
    public ChangeVariantDialog(java.awt.Frame parent, boolean modal, int projectId, int refId, String refName, String annotationIdsString) throws SQLException {
        super(parent, modal);
        initComponents();
        
        this.projectId = projectId;
        this.refId = refId;
        
        
        //set up combo
        /*List<String> referenceNames = ReferenceQueryUtil.getReferenceNames();
        referenceCombo.removeAllItems();
        for(String referenceName : referenceNames){
            referenceCombo.addItem(referenceName);
        }
        if(refName != null){
            referenceCombo.setSelectedItem(refName);
        }*/
        
        
        //set up annotation list
        List<Annotation> annotations = AnnotationQueryUtil.getAnnotations();
        List<Integer> annotationIds = new ArrayList<Integer>();
        if(annotationIdsString != null){
            for(String s : annotationIdsString.split(",")){
                annotationIds.add(Integer.parseInt(s));
            }
        }
        
        DefaultListModel model = new DefaultListModel();
        for(Annotation a : annotations){
            if(a.getReference().equals(refName)){
                model.addElement(new CheckListItem(a, annotationIds.contains(a.getId())));
            }
        }
        annotationList.setModel(model);
        
        annotationList.setCellRenderer(new CheckListRenderer());
        annotationList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event){
                JList list = (JList) event.getSource();
                int index = list.locationToIndex(event.getPoint());
                CheckListItem item = (CheckListItem) list.getModel().getElementAt(index);
                item.setSelected(! item.isSelected());
                list.repaint();
            }
        });

  
        this.setLocationRelativeTo(parent);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        annotationList = new javax.swing.JList();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jScrollPane1.setViewportView(annotationList);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        try {
            
            String annotationIds = "";
            ListModel model = annotationList.getModel();
            for(int i = 0; i < model.getSize(); i++){
                CheckListItem item = (CheckListItem)model.getElementAt(i);
                if(item.isSelected()){
                    annotationIds += item.getAnnotation().getId() + ",";
                }
            }          
            if(annotationIds.endsWith(",")){
                annotationIds = annotationIds.substring(0, annotationIds.length()-1);
            }
            
            ProjectQueryUtil.setAnnotations(projectId, refId, annotationIds);
                        
        } catch (SQLException ex) {
            Logger.getLogger(ChangeVariantDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.setVisible(false);
        this.dispose();
        
    }//GEN-LAST:event_okButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList annotationList;
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
}

class CheckListItem{
    
    private String  label;
    private boolean isSelected = false;
    private Annotation a;

    public CheckListItem(Annotation a, boolean selected){
        this.label = a.getProgram() + " " + a.getVersion();
        this.isSelected = selected;
        this.a = a;
    }

    public boolean isSelected(){
        return isSelected;
    }

    public void setSelected(boolean isSelected){
        this.isSelected = isSelected;
    }
    
    public Annotation getAnnotation(){
        return a;
    }

    public String toString(){
        return label;
    }
}

class CheckListRenderer extends JCheckBox implements ListCellRenderer {
    
    public Component getListCellRendererComponent(
        JList list, Object value, int index,
        boolean isSelected, boolean hasFocus){
        
        setEnabled(list.isEnabled());
        setSelected(((CheckListItem)value).isSelected());
        setFont(list.getFont());
        setBackground(list.getBackground());
        setForeground(list.getForeground());
        setText(value.toString());
        return this;
    }
}
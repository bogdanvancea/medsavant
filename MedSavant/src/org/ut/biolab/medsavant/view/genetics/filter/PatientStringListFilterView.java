/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.olddb.ConnectionController;
import org.ut.biolab.medsavant.olddb.MedSavantDatabase;
import org.ut.biolab.medsavant.olddb.QueryUtil;
import org.ut.biolab.medsavant.olddb.table.TableSchema;
import org.ut.biolab.medsavant.olddb.table.VariantTableSchema;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 * Create a FilterView with a list of checkboxes, which in turn filter based
 * on DNA Id. For example: ethnic group, family id, ...
 * 
 * @author AndrewBrook
 */
public class PatientStringListFilterView {
    
    public static FilterView createFilterView(TableSchema table, String filterName, String dbCol){
        return new FilterView(filterName, getContentPanel(table, filterName, dbCol));
    }
    
    private static List<String> getDefaultValues(TableSchema table, String filterName, String dbCol) {
        try {
            return QueryUtil.getDistinctValuesForColumn(ConnectionController.connect(), table, table.getDBColumn(dbCol));
        } catch (Exception ex) {
            Logger.getLogger(PatientStringListFilterView.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private static JComponent getContentPanel(final TableSchema table, final String filterName, final String dbCol) {
        
        List<String> uniq = getDefaultValues(table, filterName, dbCol);
        
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);
        final List<JCheckBox> boxes = new ArrayList<JCheckBox>();

        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                final List<String> acceptableValues = new ArrayList<String>();
                for (JCheckBox b : boxes) {
                    if (b.isSelected()) {
                        acceptableValues.add(b.getText());
                    }
                }

                if (acceptableValues.size() == boxes.size()) {
                    FilterController.removeFilter(filterName, 0); //TODO
                } else {
                    Filter f = new QueryFilter() {

                        @Override
                        public Condition[] getConditions() {
                            try {

                                List<String> individuals = QueryUtil.getDNAIdsForList(table, acceptableValues, dbCol);
                                
                                Condition[] results = new Condition[individuals.size()];
                                int i = 0;
                                for (String ind : individuals) {
                                    results[i] = BinaryCondition.equalTo(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(VariantTableSchema.ALIAS_DNAID), ind);
                                    i++;
                                }

                                return results;
                                
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                return null;
                            }
                        }

                        @Override
                        public String getName() {
                            return filterName;
                        }
                                             
                        @Override
                        public String getId() {
                            return filterName;//TODO
                        }
                    };
                    FilterController.addFilter(f, 0); //TODO
                }
            }
        });

        for (String s : uniq) {
            JCheckBox b = new JCheckBox(s);
            b.setSelected(true);
            b.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    AbstractButton abstractButton =
                            (AbstractButton) e.getSource();
                    ButtonModel buttonModel = abstractButton.getModel();
                    boolean pressed = buttonModel.isPressed();
                    if (pressed) {                       
                        applyButton.setEnabled(true);
                    }
                }
            });
            b.setAlignmentX(0F);
            container.add(b);
            boxes.add(b);
        }

        JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
        selectAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for (JCheckBox c : boxes) {
                    c.setSelected(true);
                    applyButton.setEnabled(true);
                }
            }
        });
        bottomContainer.add(selectAll);

        JButton selectNone = ViewUtil.createHyperLinkButton("Select None");

        selectNone.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for (JCheckBox c : boxes) {
                    c.setSelected(false);
                    applyButton.setEnabled(true);
                }
            }
        });
        bottomContainer.add(selectNone);

        bottomContainer.add(Box.createGlue());

        bottomContainer.add(applyButton);

        bottomContainer.setAlignmentX(0F);
        container.add(bottomContainer);

        return container;
    }
  
}

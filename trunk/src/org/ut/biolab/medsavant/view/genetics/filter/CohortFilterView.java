/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.jidesoft.range.Range;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.db.table.VariantTableSchema;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;

/**
 *
 * @author mfiume
 */
class CohortFilterView {

    private static final String FILTER_NAME = "Cohort";
    private static final String COHORT_ALL = "All Individuals";

    static FilterView getCohortFilterView() {
        return new FilterView("Cohort", getContentPanel());
    }
    
    private static List<String> getDefaultValues() {
        List<String> list = FilterCache.getDefaultValues(FILTER_NAME);
        if(list == null){
            try {
                list = QueryUtil.getDistinctCohortNames(-1);
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.getLogger(CohortFilterView.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        FilterCache.addDefaultValues(FILTER_NAME, list);
        return list;
    }   

    private static JComponent getContentPanel() {

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        final JComboBox b = new JComboBox();

        b.addItem(COHORT_ALL);
        List<String> cohortNames = getDefaultValues();
        for (String cohortName : cohortNames) {
            b.addItem(cohortName);
        }

        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);

        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                if (((String) b.getSelectedItem()).equals(COHORT_ALL)) {
                    FilterController.removeFilter(FILTER_NAME);
                } else {
                    Filter f = new QueryFilter() {

                        @Override
                        public Condition[] getConditions() {

                            String cohortName = (String) b.getSelectedItem();

                            try {

                                List<String> individuals = QueryUtil.getDNAIdsForIndividualsInCohort(cohortName);
                                

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
                            return FILTER_NAME;
                        }
                    };
                    //Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
                    System.out.println("Adding filter: " + f.getName());
                    FilterController.addFilter(f);
                }

                //TODO: why does this not work? Freezes GUI
                //apply.setEnabled(false);
            }
        });

        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                applyButton.setEnabled(true);
            }
        });



        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

        bottomContainer.add(Box.createHorizontalGlue());
        bottomContainer.add(applyButton);

        /*
        addChangeListener(new ChangeListener() {
        
        public void stateChanged(ChangeEvent e) {
        applyButton.setEnabled(true);
        }
        });
         * 
         */

        p.add(b, BorderLayout.CENTER);
        p.add(bottomContainer, BorderLayout.SOUTH);

        return p;
    }
}

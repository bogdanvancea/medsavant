/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import com.jidesoft.swing.RangeSlider;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.db.BasicQuery;
import org.ut.biolab.medsavant.db.ConnectionController;
import org.ut.biolab.medsavant.db.Database;
import org.ut.biolab.medsavant.db.table.TableSchema;
import org.ut.biolab.medsavant.db.table.VariantTableSchema;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;
import org.ut.biolab.medsavant.view.subview.Page;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 *
 * @author mfiume
 */
public class FilterPanel extends JPanel implements FiltersChangedListener {

    private final ArrayList<FilterView> filterViews;
    private CollapsiblePanes contentPanel;
    private JLabel status;

    public FilterPanel() throws NonFatalDatabaseException {
        this.setName("Filters");
        this.setLayout(new BorderLayout());
        filterViews = new ArrayList<FilterView>();
        FilterController.addFilterListener(this);
        initGUI();
    }
    

    private void initGUI() throws NonFatalDatabaseException {

        /*
        JPanel titlePanel = ViewUtil.getBannerPanel();
        JLabel title = new JLabel("Filters");
        ViewUtil.clear(title);
        title.setFont(ViewUtil.getMediumTitleFont());
        titlePanel.add(Box.createHorizontalGlue());
        titlePanel.add(title);
        titlePanel.add(Box.createHorizontalGlue());
        this.add(titlePanel,BorderLayout.NORTH);
         */
        
        JPanel statusPanel = ViewUtil.getBannerPanel();
        status = new JLabel("No filters applied");
        ViewUtil.clear(status);
        status.setFont(ViewUtil.getSmallTitleFont());
        statusPanel.add(Box.createHorizontalGlue());
        statusPanel.add(status);
        statusPanel.add(Box.createHorizontalGlue());
        this.add(statusPanel,BorderLayout.SOUTH);

        contentPanel = new CollapsiblePanes();
        contentPanel.setBackground(ViewUtil.getMenuColor());
        this.add(new JScrollPane(contentPanel), BorderLayout.CENTER);

        List<FilterView> fv;
        try {
            fv = getFilterViews();
            addFilterViews(fv);
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new FatalDatabaseException("Problem getting filters");
        }

        contentPanel.addExpansion();

        this.setPreferredSize(new Dimension(400,999));
    }

    public void addFilterViews(List<FilterView> filterViews) {
        for (FilterView view : filterViews) {
            addFilterView(view);
        }
    }

    private void addFilterView(FilterView view) {
        filterViews.add(view);
        CollapsiblePane cp = new CollapsiblePane(view.getTitle());
        try {
            cp.setCollapsed(true);
        } catch (PropertyVetoException ex) {
        }
        cp.setCollapsedPercentage(0);
        cp.setContentPane(view.getComponent());
        this.contentPanel.add(cp);
    }

    private List<FilterView> getFilterViews() throws SQLException, NonFatalDatabaseException {
        List<FilterView> views = new ArrayList<FilterView>();
        views.addAll(getVariantRecordFilterViews());
        views.add(GOFilter.getGOntologyFilterView()); 
        views.add(HPOFilter.getHPOntologyFilterView()); 
        // views.add(getGenderFilterView());
        // views.add(getAgeFilterView());
        // views.add(getGenderFilterView());
        // views.add(getAgeFilterView());

        // views.add(getGenderFilterView());
        // views.add(getAgeFilterView());

        return views;
    }
    
    

    void listenToComponent(final JCheckBox c) {
        c.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //setApplyButtonEnabled(true);
            }
        });
    }

    private List<FilterView> getVariantRecordFilterViews() throws SQLException, NonFatalDatabaseException {
        List<FilterView> l = new ArrayList<FilterView>();

        System.out.println("Making filters");

        List<String> fieldNames = VariantRecordModel.getFieldNames();
        int numFields = fieldNames.size();

        for (int i = 0; i < numFields; i++) {

            final int fieldNum = i;
            Class c = VariantRecordModel.getFieldClass(i);

            final String columnAlias = fieldNames.get(i);
            
            if (columnAlias.equals("Information")) { continue; }
            
            //System.out.println("Making filter for " + columnAlias);

            if (columnAlias.equals(VariantTableSchema.ALIAS_ID) || columnAlias.equals(VariantTableSchema.ALIAS_FILTER)) {// || columnAlias.equals(VariantTableSchema.ALIAS_INFORMATION)) {
                continue;
            }

            TableSchema table = Database.getInstance().getVariantTableSchema();
            DbColumn col = table.getDBColumn(columnAlias);
            boolean isNumeric = TableSchema.isNumeric(table.getColumnType(col));
            boolean isBoolean = TableSchema.isBoolean(table.getColumnType(col));

            if (isNumeric) {
                Range extremeValues = BasicQuery.getExtremeValuesForColumn(ConnectionController.connect(), table, col);

                JPanel container = new JPanel();
                container.setBorder(ViewUtil.getMediumBorder());
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

                final RangeSlider rs = new com.jidesoft.swing.RangeSlider();

                final int min = (int) Math.floor(extremeValues.getMin());
                final int max = (int) Math.ceil(extremeValues.getMax());
                
                rs.setMinimum(min);
                rs.setMaximum(max);

                rs.setMajorTickSpacing(5);
                rs.setMinorTickSpacing(1);

                rs.setLowValue(min);
                rs.setHighValue(max);

                JPanel rangeContainer = new JPanel();
                rangeContainer.setLayout(new BoxLayout(rangeContainer,BoxLayout.X_AXIS));
                
                
                final JLabel fromLabel = new JLabel(ViewUtil.numToString(min));
                final JLabel toLabel = new JLabel(ViewUtil.numToString(max));
                
                rangeContainer.add(fromLabel);
                rangeContainer.add(rs);
                rangeContainer.add(toLabel);
                
                container.add(rangeContainer);
                container.add(Box.createVerticalBox());
                
                rs.addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        fromLabel.setText(ViewUtil.numToString(rs.getLowValue()));
                        toLabel.setText(ViewUtil.numToString(rs.getHighValue()));
                    }
                    
                });
                
                

                final JButton applyButton = new JButton("Apply");
                applyButton.setEnabled(false);

                applyButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        applyButton.setEnabled(false);

                        Range acceptableRange = new Range(rs.getLowValue(),rs.getHighValue());

                        if (min == acceptableRange.getMin() && max == acceptableRange.getMax()) {
                            FilterController.removeFilter(VariantRecordModel.getFieldNameForIndex(fieldNum));
                        } else {
                            Filter f = new QueryFilter() {

                                @Override
                                public Condition[] getConditions() {
                                    Condition[] results = new Condition[2];
                                    results[0] = BinaryCondition.greaterThan(Database.getInstance().getVariantTableSchema().getDBColumn(columnAlias), rs.getLowValue(), true);
                                    results[1] = BinaryCondition.lessThan(Database.getInstance().getVariantTableSchema().getDBColumn(columnAlias), rs.getHighValue(), true);
                                    
                                    Condition[] resultsCombined = new Condition[1];
                                    resultsCombined[0] = ComboCondition.and(results);

                                    return resultsCombined;
                                }

                                @Override
                                public String getName() {
                                    return columnAlias;
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

                rs.addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        applyButton.setEnabled(true);
                    }

                });
                
                JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
                selectAll.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        rs.setLowValue(min);
                        rs.setHighValue(max);
                    }
                });
                
                JPanel bottomContainer = new JPanel();
                bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

                bottomContainer.add(selectAll);
                bottomContainer.add(Box.createHorizontalGlue());
                bottomContainer.add(applyButton);
                
                container.add(bottomContainer);

                final FilterView fv = new FilterView(columnAlias,container);
                l.add(fv);

            } else if (isBoolean) {

                List<String> uniq = new ArrayList<String>();
                uniq.add("True");
                uniq.add("False");

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

                        if(boxes.get(0).isSelected()) acceptableValues.add("1");
                        if(boxes.get(1).isSelected()) acceptableValues.add("0");

                        if (acceptableValues.size() == boxes.size()) {
                            FilterController.removeFilter(VariantRecordModel.getFieldNameForIndex(fieldNum));
                        } else {
                            Filter f = new QueryFilter() {

                                @Override
                                public Condition[] getConditions() {
                                    Condition[] results = new Condition[acceptableValues.size()];
                                    int i = 0;
                                    for (String s : acceptableValues) {
                                        results[i++] = BinaryCondition.equalTo(Database.getInstance().getVariantTableSchema().getDBColumn(columnAlias), s);
                                    }
                                    return results;
                                }

                                @Override
                                public String getName() {
                                    return columnAlias;
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
                            //System.out.println("Changed: a=" + armed + "/p=" + pressed + "/s=" + selected);
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

                FilterView fv = new FilterView(columnAlias, container);
                l.add(fv);


            } else {

                Connection conn = ConnectionController.connect();
           
                List<String> uniq = BasicQuery.getDistinctValuesForColumn(conn, table, col);

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
                            FilterController.removeFilter(VariantRecordModel.getFieldNameForIndex(fieldNum));
                        } else {
                            Filter f = new QueryFilter() {

                                @Override
                                public Condition[] getConditions() {
                                    Condition[] results = new Condition[acceptableValues.size()];
                                    int i = 0;
                                    for (String s : acceptableValues) {
                                        results[i++] = BinaryCondition.equalTo(Database.getInstance().getVariantTableSchema().getDBColumn(columnAlias), s);
                                    }
                                    return results;
                                }

                                @Override
                                public String getName() {
                                    return columnAlias;
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
                            //System.out.println("Changed: a=" + armed + "/p=" + pressed + "/s=" + selected);
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

                FilterView fv = new FilterView(columnAlias, container);
                l.add(fv);
            }
        }

        return l;
    }

    private void setStatus(String status) {
        this.status.setText(status);
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        setStatus(ResultController.getInstance().getAllVariantRecords().size() + " records");
    }

    /*
    private Set<String> getUniqueValuesOfVariantRecordsAtField(int i) {
    Set<String> result = new TreeSet<String>();

    /**
     * TODO: this should query the database
     *
    List<VariantRecord> records;
    try {
    records = ResultController.getInstance().getFilteredVariantRecords();
    } catch (Exception ex) {
    Logger.getLogger(FilterPanel.class.getName()).log(Level.SEVERE, null, ex);
    DialogUtil.displayErrorMessage("Problem getting data.", ex);
    return null;
    }

    for (VariantRecord r : records) {
    Object o = VariantRecordModel.getValueOfFieldAtIndex(i, r);
    if (o == null) {
    result.add("<none>");
    } else {
    result.add(o.toString());
    }
    }

    return result;
    }
     * 
     */
}
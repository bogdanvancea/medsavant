/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.gadget.chart;

import com.jidesoft.chart.Chart;
import com.jidesoft.chart.ChartType;
import com.jidesoft.chart.axis.Axis;
import com.jidesoft.chart.axis.CategoryAxis;
import com.jidesoft.chart.model.ChartCategory;
import com.jidesoft.chart.model.ChartPoint;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.model.Highlight;
import com.jidesoft.chart.render.AbstractPieSegmentRenderer;
import com.jidesoft.chart.render.RaisedPieSegmentRenderer;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.range.CategoryRange;
import com.jidesoft.range.NumericRange;
import fiume.vcf.VariantRecord;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.model.VariantRecordModel;
import org.ut.biolab.medsavant.util.Util;

/**
 *
 * @author mfiume
 */
public class ChartPanel extends JPanel {



    private int currentKeyIndex = VariantRecordModel.INDEX_OF_REF;
    //private Map<String, Integer> chartMap;
   // private DefaultChartModel chartModel;
   // private Chart chart;
    private JToolBar bar;
    private boolean isPie = false;
    private boolean isSorted = false;
    private JCheckBox isPieCB;
    private JCheckBox isSortedCB;
    private SpinnerNumberModel numberModel;
    private static final int DEFAULT_NUM_QUANTITATIVE_CATEGORIES = 5;

    public ChartPanel() {
        this.setLayout(new BorderLayout());
        initToolBar();
        updateChartMap();
    }

    private void updateChartMap() {

        Map<String,Integer> chartMap = getChartMap(currentKeyIndex, isSorted);

        printHist(chartMap);

        DefaultChartModel chartModel = new DefaultChartModel("Sample Model");
        Chart chart = new Chart(new Dimension(200, 200));

        ChartStyle s = new ChartStyle();
        s.setBarsVisible(true);
        s.setLinesVisible(false);

        if (isPie) {
            chart.setChartType(ChartType.PIE);
        }

        chart.addModel(chartModel);
        chart.setStyle(chartModel, s);
        chart.setRolloverEnabled(true);

        chart.setSelectionEnabled(true);
        chart.setSelectionShowsOutline(true);
        chart.setSelectionShowsExplodedSegments(true);

        AbstractPieSegmentRenderer r = new RaisedPieSegmentRenderer();
        chart.setPieSegmentRenderer(r);

        CategoryRange<String> categories = new CategoryRange<String>();
        int max = Integer.MIN_VALUE;

        

        for (String key : chartMap.keySet()) {
            ChartCategory cat = new ChartCategory<String>(key);
            categories.add(cat);
            Highlight h = new Highlight(key);
            chart.setHighlightStyle(h, new ChartStyle(Util.getRandomColor()));
            int value = chartMap.get(key);
            max = Math.max(max, value);
            ChartPoint p = new ChartPoint(cat, value);
            p.setHighlight(h);
            chartModel.addPoint(p);
        }

        chart.setXAxis(new CategoryAxis(categories, "Category"));
        chart.setYAxis(new Axis(new NumericRange(0, max), "Frequency"));

        this.removeAll();
        this.add(bar, BorderLayout.NORTH);
        this.add(chart, BorderLayout.CENTER);
    }

    private static Object getValueOfFieldAtIndex(int keyIndex, VariantRecord r) {
        switch (keyIndex) {
            case VariantRecordModel.INDEX_OF_ALT:
                return r.getAlt();
            case VariantRecordModel.INDEX_OF_CALLDETAILS:
                return r.getCallDetails();
            case VariantRecordModel.INDEX_OF_CHROM:
                return r.getChrom();
            case VariantRecordModel.INDEX_OF_FILTER:
                return r.getFilter();
            case VariantRecordModel.INDEX_OF_FORMAT:
                return r.getFormat();
            case VariantRecordModel.INDEX_OF_ID:
                return r.getId();
            case VariantRecordModel.INDEX_OF_POS:
                return r.getPos();
            case VariantRecordModel.INDEX_OF_QUAL:
                return r.getQual();
            case VariantRecordModel.INDEX_OF_REF:
                return r.getRef();
            case VariantRecordModel.INDEX_OF_SAMPLEID:
                return r.getSampleID();
        }

        return null;
    }

    private static void printHist(Map<String, Integer> chartMap) {
        if (true) {
            return;
        }
        System.out.println("Printing hist: ");
        for (String s : chartMap.keySet()) {
            System.out.println(s + ": " + chartMap.get(s));
        }
    }

    private void initToolBar() {
        bar = new JToolBar();
        JComboBox b = new JComboBox();

        Vector v = VariantRecordModel.getFieldNames();
        for (int i = 0; i < VariantRecordModel.getNumberOfFields(); i++) {
            b.addItem(v.get(i));
        }

        b.setSelectedIndex(currentKeyIndex);

        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String fieldName = (String) cb.getSelectedItem();
                setCurrentKeyIndex(VariantRecordModel.getIndexOfField(fieldName));
                updateChartMap();
            }
        });

        bar.setFloatable(false);
        bar.add(b);

        bar.add(Box.createHorizontalStrut(5));

        isPieCB = new JCheckBox("Display as Pie");
        isPieCB.setSelected(isPie);
        isPieCB.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setIsPie(isPieCB.isSelected());
                updateChartMap();
            }
        });
        bar.add(isPieCB);

        bar.add(Box.createHorizontalStrut(5));

        isSortedCB = new JCheckBox("Sort by frequency");
        isSortedCB.setSelected(isSorted);
        isSortedCB.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setSortByFrequency(isSortedCB.isSelected());
                updateChartMap();
            }
        });
        bar.add(isSortedCB);

        bar.add(Box.createHorizontalStrut(5));

        numberModel = new SpinnerNumberModel();
        numberModel.setStepSize(2);
        numberModel.setMinimum(1);
        numberModel.setMaximum(100);
        numberModel.setValue(DEFAULT_NUM_QUANTITATIVE_CATEGORIES);
        JSpinner numberSpinner = new JSpinner(numberModel);
        numberModel.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                updateChartMap();
            }

        });
        bar.add(numberSpinner);

        this.add(bar, BorderLayout.NORTH);
    }

    public void setIsPie(boolean b) {
        this.isPie = b;
    }

    public void setSortByFrequency(boolean b) {
        this.isSorted = b;
    }

    public void setCurrentKeyIndex(int currentKeyIndex) {
        this.currentKeyIndex = currentKeyIndex;
    }

    private static boolean isQuantatitiveClass(Class c) {
        if (c == Integer.class || c == Long.class || c == Short.class || c == Double.class || c == Float.class) { return true; }
        return false;
    }

    private int getNumberOfQuantitativeCategories() {
        return (Integer) numberModel.getValue();
    }

    private Map<String, Integer> getChartMap(int fieldIndex, boolean isSorted) {

        Map<String,Integer> chartMap = new TreeMap<String,Integer>();

        Class c = VariantRecordModel.getFieldClass(fieldIndex);
        if (isQuantatitiveClass(c)) {
            int numBins = getNumberOfQuantitativeCategories();
            List<Double> numbers = new ArrayList<Double>();
            Double min = Double.MAX_VALUE;
            Double max = Double.MIN_VALUE;
            for (VariantRecord r : ResultController.getVariantRecords()) {
                Object numericvalue = getValueOfFieldAtIndex(fieldIndex, r);
                Double v = Double.parseDouble(numericvalue.toString());
                min = Math.min(min, v);
                max = Math.max(max, v);
                numbers.add(v);
            }

            max = max+1;
            Double step = (max-min)/numBins;
            int[] bins = new int[numBins];

            for (Double d : numbers) {
                int binnumber = (int) ((d - min) / step);
                bins[binnumber]++;
            }
            
            for (int i = 0; i < numBins; i++) {
                chartMap.put(((int)(min+i*step)) + " - " + ((int)(min+(i+1)*step)), bins[i]);
            }

        } else {
            for (VariantRecord r : ResultController.getVariantRecords()) {
                String key = (String) getValueOfFieldAtIndex(fieldIndex, r);
                if (chartMap.containsKey(key)) {
                    chartMap.put(key, chartMap.get(key) + 1);
                } else {
                    chartMap.put(key, 1);
                }
            }
        }

        if (isSorted) {
            ValueComparator bvc =  new ValueComparator(chartMap);
            Map m = new TreeMap<String,Integer>(bvc);
            m.putAll(chartMap);
            chartMap = m;
        }

        return chartMap;
    }

    static class ValueComparator implements Comparator {

        Map base;

        public ValueComparator(Map base) {
            this.base = base;
        }

        public int compare(Object a, Object b) {

            if ((Integer) base.get(a) < (Integer) base.get(b)) {
                return 1;
            } else if ((Integer) base.get(a) == (Integer) base.get(b)) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
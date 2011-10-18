/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import org.ut.biolab.medsavant.view.genetics.filter.ontology.ClassifiedPositionInfo;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.jidesoft.swing.CheckBoxTree;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.ut.biolab.medsavant.olddb.OMedSavantDatabase;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.structure.MedSavantDatabase.DefaultvariantTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.view.genetics.filter.geneontology.*;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.CheckBoxTreeNew;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.ConstructJTree;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Node;
import org.ut.biolab.medsavant.view.genetics.storer.FilterObjectStorer;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Nirvana Nursimulu
 */
public class GOFilter {
    
    public static final String NAME_FILTER = "Gene Ontology";
    
    public static String NAME_TREE = "GO TREE";
    
    private static ClassifiedPositionInfo classifiedPosInfo;
        
    /**
     * Return the filter view for the gene ontology filter.
     * @return 
     */
    public static FilterView getGOntologyFilterView(){
        
        final JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        FilterView gontologyFilterView = new FilterView(NAME_FILTER, container);
        // Add button to ask whether the person wants to see the tree.
//        final JButton buttonShowTree = new JButton("Show tree");
//        container.add(buttonShowTree);
//        buttonShowTree.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) { 
//                container.remove(buttonShowTree);
//                // show progress bar while loading data, and then show the tree.
//                loadData(container);
//            }
//        });
        
        // Start loading the information at the beginning.
        loadData(container);

        return gontologyFilterView;
    }
    
    /**
     * Load data once the user has indicated the wish to show the tree (ie, 
     * pressed on the button asking to show the tree).
     * @param container 
     */
    private static void loadData(final JPanel container){
                
        final JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        container.add(progressBar);
        
          class Task extends SwingWorker{
              
              GOTree xtree;

                @Override
                protected Object doInBackground() throws Exception {
                    setProgress(0);
                    // Create the mappings file at a certain destination 
                    // then show the tree.
                    try{
                        String destination = CreateMappingsFile.getMappings();
                        xtree = XMLontology.makeTree(destination);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    setProgress(100);
                    return xtree;
                }
            }

        try{

            final Task task = new Task();
            
            task.addPropertyChangeListener(new PropertyChangeListener() {

                // To detect progress with the loading of the tree.
                public void propertyChange(PropertyChangeEvent evt) {


//                    if ("state".equals(evt.getPropertyName()) && 
//                            "DONE".equals(evt.getNewValue() + "")){
                    if (task.getState() == Task.StateValue.DONE){

                        progressBar.setIndeterminate(false);
                        progressBar.setVisible(false);
                        container.remove(progressBar);
                        // When we're done loading the information, show tree.
                        try{
                            showTree(container, task.xtree);
                        }
                        catch(Exception e){
                            container.remove(progressBar);
                            container.add(new JLabel("Could not display the tree"));
                            e.printStackTrace();
                        }
                        task.removePropertyChangeListener(this);
                    }   
                    else {

                        progressBar.setIndeterminate(true);
                    }
                }
            });
            
            // Try to load the information into the tree (i.e., download XML 
            // file etc)
            task.execute();
        }
        // If we could not show the tree (load data), say that we could not.
        catch(Exception e){

            container.remove(progressBar);
            container.add(new JLabel("Could not display the tree"));
        }
    }
    
    /**
     * Set up the panel so that the jtree component can be seen, along with the
     * button asking to "apply" a filter.
     * @param container
     * @param xtree 
     */
    private static void showTree(JPanel container, final GOTree xtree){
        
        final JButton applyButton = new JButton("Apply");

        // label will show number of locations.
        final JLabel numberSelected = new JLabel();
        // Construct jtree from xtree that has been made.
        // Put tree in scrollpane, and scrollpane in panel.
        // For the tree, we want to use a checkboxtree, a dummy root, and to add 
        // a package we will be calling "genes" to every single node.
        
        final JTree jTree = ConstructJTree.getTree(xtree, true, true, true);
        
        class ThreadTree extends Thread{
            @Override
            public void run(){
                FilterObjectStorer.addObject(NAME_TREE, xtree.getCopyTree());
            }
        }
        // Add this tree to the storer so that it does not need to be loaded 
        // again when dealing with statistics.
//        SwingUtilities.invokeLater(new Runnable() {
//
//            public void run() {
//                FilterObjectStorer.addObject(NAME_TREE, xtree.getCopyTree());
//            }
//        });
        ThreadTree thread = new ThreadTree();
        thread.start();
        
        // to keep track of the locations of the places selected.
        final HashSet<String> locations = new HashSet<String>();

        // Add a listener to the tree.  Note: tree can allow non-contiguous
        // multiple selections.
        ((CheckBoxTreeNew)jTree).getCheckBoxTreeSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

            
            public void valueChanged(TreeSelectionEvent e) {

                locations.clear();
//                TreePath[] paths = ((CheckBoxTree)jTree).getCheckBoxTreeSelectionModel().getSelectionPaths();
                TreePath[] paths = ((CheckBoxTree)jTree).getCheckBoxTreeSelectionModel().getSelectionPaths();
               
                
                if (paths != null){
 
                    for (TreePath path: paths){
                        
//                        if (path.getPathCount() == 1){
//                            
//                            continue;
//                        }
                        DefaultMutableTreeNode currNode = 
                                (DefaultMutableTreeNode)path.getLastPathComponent();
                        Node currXnode = (Node) currNode.getUserObject();
                        
                        HashSet<String> arrayLocs = currXnode.getLocs();

                        for (String arrayLoc: arrayLocs){
                            
                            String[] split = arrayLoc.split("\t"); 
                            // Need to subtract 1 because of BED format.
                            int formattedEnd = 
                                    Integer.parseInt(split[3].trim()) - 1;

                            String str = split[1].trim() + "_" + 
                                    split[2].trim() + "_" + formattedEnd;
                            if (!split[1].matches(".*_.*")){
                                locations.add(str);
                            }
                        }
                    }
                    if (locations.size() != 1){
                        numberSelected.setText(locations.size() + 
                                " transcript location ranges selected");
                    }
                    else{
                        numberSelected.setText(locations.size() + 
                                " transcript location range selected");
                    }
                }
                else{
                    numberSelected.setText("");
                }
                applyButton.setEnabled(true);
            }
        }); 

        JScrollPane scrollpane = new JScrollPane(jTree);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(Box.createVerticalBox());
        container.add(scrollpane);
        scrollpane.setAlignmentX(0F);
        
        container.add(numberSelected);

        JPanel bottomContainer = new JPanel();
        
        JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
        selectAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // select all the nodes in the tree.
                ((CheckBoxTreeNew)jTree).selectAllFromRoot();
            }
        });
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));
        bottomContainer.add(selectAll);
        JButton selectNone = ViewUtil.createHyperLinkButton("Select None");
        selectNone.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // deselect any node in the tree.
                ((CheckBoxTree)jTree).getCheckBoxTreeSelectionModel().clearSelection();
//                FilterController.removeFilter("position gene Ont");
            }
        });
        bottomContainer.add(selectNone);
        
        bottomContainer.add(Box.createGlue());
        bottomContainer.add(applyButton);
        bottomContainer.setAlignmentX(0F);

        container.add(bottomContainer);
        
        applyButton.setEnabled(false);
        applyButton.addActionListener(new ActionListener() {

            // Note that an action could have been performed only if some tree
            // element has been selected.
            public void actionPerformed(ActionEvent e) {
            
                applyButton.setEnabled(false); 
                // Select query statement for GO.
                classifiedPosInfo = new ClassifiedPositionInfo();

                TreePath[] paths = ((CheckBoxTree)jTree).getCheckBoxTreeSelectionModel().getSelectionPaths();
                
                for (String location: locations){

                    String[] split = location.split("_");
                    Double start = Integer.parseInt(split[1]) + 0.0;
                    Double end = Integer.parseInt(split[2]) + 0.0;
                    classifiedPosInfo.addCondition(split[0], start, end);
                } 

                // If there are no conditions at all, do not display
                // anything (most intuitive). So, create bogus condition that 
                // will never be satisfied.
                if (classifiedPosInfo.getConditions().isEmpty() && paths != null){
                    classifiedPosInfo.addCondition("chr1", 23.0, 22.0); 
                }
                final HashMap<String, List<Range>> map = classifiedPosInfo.getConditions();
                
                Filter f = new QueryFilter() {

                    @Override
                    public Condition[] getConditions() {
                        
                        Condition[] conds = new Condition[map.keySet().size()];
                        int i = 0;
                        List<SelectQuery> list = new ArrayList<SelectQuery>();
                        
                        TableSchema table = ProjectController.getInstance().getCurrentVariantTableSchema();
                        
                        for (String key: map.keySet()){
                            
                            List<ComboCondition> listInnerCond = 
                                    new ArrayList<ComboCondition>();
                            List<Range> ranges = map.get(key);
                            BinaryCondition chrCond = BinaryCondition.equalTo(table.getDBColumn(DefaultvariantTableSchema.COLUMNNAME_OF_CHROM), key);
                            for (Range range: ranges){
                                
                                BinaryCondition innerCond1 = BinaryCondition.greaterThan(table.getDBColumn(DefaultvariantTableSchema.COLUMNNAME_OF_POSITION), range.getMin(), true);
                                BinaryCondition innerCond2 = BinaryCondition.lessThan(table.getDBColumn(DefaultvariantTableSchema.COLUMNNAME_OF_POSITION), range.getMax(), true);
                                BinaryCondition[] condTogether = {innerCond1, innerCond2};
                                listInnerCond.add(ComboCondition.and(condTogether));
                                
                                
                            SelectQuery q = new SelectQuery();
                            q.addAllColumns();
                            list.add(q.addCondition(ComboCondition.and(chrCond, ComboCondition.and(condTogether))));
                                
                            } // for each range for the chromosome of interest.
                            conds[i++] = ComboCondition.and(chrCond, ComboCondition.or(listInnerCond.toArray()));
                            
                            
                        } // for each chromosome.
                        return conds;
                    }

                    @Override
                    public String getName() {
                        return NAME_FILTER;
                    }
                    
                    
                    @Override
                    public String getId() {
                        return NAME_FILTER;//TODO
                    }
                };
                FilterController.addFilter(f, 0); //TODO
            }
        });        
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import javax.swing.JPanel;

/**
 *
 * @author Nirvana Nursimulu
 */
public interface AggregatePanelGenerator {
    
    public String getName();
    
    public JPanel getPanel();
    
    public void setUpdate(boolean updatePanelUponFilterChanges);
}

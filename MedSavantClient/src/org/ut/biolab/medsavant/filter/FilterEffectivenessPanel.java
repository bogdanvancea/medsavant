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

package org.ut.biolab.medsavant.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.reference.ReferenceEvent;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.component.ProgressPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class FilterEffectivenessPanel extends JLayeredPane {

    private static final Log LOG = LogFactory.getLog(FilterEffectivenessPanel.class);

    int numLeft = 1;
    int numTotal = 1;
    private int waitCounter = 0;

    private final ProgressPanel progressPanel;
    private final JLabel labelVariantsRemaining;
    private WaitPanel waitPanel;
    private JPanel panel;

    public FilterEffectivenessPanel() {

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        setLayout(new GridBagLayout());

        waitPanel = new WaitPanel("Applying Filters");
        waitPanel.setVisible(false);
        add(waitPanel, gbc, JLayeredPane.DRAG_LAYER);

        panel = ViewUtil.getClearPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(ViewUtil.getMediumBorder());
        panel.setPreferredSize(waitPanel.getPreferredSize());
        add(panel, gbc, JLayeredPane.DEFAULT_LAYER);

        labelVariantsRemaining = ViewUtil.getDetailTitleLabel("");
        labelVariantsRemaining.setForeground(Color.white);

        JPanel infoPanel = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(infoPanel);
        infoPanel.add(ViewUtil.centerHorizontally(labelVariantsRemaining));

        JLabel l = ViewUtil.getWhiteLabel("of all variants pass search conditions");
        ViewUtil.makeSmall(l);
        infoPanel.add(ViewUtil.centerHorizontally(l));
        infoPanel.setBorder(ViewUtil.getMediumTopHeavyBorder());

        panel.add(infoPanel,BorderLayout.NORTH);

        progressPanel = new ProgressPanel();
        //pp.setBorder(ViewUtil.getBigBorder());
        panel.add(progressPanel, BorderLayout.SOUTH);

        FilterController.getInstance().addListener(new Listener<FilterEvent>() {
            @Override
            public void handleEvent(FilterEvent event) {
                showWaitCard();

                new MedSavantWorker<Integer>("Filters") {

                    @Override
                    protected Integer doInBackground() throws Exception {
                        return ResultController.getInstance().getFilteredVariantCount();
                    }

                    @Override
                    protected void showProgress(double fraction) {
                    }

                    @Override
                    protected void showSuccess(Integer result) {
                        showShowCard();
                        setNumLeft(result);
                    }

                    @Override
                    protected void showFailure(Throwable ex) {
                        showShowCard();
                        LOG.error("Error getting filtered variant count.", ex);
                    }
                }.execute();
            }
        });
        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED) {
                    setMaxValues();
                }
            }
        });

        setMaxValues();
    }

    private void setNumLeft(int num) {
        numLeft = num;
        refreshProgressLabel();

        progressPanel.animateToValue(num);
    }

    private void setMaxValues() {
        labelVariantsRemaining.setText("Calculating...");

        new MedSavantWorker<Integer>("Filters") {
            @Override
            protected void showProgress(double fraction) {
            }

            @Override
            protected void showSuccess(Integer result) {
                numTotal = result;
                progressPanel.setMaxValue(numTotal);
                progressPanel.setToValue(numTotal);
                setNumLeft(numTotal);
            }

            @Override
            protected Integer doInBackground() throws Exception {
                return ResultController.getInstance().getTotalVariantCount();
            }
        }.execute();

    }

    public synchronized void showWaitCard() {
        waitCounter++;
        waitPanel.setVisible(true);
        setLayer(waitPanel, JLayeredPane.DRAG_LAYER);
        waitPanel.repaint();
    }

    public synchronized void showShowCard() {
        waitCounter--;
        if (waitCounter <= 0) {
            waitPanel.setVisible(false);
            waitCounter = 0;
        }
    }

    private void refreshProgressLabel() {
        double percent = 100.0;
        if (numTotal > 0) {
            percent = (numLeft * 100.0) / numTotal;
        }
        labelVariantsRemaining.setText(String.format("%,d (%.1f%%)", numLeft, percent));
    }
}

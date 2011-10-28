/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.view.patients;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.view.util.PaintUtil;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public abstract class DetailedView extends JPanel {

    private final JLabel title;
    private final JPanel contentPanel;
    protected SplitScreenView parent;
    private final JPanel bottomPanel;
    private final Component glue;

    public DetailedView() {
        this.setPreferredSize(new Dimension(9999, 350));

        this.setOpaque(false);

        this.setLayout(new BorderLayout());

        JPanel h1 = ViewUtil.getPrimaryBannerPanel();
        h1.setLayout(new BoxLayout(h1, BoxLayout.X_AXIS));
        this.title = ViewUtil.getDetailTitleLabel("");

        h1.add(Box.createHorizontalGlue());
        h1.add(title);
        h1.add(Box.createHorizontalGlue());

        this.add(h1, BorderLayout.NORTH);

        contentPanel = ViewUtil.getClearPanel();
        contentPanel.setBorder(ViewUtil.getBigBorder());
        this.add(contentPanel, BorderLayout.CENTER);

        bottomPanel = ViewUtil.getPrimaryBannerPanel();
        ViewUtil.applyHorizontalBoxLayout(bottomPanel);
        bottomPanel.add(Box.createHorizontalGlue());

        glue = Box.createHorizontalGlue();
        bottomPanel.add(glue);

        this.add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    public void paintComponent(Graphics g) {
        PaintUtil.paintDrillDown(g, this);
    }

    public abstract void setSelectedItem(Object[] selectedRow);

    public abstract void setMultipleSelections(List<Object[]> selectedRows);

    public void setTitle(String str) {
        this.title.setText(str);
    }

    public JPanel getContentPanel() {
        return this.contentPanel;
    }

    public void setSplitScreenParent(SplitScreenView parent) {
        this.parent = parent;
    }

    public void addBottomComponent(Component c) {
        bottomPanel.add(glue);
        bottomPanel.add(c);
        bottomPanel.add(glue);
    }
}

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

package org.ut.biolab.medsavant.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.login.LoginEvent;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.project.ProjectEvent;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.reference.ReferenceEvent;
import org.ut.biolab.medsavant.view.component.HoverButton;
import org.ut.biolab.medsavant.view.variants.GeneticsSection;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.PeekingPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class Menu extends JPanel {

    private SubSectionView currentView;
    private final JPanel contentContainer;
    List<SubSectionView> subSectionViews = new ArrayList<SubSectionView>();
    private Component primaryGlue;
    private final JPanel primaryMenu;
    private final JPanel secondaryMenu;

    ButtonGroup sectionButtons;
    private final JPanel sectionDetailedMenu;
    private final JPanel sectionMenu;
    private JPanel previousSectionPanel;


    public Menu(JPanel panel) {

        sectionButtons = new ButtonGroup();

        primaryMenu = ViewUtil.getPrimaryBannerPanel();
        secondaryMenu = new JPanel();//ViewUtil.getPrimaryBannerPanel();
        secondaryMenu.setBackground(Color.darkGray);
        //secondaryMenu.setOpaque(false);

        int padding = 5;


        primaryMenu.setLayout(new BoxLayout(primaryMenu, BoxLayout.X_AXIS));
        primaryMenu.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));


        secondaryMenu.setLayout(new BoxLayout(secondaryMenu, BoxLayout.Y_AXIS));
        secondaryMenu.setBorder(ViewUtil.getRightLineBorder());

        secondaryMenu.setPreferredSize(new Dimension(150,100));

        setLayout(new BorderLayout());
        add(primaryMenu,BorderLayout.NORTH);

        contentContainer = panel;

        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED) {
                    updateSections();
                }
            }
        });

        ProjectController.getInstance().addListener(new Listener<ProjectEvent>() {
            @Override
            public void handleEvent(ProjectEvent evt) {
                if (!GeneticsSection.isInitialized && evt.getType() == ProjectEvent.Type.CHANGED) {
                    //once this section is initialized, referencecombobox fires
                    //referencechanged event on every project change
                    updateSections();
                }
            }
        });

        LoginController.getInstance().addListener(new Listener<LoginEvent>() {
            @Override
            public void handleEvent(LoginEvent evt) {
                if (evt.getType() == LoginEvent.Type.LOGGED_OUT) {
                    contentContainer.removeAll();
                    ViewController.getInstance().changeSubSectionTo(null);
                    currentView = null;
                }
            }
        });

        sectionMenu = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(sectionMenu);

        sectionDetailedMenu = new JPanel();
        sectionDetailedMenu.setOpaque(false);
        ViewUtil.applyVerticalBoxLayout(sectionDetailedMenu);

        primaryGlue = Box.createHorizontalGlue();

        clearMenu();
    }

    public JPanel getSecondaryMenu() {
        return secondaryMenu;
    }

    public void addSection(SectionView section) {

        JPanel sectionPanel = ViewUtil.getClearPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel,BoxLayout.Y_AXIS));
        sectionPanel.setVisible(false);

        HoverButton sectionButton = new SectionButton(section, sectionPanel);

        ButtonGroup subSectionsGroup = new ButtonGroup();

        for (SubSectionView v : section.getSubSections()) {
            subSectionViews.add(v);

            HoverButton subSectionButton = new SubSectionButton(v, subSectionsGroup);
            sectionPanel.add(subSectionButton);
            subSectionsGroup.add(subSectionButton);
        }

        sectionButtons.add(sectionButton);

        sectionPanel.add(Box.createVerticalStrut(50));

        if (section.getPersistentPanels() != null && section.getPersistentPanels().length > 0) {
            JCheckBox box = PeekingPanel.getCheckBoxForPanel(ViewController.getInstance().getPersistencePanel(), section.getPersistentPanels()[0].getName());
            sectionPanel.add(box);
        }

        primaryMenu.remove(primaryGlue);
        primaryMenu.add(sectionButton);

        sectionMenu.add(sectionPanel);
    }

    public void updateSections() {
        for (int i = 0; i < subSectionViews.size(); i++) {
            subSectionViews.get(i).setUpdateRequired(true);
        }
        if (currentView != null) {
            setContentTo(currentView, true);
        }
    }

    void setContentTo(SubSectionView v, boolean update) {
        currentView = v;
        contentContainer.removeAll();
        contentContainer.add(v.getView(update || v.isUpdateRequired()), BorderLayout.CENTER);

        sectionDetailedMenu.removeAll();

        if (v.getSubSectionMenuComponents() != null) {
            for (Component c : v.getSubSectionMenuComponents()) {
                sectionDetailedMenu.add(c);
            }
        }

        if (v.getParent().getSectionMenuComponents() != null) {
            for (Component c : v.getParent().getSectionMenuComponents()) {
                sectionDetailedMenu.add(c);
            }
        }

        v.setUpdateRequired(false);
        contentContainer.updateUI();
        ViewController.getInstance().changeSubSectionTo(v);
    }

    public void refreshSelection() {
        if (currentView != null) {
            setContentTo(currentView, true);
        }
    }

    public final void clearMenu() {

        while (sectionButtons.getButtonCount() > 0) {
            sectionButtons.remove(sectionButtons.getElements().nextElement());
        }

        primaryMenu.removeAll();
        secondaryMenu.removeAll();

        sectionMenu.removeAll();
        secondaryMenu.add(sectionMenu);

        sectionDetailedMenu.removeAll();
        secondaryMenu.add(sectionDetailedMenu);

        primaryMenu.add(primaryGlue);
    }

    class SectionButton extends HoverButton {
        private final JPanel panel;

        SectionButton(SectionView v, JPanel p) {
            super(v.getName());
            panel = p;

            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    sectionButtonClicked();
                }
            });
        }

        void sectionButtonClicked() {
            sectionButtons.setSelected(getModel(), true);
            if (previousSectionPanel != null) {
                previousSectionPanel.setVisible(false);
            }
            // Act as if we clicked the first sub-section button.
            ((SubSectionButton)panel.getComponent(0)).subSectionClicked();
            panel.setVisible(true);

            previousSectionPanel = panel;
            primaryMenu.invalidate();
        }
    }

    private class SubSectionButton extends HoverButton {
        private final SubSectionView view;
        private final ButtonGroup group;

        SubSectionButton(SubSectionView v, ButtonGroup g) {
            super(v.getPageName());
            view = v;
            group = g;
            setFontSize(12);
            setFontStyle(Font.BOLD);

            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    subSectionClicked();
                }
            });
        }

        private void subSectionClicked() {
            group.setSelected(getModel(), true);
            setContentTo(view, false);
        }
    }
}

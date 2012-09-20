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
package org.ut.biolab.medsavant.view.variants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.*;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.filter.FilterController;
import org.ut.biolab.medsavant.filter.FilterEvent;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Chromosome;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.reference.ReferenceEvent;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.ThreadController;
import org.ut.biolab.medsavant.view.variants.browser.MedSavantDataSource;
import org.ut.biolab.medsavant.view.variants.inspector.InspectorPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.PeekingPanel;
import savant.api.data.DataFormat;
import savant.api.event.GenomeChangedEvent;
import savant.controller.FrameController;
import savant.controller.GenomeController;
import savant.exception.SavantTrackCreationCancelledException;
import savant.settings.PersistentSettings;
import savant.util.ColourKey;
import savant.view.swing.Savant;
import savant.view.tracks.Track;
import savant.view.tracks.TrackFactory;
import savant.view.variation.VariationController;

/**
 *
 * @author mfiume
 */
public class BrowserPage extends SubSectionView {

    private JPanel panel;
    private JPanel browserPanel;
    private GenomeContainer genomeContainer;
    private PeekingPanel genomeView;
    private PeekingPanel variationPanel;
    private Component[] settingComponents;
    private boolean variantTrackLoaded = false;

    public BrowserPage(SectionView parent) {
        super(parent, "Browser");
        FilterController.getInstance().addListener(new Listener<FilterEvent>() {

            @Override
            public void handleEvent(FilterEvent event) {
                updateContents();
            }
        });
        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {

            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED) {
                    updateContents();
                }
            }
        });


        GenomeController.getInstance().addListener(new savant.api.util.Listener<GenomeChangedEvent>() {

            @Override
            public void handleEvent(GenomeChangedEvent event) {
                if (!variantTrackLoaded) {
                    addTrackFromURLString("http://compbio.cs.toronto.edu/savant/data/genes/hg19/human.hg19.refseq.bed.savant");
                    //addTrackFromURLString("http://compbio.cs.toronto.edu/savant/data/SNPs/hg19/human.snp130.hg19.bed.savant");

                    System.out.println("Loading variant track");
                    MedSavantDataSource s = new MedSavantDataSource();
                    try {
                        FrameController.getInstance().createFrame(new Track[]{TrackFactory.createTrack(s)});
                    } catch (SavantTrackCreationCancelledException ex) {
                        ex.printStackTrace();
                    }

                    variantTrackLoaded = true;
                }
            }
        });
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        if (settingComponents == null) {
            settingComponents = new Component[2];
            settingComponents[0] = PeekingPanel.getCheckBoxForPanel(genomeView, "Genome");
            settingComponents[1] = PeekingPanel.getCheckBoxForPanel(variationPanel, "Variation");
        }
        return settingComponents;
    }

    @Override
    public JPanel getView(boolean update) {
        try {
            if (panel == null || update) {
                ThreadController.getInstance().cancelWorkers(pageName);
                setPanel();
            } else {
                genomeContainer.updateIfRequired();
            }
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error generating genome view: %s", ex);
        }
        return panel;
    }

    private void setPanel() throws SQLException, RemoteException {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        Chromosome[] chroms = MedSavantClient.ReferenceManager.getChromosomes(LoginController.sessionId, ReferenceController.getInstance().getCurrentReferenceID());
        genomeContainer = new GenomeContainer(pageName, chroms);

        genomeView = new PeekingPanel("Genome", BorderLayout.SOUTH, genomeContainer, false, 225);
        genomeView.setToggleBarVisible(false);

        panel.add(genomeView, BorderLayout.NORTH);

        browserPanel = new JPanel();
        browserPanel.setLayout(new BorderLayout());

        Savant savantInstance = Savant.getInstance(true, false);

        PersistentSettings.getInstance().setColor(ColourKey.GRAPH_PANE_BACKGROUND_BOTTOM, Color.white);
        PersistentSettings.getInstance().setColor(ColourKey.GRAPH_PANE_BACKGROUND_TOP, Color.white);
        PersistentSettings.getInstance().setColor(ColourKey.AXIS_GRID, new Color(240,240,240));

        savantInstance.setTrackBackground(new Color(210,210,210));
        savantInstance.setBookmarksVisibile(false);
        savantInstance.setVariantsVisibile(false);

        GenomeController.getInstance().setGenome(null);
        addTrackFromURLString("http://genomesavant.com/savant/data/hg19/hg19.fa.savant");

        browserPanel.add(savantInstance.getBrowserPanel(), BorderLayout.CENTER);

        panel.add(browserPanel, BorderLayout.CENTER);

        variationPanel = new PeekingPanel("Variations", BorderLayout.WEST, VariationController.getInstance().getModule(), false, 325);
        variationPanel.setToggleBarVisible(false);

        panel.add(variationPanel,BorderLayout.EAST);
    }

    private void addTrackFromURLString(String urlString) {
        try {
            URL url = new URL(urlString);
            //TODO: get data format appropriately
            FrameController.getInstance().addTrackFromURI(url.toURI(), DataFormat.SEQUENCE, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void viewDidLoad() {
        super.viewDidLoad();
        genomeContainer.updateIfRequired();
    }

    @Override
    public void viewDidUnload() {
        super.viewDidUnload();
    }

    public void updateContents() {
        ThreadController.getInstance().cancelWorkers(pageName);
        if (genomeContainer == null) {
            return;
        }
        genomeContainer.setUpdateRequired(true);
        if (loaded) {
            genomeContainer.updateIfRequired();
        }
    }
}

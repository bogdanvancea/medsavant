/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.util.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class PeekingPanel extends JPanel {
    
    private final JPanel panel;
    private boolean isExpanded;
    private final JLabel title;
    private final String titleString;
    private final JPanel titlePanel;

    public PeekingPanel(String label, String borderLayoutPosition, JPanel panel, boolean isExpanded) {
        this(label, borderLayoutPosition, panel, isExpanded, 350);
    }

    public PeekingPanel(String label, String borderLayoutPosition, JPanel panel, boolean isExpanded, int size) {

        final boolean isVertical = borderLayoutPosition.equals(BorderLayout.EAST) || borderLayoutPosition.equals(BorderLayout.WEST);
        
        this.setLayout(new BorderLayout());
        this.panel = panel;

        if (isVertical) {
            panel.setPreferredSize(new Dimension(size,999));
        } else {
            panel.setPreferredSize(new Dimension(999,size));
        }
        titlePanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                //g.setColor(tabColor);
                //g.fillRect(0, 0, titlePanel.getWidth(), titlePanel.getHeight());

                Color start = Color.lightGray;//new Color(37,113,190);//Color.black;
                Color end = Color.white; //new Color(109,164,221);//Color.black;

                /*
                if (!isExpanded()) {
                    end = Color.orange;
                }
                 * 
                 */
                
                if (isVertical) {
                    GradientPaint p = new GradientPaint(0,0,end,titlePanel.getWidth(),0,start);
                    ((Graphics2D)g).setPaint(p);
                    g.fillRect(0, 0, titlePanel.getWidth(), titlePanel.getHeight());
                } else {
                    GradientPaint p = new GradientPaint(0,0,end,0,titlePanel.getHeight(),start);
                    ((Graphics2D)g).setPaint(p);
                    g.fillRect(0, 0, titlePanel.getWidth(), titlePanel.getHeight());
                }
            }
        };
        titlePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        //titlePanel.setBorder(ViewUtil.getTinyBorder());
        if (isVertical) {
            titlePanel.setLayout(new BoxLayout(titlePanel,BoxLayout.Y_AXIS));
        } else {
            titlePanel.setLayout(new BoxLayout(titlePanel,BoxLayout.X_AXIS));
        }
        titlePanel.add(Box.createHorizontalGlue());
       // titlePanel.setBackground(Color.black); //new Color(1,73,98));//

        this.titleString = label.toUpperCase();
        title = new JLabel(" ");//titleString);
        
        title.setForeground(Color.darkGray);
        if (borderLayoutPosition.equals(BorderLayout.EAST)) {
            title.setUI(new VerticalLabelUI(true));
        } else if (borderLayoutPosition.equals(BorderLayout.WEST)) {
            title.setUI(new VerticalLabelUI(false));
        }
        titlePanel.add(title);
        
        if (!isVertical) {
            titlePanel.add(Box.createHorizontalGlue());
        }

        titlePanel.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                toggleExpanded();
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

        });

        this.add(titlePanel, borderLayoutPosition);
        this.add(panel, BorderLayout.CENTER);

        setExpanded(isExpanded);
    }

    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
        String s = this.isExpanded ? " HIDE " + titleString : " SHOW " + titleString;
        titlePanel.setToolTipText(s);
        if (!this.isExpanded) {
            this.title.setText(s);
            title.setFont(ViewUtil.getSmallTitleFont());
        } else {
            this.title.setText(" ");
            title.setFont(ViewUtil.getSuperSmallTitleFont());
        }
        panel.setVisible(isExpanded);
    }

    public void toggleExpanded() {
        setExpanded(!this.isExpanded);
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    
}

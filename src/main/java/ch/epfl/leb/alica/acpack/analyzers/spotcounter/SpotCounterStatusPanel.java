/*
 * Copyright (C) 2017 Laboratory of Experimental Biophysics
 * Ecole Polytechnique Federale de Lausanne
 * 
 * Author: Marcel Stefko
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.epfl.leb.alica.acpack.analyzers.spotcounter;

import ch.epfl.leb.alica.interfaces.analyzers.AnalyzerStatusPanel;
import ij.IJ;

/**
 * Status panel of SpotCounter, enables modification of threshold in real time.
 * @author Marcel Stefko
 */
public class SpotCounterStatusPanel extends AnalyzerStatusPanel {
    private final SpotCounterCore core;
    
    /**
     * Creates new form SpotCounterStatusPanel
     * @param core SpotCounterCore
     */
    public SpotCounterStatusPanel(SpotCounterCore core) {
        initComponents();
        this.core = core;
        this.e_box_size.setText(Integer.toString(core.getBoxSize()));
        this.e_noise_tolerance.setText(Integer.toString(core.getNoiseTolerance()));
        
        if (this.core.isLiveModeOn()) {
            this.cb_live_view.setSelected(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        e_noise_tolerance = new javax.swing.JTextField();
        e_box_size = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        b_set = new javax.swing.JButton();
        cb_live_view = new javax.swing.JCheckBox();

        jLabel1.setText("SpotCounter");

        jLabel2.setText("Noise tolerance:");

        e_noise_tolerance.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        e_noise_tolerance.setText("80");

        e_box_size.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        e_box_size.setText("5");

        jLabel3.setText("Box size:");

        b_set.setText("Set");
        b_set.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_setActionPerformed(evt);
            }
        });

        cb_live_view.setText("Live view");
        cb_live_view.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cb_live_viewActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addComponent(b_set)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cb_live_view))
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(31, 31, 31)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(e_box_size, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(e_noise_tolerance, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))))
                .addGap(45, 45, 45))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(e_noise_tolerance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(e_box_size, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cb_live_view)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 4, Short.MAX_VALUE)
                .addComponent(b_set)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void b_setActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_setActionPerformed
        int noise_tolerance = Integer.parseInt(e_noise_tolerance.getText());
        int box_size = Integer.parseInt(e_box_size.getText());
        
        if (noise_tolerance<1 || box_size<1) {
            IJ.showMessage("Wrong parameters for SpotCounter!");
            return;
        }
        
        core.setParams(noise_tolerance, box_size);
    }//GEN-LAST:event_b_setActionPerformed

    private void cb_live_viewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cb_live_viewActionPerformed
        if (cb_live_view.isSelected()) {
            core.liveModeOn();
        } else {
            core.liveModeOff();
        }
    }//GEN-LAST:event_cb_live_viewActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton b_set;
    private javax.swing.JCheckBox cb_live_view;
    private javax.swing.JTextField e_box_size;
    private javax.swing.JTextField e_noise_tolerance;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    // End of variables declaration//GEN-END:variables
}

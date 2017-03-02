/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.cgee.gephi.statistics;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jorg Bliesener
 */
@ServiceProvider(service = StatisticsUI.class)
public class CorrelationStatisticsEdgePearsonUI implements StatisticsUI {
    
    DelegateUserInterface ui;
    
    public CorrelationStatisticsEdgePearsonUI() {
        ui=new DelegateUserInterface(DelegateCorrelationStatistics.CorrelationType.EDGE_PEARSON, 
                CorrelationStatisticsEdgePearson.class);
    }

    @Override
    public JPanel getSettingsPanel() {
        return ui.getSettingsPanel();
    }

    @Override
    public void setup(Statistics ststcs) {
        ui.setup(ststcs);
    }

    @Override
    public void unsetup() {
        ui.unsetup();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return ui.getStatisticsClass();
    }

    @Override
    public String getValue() {
        return ui.getValue();
    }

    @Override
    public String getDisplayName() {
        return ui.getDisplayName();
    }

    @Override
    public String getShortDescription() {
        return ui.getShortDescription();
    }

    @Override
    public String getCategory() {
        return ui.getCategory();
    }

    @Override
    public int getPosition() {
        return ui.getPosition();
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.cgee.gephi.statistics;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jorg Bliesener
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class CorrelationStatisticsNodePearsonBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return NbBundle.getMessage(DelegateCorrelationStatistics.class, 
                "CorrelationPanel.title.node_pearson");
    }

    @Override
    public Statistics getStatistics() {
        return new CorrelationStatisticsNodePearson();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return CorrelationStatisticsNodePearson.class;
    }

    
}

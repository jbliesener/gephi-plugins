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
public class CorrelationStatisticsEdgeSpearmanBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return NbBundle.getMessage(DelegateCorrelationStatistics.class, 
                "CorrelationPanel.title.edge_spearman");
    }

    @Override
    public Statistics getStatistics() {
        return new CorrelationStatisticsEdgeSpearman();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return CorrelationStatisticsEdgeSpearman.class;
    }

    
}

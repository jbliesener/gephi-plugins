/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.cgee.gephi.statistics;

import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.spi.Statistics;

/**
 *
 * @author Jorg Bliesener
 */
public class CorrelationStatisticsEdgePearson extends DelegateCorrelationStatistics implements Statistics {
    
    public CorrelationStatisticsEdgePearson() {
        super(CorrelationType.EDGE_PEARSON);
    }

    @Override
    public void execute(GraphModel gm) {
        delegateExecute(gm);
    }

    @Override
    public String getReport() {
        return delegateGetReport();
    }
    
    
    
}

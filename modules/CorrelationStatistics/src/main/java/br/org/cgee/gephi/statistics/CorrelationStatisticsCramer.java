/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.cgee.gephi.statistics;

import static br.org.cgee.gephi.statistics.DelegateCorrelationStatistics.isNodeStatistics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.statistics.spi.Statistics;


/**
 *
 * @author Jorg Bliesener
 */
public class CorrelationStatisticsCramer extends DelegateCorrelationStatistics implements Statistics {
    
    public CorrelationStatisticsCramer() {
        super(CorrelationType.NODE_CRAMER);
    }

    protected Map<Object, Map<Object, Integer>> values;
    private final Object NULL_OBJECT = new Object();

    private void addAttributes(Element a,
            Column col1, Column col2) {
        Object val1 = a.getAttribute(col1.getId());
        Object val2 = a.getAttribute(col2.getId());
        if (val1 == null) {
            if (ignoreMissingAttribute1) {
                return;
            } else {
                val1 = NULL_OBJECT;
            }
        }

        if (val2 == null) {
            if (ignoreMissingAttribute2) {
                return;
            } else {
                val2 = NULL_OBJECT;
            }
        }
        
        Map<Object, Integer> val2Map = values.get(val1);
        if (val2Map == null) {
            val2Map = new HashMap<Object,Integer>();
            values.put(val1, val2Map);
        }
        Integer val2Count = val2Map.get(val2);
        if (val2Count == null) {
            val2Count = 0;
        }
        val2Map.put(val2, val2Count+1);
    }
    
    private Set<Object> getAttribute1Values() {
        return values.keySet();
    }
    
    private Set<Object> getAttribute2Values() {
        Set<Object> result = new HashSet<Object>();
        for (Entry<Object,Map<Object,Integer>> entry : values.entrySet()) {
            result.addAll(entry.getValue().keySet());
        }
        return result;
    }
    
    private Integer getCount(Object attributeValue1, Object attributeValue2) {
        
        if (attributeValue1 == null) {
            // sum all values for attributeValue2
            Integer sum=0;
            for (Map<Object,Integer> attributeValues2 : values.values()) {
                Integer value = attributeValues2.get(attributeValue2);
                if (value != null) {
                    sum += value;
                }
            }
            return sum;
        } else {
            Map<Object,Integer> attributeValues2 = values.get(attributeValue1);
            if (attributeValues2 == null) {
                return 0;
            }
            Integer sum;
            
            if (attributeValue2 == null) {
                // sum all values for attributeValue1
                sum = 0;
                for (Integer i : attributeValues2.values()) {
                    sum += i;
                }
            } else {
                // get just one
                sum = attributeValues2.get(attributeValue2);
                if (sum == null) {
                    sum=0;
                }
            }
            return sum;
        }
    }

    @Override
    protected void delegateExecute(GraphModel gm) {
        correlationCoefficient = Double.NaN;

        values = new HashMap<Object, Map<Object, Integer>>();

        Table table = getAttributeTable(gm, correlationType);
        if (table == null) {
            return;
        }
        if (attribute1 == null || attribute2 == null) {
            return;
        }

        Column col1 = findColumn(table,attribute1);
        Column col2 = findColumn(table,attribute2);
        if (col1 == null || col2 == null) {
            return;
        }

        Graph g = gm.getGraphVisible();

        if (isNodeStatistics(correlationType)) {
            Iterator<Node> nodeIterator = g.getNodes().iterator();
            while (nodeIterator.hasNext()) {
                Node n = nodeIterator.next();
                addAttributes(n, col1, col2);
            }
        } else {
            Iterator<Edge> edgeIterator = g.getEdges().iterator();
            while (edgeIterator.hasNext()) {
                Edge e = edgeIterator.next();
                addAttributes(e, col1, col2);
            }
        }
        
        Map<Object,Integer> attribute1ValueSums = new HashMap<Object,Integer>();
        for (Object o : getAttribute1Values()) {
            attribute1ValueSums.put(o,getCount(o,null));
        }
        Map<Object,Integer> attribute2ValueSums = new HashMap<Object,Integer>();
        for (Object o : getAttribute2Values()) {
            attribute2ValueSums.put(o,getCount(null,o));
        }
        Integer totalCount = 0;
        for (Entry<Object,Integer> entry : attribute1ValueSums.entrySet()) {
            totalCount += entry.getValue();
        }
        
        Double chiSquare = 0.0d;
        Set<Object> attribute1Values = getAttribute1Values();
        Set<Object> attribute2Values = getAttribute2Values();
        for (Object val1 : attribute1Values) {
            for (Object val2 : attribute2Values) {
                Integer n1 = attribute1ValueSums.get(val1);
                if (n1==null) {
                    n1 = 0;
                }
                Integer n2 = attribute2ValueSums.get(val2);
                if (n2==null) {
                    n2 = 0;
                }
                Integer n12 = getCount(val1,val2);
                
                Double term2 = 1.0d * n1 * n2 / totalCount;
                chiSquare += (n12-term2)*(n12-term2)/term2;
            }
        }
        
        Integer degree = Math.min(attribute1Values.size(), attribute2Values.size())-1;
        correlationCoefficient = Math.sqrt(chiSquare/totalCount/degree);


    }
    
    @Override
    public void execute(GraphModel gm) {
        delegateExecute(gm);
    }

    @Override
    public String getReport() {
        return null;
    }
    

}

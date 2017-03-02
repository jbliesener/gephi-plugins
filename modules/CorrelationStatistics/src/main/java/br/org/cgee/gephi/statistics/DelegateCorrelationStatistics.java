/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.cgee.gephi.statistics;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.RankingAlgorithm;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.utils.TempDirUtils;
import org.gephi.utils.TempDirUtils.TempDir;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jorg Bliesener
 */
class DelegateCorrelationStatistics {

    public enum CorrelationType {
        NODE_PEARSON,
        NODE_CRAMER,
        NODE_SPEARMAN,
        EDGE_PEARSON,
        EDGE_SPEARMAN
    }

    private final Set<Class> validAttributeTypesEnumerated = new HashSet<Class>(
            Arrays.asList(
                    BigInteger.class,
                    Boolean.class,
                    Byte.class,
                    Character.class,
                    Integer.class,
                    Long.class,
                    Short.class,
                    String.class)
    );

    private final Set<Class> validAttributeTypesNumeric = new HashSet<Class>(
            Arrays.asList(
                    BigInteger.class,
                    BigDecimal.class,
                    Byte.class,
                    Character.class,
                    Double.class,
                    Float.class,
                    Integer.class,
                    Long.class,
                    Short.class
            )
    );

    protected CorrelationType correlationType;
    protected String attribute1;
    protected String attribute2;
    protected Column attributeColumn1;
    protected Column attributeColumn2;
    protected double correlationCoefficient;
    protected Boolean ignoreMissingAttribute1;
    protected Boolean ignoreMissingAttribute2;
    private List<DoublePair> values;

    public DelegateCorrelationStatistics(CorrelationType type) {
        this.correlationType = type;
    }

    public static boolean isNodeStatistics(CorrelationType type) {
        return (type == CorrelationType.NODE_CRAMER
                || type == CorrelationType.NODE_PEARSON
                || type == CorrelationType.NODE_SPEARMAN);
    }

    public static boolean isNumericAttributes(CorrelationType type) {
        return (type == CorrelationType.NODE_PEARSON
                || type == CorrelationType.NODE_SPEARMAN
                || type == CorrelationType.EDGE_PEARSON
                || type == CorrelationType.EDGE_SPEARMAN);
    }

    public static boolean isPearson(CorrelationType type) {
        return (type == CorrelationType.EDGE_PEARSON
                || type == CorrelationType.NODE_PEARSON);
    }

    public static boolean isSpearman(CorrelationType type) {
        return (type == CorrelationType.EDGE_SPEARMAN
                || type == CorrelationType.NODE_SPEARMAN);
    }

    public List<String> getValidAttributeNames() {
        List<String> attributeNames = new ArrayList<String>();
        Set<Class> validAttributeTypes;
        if (isNumericAttributes(correlationType)) {
            validAttributeTypes = validAttributeTypesNumeric;
        } else {
            validAttributeTypes = validAttributeTypesEnumerated;
        }

        GraphModel model = getGraphModel();
        if (model != null) {
            Table table = getAttributeTable(model, correlationType);
            Column[] columns = table.toArray();
            for (Column acol : columns) {
                if (validAttributeTypes.contains(acol.getTypeClass())) {
                    attributeNames.add(acol.getTitle());
                }
            }
            return attributeNames;
        } else {
            return null;
        }
    }

    protected GraphModel getGraphModel() {
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        if (gc != null) {
            return gc.getGraphModel();
        }
        return null;
    }

    protected Table getAttributeTable(GraphModel model, CorrelationType type) {
        Table table;
        if (isNodeStatistics(correlationType)) {
            table = model.getNodeTable();
        } else {
            table = model.getEdgeTable();
        }
        return table;
    }

    protected Column findColumn(Table table, String columnName) {
        Column[] columns = table.toArray();
        for (Column acol : columns) {
            if (acol.getTitle().equals(columnName)) {
                return acol;
            }
        }
        return null;
    }

    public void setCorrelationAttributes(String attribute1, String attribute2,
            Boolean ignoreMissingAttribute1, Boolean ignoreMissingAttribute2) {
        GraphModel model = getGraphModel();
        if (model != null) {
            Table table = getAttributeTable(model, correlationType);
            attributeColumn1 = findColumn(table, attribute1);
            if (attributeColumn1 != null) {
                this.attribute1 = attribute1;
            } else {
                this.attribute1 = null;
            }
            attributeColumn2 = findColumn(table, attribute2);
            if (attributeColumn2 != null) {
                this.attribute2 = attribute2;
            } else {
                this.attribute2 = null;
            }
            this.ignoreMissingAttribute1 = ignoreMissingAttribute1;
            this.ignoreMissingAttribute2 = ignoreMissingAttribute2;
        }
    }

    public String getCorrelationAttribute1() {
        return attribute1;
    }

    public String getCorrelationAttribute2() {
        return attribute2;
    }

    public Boolean isIgnoreMissingAttribute1() {
        return ignoreMissingAttribute1;
    }

    public Boolean isIgnoreMissingAttribute2() {
        return ignoreMissingAttribute2;
    }

    public CorrelationType getCorrelationType() {
        return correlationType;
    }

    public double getCorrelationCoefficient() {
        return correlationCoefficient;
    }

    private void addAttributes(Element a,
            Column col1, Column col2) {
        Object val1 = a.getAttribute(col1.getId());
        Object val2 = a.getAttribute(col2.getId());
        if (val1 == null || !(val1 instanceof Number)) {
            if (ignoreMissingAttribute1) {
                return;
            } else {
                val1 = 0d;
            }
        }

        if (val2 == null || !(val2 instanceof Number)) {
            if (ignoreMissingAttribute2) {
                return;
            } else {
                val2 = 0d;
            }
        }

        double d1 = ((Number) val1).doubleValue();
        double d2 = ((Number) val2).doubleValue();
        if (Double.isNaN(1)) {
            if (ignoreMissingAttribute1) {
                return;
            } else {
                d1 = 0d;
            }
        }

        if (Double.isNaN(d2)) {
            if (ignoreMissingAttribute1) {
                return;
            } else {
                d2 = 0d;
            }
        }

        values.add(new DoublePair(d1, d2));

    }
    
    protected void delegateExecute(GraphModel gm) {
        correlationCoefficient = Double.NaN;
        values = new ArrayList<DoublePair>();

        Table table = getAttributeTable(gm, correlationType);
        if (table == null) {
            return;
        }
        if (attribute1 == null || attribute2 == null) {
            return;
        }

        Column col1 = findColumn(table, attribute1);
        Column col2 = findColumn(table, attribute2);
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

        double[] x = new double[values.size()];
        double[] y = new double[values.size()];
        int i = 0;
        for (DoublePair e : values) {
            x[i] = e.getX();
            y[i] = e.getY();
            i++;
        }

        // Spearman's correlation is nothing but Pearson's correlation on 
        // ranked data. See http://commons.apache.org/proper/commons-math/userguide/stat.html.
        // In order to be able to plot ranked data, we'll write the ranked data 
        // back to the "values" array.
        if (isSpearman(correlationType)) {
            RankingAlgorithm ranking = new NaturalRanking();
            x = ranking.rank(x);
            y = ranking.rank(y);
            values = new ArrayList<DoublePair>();
            for (i = 0; i < x.length; i++) {
                values.add(new DoublePair(x[i], y[i]));
            }
        }

        PearsonsCorrelation corr = new PearsonsCorrelation();
        correlationCoefficient = corr.correlation(x, y);
    }

    public String delegateGetReport() {
        String title = NbBundle.getMessage(DelegateCorrelationStatistics.class,
                "CorrelationPanel.title." + correlationType.name().toLowerCase());
        XYSeries series = new XYSeries(title);
        for (DoublePair p : values) {
            series.add(p.getX(), p.getY());
        }
        XYSeriesCollection dataset1 = new XYSeriesCollection();
        dataset1.addSeries(series);
        String axisPrefix = isSpearman(correlationType) ? "Rank of " : "";
        JFreeChart chart1 = ChartFactory.createXYLineChart(
                title,
                axisPrefix + attribute1,
                axisPrefix + attribute2,
                dataset1,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart1.removeLegend();

        XYPlot plot = (XYPlot) chart1.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(0, 0, 2, 2));
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setDomainGridlinePaint(java.awt.Color.GRAY);
        plot.setRangeGridlinePaint(java.awt.Color.GRAY);
        plot.setRenderer(renderer);

        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setRange(series.getMinX() - 0.1 * Math.sqrt(series.getMinX()),
                series.getMaxX() + 0.1 * Math.sqrt(series.getMaxX()));
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(series.getMinY() - 0.1 * Math.sqrt(series.getMinY()),
                series.getMaxY() + 0.1 * Math.sqrt(series.getMaxY()));

        String imageFile = "";
        try {
            final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
            TempDir tempDir = TempDirUtils.createTempDir();
            File file1 = tempDir.createFile("chart.png");
            imageFile = "<IMG SRC=\"file:" + file1.getAbsolutePath() + "\" " + "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\"></IMG>";
            ChartUtilities.saveChartAsPNG(file1, chart1, 600, 400, info);
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        String formattedNumber;
        if (Double.isNaN(correlationCoefficient)) {
            formattedNumber = "undefined";
        } else {
            NumberFormat f = new DecimalFormat("#0.0000");
            formattedNumber = f.format(correlationCoefficient);
        }

        String report = "<HTML> <BODY> <h1>" + title + "</h1> "
                + "<hr>"
                + "<br> <h2> Results: </h2>"
                + "Correlation coefficient: " + formattedNumber
                + "<br /><br />" + imageFile
                + "</BODY></HTML>";
        return report;
    }

    private class DoublePair {

        double x;
        double y;

        public DoublePair(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }
}

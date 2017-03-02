/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.cgee.gephi.statistics;

import br.org.cgee.gephi.statistics.DelegateCorrelationStatistics.CorrelationType;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import static org.gephi.statistics.spi.StatisticsUI.CATEGORY_EDGE_OVERVIEW;
import static org.gephi.statistics.spi.StatisticsUI.CATEGORY_NODE_OVERVIEW;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jorg Bliesener
 */
class DelegateUserInterface  {

    DelegateCorrelationStatistics stats;
    CorrelationType type;
    CorrelationStatisticsPanel panel;
    Class<? extends Statistics> cls;
    
    public DelegateUserInterface(CorrelationType type, Class<? extends Statistics> cls) {
        this.type=type;
        this.cls=cls;
    }
    
    public JPanel getSettingsPanel() {
        panel=new CorrelationStatisticsPanel();
        panel.setStatisticsType(type);
        return panel;
    }

    public void setup(Statistics ststcs) {
        this.stats = (DelegateCorrelationStatistics) ststcs;
        List<String> attributeList = stats.getValidAttributeNames();
        if (attributeList == null || attributeList.isEmpty()) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    NbBundle.getMessage(DelegateUserInterface.class, "CorrelationStatistics.noValidProperty"),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
        if (panel != null) {
            panel.setAttributeList(attributeList);
            String oldValue1 = stats.getCorrelationAttribute1();
            String oldValue2 = stats.getCorrelationAttribute2();
            Boolean ignoreMissingAttribute1 = stats.isIgnoreMissingAttribute1();
            Boolean ignoreMissingAttribute2 = stats.isIgnoreMissingAttribute2();
            Preferences prefs = NbPreferences.forModule(DelegateCorrelationStatistics.class);
            if (oldValue1 == null) {
                oldValue1 = prefs.get(type.name()+".attribute1", null);
            }
            if (oldValue2 == null) {
                oldValue2 = prefs.get(type.name()+".attribute2", null);
            }
            if (ignoreMissingAttribute1 == null) {
                ignoreMissingAttribute1 = prefs.getBoolean(type.name()+".ignoreMissingAttribute1", true);
            } 
            if (ignoreMissingAttribute2 == null) {
                ignoreMissingAttribute2 = prefs.getBoolean(type.name()+".ignoreMissingAttribute2", true);
            } 
            panel.setAttributes(oldValue1,oldValue2,ignoreMissingAttribute1,ignoreMissingAttribute2);
        }
    }

    public void unsetup() {
        if (panel != null) {

            String attribute1 = panel.getAttribute1();
            String attribute2 = panel.getAttribute2();
            boolean ignoreMissingAttribute1 = panel.isIgnoreMissingAttribute1();
            boolean ignoreMissingAttribute2 = panel.isIgnoreMissingAttribute2();

            if (attribute1 == null ||attribute2 == null) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                        NbBundle.getMessage(DelegateUserInterface.class, "CorrelationStatistics.nullProperty"),
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                return;

            }

            Preferences prefs = NbPreferences.forModule(DelegateCorrelationStatistics.class);
            prefs.put(type.name()+".attribute1", attribute1);
            prefs.put(type.name()+".attribute2", attribute2);
            prefs.putBoolean(type.name()+".ignoreMissingAttribute1", ignoreMissingAttribute1);
            prefs.putBoolean(type.name()+".ignoreMissingAttribute2", ignoreMissingAttribute2);
            
            stats.setCorrelationAttributes(attribute1, attribute2, ignoreMissingAttribute1, ignoreMissingAttribute2);
        }
        this.panel = null;
        this.stats = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return cls;
    }

    public String getValue() {
        if (stats != null) {
            double value = stats.getCorrelationCoefficient();
            if (Double.isNaN(value)) {
                return "undefined";
            } else {
                NumberFormat nf = new DecimalFormat("0.0000");
                return nf.format(value);
            }
        } else {
            return "";
        }    
    }

    public String getDisplayName() {
        return NbBundle.getMessage(DelegateUserInterface.class, "CorrelationPanel.title."+type.name().toLowerCase());
    }

    public String getShortDescription() {
        return NbBundle.getMessage(DelegateUserInterface.class, "CorrelationPanel.description."+type.name().toLowerCase());
    }

    public String getCategory() {
        if (DelegateCorrelationStatistics.isNodeStatistics(type)) {
            return CATEGORY_NODE_OVERVIEW;
        } else {
            return CATEGORY_EDGE_OVERVIEW;
        }
    }

    public int getPosition() {
        return 12000;
    }
    
}

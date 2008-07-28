package org.mule.galaxy.web.client.property;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.mule.galaxy.web.rpc.AbstractCallback;
import org.mule.galaxy.web.rpc.WLifecycle;
import org.mule.galaxy.web.rpc.WPhase;

public class LifecyclePropertyPanel extends PropertyPanel {

    private FlexTable lifecycleTable;
    private Label valueLabel;
    private WLifecycle lifecycle;
    private WPhase phase;
    protected Collection lifecycles;
    private ListBox lifecyclesLB;
    private ListBox phaseLB;
    
    protected Widget createEditForm() {
        lifecycleTable = createColumnTable();
        
        return lifecycleTable;
    }

    protected Object getRemoteValue() {
        ArrayList values = new ArrayList();
        values.add(getSelectedLifecycle().getId());
        values.add(getSelectedPhase().getId());
        return values;
    }

    protected Widget createViewWidget() {
        valueLabel = new Label();
        return valueLabel;
    }

    public void showEdit() {
        valueLabel.setText("Loading...");
        if (lifecycles == null) {
            galaxy.getRegistryService().getLifecycles(new AbstractCallback(errorPanel) {
                public void onSuccess(Object o) {
                    lifecycles = (Collection) o;
                    doShowLifecycles();
                }
            });
        } else {
            doShowLifecycles();
        }
        super.showEdit();
    }

    private void doShowLifecycles() {
        lifecycleTable.setText(0, 0, "Lifecycle:");
        lifecycleTable.setText(1, 0, "Phase:");
        
        lifecyclesLB = new ListBox();
        for (Iterator iterator = lifecycles.iterator(); iterator.hasNext();) {
            WLifecycle l = (WLifecycle)iterator.next();
            
            lifecyclesLB.addItem(l.getName(), l.getId());
        }
        lifecyclesLB.addChangeListener(new ChangeListener() {

            public void onChange(Widget arg0) {
                WLifecycle selected = getSelectedLifecycle();
                
                showPhasesForLifecycle(selected);
            }
            
        });
        lifecycleTable.setWidget(0, 1, lifecyclesLB);
        
        phaseLB = new ListBox();
        showPhasesForLifecycle(lifecycle);
        lifecycleTable.setWidget(1, 1, phaseLB);
    }

    protected WLifecycle getSelectedLifecycle() {
        int idx = lifecyclesLB.getSelectedIndex();
        String id = lifecyclesLB.getValue(idx);
        
        for (Iterator itr = lifecycles.iterator(); itr.hasNext();) {
            WLifecycle l = (WLifecycle) itr.next();
            
            if (l.getId().equals(id)) {
                return l;
            }
        }
        return null;
    }

    private void showPhasesForLifecycle(WLifecycle lifecycle) {
        for (Iterator iterator = lifecycle.getPhases().iterator(); iterator.hasNext();) {
            WPhase p = (WPhase)iterator.next();
            
            if (p.getNextPhases().contains(phase) || phase.getNextPhases().contains(p) || p == phase) {
                phaseLB.addItem(p.getName(), p.getId());
                
                if (p == phase) {
                    phaseLB.setSelectedIndex(phaseLB.getSelectedIndex());
                }
            }
        }
    }

    protected WPhase getSelectedPhase() {
        int idx = phaseLB.getSelectedIndex();
        String id = phaseLB.getValue(idx);
        
        for (Iterator itr = lifecycle.getPhases().iterator(); itr.hasNext();) {
            WPhase l = (WPhase) itr.next();
            
            if (l.getId().equals(id)) {
                return l;
            }
        }
        return null;
    }

    public void showView(boolean initial) {
        super.showView(initial);
        
        if (initial) {
            valueLabel.setText("Loading...");
            final List ids = getProperty().getListValue();
            
            galaxy.getRegistryService().getLifecycle((String)ids.get(0), new AbstractCallback(errorPanel) {
    
                public void onSuccess(Object o) {
                    lifecycle = (WLifecycle) o;
                    phase = lifecycle.getPhaseById((String) ids.get(1));
                    
                    updateLabel();
                }
                
            });
        } else {
            updateLabel();
        }
    }

    private void updateLabel() {
        valueLabel.setText(lifecycle.getName() + " - " + phase.getName());
    }
    
    protected void onSave(Object value) {
        phase = getSelectedPhase();
        lifecycle = getSelectedLifecycle();
        
        super.onSave(value);
    }

    protected void onSaveFailure(Throwable caught, AbstractCallback saveCallback) {
        // TODO Auto-generated method stub
        super.onSaveFailure(caught, saveCallback);
    }

    public boolean saveAsCollection() {
        // Lifecycles are weird, we have two values but the Property isn't multivalued
        return true;
    }
    
    
}

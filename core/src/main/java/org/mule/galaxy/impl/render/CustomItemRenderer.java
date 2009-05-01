package org.mule.galaxy.impl.render;

import java.util.ArrayList;
import java.util.List;

import org.mule.galaxy.Item;
import org.mule.galaxy.render.Column;
import org.mule.galaxy.render.ColumnEvaluator;
import org.mule.galaxy.render.ItemRenderer;

public class CustomItemRenderer implements ItemRenderer {
    private List<Column> columns = new ArrayList<Column>();

    public CustomItemRenderer() {
        super();
        columns.add(new Column("Name", new ColumnEvaluator() {
            public Object getValue(Object artifact) {
                return ((Item) artifact).getName();
            }
        }));
        columns.add(new Column("Location", new ColumnEvaluator() {
            public Object getValue(Object artifact) {
                return ((Item) artifact).getParent().getPath();
            }
        }));
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public String[] getColumnNames() {
        List<String> names = new ArrayList<String>();
        for (Column c : columns) {
            names.add(c.getName());
        }
        return names.toArray(new String[names.size()]);
    }

    public String getColumnValue(Item row, int column) {
        Column col = columns.get(column);
        if (col == null) {
            throw new RuntimeException("Invalid Column!");
        }
        
        try {
            Object value = col.getEvaluator().getValue(row);
            if (value == null) {
                return "";
            }
            return value.toString();
        } catch (RuntimeException e) {
            System.out.println("Could not evaluate for " + row.getPath());
            e.printStackTrace();
            return "";
        }
    }

    public boolean isSummary(int column) {
        Column col = columns.get(column);
        
        return col.isSummary();
    }

    public boolean isDetail(int column) {
        Column col = columns.get(column);
        
        return col.isDetail();
    }
    
}
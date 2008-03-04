package org.mule.galaxy.web.client.admin;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Iterator;

import org.mule.galaxy.web.client.AbstractComposite;
import org.mule.galaxy.web.client.util.InlineFlowPanel;
import org.mule.galaxy.web.client.util.QNameListBox;
import org.mule.galaxy.web.rpc.AbstractCallback;
import org.mule.galaxy.web.rpc.ItemNotFoundException;
import org.mule.galaxy.web.rpc.RegistryServiceAsync;
import org.mule.galaxy.web.rpc.WIndex;

public class IndexForm extends AbstractComposite {

    private AdministrationPanel adminPanel;
    private WIndex index;
    private Button save;
    private TextBox nameTB;
    private TextArea xqueryExpressionTA;
    private TextBox idTB;
    private ListBox languageList;
    private TextBox xpathExpressionTB;
    private ListBox resultTypeLB;
    private QNameListBox docTypesLB;
    
    public IndexForm(AdministrationPanel adminPanel, WIndex u) {
        this (adminPanel, u, false);
    }
    
    public IndexForm(AdministrationPanel adminPanel) {
        this (adminPanel, new WIndex(), true);
    }
    
    protected IndexForm(AdministrationPanel adminPanel, WIndex idx, boolean add){
        this.adminPanel = adminPanel;
        this.index = idx;
        
        FlowPanel panel = new FlowPanel();
        String title;
        if (add) {
            title = "Add index";
        } else {
            title = "Edit index: " + idx.getName();
        }
        
        final FlexTable table = createTitledColumnTable(panel, title);
        
        table.setText(0, 0, "ID");
        table.setText(1, 0, "Name");
        table.setText(2, 0, "Language");
        table.setText(3, 0, "Result Type");
        table.setText(4, 0, "Expression");
        table.setText(5, 0, "Document Types");
        
        idTB = new TextBox();
        idTB.setText(idx.getId());
        table.setWidget(0, 1, idTB);
        
        nameTB = new TextBox();
        nameTB.setText(idx.getName());
        table.setWidget(1, 1, nameTB);
        
        xqueryExpressionTA = new TextArea();
        xqueryExpressionTA.setVisibleLines(15);
        xqueryExpressionTA.setCharacterWidth(80);
        
        xpathExpressionTB = new TextBox();
        xpathExpressionTB.setVisibleLength(80);
        
        languageList = new ListBox();
        languageList.addItem("XPath");
        languageList.addItem("XQuery");
        
        if (idx.getIndexer() == null || idx.getIndexer().equals("xpath")) {
            languageList.setSelectedIndex(0);
            xpathExpressionTB.setText(idx.getExpression());
            table.setWidget(4, 1, xpathExpressionTB);
        } else {
            languageList.setSelectedIndex(1);
            xqueryExpressionTA.setText(idx.getExpression());
            table.setWidget(4, 1, xqueryExpressionTA);
        }
        
        languageList.addChangeListener(new ChangeListener() {

            public void onChange(Widget sender) {
                String value = languageList.getValue(languageList.getSelectedIndex());
                
                if (value.equals("XPath")) {
                    table.setWidget(4, 1, xpathExpressionTB);
                } else {
                    table.setWidget(4, 1, xqueryExpressionTA);
                }
            }
            
        });
        
        table.setWidget(2, 1, languageList);
        
        resultTypeLB = new ListBox();
        resultTypeLB.addItem("String");
        resultTypeLB.addItem("QName");
        table.setWidget(3, 1, resultTypeLB);
        
        if (idx.getResultType() == null || idx.getResultType().equals("String")) {
            resultTypeLB.setSelectedIndex(0);
        } else {
            resultTypeLB.setSelectedIndex(1);
        }
        
        docTypesLB = new QNameListBox(idx.getDocumentTypes());

        table.setWidget(5, 1, docTypesLB);
        
        save = new Button("Save");
        table.setWidget(6, 1, save);
        save.addClickListener(new ClickListener() {

            public void onClick(Widget sender) {
                save();
            }
            
        });
        
        styleHeaderColumn(table);
        
        initWidget(panel);
    }


    protected void save() {
        save.setEnabled(false);
        save.setText("Saving...");
        
        RegistryServiceAsync svc = adminPanel.getRegistryService();
        
        index.setId(idTB.getText());

        index.setName(nameTB.getText());
        index.setResultType(resultTypeLB.getValue(resultTypeLB.getSelectedIndex()));
        
        String language = languageList.getValue(languageList.getSelectedIndex()).toLowerCase();
        index.setIndexer(language);
        if ("XPATH".equals(language)) {
            index.setExpression(xpathExpressionTB.getText());
        } else {
            index.setExpression(xqueryExpressionTA.getText());
        }
        
        
        index.setDocumentTypes(docTypesLB.getItems());
        
        // validation
        
        if (!assertValid(index.getId(), "id")) return;
        
        if (index.getId().indexOf(' ') != -1) {
            adminPanel.setMessage("The index id cannot contain spaces.");
            reenable();
            return;
        }

        if (!assertValid(index.getName(), "name")) return;
        if (!assertValid(index.getExpression(), "expression")) return;

        svc.saveIndex(index, new AbstractCallback(adminPanel) {

            public void onFailure(Throwable caught) {
                if (caught instanceof ItemNotFoundException) {
                    adminPanel.setMessage("Index was not found! " + index.getId());
                    reenable();
                } else {
                    super.onFailure(caught);
                }
            }

            public void onSuccess(Object result) {
                History.newItem("indexes");
                adminPanel.setMessage("Index " + index.getName() + " was saved.");
            }
            
        });
    }

    private boolean assertValid(String value, String name) {
        if (value == null || value.equals("") ) {
            adminPanel.setMessage("You must supply an index " + name + " .");
            reenable();
            return false;
        }
        return true;
    }

    private void reenable() {
        save.setEnabled(true);
        save.setText("Save");
    }

}

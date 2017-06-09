package org.adempiere.webui.editor;

import java.util.List;

import org.adempiere.base.Service;
import org.adempiere.model.GridField;
import org.adempiere.webui.factory.IEditorFactoryPOS;
import org.compiere.model.GridTab;

public class WebEditorFactoryPOS extends WebEditorFactory {

	public WebEditorFactoryPOS() {
		// TODO Auto-generated constructor stub
	}
	
    public static WEditorPOS getEditor(GridField gridField, boolean tableEditor) {
    	return getEditor(gridField.getGridTab(), gridField, tableEditor);
    }
    
    public static WEditorPOS getEditor(GridTab gridTab, GridField gridField, boolean tableEditor) {
        WEditorPOS editor = null;
        List<IEditorFactoryPOS> factoryList = Service.locator().list(IEditorFactoryPOS.class).getServices();
        for(IEditorFactoryPOS factory : factoryList)
        {
        	editor = factory.getEditor(gridTab, gridField, tableEditor);
        	if (editor != null)
        		break;
        }
        return editor;
    }


}

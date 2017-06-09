package org.adempiere.webui.factory;

import org.adempiere.webui.editor.WEditorPOS;
import org.adempiere.model.GridField;
import org.compiere.model.GridTab;

public interface IEditorFactoryPOS extends IEditorFactory {
	
	/**
	 * @param gridTab
	 * @param gridField
	 * @param tableEditor
	 * @return WEditor
	 */
	public WEditorPOS getEditor(GridTab gridTab, GridField gridField, boolean tableEditor);

}

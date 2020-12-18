package org.adempiere.webui.grid;

//import java.util.logging.Level;

import org.adempiere.pos.AdempierePOSException;
import org.adempiere.pos.WPOS;
//import org.adempiere.webui.ClientInfo;
//import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.editor.WEditor;
import org.adempiere.webui.editor.WebEditorFactory;
//import org.adempiere.webui.theme.ThemeManager;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.GridWindow;
import org.compiere.model.MField;
//import org.compiere.model.MRole;
//import org.compiere.model.MTab;
import org.compiere.model.MWindow;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
//import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Span;

public class WQuickEntryPOS extends WQuickEntry {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int windowID_ref;
	private int tabID_ref;
	private int seqTab;

//	public WQuickEntryPOS(int WindowNo, int AD_Window_ID) {
//		super(WindowNo, AD_Window_ID);
//		// TODO Auto-generated constructor stub
//	}
//
//	public WQuickEntryPOS(int AD_Window_ID) {
//		super(AD_Window_ID);
//		// TODO Auto-generated constructor stub
//	}
	
	public WQuickEntryPOS(WPOS pos, int sequence){
		//super(pos.getWindowNo(), pos.getQuickAD_Window_ID());
		super(pos.getQuickAD_Window_ID());
		windowID_ref = pos.getQuickAD_Window_ID();
		//Prende in considerazione la testata della maschera se siamo sul pannello di testa del POS (WPOSDocumentPanel), altrimenti dopo legge tab della 'line' (panel WPOSQuantityPanel) 
		if(sequence==0)
			tabID_ref = pos.getQuickAD_Tab_ID();
		else
			seqTab = sequence;
		
		initPOs();
		
	}
		
	protected void initPOs(){
		GridWindow gridwindow = GridWindow.get(Env.getCtx(), m_WindowNo, m_AD_Window_ID);
		//iDempiereConsulting __08/11/2019 --- Segnalazione di problema accesso ruolo window 
		if(gridwindow == null) {
			MWindow win = new MWindow(Env.getCtx(), m_AD_Window_ID, null);
			String nameWindow = win.get_Translation("Name");
			throw new AdempierePOSException("Problema di accesso Ruolo per maschera: \""+nameWindow+"\". Contattare Amministratore di Sistema per risolvere il problema.");
		}
		
		this.setTitle(gridwindow.getName());
		boolean newTab = false;
		
		GridTab gridtab = null;
		for (int i=0; i < gridwindow.getTabCount(); i++) {
			gridtab = gridwindow.getTab(i);
			if(gridtab.getAD_Tab_ID()==tabID_ref  || (tabID_ref==0 && seqTab==i)){ //Prende la testata della maschera, o prende in considerazione la 'line' (panel WPOSQuantityPanel), che nell'array viene subito dopo....
				if (!gridtab.isLoadComplete())
					gridwindow.initTab(i);
				for (GridField gridfield : gridtab.getFields()) {
					MField field = new MField(Env.getCtx(), gridfield.getAD_Field_ID(), null);
					if (field.get_ValueAsBoolean("LIT_isQuickEntryPos")) {
						if (! isValidQuickEntryType(field.getAD_Reference_ID()))
							continue;
				        WEditor editor = WebEditorFactory.getEditor(gridfield, false);
				        if (gridfield.isMandatory(false))
				        	editor.setMandatory(true);
						createLine(editor, newTab, gridtab);
						quickFields.add(gridfield);
						quickEditors.add(editor);
						editor.addValueChangeListener(this);
						
						if (! quickTabs.contains(gridtab)) {
							quickTabs.add(gridtab);
						}
//						isHasField = true;
						newTab = false;
					}
				}
				newTab = true;
				break;
			}
			else
				continue;
		}
		
		
	}
	
	private boolean isValidQuickEntryType(int refID) {
		boolean valid =
			! (
				 refID == DisplayType.Button
			  || refID == DisplayType.Binary
			  || refID == DisplayType.ID
			  );
		return valid;
	}
	
	private void createLine(WEditor editor, boolean newTab, GridTab gt) {
		if (newTab) {
			Separator sep = new Separator();
			centerPanel.appendChild(sep);

			Label tabLabel = new Label(gt.getName());
			centerPanel.appendChild(tabLabel);

			Separator sepl = new Separator();
			sepl.setBar(true);
			centerPanel.appendChild(sepl);
		}
		Component field = editor.getComponent();
		Hlayout layout = new Hlayout();

		ZKUpdateUtil.setHflex(layout, "10");

		Span span = new Span();
//		if(parent_WindowNo!= 0)
//			ZKUpdateUtil.setHflex(span, "3");
		layout.appendChild(span);
		Label label = editor.getLabel();
		span.appendChild(label);
		label.setSclass("field-label");

		layout.appendChild(field);
		ZKUpdateUtil.setHflex((HtmlBasedComponent)field, "7");
		
		//editor.setValue("Y");
		centerPanel.appendChild(layout);
	}
	
//	public void dynamicDisplay()
//	{
//		for (int idxf = 0; idxf < quickFields.size(); idxf++) {
//			GridField field = quickFields.get(idxf);
//			WEditor editor = quickEditors.get(idxf);
//			editor.setValue(field.getValue());
//			editor.setReadWrite(true);
//			editor.setVisible(true);
//		}
//	} // dynamicDisplay

}

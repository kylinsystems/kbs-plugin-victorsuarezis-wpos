/** ****************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Copyright (C) 2003-2016 e-Evolution,SC. All Rights Reserved.               *
 * Contributor(s): Victor Perez www.e-evolution.com                           *
 * ****************************************************************************/
package org.adempiere.pos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.adempiere.exceptions.AdempiereException;
//import org.adempiere.pos.search.QueryBPartner;
import org.adempiere.pos.search.WPOSQuery;
import org.adempiere.pos.search.WQueryBPartner;
import org.adempiere.pos.service.POSQueryInterface;
import org.adempiere.pos.service.POSQueryListener;
import org.adempiere.webui.ISupportMask;
import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.adwindow.ToolbarProcessButton;
import org.adempiere.webui.apps.ProcessModalDialog;
import org.adempiere.webui.event.ActionEvent;
import org.adempiere.webui.event.ActionListener;
import org.adempiere.webui.event.DialogEvents;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.MOrder;
import org.compiere.model.MPInstance;
import org.compiere.model.MProcess;
import org.compiere.model.MRole;
import org.compiere.model.MTable;
import org.compiere.model.MToolBarButton;
import org.compiere.model.MToolBarButtonRestrict;
import org.compiere.model.X_AD_ToolBarButton;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;

//import jpiere.plugin.simpleinputwindow.window.SimpleInputWindowProcessModelDialog;

/**
 * Class that execute business logic from POS
 * eEvolution author Victor Perez <victor.perez@e-evolution.com> 
 * Raul Muñoz, rmunoz@erpcya.com, ERPCYA http://www.erpcya.com
 * @contributor Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * 		<a href="https://github.com/adempiere/adempiere/issues/670">
 * 		@see FR [ 670 ] Standard process for return material on POS</a>
 */
public class WPOSActionMenu implements  POSQueryListener, EventListener<Event>,ActionListener{

    private WPOS pos;
    private WPOSQuery queryPartner;
    private Menupopup popupMenu;
	public static final String EVENT_ATTRIBUTE = "EVENT";
	
	private int tabID = 0; 

    public WPOSActionMenu(WPOS pos)
    {
        this.pos = pos;
        this.popupMenu =  new Menupopup();
        this.popupMenu.setStyle("background: #E8E3E3 repeat-y scroll 0 0 !important;");
        
       initActionMenu_();

    }
	
	public void initActionMenu_(){
		
		tabID = pos.getQuickAD_Tab_ID();
		
		
//		boolean isPO_order = pos.isPurchaseOrder();
//        MTable table = MTable.get(Env.getCtx(), MOrder.Table_Name);
//        tabID = 0;
//        if(isPO_order)
//        	tabID = DB.getSQLValue(null, "SELECT AD_Tab_ID FROM AD_Tab WHERE AD_Window_ID= ?  AND TabLevel =0", table.getPO_Window_ID());
//        else
//        	tabID = DB.getSQLValue(null, "SELECT AD_Tab_ID FROM AD_Tab WHERE AD_Window_ID= ?  AND TabLevel =0",table.getAD_Window_ID());
        
        loadToolbarButtons();
        
	}
    
    private ArrayList<ToolbarProcessButton> toolbarProcessButtons = null;

    public ArrayList<ToolbarProcessButton> getToolbarProcessButtons() {
		return toolbarProcessButtons;
	}

	private void loadToolbarButtons() {
    	//get extra toolbar process buttons
    	if(tabID>0){
    		if(toolbarProcessButtons!=null && !toolbarProcessButtons.isEmpty())
    			toolbarProcessButtons.clear();
    		else if(toolbarProcessButtons == null)
    			toolbarProcessButtons = new ArrayList<ToolbarProcessButton>();



    		MToolBarButton[] mToolbarButtons = MToolBarButton.getProcessButtonOfTab(tabID, null);
    		for(MToolBarButton mToolbarButton : mToolbarButtons) {
    			Boolean access = MRole.getDefault().getProcessAccess(mToolbarButton.getAD_Process_ID());
    			if (access != null && access.booleanValue()) {
    				ToolbarProcessButton toolbarProcessButton = new ToolbarProcessButton(mToolbarButton, null, this, pos.getWindowNo());
    				toolbarProcessButtons.add(toolbarProcessButton);
    			}
    		}

    		if (toolbarProcessButtons.size() > 0) {
    			int ids[] = MToolBarButtonRestrict.getProcessButtonOfTab(Env.getCtx(), Env.getAD_Role_ID(Env.getCtx()), tabID, null);
    			if (ids != null && ids.length > 0) {
    				for(int id : ids) {
    					X_AD_ToolBarButton tbt = new X_AD_ToolBarButton(Env.getCtx(), id, null);
    					for(ToolbarProcessButton btn : toolbarProcessButtons) {
    						if (tbt.getComponentName().equals(btn.getColumnName())) {
    							toolbarProcessButtons.remove(btn);
    							break;
    						}
    					}
    				}
    			}
    		}
    	}
    }
    
    private void addMenuItem(String optionName)
    {
        Menuitem menuItem =  new Menuitem(optionName, null);
        popupMenu.appendChild(menuItem);
        menuItem.setAttribute(EVENT_ATTRIBUTE, optionName);
        menuItem.addEventListener(Events.ON_CLICK, this);
    }

    @Override
    public void onEvent(Event actionEvent) throws Exception {
        try {
        //popupMenu.setVisible(false);
        } catch (AdempiereException exception) {
            FDialog.error(pos.getWindowNo(), pos.getForm() , exception.getLocalizedMessage());
        }
    }

    public Menupopup getPopUp()
    {
    	return popupMenu;
    }

    @Override
    public void okAction(POSQueryInterface query) {
        if (query.getRecord_ID() <= 0)
            return;
        //	For Ticket
        if(query instanceof WQueryBPartner) {
            
        }
    }

    @Override
    public void cancelAction(POSQueryInterface query) {
    }

    private void showError(ProcessInfo processInfo) throws AdempierePOSException
    {
        Optional<String> summary = Optional.ofNullable(processInfo.getSummary());
        Optional<String> logs = Optional.ofNullable(processInfo.getLogInfo());
        String errorMessage = Msg.parseTranslation(pos.getCtx() , processInfo.getTitle() + " @ProcessRunError@ @Summary@ : " + summary.orElse("") + " @ProcessFailed@ : " + logs.orElse(""));
        throw new AdempierePOSException(errorMessage);
    }

    private void showOkMessage(ProcessInfo processInfo)
    {
        pos.refreshHeader();
        Optional<String> summary = Optional.ofNullable(processInfo.getSummary());
        Optional<String> logs = Optional.ofNullable(processInfo.getLogInfo());
        String okMessage = Msg.parseTranslation(pos.getCtx() , " @AD_Process_ID@ "+ processInfo.getTitle() +" @Summary@ : " + summary.orElse("") + " @ProcessOK@ : " + logs.orElse(""));
        FDialog.info(pos.getWindowNo(), popupMenu ,"ProcessOK", okMessage);
    }

	@Override
	public void actionPerformed(ActionEvent event) {
		ToolbarProcessButton button = (ToolbarProcessButton)event.getSource();

		ProcessInfo pInfo = prepareProcess(button.getProcess_ID());
		
		Collection<KeyNamePair> m_viewIDMap = new ArrayList <KeyNamePair>();
		m_viewIDMap.add(new KeyNamePair(pos.getC_Order_ID(),""));
		
		DB.createT_SelectionNew(pInfo.getAD_PInstance_ID() , m_viewIDMap, null);


		ProcessModalDialog dialog = new ProcessModalDialog(pos.getWindowNo(),pInfo, false);

		//Mask
		Object window = SessionManager.getAppDesktop().findWindow(pos.getWindowNo());
		final ISupportMask parent = LayoutUtils.showWindowWithMask(dialog, (Component)window, LayoutUtils.OVERLAP_PARENT);
		dialog.addEventListener(DialogEvents.ON_WINDOW_CLOSE, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				parent.hideMask();
			}
		});

		if (dialog.isValid())
		{
			//dialog.setWidth("500px");
			dialog.setBorder("normal");
			pos.getForm().getParent().appendChild(dialog);
			//showBusyMask(dialog);
			LayoutUtils.openOverlappedWindow(pos.getForm().getParent(), dialog, "middle_center");
			dialog.focus();
		}
		else
		{
			//onRefresh(true, false);
		}
		
	}
	
	protected ProcessInfo prepareProcess (int processId){
		 final MProcess m_process = MProcess.get(Env.getCtx(), processId);
		 final ProcessInfo m_pi = new ProcessInfo(m_process.getName(), processId);
		 m_pi.setAD_User_ID(Env.getAD_User_ID(Env.getCtx()));
		 m_pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));
		 m_pi.setRecord_ID(pos.getC_Order_ID());

		 MPInstance instance = new MPInstance(Env.getCtx(), processId, 0);
		 instance.saveEx();
		 final int pInstanceID = instance.getAD_PInstance_ID();
		 // Execute Process
		 m_pi.setAD_PInstance_ID(pInstanceID);
		 m_pi.setAD_InfoWindow_ID(0);

		 return m_pi;
	 }
}

/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 Adempiere, Inc. All Rights Reserved.               *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/

package org.adempiere.pos.posmovement;

import java.text.DecimalFormat;

import org.adempiere.model.MInventory_lit;
import org.adempiere.pos.POSKeyListener;
import org.adempiere.pos.WPOSKeyPanel;
import org.adempiere.pos.WPOSScalesPanel;
import org.adempiere.pos.service.POSPanelInterface;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.I_C_Order;
import org.compiere.model.MPOSKey;
import org.compiere.model.MRefList;
import org.compiere.util.CLogger;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Style;

/**
 * @author Mario Calderon, mario.calderon@westfalia-it.com, Systemhaus Westfalia, http://www.westfalia-it.com
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * @author victor.perez@e-evolution.com , http://www.e-evolution.com
 */
public class WPOSDocumentPanel_Move extends WPOSSubPanel_Move implements POSKeyListener, POSPanelInterface {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2079568840839699509L;

	/**
	 * 	Constructor
	 *	@param posPanel POS Panel
	 */
	public WPOSDocumentPanel_Move(WPOS_Move posPanel) {
		super (posPanel);
	}	//	PosSubFunctionKeys
	
	/** Fields               */
	private Label 			salesRep;
	private Label	 		documentType;
	private Label 			documentNo;
	private Label 			documentStatus;
	private Label 			documentDate;
	private boolean			isKeyboard;

	/**	Format				*/
	private DecimalFormat	m_Format;
	/**	Logger				*/
	private static CLogger 	log = CLogger.getCLogger(WPOSDocumentPanel_Move.class);
	/**	Panels				*/
	private Caption 		v_TitleBorder;
	private Caption 		v_TitleInfo;
	private Groupbox 		v_TotalsGroup;
	private Groupbox 		v_InfOrderGroup;
	private Grid 			v_TotalsPanel;
	private Grid 			v_OrderPanel;
	private Grid 			v_GroupPanel;
	/** Scala Dialog 		*/
	private WPOSScalesPanel 	scalesPanel;
	private WPOSKeyPanel 	keyboardPanel;
	private Row 			row; 
	
	@Override
	public void init(){
		int C_POSKeyLayout_ID = posPanel.getC_POSKeyLayout_ID();
		if (C_POSKeyLayout_ID == 0)
			return;
		m_Format = DisplayType.getNumberFormat(DisplayType.Amount);
		isKeyboard = false;
		v_TotalsPanel = GridFactory.newGridLayout();
//		v_TotalsPanel.setHeight("100%");
//		v_TotalsPanel.setStyle("width:130%;height:100%");
		ZKUpdateUtil.setWidth(v_TotalsPanel, "99%");
		ZKUpdateUtil.setHeight(v_TotalsPanel, "100%");
		
		v_OrderPanel = GridFactory.newGridLayout();
		
		v_OrderPanel.setStyle("border: none; width:130%; height:100%");
		v_GroupPanel = GridFactory.newGridLayout();
		v_GroupPanel.setWidth("100%");
		v_GroupPanel.setHeight("auto");
		
		//  Define the criteria rows and grid  
		Rows rows = new Rows();
		//
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		ZKUpdateUtil.setWidth(rows, "100%");
		v_TotalsGroup = new Groupbox();
		v_InfOrderGroup = new Groupbox();
		v_InfOrderGroup.appendChild(v_OrderPanel);
		v_InfOrderGroup.setWidth("85%");
		row.appendChild(v_InfOrderGroup);
		row.appendChild(v_TotalsGroup);
		
		v_GroupPanel.appendChild(rows);
		v_GroupPanel.setStyle("Overflow:hidden;");
		v_OrderPanel.setStyle("Overflow:hidden;");
		v_TotalsGroup.appendChild(v_TotalsPanel);
		v_TotalsGroup.setWidth("85%");
		
		v_TitleBorder = new Caption(Msg.getMsg(Env.getCtx(), "Totals"));
		Style style = new Style();
		style.setContent(".z-fieldset { margin-left:-5px }"
		    + ".z-combo-item-text { Font-family:Courier New}"
				+ ".z-fieldset legend {font-size: medium; font-weight:bold; width:100%;} "
				+ ".input-search table tr td input{font-size: medium; font-weight:bold; width:100%; height:20px;}"
				+ ".Table-InventoryLine tr th div{font-size: 13px; padding:5px} "
				+ ".Table-InventoryLine tr td div, .Table-InventoryLine tr td div input{font-size: 13; height:auto}"
				+ ".label-description {"+WPOS_Move.FONTSIZEMEDIUM+" display:block; height:15px; font-weight:bold; width: 415px; overflow:hidden;}"
				+ ".fontLarge label  {font-size: medium;}"
				+ "div.z-grid-body {-moz-box-shadow: 0 0 0px #888;-webkit-box-shadow: 0 0 0px #888;box-shadow: 0 0 0px #888;}");
		style.setParent(v_TitleBorder);
		v_TotalsGroup.appendChild(v_TitleBorder);

		v_TitleInfo = new Caption(Msg.getMsg(Env.getCtx(), "InfoOrder"));
		
		v_InfOrderGroup.appendChild(v_TitleInfo);

		rows = null;
		row = null;
		rows = v_OrderPanel.newRows();

		appendChild(v_GroupPanel);

		//
		row = rows.newRow();
		row.setHeight("10px");
		
		Label f_lb_DocumentNo = new Label (Msg.translate(Env.getCtx(), I_C_Order.COLUMNNAME_DocumentNo) + ":");
		f_lb_DocumentNo.setStyle(WPOS_Move.FONTSIZEMEDIUM);
		row.appendChild(f_lb_DocumentNo.rightAlign());
		
		documentNo = new Label();
		documentNo.setStyle(WPOS_Move.FONTSIZEMEDIUM+"; font-weight:bold");
		row.appendChild(documentNo.rightAlign());
		
		row = rows.newRow();
		row.setHeight("20px");
		ZKUpdateUtil.setWidth(row, "100%");
		Label f_lb_DocumentType = new Label (Msg.translate(Env.getCtx(), I_C_Order.COLUMNNAME_C_DocType_ID) + ":");
		f_lb_DocumentType.setStyle(WPOS_Move.FONTSIZEMEDIUM);
		row.appendChild(f_lb_DocumentType.rightAlign());
		
		documentType = new Label();
		documentType.setClass("label-description");
		documentType.setStyle(WPOS_Move.FONTSIZEMEDIUM+"; font-weight:bold; width:auto !important;max-width:225px !important; white-space:pre;");
		row.appendChild(documentType.rightAlign());
		
		row = rows.newRow();
		row.setHeight("20px");
		
		Label f_lb_DocumentStatus = new Label (Msg.translate(Env.getCtx(), I_C_Order.COLUMNNAME_DocStatus) + ":");
		f_lb_DocumentStatus.setStyle(WPOS_Move.FONTSIZEMEDIUM);
		row.appendChild(f_lb_DocumentStatus.rightAlign());
		documentStatus= new Label();
		documentStatus.setStyle(WPOS_Move.FONTSIZEMEDIUM+"; font-weight:bold");
		row.appendChild(documentStatus.rightAlign());
		
		row = rows.newRow();
		row.setHeight("20px");
		
		Label f_lb_SalesRep = new Label (Msg.translate(Env.getCtx(), I_C_Order.COLUMNNAME_SalesRep_ID) + ":");
		f_lb_SalesRep.setStyle(WPOS_Move.FONTSIZEMEDIUM);
		row.appendChild(f_lb_SalesRep.rightAlign());
		
		salesRep = new Label(posPanel.getSalesRepName());
		salesRep.setStyle(WPOS_Move.FONTSIZEMEDIUM+"; font-weight:bold");
		row.appendChild(salesRep.rightAlign());
		
		
		row = rows.newRow();
		rows = v_TotalsPanel.newRows();

		//
		row = rows.newRow();
		row.setHeight("10px");

		Label lDocumentDate = new Label (Msg.translate(Env.getCtx(), I_C_Order.COLUMNNAME_DateOrdered) + ":");
		lDocumentDate.setStyle(WPOS_Move.FONTSIZEMEDIUM);
		row.appendChild(lDocumentDate);
		
		documentDate = new Label();
		documentDate.setStyle(WPOS_Move.FONTSIZEMEDIUM+"; font-weight:bold");
		row.appendChild(documentDate.rightAlign());
		
		// Center Panel
		Grid layout = GridFactory.newGridLayout();

		org.adempiere.webui.component.Panel centerPanel = new org.adempiere.webui.component.Panel();
		appendChild(centerPanel);
		centerPanel.setStyle("overflow:auto; height:75%");
		centerPanel.appendChild(layout);
		layout.setWidth("100%");
		layout.setStyle("");
		
		rows = layout.newRows();
		
	
		keyboardPanel = new WPOSKeyPanel(C_POSKeyLayout_ID, this);
		row = rows.newRow();
		row.setHeight("50%");
		row.setSpans("4");
		row.appendChild(keyboardPanel);
		
//		scalesPanel = new WPOSScalesPanel(posPanel);
//		scalesPanel.hidePanel();
		//add(scalesPanel.getPanel(), scalesConstraint);
		
		refreshPanel();
	}	//	init

	/**
	 * Call back from key panel
	 */
	@Override
	public void keyReturned(MPOSKey key) {
		// processed order
		if (posPanel.hasInventory()
				&& posPanel.isCompleted()) {
			//	Show Product Info
			posPanel.refreshProductInfo(key);
			return;
		}
		// Add line
		try{
      //  Issue 139
		  posPanel.setAddQty(true);
			posPanel.addOrUpdateLine(key.getM_Product_ID(), key.getQty());
			posPanel.refreshPanel();
			posPanel.changeViewPanel();
			posPanel.getMainFocus();

		} catch (Exception exception) {
			FDialog.error(posPanel.getWindowNo(), this, exception.getLocalizedMessage());
		}
		//	Show Product Info
		posPanel.refreshProductInfo(key);
		return;
	}
	
	@Override
	public void onEvent(Event e) throws Exception {
		//	Name
//		if(e.getTarget().equals(bPartnerName.getComponent(WPOSTextField.SECONDARY)) && e.getName().equals(Events.ON_FOCUS) && !isKeyboard){
//			isKeyboard = true;
//			if(!bPartnerName.showKeyboard()){
//				findBPartner();
//			}
//			if(posPanel.getKeyboard() == null){
//				bPartnerName.setValue(" ");
//				findBPartner();
//			}
//			bPartnerName.setFocus(true);
//		}
//		if(e.getTarget().equals(bPartnerName.getComponent(WPOSTextField.PRIMARY)) && e.getName().equals(Events.ON_FOCUS)){
//			isKeyboard = false;
//		}
	}
	
	@Override
	public void refreshPanel() {
		log.fine("RefreshPanel");
		if (!posPanel.hasInventory()) {
			//	Document Info
			v_TitleBorder.setLabel(Msg.getMsg(Env.getCtx(), "Totals"));
			salesRep.setText(posPanel.getSalesRepName());
			documentType.setText(Msg.getMsg(posPanel.getCtx(), "Order"));
			documentNo.setText(Msg.getMsg(posPanel.getCtx(), "New"));
			documentStatus.setText("");
			documentDate.setText("");
		} else {
			//	Set Values
			//	Document Info
			v_TitleBorder.setLabel(Msg.getMsg(Env.getCtx(), "Info"));
			salesRep.setText(posPanel.getSalesRepName());
			documentType.setText(posPanel.getDocumentTypeName());
			documentNo.setText(posPanel.getDocumentNo());
			documentStatus.setText(MRefList.getListName(Env.getCtx(), MInventory_lit.AD_REFERENCE_ID, posPanel.getInventory().getDocStatus()));
			documentDate.setText(posPanel.getDateOrderedForView());
		}
		//	Repaint
		v_TotalsPanel.invalidate();
		v_OrderPanel.invalidate();
		v_GroupPanel.invalidate();
	}


	@Override
	public String validatePayment() {
		return null;
	}

	@Override
	public void changeViewPanel() {
		
	}

	@Override
	public void moveUp() {
	}

	@Override
	public void moveDown() {
	}

	public WPOSScalesPanel getScalesPanel()
	{
		return scalesPanel;
	}

	public WPOSKeyPanel getKeyboard()
	{
		return keyboardPanel;
	}

}
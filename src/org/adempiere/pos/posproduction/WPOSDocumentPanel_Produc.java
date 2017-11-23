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

package org.adempiere.pos.posproduction;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;

import org.adempiere.model.MInventory_lit;
import org.adempiere.model.MPPCostCollector_lit;
import org.adempiere.pos.POSKeyListener;
import org.adempiere.pos.WPOSKeyPanel;
import org.adempiere.pos.WPOSScalesPanel;
import org.adempiere.pos.service.POSPanelInterface;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.editor.WSearchEditor;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.event.ValueChangeListener;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.I_C_Order;
import org.compiere.model.Lookup;
import org.compiere.model.MColumn;
import org.compiere.model.MDocType;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MPOSKey;
import org.compiere.model.MProduct;
import org.compiere.model.MRefList;
import org.compiere.model.MResource;
import org.compiere.model.MWarehouse;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.libero.model.MPPCostCollector;
import org.libero.model.MPPOrder;
import org.libero.model.MPPOrderNode;
import org.libero.model.RoutingService;
import org.libero.model.RoutingServiceFactory;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Style;

/**
 * @author Mario Calderon, mario.calderon@westfalia-it.com, Systemhaus Westfalia, http://www.westfalia-it.com
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * @author victor.perez@e-evolution.com , http://www.e-evolution.com
 */
public class WPOSDocumentPanel_Produc extends WPOSSubPanel_Produc implements POSKeyListener, POSPanelInterface {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = -8605876792613595291L;

	/**
	 * 	Constructor
	 *	@param posPanel POS Panel
	 */
	public WPOSDocumentPanel_Produc(WPOS_Produc posPanel) {
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
	private static CLogger 	log = CLogger.getCLogger(WPOSDocumentPanel_Produc.class);
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
	private Row 			row; 
	
	private WSearchEditor onlyPPOrder;
	private WSearchEditor onlyResource;
	private WSearchEditor onlyDocType;
	private WSearchEditor onlyProduct;
	private WSearchEditor onlyWarehouse;
	private WSearchEditor onlyManufOrderActivity;
	
	private Button 		buttonComplete;
	private Button      buttonStart;
	
	private MPPOrderNode m_node;
	
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
		onlyPPOrder = createField(posPanel.getWindowNo(), MPPOrder.Table_Name, MPPOrder.COLUMNNAME_PP_Order_ID);
		ZKUpdateUtil.setWidth(onlyPPOrder.getComponent(), "98%");
		ZKUpdateUtil.setHeight(onlyPPOrder.getComponent().getTextbox(),"35px");
		ZKUpdateUtil.setHeight(onlyPPOrder.getComponent().getButton(),"35px");
		Label f_lb_ppOrder = new Label (Msg.translate(Env.getCtx(), "PP_Order_ID") + ":");
		f_lb_ppOrder.setStyle(WPOS_Produc.FONTSIZEMEDIUM);
		row.appendChild(f_lb_ppOrder);
		row.appendChild(onlyPPOrder.getComponent());
		onlyPPOrder.getComponent().getTextbox().setStyle("Font-size:medium; font-weight:bold");
		onlyPPOrder.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent evt) {
				if(evt.getNewValue()!=null){
					//
					posPanel.resetProduction();
					refreshPanel();
					//
					Integer ppOrder_ID = (Integer) evt.getNewValue();
					if(ppOrder_ID>0){
						if(posPanel.getProduction()==null)
							posPanel.newProduction();
						
						MPPOrder pp_order = new MPPOrder(Env.getCtx(), ppOrder_ID, null);
						posPanel.getProduction().setPP_Order_ID(pp_order.getPP_Order_ID());
						posPanel.getProduction().setPP_Order_Workflow_ID(pp_order.getMPPOrderWorkflow().get_ID());
						posPanel.getProduction().setAD_Org_ID(pp_order.getAD_Org_ID());
						posPanel.getProduction().setM_Warehouse_ID(pp_order.getM_Warehouse_ID());
						onlyWarehouse.setValue(pp_order.getM_Warehouse_ID());
						posPanel.getProduction().setAD_OrgTrx_ID(pp_order.getAD_OrgTrx_ID());
						posPanel.getProduction().setC_Activity_ID(pp_order.getC_Activity_ID());
						posPanel.getProduction().setC_Campaign_ID(pp_order.getC_Campaign_ID());
						posPanel.getProduction().setC_Project_ID(pp_order.getC_Project_ID());
						posPanel.getProduction().setDescription(pp_order.getDescription());
						posPanel.getProduction().setS_Resource_ID(pp_order.getS_Resource_ID());
						onlyResource.setValue(pp_order.getS_Resource_ID());
						posPanel.getProduction().setM_Product_ID(pp_order.getM_Product_ID());
						onlyProduct.setValue(pp_order.getM_Product_ID());
						posPanel.getProduction().setC_UOM_ID(pp_order.getC_UOM_ID());
						posPanel.getProduction().setM_AttributeSetInstance_ID(pp_order.getM_AttributeSetInstance_ID());
						posPanel.getProduction().setMovementQty(pp_order.getQtyOrdered());
						posPanel.setQty(pp_order.getQtyOrdered());
						posPanel.getProduction().setC_DocTypeTarget_ID(pp_order.getC_DocTypeTarget_ID());
						onlyDocType.setValue(pp_order.getC_DocTypeTarget_ID());
						posPanel.getProduction().setCostCollectorType(MPPCostCollector.COSTCOLLECTORTYPE_ActivityControl);
						Timestamp now = new Timestamp(System.currentTimeMillis());
						posPanel.getProduction().setMovementDate(now);
						posPanel.getProduction().setDateAcct(now);
						
						setDataProduct((int) onlyProduct.getValue());
						
						refreshPanel();
						
					}
				}
				
			}
			
		});
				
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		rows.setWidth("100%");
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
				+ ".label-description {"+WPOS_Produc.FONTSIZEMEDIUM+" display:block; height:15px; font-weight:bold; width: 415px; overflow:hidden;}"
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
		f_lb_DocumentNo.setStyle(WPOS_Produc.FONTSIZEMEDIUM);
		row.appendChild(f_lb_DocumentNo.rightAlign());
		
		documentNo = new Label();
		documentNo.setStyle(WPOS_Produc.FONTSIZEMEDIUM+"; font-weight:bold");
		row.appendChild(documentNo.rightAlign());
		
		row = rows.newRow();
		row.setHeight("20px");
		row.setWidth("100%");
		Label f_lb_DocumentType = new Label (Msg.translate(Env.getCtx(), I_C_Order.COLUMNNAME_C_DocType_ID) + ":");
		f_lb_DocumentType.setStyle(WPOS_Produc.FONTSIZEMEDIUM);
		row.appendChild(f_lb_DocumentType.rightAlign());
		
		documentType = new Label();
		documentType.setClass("label-description");
		documentType.setStyle(WPOS_Produc.FONTSIZEMEDIUM+"; font-weight:bold; width:auto !important;max-width:225px !important; white-space:pre;");
		row.appendChild(documentType.rightAlign());
		
		row = rows.newRow();
		row.setHeight("20px");
		
		Label f_lb_DocumentStatus = new Label (Msg.translate(Env.getCtx(), I_C_Order.COLUMNNAME_DocStatus) + ":");
		f_lb_DocumentStatus.setStyle(WPOS_Produc.FONTSIZEMEDIUM);
		row.appendChild(f_lb_DocumentStatus.rightAlign());
		documentStatus= new Label();
		documentStatus.setStyle(WPOS_Produc.FONTSIZEMEDIUM+"; font-weight:bold");
		row.appendChild(documentStatus.rightAlign());
		
		row = rows.newRow();
		row.setHeight("20px");
		
		Label f_lb_SalesRep = new Label (Msg.translate(Env.getCtx(), I_C_Order.COLUMNNAME_SalesRep_ID) + ":");
		f_lb_SalesRep.setStyle(WPOS_Produc.FONTSIZEMEDIUM);
		row.appendChild(f_lb_SalesRep.rightAlign());
		
		salesRep = new Label(posPanel.getSalesRepName());
		salesRep.setStyle(WPOS_Produc.FONTSIZEMEDIUM+"; font-weight:bold");
		row.appendChild(salesRep.rightAlign());
		
		
		row = rows.newRow();
		rows = v_TotalsPanel.newRows();

		//
		row = rows.newRow();
		row.setHeight("10px");

		Label lDocumentDate = new Label (Msg.translate(Env.getCtx(), I_C_Order.COLUMNNAME_DateOrdered) + ":");
		lDocumentDate.setStyle(WPOS_Produc.FONTSIZEMEDIUM);
		row.appendChild(lDocumentDate);
		
		documentDate = new Label();
		documentDate.setStyle(WPOS_Produc.FONTSIZEMEDIUM+"; font-weight:bold");
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
		
//		scalesPanel = new WPOSScalesPanel(posPanel);
//		scalesPanel.hidePanel();
		//add(scalesPanel.getPanel(), scalesConstraint);
		
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		onlyResource = createField(posPanel.getWindowNo(), MResource.Table_Name, MResource.COLUMNNAME_S_Resource_ID);
		ZKUpdateUtil.setWidth(onlyResource.getComponent(), "98%");
		ZKUpdateUtil.setHeight(onlyResource.getComponent().getTextbox(),"35px");
		ZKUpdateUtil.setHeight(onlyResource.getComponent().getButton(),"35px");
		Label f_lb_resource = new Label (Msg.translate(Env.getCtx(), "S_Resource_ID") + ":");
		f_lb_resource.setStyle(WPOS_Produc.FONTSIZEMEDIUM);
		row.appendChild(f_lb_resource);
		row.appendChild(onlyResource.getComponent());
		onlyResource.getComponent().getTextbox().setStyle("Font-size:medium; font-weight:bold");
		onlyResource.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent evt) {
				if(evt.getNewValue()!=null){
					
				}
				
			}
			
		});
		
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		onlyDocType = createField(posPanel.getWindowNo(), MDocType.Table_Name, MDocType.COLUMNNAME_C_DocType_ID);
		ZKUpdateUtil.setWidth(onlyDocType.getComponent(), "98%");
		ZKUpdateUtil.setHeight(onlyDocType.getComponent().getTextbox(),"35px");
		ZKUpdateUtil.setHeight(onlyDocType.getComponent().getButton(),"35px");
		Label f_lb_docType = new Label (Msg.translate(Env.getCtx(), "C_DocType_ID") + ":");
		f_lb_docType.setStyle(WPOS_Produc.FONTSIZEMEDIUM);
		row.appendChild(f_lb_docType);
		row.appendChild(onlyDocType.getComponent());
		onlyDocType.getComponent().getTextbox().setStyle("Font-size:medium; font-weight:bold");
		onlyDocType.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent evt) {
				if(evt.getNewValue()!=null){
					
				}
				
			}
			
		});
		
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		onlyManufOrderActivity = createField(posPanel.getWindowNo(), "PP_Cost_Collector", "PP_Order_Node_ID");
		ZKUpdateUtil.setWidth(onlyManufOrderActivity.getComponent(), "98%");
		ZKUpdateUtil.setHeight(onlyManufOrderActivity.getComponent().getTextbox(),"35px");
		ZKUpdateUtil.setHeight(onlyManufOrderActivity.getComponent().getButton(),"35px");
		Label f_lb_ManOrdActivity = new Label (Msg.translate(Env.getCtx(), "PP_Order_Node_ID") + ":");
		f_lb_ManOrdActivity.setStyle(WPOS_Produc.FONTSIZEMEDIUM);
		row.appendChild(f_lb_ManOrdActivity);
		row.appendChild(onlyManufOrderActivity.getComponent());
		onlyManufOrderActivity.getComponent().getTextbox().setStyle("Font-size:medium; font-weight:bold");
		onlyManufOrderActivity.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent evt) {
				if(evt.getNewValue()!=null && posPanel.getProduction()!=null){
					int ppOrdNodeID = (int) evt.getNewValue();
					posPanel.getProduction().setPP_Order_Node_ID(ppOrdNodeID);
					
					if(m_node == null || m_node.get_ID() != ppOrdNodeID)
						m_node = new MPPOrderNode(ctx, ppOrdNodeID, null); 
					
					posPanel.getProduction().setS_Resource_ID(m_node.getS_Resource_ID());
					onlyResource.setValue(m_node.getS_Resource_ID());
					posPanel.getProduction().setIsSubcontracting(m_node.isSubcontracting());
					posPanel.getProduction().setMovementQty(m_node.getQtyDelivered());
					if(m_node.getQtyDelivered().intValue()>0)
						posPanel.setQty(m_node.getQtyDelivered());
					
					RoutingService routingService = RoutingServiceFactory.get().getRoutingService(ctx);
					BigDecimal durationReal = routingService.estimateWorkingTime(posPanel.getProduction());
					posPanel.getProduction().setDurationReal(durationReal);
					
				}
				
			}
			
		});
		
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		onlyProduct = createField(posPanel.getWindowNo(), MProduct.Table_Name, MProduct.COLUMNNAME_M_Product_ID);
		ZKUpdateUtil.setWidth(onlyProduct.getComponent(), "98%");
		ZKUpdateUtil.setHeight(onlyProduct.getComponent().getTextbox(),"35px");
		ZKUpdateUtil.setHeight(onlyProduct.getComponent().getButton(),"35px");
		Label f_lb_product = new Label (Msg.translate(Env.getCtx(), "M_Product_ID") + ":");
		f_lb_product.setStyle(WPOS_Produc.FONTSIZEMEDIUM);
		row.appendChild(f_lb_product);
		row.appendChild(onlyProduct.getComponent());
		onlyProduct.getComponent().getTextbox().setStyle("Font-size:medium; font-weight:bold");
		onlyProduct.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent evt) {
				if(evt.getNewValue()!=null){

					setDataProduct((Integer) evt.getNewValue());
//					Integer prod_ID = (Integer) evt.getNewValue();
//					String where = "M_Product_ID=? AND C_POSKeyLayout_ID=?";
//					MPOSKey key = new Query(ctx, MPOSKey.Table_Name, where, null)
//							.setOnlyActiveRecords(true)
//							.setClient_ID()
//							.setParameters(prod_ID, posPanel.getC_POSKeyLayout_ID())
//							.first();
//					
//					keyReturned(key);
//					((WSearchEditor)evt.getSource()).getComponent().getTextbox().focus();
				}
				
			}
			
		});
		
//		onlyProduct.getComponent().getTextbox().addFocusListener(new EventListener<Event>() {
//
//			@Override
//			public void onEvent(Event evt2) throws Exception {
//				((Textbox)evt2.getTarget()).select();
//				
//			}
//		});
		
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		onlyWarehouse = createField(posPanel.getWindowNo(), MWarehouse.Table_Name, MWarehouse.COLUMNNAME_M_Warehouse_ID);
		ZKUpdateUtil.setWidth(onlyWarehouse.getComponent(), "98%");
		ZKUpdateUtil.setHeight(onlyWarehouse.getComponent().getTextbox(),"35px");
		ZKUpdateUtil.setHeight(onlyWarehouse.getComponent().getButton(),"35px");
		Label f_lb_warehouse = new Label (Msg.translate(Env.getCtx(), "M_Warehouse_ID") + ":");
		f_lb_warehouse.setStyle(WPOS_Produc.FONTSIZEMEDIUM);
		row.appendChild(f_lb_warehouse);
		row.appendChild(onlyWarehouse.getComponent());
		onlyWarehouse.getComponent().getTextbox().setStyle("Font-size:medium; font-weight:bold");
		onlyWarehouse.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent evt) {
				if(evt.getNewValue()!=null){
					
				}
				
			}
			
		});
		
		// Start and Complete
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		buttonStart = createButtonAction("wfNext", "");
		buttonStart.addActionListener(this);
		row.appendChild(buttonStart);
		buttonStart.setEnabled(true);
		buttonComplete = createButtonAction("Ok", "");
		buttonComplete.addActionListener(this);
		row.appendChild(buttonComplete);
		buttonComplete.setEnabled(true);
		
		
		refreshPanel();
	}	//	init

	/**
	 * Call back from key panel
	 */
	@Override
	public void keyReturned(MPOSKey key) {
		// processed order
		if (posPanel.hasProduction()
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
		if(e.getTarget().equals(buttonStart)){
			if(posPanel.getProduction()!=null){
				posPanel.getProduction().saveEx();
				DB.commit(false, posPanel.getProduction().get_TrxName());
				FDialog.info(posPanel.getWindowNo(), posPanel.getForm(), 
						"Activity Control Report created! DocNo: "+posPanel.getProduction().getDocumentNo());
				
				refreshPanel();
			}
		}
		else if(e.getTarget().equals(buttonComplete)){
			completeProduction();
			FDialog.info(posPanel.getWindowNo(), posPanel.getForm(), 
					"Activity Control Report __DocNo: "+posPanel.getProduction().getDocumentNo()+" completed!");
			refreshPanel();
		}
	}
	
	@Override
	public void refreshPanel() {
		log.fine("RefreshPanel");
		if (!posPanel.hasProduction() && posPanel.getProduction()==null) {
			//	Document Info
			v_TitleBorder.setLabel(Msg.getMsg(Env.getCtx(), "Info"));
			salesRep.setText(posPanel.getSalesRepName());
			documentType.setText(Msg.getMsg(posPanel.getCtx(), "Order"));
			documentNo.setText(Msg.getMsg(posPanel.getCtx(), "New"));
			documentStatus.setText("");
			documentDate.setText("");
			
			onlyPPOrder.setValue(0);          
			onlyResource.setValue(0);         
			onlyDocType.setValue(0);          
			onlyProduct.setValue(0);          
			onlyWarehouse.setValue(0);        
			onlyManufOrderActivity.setValue(0);
		} else {
			//	Set Values
			//	Document Info
			v_TitleBorder.setLabel(Msg.getMsg(Env.getCtx(), "Info"));
			salesRep.setText(posPanel.getSalesRepName());
			documentType.setText(posPanel.getDocumentTypeName());
			documentNo.setText(posPanel.getDocumentNo());
			documentStatus.setText(MRefList.getListName(Env.getCtx(), 131, posPanel.getProduction().getDocStatus()));
			documentDate.setText(posPanel.getDateOrderedForView());
		}
		//	Repaint
//		v_TotalsPanel.invalidate();
//		v_OrderPanel.invalidate();
//		v_GroupPanel.invalidate();
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

	
	/**
	 * @param windowNo
	 * @return WSearchEditor
	 */
	private WSearchEditor createField(int windowNo, String tableName, String colNameKey) {
		//int AD_Column_ID = 6862;    //  S_Resource_ID
		int AD_Column_ID = MColumn.getColumn_ID(tableName, colNameKey);    //  M_Product_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), windowNo,
					0, AD_Column_ID, DisplayType.Search);
			return new WSearchEditor (colNameKey, false, false, true, lookup);
		}
		catch (Exception e)
		{
			FDialog.error(posPanel.getWindowNo(), e.getLocalizedMessage());
		}
		return null;
	}

	private void setDataProduct(int p_mProductID){
//		Integer prod_ID = p_mProductID;
		MPOSKey key = new MPOSKey(Env.getCtx(), 0, null);
		key.setM_Product_ID(p_mProductID);
		key.setQty(BigDecimal.ONE);
		keyReturned(key);
	}
	/**
	 * Execute order payment
	 * If order is not processed, process it first.
	 * If it is successful, proceed to pay and print ticket
	 */
	private void completeProduction() {
		//Check if order is completed, if so, print and open drawer, create an empty order and set cashGiven to zero
		if(!posPanel.hasProduction()) {
			FDialog.warn(posPanel.getWindowNo(), Msg.getMsg(ctx, "POS.MustCreateOrder"));
		} 
		else if(posPanel.hasProduction())
		{
				
			posPanel.processProduction(posPanel.getProduction().get_TrxName());
			posPanel.showKeyboard();
			posPanel.setProduction(posPanel.getProduction().get_ID());
			posPanel.refreshPanel();
			posPanel.refreshProductInfo(null);
            posPanel.restoreTable();
			return;
		}
		else {
			posPanel.hideKeyboard();
			
		}
	}
	
	public void setPrimaryFocus(){
		onlyPPOrder.getComponent().getTextbox().focus();
	}

}
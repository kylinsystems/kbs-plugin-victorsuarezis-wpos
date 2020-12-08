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

package org.adempiere.pos.posshopfloor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.pos.POSKeyListener;
import org.adempiere.pos.WPOSScalesPanel;
import org.adempiere.pos.service.POSPanelInterface;
import org.adempiere.util.Callback;
import org.adempiere.webui.adwindow.DetailPane;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Checkbox;
import org.adempiere.webui.component.DatetimeBox;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Messagebox;
import org.adempiere.webui.component.NumberBox;
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
import org.compiere.model.MProduction;
import org.compiere.model.MRefList;
import org.compiere.model.MResource;
import org.compiere.model.MWarehouse;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.wf.MWorkflow;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Space;
import org.zkoss.zul.Style;

import it.cnet.idempiere.LIT_WarehouseExtend.model.MProductionNode;
import it.cnet.idempiere.LIT_WarehouseExtend.model.MProductionWorkflow;
import it.cnet.impl.editorNatIDNumber.editor_2.WTaxIdFormWindow;
import it.idIta.idempiere.coreUtil.webui.UtilWebui;

import org.adempiere.pos.service.POSPanelInterface;
import org.compiere.model.MPOSKey;
import org.zkoss.zk.ui.event.Event;

/**
 * @author Andrea Checchia
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public class WPOSDocumentPanel_Shopfl extends WPOSSubPanel_Shopfl implements POSKeyListener, POSPanelInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8605876792613595291L;

	/**
	 * 	Constructor
	 *	@param posPanel POS Panel
	 */
	public WPOSDocumentPanel_Shopfl(WPOS_Shopfl posPanel) {
		super (posPanel);
	}	//	PosSubFunctionKeys
	
	/** Fields               */
	private Label 			salesRep;
	private Label 			documentNo;
	private Label 			documentStatus;
	private Label 			documentDate;
	private Label           prodNode;
	private Label			infoMInout;
	private boolean			isKeyboard;

	/**	Format				*/
	private DecimalFormat	m_Format;
	/**	Logger				*/
	private static CLogger 	log = CLogger.getCLogger(WPOSDocumentPanel_Shopfl.class);
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
	
	private WSearchEditor onlyProduction;
	private WSearchEditor onlyResource;
	private WSearchEditor onlyDocType;
	private WSearchEditor onlyProduct;
	private WSearchEditor onlyWarehouse;
	private WSearchEditor onlyWorkflow;
	private WSearchEditor onlyActivity;
	
	private NumberBox qtyHour;
	private DatetimeBox dateGo;
	private NumberBox fieldQuantity;
	private Checkbox  chkActivity;
	private Checkbox  chkCompleteOrder;
	private Textbox   txtReference_1;
	private Textbox   txtReference_2;
	
	private Label   execution;
	
	private Button 		buttonStop;
	private Button      buttonStart;
	private Button		buttonStart_Stop;
	
	private MWorkflow m_node;
	private MProductionNode pNode;
	
	private boolean isStandardMaskMode = false;
	
	@Override
	public void init(){
		isStandardMaskMode = (posPanel.getM_POS().get_ValueAsString("LIT_MaskMode").equals("S"));
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
		onlyResource = createField(posPanel.getWindowNo(), MResource.Table_Name, MResource.COLUMNNAME_S_Resource_ID);
		onlyResource.setMandatory(true);
		ZKUpdateUtil.setWidth(onlyResource.getComponent(), "98%");
		ZKUpdateUtil.setHeight(onlyResource.getComponent().getCombobox(),"35px");
		ZKUpdateUtil.setHeight(onlyResource.getComponent().getButton(),"35px");
		Label f_lb_resource = new Label (Msg.translate(Env.getCtx(), "S_Resource_ID") + ":");
		f_lb_resource.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(f_lb_resource);
		row.appendChild(onlyResource.getComponent());
		onlyResource.getComponent().getCombobox().setStyle("Font-size:medium; font-weight:bold");
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
		onlyProduction = createField(posPanel.getWindowNo(), MProduction.Table_Name, MProduction.COLUMNNAME_M_Production_ID);
		try {
			Lookup lkp_change = getLookup_ProductionNotComplete();
			UtilWebui.setLookup_class(onlyProduction, "lookup", lkp_change);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		ZKUpdateUtil.setWidth(onlyProduction.getComponent(), "98%");
		ZKUpdateUtil.setHeight(onlyProduction.getComponent().getCombobox(),"35px");
		ZKUpdateUtil.setHeight(onlyProduction.getComponent().getButton(),"35px");
		Label f_lb_ppOrder = new Label (Msg.translate(Env.getCtx(), "M_Production_ID") + ":");
		f_lb_ppOrder.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(f_lb_ppOrder);
		row.appendChild(onlyProduction.getComponent());
		onlyProduction.getComponent().getCombobox().setStyle("Font-size:medium; font-weight:bold");
		onlyProduction.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent evt) {
				if(evt.getNewValue()!=null){
					//
					posPanel.resetProduction();
					refreshPanel();
					//
					Integer mProduction_ID = (Integer) evt.getNewValue();
					if(mProduction_ID>0){
						//if(posPanel.getProduction()==null)   TODO verifdicare se non è da modificare il record in seguito
							posPanel.newProduction();
						
						MProduction prod_order = new MProduction(Env.getCtx(), mProduction_ID, null);
						posPanel.getProduction().setM_Production_ID(prod_order.getM_Production_ID());
						posPanel.getProduction().setAD_Org_ID(prod_order.getAD_Org_ID());
						posPanel.getProduction().setM_Locator_ID(prod_order.getM_Locator_ID());
						posPanel.getProduction().setM_Warehouse_ID(prod_order.getM_Locator().getM_Warehouse_ID());
						onlyWarehouse.setValue(prod_order.getM_Locator().getM_Warehouse_ID());
						posPanel.getProduction().setAD_OrgTrx_ID(prod_order.getAD_OrgTrx_ID());
						posPanel.getProduction().setC_Activity_ID(prod_order.getC_Activity_ID());
						posPanel.getProduction().setC_Project_ID(prod_order.getC_Project_ID());
						posPanel.getProduction().setDescription(prod_order.getDescription());
						posPanel.getProduction().setM_Product_ID(prod_order.getM_Product_ID());
						onlyProduct.setValue(prod_order.getM_Product_ID());
						posPanel.getProduction().setC_UOM_ID(prod_order.getM_Product().getC_UOM_ID());
						posPanel.getProduction().setM_AttributeSetInstance_ID(prod_order.getM_Product().getM_AttributeSetInstance_ID());
						posPanel.getProduction().setMovementQty(prod_order.getProductionQty());
						posPanel.setQty(prod_order.getProductionQty());
						posPanel.getProduction().setC_DocType_ID(prod_order.get_ValueAsInt("C_DocType_ID"));
						onlyDocType.setValue(prod_order.get_ValueAsInt("C_DocType_ID"));
						Timestamp now = new Timestamp(System.currentTimeMillis());
						posPanel.getProduction().setMovementDate(now);
						posPanel.getProduction().setDateAcct(now);
						dateGo.setValue(new Date(now.getTime()));
						MProductionWorkflow prodWF = getWorkflowProduction(mProduction_ID);
						posPanel.getProduction().setAD_Workflow_ID(prodWF.getAD_Workflow_ID());
						onlyWorkflow.setValue(prodWF.getAD_Workflow_ID());
						try {
							Lookup lkp_change = getLookup_NodeByWorkflow(prodWF.getAD_Workflow_ID());
							UtilWebui.setLookup_class(onlyActivity, "lookup", lkp_change);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						
						pNode = prodWF.getLastActivity();
						if(pNode!=null) {
							prodNode.setValue(pNode.getAD_WF_Node().getName());
							posPanel.getProduction().setAD_WF_Node_ID(pNode.getAD_WF_Node_ID());
						}
						posPanel.getProduction().setDocumentNo(prod_order.getDocumentNo());
						posPanel.getProduction().setDocStatus(prod_order.getDocStatus());
						posPanel.getProduction().setS_Resource_ID((Integer)onlyResource.getValue());
						
						setDataProduct((int) onlyProduct.getValue());
						refreshPanel();
						posPanel.getInfoProduct().setColor(posPanel.setMaterialAndColor());
						posPanel.getQtyProduct().updateSummaryInventory(mProduction_ID);
						posPanel.getInfoProduct().addProductionLine(mProduction_ID);
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
				+ ".label-description {"+WPOS_Shopfl.FONTSIZEMEDIUM+" display:block; height:15px; font-weight:bold; width: 415px; overflow:hidden;}"
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
		f_lb_DocumentNo.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(f_lb_DocumentNo.rightAlign());
		
		documentNo = new Label();
		documentNo.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM+"; font-weight:bold");
		row.appendChild(documentNo.rightAlign());
				
		row = rows.newRow();
		row.setHeight("20px");
		
		Label f_lb_DocumentStatus = new Label (Msg.translate(Env.getCtx(), I_C_Order.COLUMNNAME_DocStatus) + ":");
		f_lb_DocumentStatus.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(f_lb_DocumentStatus.rightAlign());
		documentStatus= new Label();
		documentStatus.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM+"; font-weight:bold");
		row.appendChild(documentStatus.rightAlign());
		
		row = rows.newRow();
		row.setHeight("20px");
		
		Label f_lb_SalesRep = new Label (Msg.translate(Env.getCtx(), I_C_Order.COLUMNNAME_SalesRep_ID) + ":");
		f_lb_SalesRep.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(f_lb_SalesRep.rightAlign());
		
		salesRep = new Label(posPanel.getSalesRepName());
		salesRep.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM+"; font-weight:bold");
		row.appendChild(salesRep.rightAlign());
		
		
		row = rows.newRow();
		rows = v_TotalsPanel.newRows();

		//
		row = rows.newRow();
		row.setHeight("10px");

		Label lDocumentDate = new Label (Msg.translate(Env.getCtx(), I_C_Order.COLUMNNAME_DateOrdered) + ":");
		lDocumentDate.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(lDocumentDate);
		
		documentDate = new Label();
		documentDate.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM+"; font-weight:bold");
		row.appendChild(documentDate.rightAlign());
		
		row = rows.newRow();
		row.setHeight("10px");
		
		Label lProdNode = new Label (Msg.translate(Env.getCtx(), "AD_WF_Activity_ID") + ":");
		lProdNode.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(lProdNode);
		
		prodNode = new Label();
		prodNode.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM+"; font-weight:bold");
		row.appendChild(prodNode.rightAlign());
		
		row = rows.newRow();
		row.setHeight("10px");
		
		String trlM_InOut = (Env.getAD_Language(ctx).equals("it_IT"))?"Entrata Merce":"Material Receipt";
		Label linfoMInOut = new Label (trlM_InOut + ":");
		linfoMInOut.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(linfoMInOut);
		
		infoMInout = new Label();
		infoMInout.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM+"; font-weight:bold");
		row.appendChild(infoMInout.rightAlign());
		
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
		onlyDocType = createField(posPanel.getWindowNo(), MDocType.Table_Name, MDocType.COLUMNNAME_C_DocType_ID);
		ZKUpdateUtil.setWidth(onlyDocType.getComponent(), "98%");
		ZKUpdateUtil.setHeight(onlyDocType.getComponent().getCombobox(),"35px");
		ZKUpdateUtil.setHeight(onlyDocType.getComponent().getButton(),"35px");
		Label f_lb_docType = new Label (Msg.translate(Env.getCtx(), "C_DocType_ID") + ":");
		f_lb_docType.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(f_lb_docType);
		row.appendCellChild(onlyDocType.getComponent(),2);
		onlyDocType.getComponent().getCombobox().setStyle("Font-size:medium; font-weight:bold");
		onlyDocType.getComponent().setEnabled(false);
		/* onlyDocType.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent evt) {
				if(evt.getNewValue()!=null){}
			}
		});  */
		
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		onlyProduct = createField(posPanel.getWindowNo(), MProduct.Table_Name, MProduct.COLUMNNAME_M_Product_ID);
		ZKUpdateUtil.setWidth(onlyProduct.getComponent(), "98%");
		ZKUpdateUtil.setHeight(onlyProduct.getComponent().getCombobox(),"35px");
		ZKUpdateUtil.setHeight(onlyProduct.getComponent().getButton(),"35px");
		Label f_lb_product = new Label (Msg.translate(Env.getCtx(), "M_Product_ID") + ":");
		f_lb_product.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(f_lb_product);
		row.appendCellChild(onlyProduct.getComponent(), 2);
		onlyProduct.getComponent().getCombobox().setStyle("Font-size:medium; font-weight:bold");
		onlyProduct.getComponent().setEnabled(false);
		/* onlyProduct.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent evt) {
				if(evt.getNewValue()!=null){
					setDataProduct((Integer) evt.getNewValue());
				}
			}
		}); */
		
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		onlyWarehouse = createField(posPanel.getWindowNo(), MWarehouse.Table_Name, MWarehouse.COLUMNNAME_M_Warehouse_ID);
		ZKUpdateUtil.setWidth(onlyWarehouse.getComponent(), "98%");
		ZKUpdateUtil.setHeight(onlyWarehouse.getComponent().getCombobox(),"35px");
		ZKUpdateUtil.setHeight(onlyWarehouse.getComponent().getButton(),"35px");
		Label f_lb_warehouse = new Label (Msg.translate(Env.getCtx(), "M_Warehouse_ID") + ":");
		f_lb_warehouse.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(f_lb_warehouse);
		row.appendCellChild(onlyWarehouse.getComponent(), 2);
		onlyWarehouse.getComponent().getCombobox().setStyle("Font-size:medium; font-weight:bold");
		onlyWarehouse.getComponent().setEnabled(false);
		/* onlyWarehouse.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent evt) {
				if(evt.getNewValue()!=null){}
			}
		}); */
		
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		onlyWorkflow = createField(posPanel.getWindowNo(), "M_Production", "AD_Workflow_ID"); //TODO ricontrollare la tabella per il campo.....
		ZKUpdateUtil.setWidth(onlyWorkflow.getComponent(), "98%");
		ZKUpdateUtil.setHeight(onlyWorkflow.getComponent().getCombobox(),"35px");
		ZKUpdateUtil.setHeight(onlyWorkflow.getComponent().getButton(),"35px");
		Label f_lb_Workflow = new Label (Msg.translate(Env.getCtx(), "AD_Workflow_ID") + ":");
		f_lb_Workflow.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(f_lb_Workflow);
		row.appendCellChild(onlyWorkflow.getComponent(), 2);
		onlyWorkflow.getComponent().getCombobox().setStyle("Font-size:medium; font-weight:bold");
		onlyWorkflow.getComponent().setEnabled(false);
		/* onlyWorkflow.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent evt) {
				if(evt.getNewValue()!=null && posPanel.getProduction()!=null){
					int workflowID = (int) evt.getNewValue();
					posPanel.getProduction().setAD_Workflow_ID(workflowID);			
				}
			}
		}); */
		
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		onlyActivity = createField(posPanel.getWindowNo(), "AD_WF_Node", "AD_WF_Node_ID");
		ZKUpdateUtil.setWidth(onlyActivity.getComponent(), "98%");
		ZKUpdateUtil.setHeight(onlyActivity.getComponent().getCombobox(),"35px");
		ZKUpdateUtil.setHeight(onlyActivity.getComponent().getButton(),"35px");
		Label f_lb_Activity = new Label (Msg.translate(Env.getCtx(), "AD_WF_Activity_ID") + ":");
		f_lb_Activity.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(f_lb_Activity);
		row.appendCellChild(onlyActivity.getComponent(), 2);
		onlyActivity.getComponent().getCombobox().setStyle("Font-size:medium; font-weight:bold");
	/*	
		onlyActivity.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent evt) {
				if(evt.getNewValue()!=null && posPanel.getProduction()!=null){
					int activity_ID = (int) evt.getNewValue();
					posPanel.getProduction().setAD_WF_Node_ID(activity_ID);
					Timestamp start = null;
					if(pNode!=null)
						start = pNode.getDateStartSchedule();
					Timestamp finish = new Timestamp(System.currentTimeMillis());
					double duration = 0;
					if(start!=null)
						duration = Duration.between((start.toLocalDateTime()), (finish.toLocalDateTime())).toSeconds();
					BigDecimal qty = differenceTime(duration);
					if(pNode!=null) {
						if (pNode.getAD_WF_Node_ID()==activity_ID) {
							if(pNode.getDateFinishSchedule()==null) {
								//pNode.setDurationRequired(pNode.getDurationRequired().add(BigDecimal.valueOf(duration)));
								if(duration > 0) {
									execution.setText("Sono le "+(finish.toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")))+" --- Qty "+qty);
									//execution.setVisible(true);
									qtyHour.setValue(qty);
									if(pNode.getDateFinishSchedule()==null) {
//										if(!isStandardMaskMode)
											buttonStart.setDisabled(true);
//										else
//											buttonStart_Stop.setDisabled(true);
									}else {
//										if(!isStandardMaskMode)
											buttonStart.setDisabled(false);
//										else
//											buttonStart_Stop.setDisabled(false);
									}
								}
							}
							else {
//								if(!isStandardMaskMode)
									buttonStop.setDisabled(true);
//								else
//									buttonStart_Stop.setDisabled(true);
							}
						}
						else {
							execution.setText("Sono le "+(finish.toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")))+" --- FASE: "+pNode.getAD_WF_Node().getName()+" Qty "+qty);
							//execution.setVisible(true);
							pNode.setDurationReal(pNode.getDurationRequired().add(qty));
							pNode.setDurationRequired(null);
							pNode.setDateFinish(posPanel.getProduction().getMovementDate());
							pNode.setDateStartSchedule(null);
							pNode.setDateFinishSchedule(null);
							pNode.saveEx();
							pNode = null;
						}
					}
				}
			}
		});
	*/	
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		execution = new Label();
		execution.setStyle("Font-size:medium; font-weight:bold");
		ZKUpdateUtil.setHeight(execution, "35px");
		row.appendCellChild(execution, 2);
		execution.setVisible(false);
		 
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		dateGo = new DatetimeBox();
		ZKUpdateUtil.setWidth(dateGo, "50%");
		ZKUpdateUtil.setHeight(dateGo.getDatebox(),"30px");
		ZKUpdateUtil.setHeight(dateGo.getTimebox(),"30px");
		dateGo.getDatebox().setStyle("display: inline;Font-size:medium;");
		dateGo.getTimebox().setStyle("display: inline;Font-size:medium;");
		if(isStandardMaskMode)
			dateGo.setEnabled(true);
		else
			dateGo.setEnabled(false);
		Label f_lb_DateGo = new Label (Msg.translate(Env.getCtx(), "DateStart") + ":");
		f_lb_DateGo.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(f_lb_DateGo);
		row.appendCellChild(dateGo, 2);
		
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		qtyHour = new NumberBox(false);
		ZKUpdateUtil.setWidth(qtyHour, "65px");
		ZKUpdateUtil.setHeight(qtyHour.getDecimalbox(),"30px");
		ZKUpdateUtil.setHeight(qtyHour.getButton(),"30px");
		qtyHour.getDecimalbox().setStyle("display: inline;Font-size:medium;");
		Label f_lb_Time = new Label (Msg.translate(Env.getCtx(), "WorkingTime") + ":");
		f_lb_Time.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(f_lb_Time);
		row.appendCellChild(qtyHour, 2);
		
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		Label qtyLabel = new Label(Msg.translate(Env.getCtx(), "QtyInternalUse"));
		qtyLabel.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(qtyLabel);
		fieldQuantity = new NumberBox(true);
		row.appendCellChild(fieldQuantity, 2);
		fieldQuantity.addEventListener(Events.ON_OK, this);
		fieldQuantity.addEventListener(Events.ON_CHANGE, this);
		//		fieldQuantity.setStyle("display: inline;width:65px;height:30px;Font-size:medium;");
		ZKUpdateUtil.setWidth(fieldQuantity, "65px");
		ZKUpdateUtil.setHeight(fieldQuantity.getDecimalbox(),"30px");
		ZKUpdateUtil.setHeight(fieldQuantity.getButton(),"30px");
		fieldQuantity.getDecimalbox().setStyle("display: inline;Font-size:medium;");
		
		row = new Row();
		rows.appendChild(row);
		rows.setHeight("100%");
		Label f_lb_reference = new Label (Msg.translate(Env.getCtx(), "Reference") + ":");
		f_lb_reference.setStyle(WPOS_Shopfl.FONTSIZEMEDIUM);
		row.appendChild(f_lb_reference);
		txtReference_1 = new Textbox();
		row.appendChild(txtReference_1);
		ZKUpdateUtil.setWidth(txtReference_1, "50%");
		ZKUpdateUtil.setHeight(txtReference_1,"30px");
		txtReference_1.setStyle("Font-size:medium; font-weight:bold");
		row.appendChild(new Label("A"));
		txtReference_2 = new Textbox();
		row.appendChild(txtReference_2);
		ZKUpdateUtil.setWidth(txtReference_2, "50%");
		ZKUpdateUtil.setHeight(txtReference_2,"30px");
		txtReference_2.setStyle("Font-size:medium; font-weight:bold");
		// Start and Complete
//		if(!isStandardMaskMode) {
			row = new Row();
			rows.appendChild(row);
			rows.setHeight("100%");
			
			buttonStart = createButton("START");
			buttonStart.setStyle("font-size: 28px; line-height: 40px;");
			buttonStart.addActionListener(this);
			row.appendChild(new Space());
			row.appendChild(buttonStart);
			//buttonStart.setEnabled(true);
			buttonStart.setDisabled(true);
			buttonStop = createButton("STOP");
			buttonStop.setStyle("font-size: 28px; line-height: 40px;");
			buttonStop.addActionListener(this);
			row.appendChild(buttonStop);
			buttonStop.setEnabled(true);
			
			row = new Row();
			rows.appendChild(row);
			rows.setHeight("100%");
			chkCompleteOrder = new Checkbox();
			chkCompleteOrder.setText("COMPLETA ORDINE");
			chkCompleteOrder.setStyle("font-weight:bold; font-size:18px;");
			ZKUpdateUtil.setWidth(chkCompleteOrder, "65px");
			ZKUpdateUtil.setHeight(chkCompleteOrder,"30px");
			row.appendChild(chkCompleteOrder);
			chkActivity = new Checkbox();
			chkActivity.setText("FINE FASE");
			chkActivity.setStyle("font-weight:bold; font-size:18px;");
			ZKUpdateUtil.setWidth(chkActivity, "65px");
			ZKUpdateUtil.setHeight(chkActivity,"30px");
			row.appendChild(new Space());
			row.appendChild(chkActivity);
			
//		} else {
//			row = new Row();
//			rows.appendChild(row);
//			rows.setHeight("100%");
//			buttonStart_Stop = createButton("START & STOP");
//			buttonStart_Stop.setStyle("font-size: 28px; line-height: 40px;");
//			buttonStart_Stop.addActionListener(this);
//			row.appendChild(new Space());
//			row.appendChild(buttonStart_Stop);
//			buttonStart_Stop.setEnabled(true);
//		}
		
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
		
		if(e.getTarget().equals(buttonStart)){
			if(posPanel.getProduction()!=null){
				checkOK();
				//Aggiornamento delle date
				Timestamp dateUpdated = new Timestamp(dateGo.getValue().getTime());
				posPanel.getProduction().setMovementDate(dateUpdated);
				posPanel.getProduction().setDateAcct(dateUpdated);
				posPanel.getProduction().setAD_WF_Node_ID((Integer)onlyActivity.getValue());
				posPanel.getProduction().saveEx();
				DB.commit(false, posPanel.getProduction().get_TrxName());

				if(pNode==null) {	
					pNode = new Query(ctx, MProductionNode.Table_Name, "AD_WF_Node_ID=? AND M_Production_ID=?", null)
							.setOnlyActiveRecords(true)
							.setClient_ID()
							.setParameters(((Integer)onlyActivity.getValue()), posPanel.getProduction().getM_Production_ID())
							.first();
					pNode.setS_Resource_ID(posPanel.getProduction().getS_Resource_ID());
					pNode.setDateStart(posPanel.getProduction().getMovementDate());
					pNode.setDateStartSchedule(pNode.getDateStart());
					pNode.setDateFinish(null);
					pNode.setLIT_ReferenceFrom(new BigDecimal(txtReference_1.getValue()));
					pNode.setLIT_ReferenceTO(new BigDecimal(txtReference_2.getValue()));
					pNode.saveEx();
				}
				else {
					pNode.setDateStartSchedule(new Timestamp(System.currentTimeMillis()));
					pNode.setDateFinishSchedule(null);
					pNode.setLIT_ReferenceFrom(new BigDecimal(txtReference_1.getValue()));
					pNode.setLIT_ReferenceTO(new BigDecimal(txtReference_2.getValue()));
					pNode.saveEx();
				}
				
				Messagebox.showDialog("FASE AVVIATA / CONFERMATA", "", Messagebox.OK, Messagebox.INFORMATION, new Callback<Integer>() {					
					@Override
					public void onCallback(Integer result) {
						//Events.echoEvent("onSaveError", posPanel.getForm(), null);
						onlyProduction.getComponent().setFocus(true);
					}
				});
				posPanel.resetProduction();
				refreshPanel();
				
			}
		}
		else if(e.getTarget().equals(buttonStop)){
			//completeProduction();  TODO verificare
			checkOK();
			String fillMandatory = Msg.translate(Env.getCtx(), "Mandatory");
			BigDecimal durationReal= null;
			if (qtyHour.getValue()==null || (qtyHour.getValue()).compareTo(BigDecimal.ZERO)==0 ) 
				throw new WrongValueException(qtyHour, fillMandatory);

			if(pNode!=null /*&& pNode.getAD_WF_Node_ID()==posPanel.getProduction().getAD_WF_Node_ID()*/) {	
				//Chiudo eventuale attività aperta
				Timestamp start  = null;
				Timestamp finish = new Timestamp(System.currentTimeMillis());
				BigDecimal qty = BigDecimal.ZERO;
				
				if(pNode.getAD_WF_Node_ID()==posPanel.getProduction().getAD_WF_Node_ID()) {
					start = new Timestamp(dateGo.getValue().getTime());
				}
				else {
					start =  pNode.getDateStartSchedule();
				}
				if(pNode.getDateFinishSchedule()==null || pNode.getAD_WF_Node_ID()==posPanel.getProduction().getAD_WF_Node_ID()) {
					finish = new Timestamp(dateGo.getValue().getTime());
					double duration = getDurationAndSetDateFinish(start, finish, qtyHour.getValue().doubleValue());
					qty = differenceTime(duration);
					pNode.setDurationReal(pNode.getDurationReal().add(qty));
				}
				else {
					finish = pNode.getDateFinishSchedule();
				}
				
				if((Integer)onlyActivity.getValue()!=posPanel.getProduction().getAD_WF_Node_ID() || ((Integer)onlyActivity.getValue()==posPanel.getProduction().getAD_WF_Node_ID() && chkActivity.isChecked())) {
					pNode.setDateFinish(finish);
					pNode.setDateStartSchedule(null);
					pNode.setDateFinishSchedule(null);
					//salvo l'avanzamento, SOLO in questo caso perchè poi dopo c'è un altro avanzamento di un'altra fase
					if(qty.compareTo(BigDecimal.ZERO)>0) {
						posPanel.getProduction().setMovementDate(new Timestamp(dateGo.getValue().getTime()));
						posPanel.getProduction().setDurationReal(qty);
						posPanel.getProduction().saveEx();
					}
					///
				}
				else {
					pNode.setDateStartSchedule(start);
					pNode.setDateFinishSchedule(finish);
				}
				pNode.saveEx();
				durationReal = qty;
				pNode = null;
				
			}
			if(pNode==null && onlyActivity.getValue()!=null && ((Integer)onlyActivity.getValue()!=posPanel.getProduction().getAD_WF_Node_ID() || posPanel.getProduction().getAD_WF_Node_ID()<=0)) {
				if(posPanel.getProduction().getLIT_ShopfloorControl_ID()>0) {
					posPanel.getProduction().setLIT_ShopfloorControl_ID(0); //vedi commento riga sopra, n.777
					posPanel.getProduction().setLIT_ShopfloorControl_UU(null);
				}
				Timestamp start  = null;
				Timestamp finish = new Timestamp(System.currentTimeMillis());
				BigDecimal qty = BigDecimal.ZERO;
				pNode = new Query(ctx, MProductionNode.Table_Name, "AD_WF_Node_ID=? AND M_Production_ID=?", null)
						.setOnlyActiveRecords(true)
						.setClient_ID()
						.setParameters(((Integer)onlyActivity.getValue()), posPanel.getProduction().getM_Production_ID())
						.first();
				
				pNode.setS_Resource_ID(posPanel.getProduction().getS_Resource_ID());
				if(chkActivity.isChecked()) {
					start = new Timestamp(dateGo.getValue().getTime());
					if(pNode.getDateStart()==null)
						pNode.setDateStart(start);
					pNode.setDateStartSchedule(start);

					double duration = getDurationAndSetDateFinish(start, finish, qtyHour.getValue().doubleValue());
					qty = differenceTime(duration);
					pNode.setDateFinishSchedule(finish);
					pNode.setDurationReal(((pNode.getDurationReal()==null)?BigDecimal.ZERO:pNode.getDurationReal()).add(qty));
					pNode.setDateFinish(pNode.getDateFinishSchedule());
					pNode.setDateStartSchedule(null);
					pNode.setDateFinishSchedule(null);
				}
				else {
					start = new Timestamp(dateGo.getValue().getTime());
					if(pNode.getDateStart()==null)
						pNode.setDateStart(start);
					pNode.setDateStartSchedule(start);

					double duration = getDurationAndSetDateFinish(start, finish, qtyHour.getValue().doubleValue());
					qty = differenceTime(duration);
					pNode.setDateStartSchedule(start);
					pNode.setDateFinishSchedule(finish);
					pNode.setDurationReal(((pNode.getDurationReal()==null)?BigDecimal.ZERO:pNode.getDurationReal()).add(qty));
				}
				pNode.setLIT_ReferenceFrom(new BigDecimal(txtReference_1.getValue()));
				pNode.setLIT_ReferenceTO(new BigDecimal(txtReference_2.getValue()));
				pNode.saveEx();
				durationReal = qty;
				
			}
			//Verifico se siamo in una gestione di START/STOP immediato di una nuova fase; se è nuova, inserisco record su "Controllo Avanzamento Produzione
			if(posPanel.getProduction().getLIT_ShopfloorControl_ID()<=0) {
				posPanel.getProduction().setMovementDate(new Timestamp(dateGo.getValue().getTime()));
				posPanel.getProduction().setAD_WF_Node_ID((Integer)onlyActivity.getValue());
				posPanel.getProduction().setDurationReal(durationReal);
				posPanel.getProduction().saveEx();
			}
			else {
				
			}
			//FDialog.info(posPanel.getWindowNo(), posPanel.getForm(), "FASE FERMATA / TERMINATA");
			Messagebox.showDialog("FASE FERMATA / TERMINATA", "", Messagebox.OK, Messagebox.INFORMATION, new Callback<Integer>() {					
				@Override
				public void onCallback(Integer result) {
					//Events.echoEvent("onSaveError", posPanel.getForm(), null);
					onlyProduction.getComponent().setFocus(true);
				}
			});
			if(chkCompleteOrder.isChecked()) {
				MProduction productionforComplete = new MProduction(ctx, posPanel.getProduction().getM_Production_ID(), null);
				productionforComplete.setIsComplete(true);
				productionforComplete.saveEx();
			}
			posPanel.resetProduction();
			refreshPanel();
			posPanel.refreshPanel();
		}
//		else if(e.getTarget().equals(buttonStart_Stop)){
//			if(posPanel.getProduction()!=null){
//				checkOK();
//				posPanel.getProduction().saveEx();
//				DB.commit(false, posPanel.getProduction().get_TrxName());
//				pNode = new MProductionNode(ctx,posPanel.getProduction().getAD_WF_Node_ID(), null);
//				pNode.setS_Resource_ID(posPanel.getProduction().getS_Resource_ID());
//				pNode.setDateStart(new Timestamp(dateGo.getValue().getTime()));
//					//prodNode.setDateStartSchedule(prodNode.getDateStart());  (null);
//				pNode.setDurationReal(qtyHour.getValue());
//				Timestamp newDateFinish = Timestamp.valueOf((pNode.getDateStart().toLocalDateTime()).plusHours(qtyHour.getValue().longValue())) ;
//				pNode.setDateFinish(newDateFinish);
//				pNode.setHelp(txtReference.getValue());
//				pNode.saveEx();
//				
//				FDialog.info(posPanel.getWindowNo(), posPanel.getForm(), "FASE AVVIATA E CONCLUSA");
//				posPanel.resetProduction();
//				pNode = null;
//				refreshPanel();
//			}
//		}
		else if(Events.ON_OK.equals(e.getName()) || Events.ON_CHANGE.equals(e.getName())) {
		
			BigDecimal value = fieldQuantity.getValue();
			if(value == null)
				value = BigDecimal.ZERO;
			if(e.getTarget().equals(fieldQuantity.getDecimalbox())) {
				if(Events.ON_OK.equals(e.getName()) || Events.ON_CHANGE.equals(e.getName())){
					posPanel.setQty(value);
					posPanel.changeViewPanel();
					
				}
			}
		}
	}
	
	private double getDurationAndSetDateFinish (Timestamp start, Timestamp finish, double valQtyHour){
		//double valQtyHour = qtyHour.getValue().doubleValue();
		long timeInMilliSeconds = (long) Math.floor(valQtyHour * 60 * 60 * 1000);
		finish.setTime(start.getTime()+timeInMilliSeconds);
		double duration = Duration.between((start.toLocalDateTime()), (finish.toLocalDateTime())).toSeconds();
		return duration;
	}
	
	private void checkOK() {
		//Check OK
		String fillMandatory = Msg.translate(Env.getCtx(), "Mandatory");
		if (onlyResource.getValue()==null || ((Integer)onlyResource.getValue()) <=0 ) 
			throw new WrongValueException(onlyResource.getComponent(), fillMandatory);

		if (onlyProduction.getValue()==null || ((Integer)onlyProduction.getValue()) <=0 ) 
			throw new WrongValueException(onlyProduction.getComponent(), fillMandatory);
		
		if (onlyActivity.getValue()==null || ((Integer)onlyActivity.getValue()) <=0 ) 
			throw new WrongValueException(onlyActivity.getComponent(), fillMandatory);
		
		if(dateGo.getValue()==null)
			throw new WrongValueException(dateGo, fillMandatory);
		/////
	}
	
	@Override
	public void refreshPanel() {
		log.fine("RefreshPanel");
		isStandardMaskMode = (posPanel.getM_POS().get_ValueAsString("LIT_maskMode").equals("S"));
		if (!posPanel.hasProduction() && posPanel.getProduction()==null) {
			//	Document Info
			v_TitleBorder.setLabel(Msg.getMsg(Env.getCtx(), "Info"));
			salesRep.setText(posPanel.getSalesRepName());
			documentNo.setText(Msg.getMsg(posPanel.getCtx(), "New"));
			documentStatus.setText("");
			documentDate.setText("");
			infoMInout.setText("");
			
			onlyProduction.setValue(null);          
			//onlyResource.setValue(0);         
			onlyDocType.setValue(null);          
			onlyProduct.setValue(null);          
			onlyWarehouse.setValue(null);        
			onlyWorkflow.setValue(null);
			onlyActivity.setValue(null);
			execution.setVisible(false);
//			if(!isStandardMaskMode) {
				//buttonStart.setEnabled(true);
				buttonStart.setDisabled(true);
				buttonStop.setEnabled(true);
				dateGo.setValue(null);
				if(isStandardMaskMode)
					dateGo.setEnabled(true);
				else
					dateGo.setEnabled(false);
//			}
//			else 
//				buttonStart_Stop.setEnabled(true);
			prodNode.setValue("");
			qtyHour.setValue(0);
			fieldQuantity.setValue(0);
			
			txtReference_1.setValue(null);
			txtReference_2.setValue(null);
			chkActivity.setChecked(false);
			
		} else {
			//	Set Values
			//	Document Info
			v_TitleBorder.setLabel(Msg.getMsg(Env.getCtx(), "Info"));
			salesRep.setText(posPanel.getSalesRepName());
			documentNo.setText(posPanel.getDocumentNo());
			documentStatus.setText(MRefList.getListName(Env.getCtx(), 131, posPanel.getProduction().getDocStatus()));
			documentDate.setText(posPanel.getDateOrderedForView());
			infoMInout.setText(posPanel.getDocumentNoMinOut());
		}
		//	Repaint
//		v_TotalsPanel.invalidate();
//		v_OrderPanel.invalidate();
//		v_GroupPanel.invalidate();
	}

	private MProductionWorkflow getWorkflowProduction(int mProduction_ID) {
		MProductionWorkflow prodWF =  new Query(ctx, MProductionWorkflow.Table_Name, "M_Production_ID=?", null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setParameters(mProduction_ID)
				.first();
		
		return prodWF;
	}
	
	public void setQuantity(BigDecimal value) {
		fieldQuantity.setValue(value);
	}

	public int getProdLineEndProduct() {
		int id_productionLine = 0;
		//int m_production_id = ((Integer)onlyProduction.getValue());
		int m_production_id = -1;
		if(posPanel.getProduction()!=null && posPanel.getProduction().getM_Production_ID()>0) {
			m_production_id = posPanel.getProduction().getM_Production_ID();
			MProduction production = new MProduction(ctx, m_production_id, null);
			id_productionLine = Arrays.asList(production.getLines()).stream()
					.filter(ln -> ln.isEndProduct())
					.findFirst().get().getM_ProductionLine_ID();
		}
		return id_productionLine;
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

	private BigDecimal differenceTime(double time) {
		BigDecimal diff = BigDecimal.ZERO;
		if(time>0)
			diff = BigDecimal.valueOf((time/(60*60/*\*1000*/))).setScale(2, RoundingMode.CEILING);	
		return diff;
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
	
	private Lookup getLookup_NodeByWorkflow(int AD_Workflow_ID) throws Exception {
		Lookup lookup = null;
		String validationCode = "AD_Workflow_ID="+AD_Workflow_ID;
		lookup = MLookupFactory.get (Env.getCtx(), 0, 
				0, DisplayType.Search,
				Env.getLanguage(Env.getCtx()), "AD_WF_Node_ID", 0, false,
				validationCode);
		return lookup;
	}
	
	private Lookup getLookup_ProductionNotComplete() throws Exception{
		Lookup lookup = null;
		String validationCode = "isComplete='N' OR isComplete IS NULL";
		lookup = MLookupFactory.get (Env.getCtx(), 0, 
				0, DisplayType.Search,
				Env.getLanguage(Env.getCtx()), "M_Production_ID", 0, false,
				validationCode);
		return lookup;
	}

	private void setDataProduct(int p_mProductID){
//		Integer prod_ID = p_mProductID;
		MPOSKey key = new MPOSKey(Env.getCtx(), 0, null);
		key.setM_Product_ID(p_mProductID);
		key.setQty(BigDecimal.ONE);
		keyReturned(key);
	}
	
	public void setPrimaryFocus(){
		onlyProduction.getComponent().getCombobox().focus();
	}

	private void onProva() {
		
	}
}

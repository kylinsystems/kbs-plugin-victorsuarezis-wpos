/******************************************************************************
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
 * Copyright (C) 2003-2014 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * 
 *****************************************************************************/

package org.adempiere.pos.posshopfloor;

//import java.awt.MouseInfo;
//import java.awt.Point;
import java.math.BigDecimal;
//import java.text.DecimalFormat;
import java.util.List;

import org.adempiere.exceptions.AdempiereException;
//import org.adempiere.pos.WPOSFilterMenu;
//import org.adempiere.pos.search.WQueryDocType;
//import org.adempiere.pos.search.WQueryUserQry;
import org.adempiere.pos.service.POSPanelInterface;
//import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
//import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListCell;
import org.adempiere.webui.component.ListItem;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.NumberBox;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
//import org.adempiere.webui.component.Window;
import org.adempiere.webui.editor.WSearchEditor;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.Lookup;
import org.compiere.model.MColumn;
import org.compiere.model.MLookupFactory;
//import org.compiere.model.MOrder;
import org.compiere.model.MProduct;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
//import org.zkforge.keylistener.Keylistener;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
/**
 * Button panel Up Down
 * @author Mario Calderon, mario.calderon@westfalia-it.com, Systemhaus Westfalia, http://www.westfalia-it.com
 * @author Raul Muñoz, rmunoz@erpcya.com, ERPCYA http://www.erpcya.com
 *  <li><a href="https://github.com/adempiere/adempiere/issues/533">
 *           BF/FR [ 533 ] Update Fields when selected line</a> 
 *  <li><a href="https://github.com/adempiere/adempiere/issues/530">
 *           BF/FR [ 530 ] Added Validation in fields quantity, price and Discount</a>
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * @author victor.perez@e-evolution.com , http://www.e-evolution.com
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public class WPOSQuantityPanel_Shopfl extends WPOSSubPanel_Shopfl implements POSPanelInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = -21082207371857594L;

	public WPOSQuantityPanel_Shopfl(WPOS_Shopfl posPanel) {
		super(posPanel);
	}

	/** Buttons Controls  		*/
	private Button 			buttonDelete;
	private Button 			buttonComplete;
	
	/**	Process Action 						*/
	private WPOSFilterMenu_Shopfl actionFilterMenu;

	private final String ACTION_UP       	= "Previous";
	private final String ACTION_DOWN  		= "Next";
	private final BigDecimal CurrentQuantity =  Env.ONE;

	private Panel 		parameterPanel;
	
	private Grid		infoProductLayout_3;
	private Listbox     lst_summary;
	
	@Override
	protected void init() {

		Grid LayoutButton = GridFactory.newGridLayout();

		Rows rows = null;
		Row row = null;	

		parameterPanel = new Panel();
		parameterPanel.appendChild(LayoutButton);
		LayoutButton.setWidth("100%");
		LayoutButton.setHeight("100%");

		rows = LayoutButton.newRows();
		row = rows.newRow();
		row.setHeight("55px");
		buttonDelete = createButtonAction("Cancel", "Ctrl+F3");
		buttonDelete.setTooltiptext("Ctrl+F3-"+Msg.translate(ctx, "DeleteLine"));
		buttonDelete.addActionListener(this);
		row.appendChild (buttonDelete);
		
		// Complete
		buttonComplete = createButtonAction("Ok", "F4");
		buttonComplete.setTooltiptext("F4"+"-"+Msg.translate(ctx, "Complete"));
		buttonComplete.addActionListener(this);
		row.appendChild(buttonComplete);
		buttonComplete.setEnabled(true);
		
		Groupbox groupPanel = new Groupbox();
		infoProductLayout_3 = GridFactory.newGridLayout();
		Caption v_TitleBorder = new Caption("CONSUMI EFFETTIVI");
		groupPanel.appendChild(v_TitleBorder);
		groupPanel.appendChild(infoProductLayout_3);
		parameterPanel.appendChild(groupPanel);
		infoProductLayout_3.setStyle("border: none; width:100%; moz-box-shadow: 0 0 0px #888;-webkit-box-shadow: 0 0 0px #888;box-shadow: 0 0 0px #888;");
		rows = infoProductLayout_3.newRows();
		row = rows.newRow();
		lst_summary = initSummary();
		row.appendChild(lst_summary);
		
		actionFilterMenu = new WPOSFilterMenu_Shopfl(posPanel);

		changeStatus(false);
	}

	@Override
	public void onEvent(Event e) throws Exception {
		try {
			if (Events.ON_CTRL_KEY.equals(e.getName())) {
				KeyEvent keyEvent = (KeyEvent) e;
				//Alt+up == 38
				if (keyEvent.getKeyCode() == 38 ) {
					posPanel.moveUp();
				}
				//Alt+down == 40
				if (keyEvent.getKeyCode() == 40 ) {
					posPanel.moveDown();
				}
				//F4 == 115
                else if (keyEvent.getKeyCode() == 115 ) {
                	if(posPanel.getInfoProduct().getCurrentInventory()!=null)
            		{
            			posPanel.getInfoProduct().completeInventory();
            			posPanel.refreshPanel();
            			return;
            		}
                }
			}
			else if (e.getTarget().equals(buttonDelete)){
				if(posPanel.getInfoProduct().getCurrentInventory()!=null)
				{
					posPanel.getInfoProduct().deleteLineInv();
					posPanel.updateLineTable();
					posPanel.refreshPanel();
					return;
				}
			}
			else if (e.getTarget().equals(buttonComplete)) {
				if(posPanel.getInfoProduct().getCurrentInventory()!=null)
				{
//					int docTypeId = posPanel.getM_POS().getC_DocType_ID();
//					posPanel.getInfoProduct().createInventory(docTypeId);
					
//					posPanel.getInfoProduct().completeInventory();
//					posPanel.refreshPanel();
//					int M_Production_ID = posPanel.getProduction().getM_Production_ID();
//					updateSummaryInventory(M_Production_ID);
//					//delete inventory not used
//					String sqlDelete = "DELETE FROM M_Inventory "
//							+ "WHERE M_Inventory_ID IN (SELECT M_Inventory_ID FROM M_InventoryLine "
//							+ "							WHERE M_ProductionLine_ID IN (SELECT M_ProductionLine_ID FROM M_ProductionLine "
//							+ "															WHERE M_Production_id=?) "
//							+ "							AND M_Inventory_ID IN (SELECT M_Inventory_ID FROM M_Inventory WHERE DocStatus NOT IN('CO')))";
//					DB.executeUpdate(sqlDelete, M_Production_ID, null);
//					return;
				}
			}

			posPanel.updateLineTable();
			//posPanel.refreshPanel();
			posPanel.changeViewPanel();
			posPanel.getMainFocus();
		} catch (AdempiereException exception) {
			FDialog.error(posPanel.getWindowNo(), this, exception.getLocalizedMessage());
		}
	}

	/**
	 * Change Status 
	 * @param status
	 */
	public void changeStatus(boolean status) {
		buttonDelete.setEnabled(status);
		buttonComplete.setEnabled(status);
	}
	
	private Listbox initSummary() {

		Listbox box = new Listbox();
		box.setSizedByContent(true);
		box.setSpan(true);
		box.setRows(4);
		
		Listhead listhead = new Listhead();
		listhead.setSizable(true);
		Listheader listheader = new Listheader("Prodotto");
		listhead.appendChild(listheader);
		listheader = new Listheader("Qtà Consumata");
		listhead.appendChild(listheader);
		
		box.appendChild(listhead);
		
		return box;
	}
	
	public void updateSummaryInventory(int M_Production_ID) {
		if(lst_summary!=null) {
			String sql = "SELECT M_Product_id, QtyInternalUse "
					+ "FROM M_InventoryLine "
					+ "WHERE AD_Client_ID=? AND M_ProductionLine_ID IN (SELECT M_ProductionLine_ID "
					+ "														FROM M_ProductionLine "
					+ "														WHERE M_Production_id=?) "
					+ "AND M_Inventory_ID IN (SELECT M_Inventory_ID FROM M_Inventory WHERE DocStatus='CO')";
			
			List<List<Object>> listConsum = DB.getSQLArrayObjectsEx(null, sql, Env.getAD_Client_ID(ctx), M_Production_ID);
			if(listConsum!=null && listConsum.size()>0) {
				int mproductID = 0;
				BigDecimal count = BigDecimal.ZERO;
				int idx = 1;
				for (List<Object> row : listConsum) {
					mproductID = ((BigDecimal) row.get(0)).intValue();
					count = ((BigDecimal) row.get(1));
					
					ListItem item = new ListItem();
					item.setId("it_"+idx);
					ListCell cellColumn = null;
					WSearchEditor product = createField(posPanel.getWindowNo(), MProduct.Table_Name, MProduct.COLUMNNAME_M_Product_ID, null);;
					product.getComponent().setId("1prodConsum_"+idx);
					ZKUpdateUtil.setHeight(product.getComponent().getTextbox(),"35px");
					ZKUpdateUtil.setHeight(product.getComponent().getButton(),"35px");
					cellColumn = new ListCell();
					cellColumn.setId("1prodConsum1_"+idx);
					cellColumn.appendChild(product.getComponent());
					product.getComponent().getTextbox().setStyle("Font-size:medium; font-weight:bold");
					product.getComponent().setEnabled(false);
					product.setValue(mproductID);
					item.appendChild(cellColumn);
					NumberBox fieldQtyConsum = new NumberBox(true);
					fieldQtyConsum.setId("2qtyConsum_"+idx);
					ZKUpdateUtil.setHeight(fieldQtyConsum.getDecimalbox(),"35px");
					ZKUpdateUtil.setHeight(fieldQtyConsum.getButton(),"35px");
					cellColumn = new ListCell();
					cellColumn.setId("2qtyConsum_#2"+idx);
					cellColumn.appendChild(fieldQtyConsum);
					fieldQtyConsum.getDecimalbox().setStyle("display: inline;Font-size:medium; font-weight:bold;");
					fieldQtyConsum.getDecimalbox().setDisabled(true);
					fieldQtyConsum.getButton().setVisible(false);
					fieldQtyConsum.setValue(count);
					item.appendChild(cellColumn);
					item.setValue(""+mproductID+"_"+count+"#"+idx);
					lst_summary.appendChild(item);
					idx++;
				}
			}
		}
	}

	@Override
	public void refreshPanel() {
		if(posPanel.hasRecord()){
			// Only enable buttons if status==(drafted or in progress)
//			if(posPanel.getProduction().getDocStatus().compareToIgnoreCase(MOrder.STATUS_Drafted)==0 || 
//					posPanel.getProduction().getDocStatus().compareToIgnoreCase(MOrder.STATUS_InProgress)==0 ){
//				buttonDelete.setEnabled(true);
//				buttonComplete.setEnabled(true);
//			}else {
//				buttonDelete.setEnabled(false);
//				buttonComplete.setEnabled(false);
//			}
		} 
		changeViewPanel();
	}

	@Override
	public void moveUp() {

	}

	@Override
	public void moveDown() {
	}

	@Override
	public String validatePayment() {
		return null;
	}

	@Override
	public void changeViewPanel() {
		if(posPanel.getQty().compareTo(Env.ZERO) == 0)
			changeStatus(false);
		else
			changeStatus(true);
	}

	public void resetPanel() {
		buttonDelete.setEnabled(false);
		buttonComplete.setEnabled(false);
		lst_summary.getItems().clear();
	}

	public void setQuantity(BigDecimal value) {
	}

	public void requestFocus() {
	}

	/**
	 * Get Panel 
	 * @return Panel
	 */
	public Panel getPanel(){
		return parameterPanel;
	}

	private WSearchEditor createField(int windowNo, String tableName, String colNameKey, String validationCode) {
		//int AD_Column_ID = 6862;    //  S_Resource_ID
		int AD_Column_ID = MColumn.getColumn_ID(tableName, colNameKey);    //  M_Product_ID
		try
		{
			Lookup lookup = null;
			if(validationCode== null || validationCode.isEmpty()) {
				lookup = MLookupFactory.get (Env.getCtx(), windowNo,
						0, AD_Column_ID, DisplayType.Search);
			}
			else {
				lookup = MLookupFactory.get (Env.getCtx(), windowNo, 
						AD_Column_ID, DisplayType.Search,
						Env.getLanguage(Env.getCtx()), colNameKey, 0, false,
						validationCode);
			}
			
			return new WSearchEditor (colNameKey, false, false, true, lookup);
		}
		catch (Exception e)
		{
			FDialog.error(posPanel.getWindowNo(), e.getLocalizedMessage());
		}
		return null;
	}
//	/** 
//	 * Open window Filter 
//	 */
//	private void openFilter() { 
//		WQueryUserQry qt = new WQueryUserQry(posPanel);
//		qt.setVisible(true);
//		qt.setAttribute(Window.MODE_KEY, Window.MODE_HIGHLIGHTED);
//		AEnv.showWindow(qt);
//	}

}

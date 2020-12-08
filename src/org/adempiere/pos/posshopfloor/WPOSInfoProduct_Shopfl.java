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
 * Contributor(s): Raul Muñoz www.erpcya.com					              *
 *****************************************************************************/

package org.adempiere.pos.posshopfloor;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;

import org.adempiere.model.MInventory_lit;
import org.adempiere.pos.service.ProductInfo;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListCell;
import org.adempiere.webui.component.ListItem;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.NumberBox;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.editor.WSearchEditor;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.event.ValueChangeListener;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.Lookup;
import org.compiere.model.MColumn;
import org.compiere.model.MDocType;
import org.compiere.model.MInventoryLine;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MOrgInfo;
import org.compiere.model.MPOSKey;
import org.compiere.model.MProduct;
import org.compiere.process.DocAction;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Space;

/**
 * @author Mario Calderon, mario.calderon@westfalia-it.com, Systemhaus Westfalia, http://www.westfalia-it.com
 * @author Raul Muñoz, rmunoz@erpcya.com, ERPCYA http://www.erpcya.com
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * @author victor.perez@e-evolution.com , http://www.e-evolution.com
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public class WPOSInfoProduct_Shopfl extends WPOSSubPanel_Shopfl {

	/**
	 * 
	 * *** Constructor ***
	 * @param posPanel
	 */
	public WPOSInfoProduct_Shopfl(WPOS_Shopfl posPanel) {
		super(posPanel);
		
	}
	
	private Panel 		parameterPanel;
	/** Grid Panel 			*/
	private Grid 		infoProductLayout;
	private Grid        infoProductLayout_2;

	private Label       lbl_Color;
	private Listbox     lst_productionComponents;
	private Listbox     lst_consumables;
	private WSearchEditor onlyProductSearch;
	
	private MInventory_lit currentInventory;
	
	private DecimalFormat nf = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = -175459707049618428L;

	@Override
	public void onEvent(Event arg0) throws Exception {
		
	}

	@Override
	protected void init() {
		
		parameterPanel = new Panel();
		Groupbox groupPanel = new Groupbox();
		infoProductLayout = GridFactory.newGridLayout();

		Caption v_TitleBorder = new Caption("COMPONENTI ORDINE DI PRODUZIONE");
		groupPanel.appendChild(v_TitleBorder);
		groupPanel.appendChild(infoProductLayout);
		parameterPanel.appendChild(groupPanel);
		
//		buttonPanel.setStyle("border: none; width:99%;moz-box-shadow: 0 0 0px #888;-webkit-box-shadow: 0 0 0px #888;box-shadow: 0 0 0px #888;");
		infoProductLayout.setStyle("border: none; width:100%; moz-box-shadow: 0 0 0px #888;-webkit-box-shadow: 0 0 0px #888;box-shadow: 0 0 0px #888;");
		parameterPanel.setStyle("border: none; width:99%;");
		Rows rows = null;
		Row  row = null;
		rows = infoProductLayout.newRows();
		row = rows.newRow();
		lst_productionComponents = initComponents();
		row.appendChild(lst_productionComponents);
		
		parameterPanel.appendChild(new Space());
		
		groupPanel = new Groupbox();
		infoProductLayout_2 = GridFactory.newGridLayout();
		lbl_Color = new Label("COLORE: ");
		v_TitleBorder = new Caption("MATERIALI DI CONSUMO");
		groupPanel.appendChild(v_TitleBorder);
		groupPanel.appendChild(lbl_Color);
		groupPanel.appendChild(infoProductLayout_2);
		parameterPanel.appendChild(groupPanel);
		infoProductLayout_2.setStyle("border: none; width:100%; moz-box-shadow: 0 0 0px #888;-webkit-box-shadow: 0 0 0px #888;box-shadow: 0 0 0px #888;");
		rows = infoProductLayout_2.newRows();
		row = rows.newRow();
		lst_consumables = initConsumables();
		row.appendChild(lst_consumables);
		onlyProductSearch = createField(posPanel.getWindowNo(), MProduct.Table_Name, MProduct.COLUMNNAME_M_Product_ID, "IsKanban='Y'");
		ZKUpdateUtil.setWidth(onlyProductSearch.getComponent(), "45%");
		ZKUpdateUtil.setHeight(onlyProductSearch.getComponent().getCombobox(),"35px");
		ZKUpdateUtil.setHeight(onlyProductSearch.getComponent().getButton(),"35px");
		onlyProductSearch.getComponent().getCombobox().setStyle("Font-size:medium; font-weight:bold");
		onlyProductSearch.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent evt) {
				if(evt.getNewValue()!=null && posPanel.getProduction()!=null && posPanel.getProduction().getM_Production_ID()>0){
					if(lst_consumables.getItemCount()==0) {
						int docTypeId = posPanel.getM_POS().getC_DocType_ID();
						createInventory(docTypeId);
					}
					Integer prod_ID = (Integer) evt.getNewValue();
					addProductConsume(prod_ID);
				}
			}
		});
		parameterPanel.appendChild(onlyProductSearch.getComponent());
		
	}
	
	
	private Listbox initComponents() {
		Listbox box = new Listbox();
		box.setSizedByContent(true);
		box.setSpan(true);
		box.setRows(2);		
		Listhead listhead = new Listhead();
		listhead.setSizable(true);
		Listheader listheader = new Listheader("Prodotto");
		listhead.appendChild(listheader);
		listheader = new Listheader("Qta'");
		listhead.appendChild(listheader);
		listheader = new Listheader("Prelievo");
		listhead.appendChild(listheader);
		listheader = new Listheader("Prelevata");
		listhead.appendChild(listheader);
		
		box.appendChild(listhead);
//		box.appendChild(item);
		
		return box;
	}
	
	private Listbox initConsumables() {

		Listbox box = new Listbox();
		box.setSizedByContent(true);
		box.setSpan(true);
		box.setRows(3);
		
		Listhead listhead = new Listhead();
		listhead.setSizable(true);
		Listheader listheader = new Listheader("Prodotto");
		listhead.appendChild(listheader);
		listheader = new Listheader("Qtà da Consumare");
		listhead.appendChild(listheader);
		
		box.appendChild(listhead);
		
		return box;
	}
	
	public void addProductConsume(int M_Product_ID) {
		if(lst_consumables!=null) {
			onlyProductSearch.setValue(null);
			onlyProductSearch.getComponent().setText(null);;
			ListItem itemProd = null;
			MInventoryLine invLine = null;
			BigDecimal qtyInternalUse = BigDecimal.ONE;
			int index = lst_consumables.getItemCount();
			if(index>0) {
				itemProd = (ListItem) lst_consumables.getItems().stream()
					.filter(it -> ((MInventoryLine)it.getValue()).getM_Product_ID()==M_Product_ID)
					.findAny().orElse(null);
			}
			if(itemProd!=null) {
				invLine = itemProd.getValue();
				
				BigDecimal currentQty = invLine.getQtyInternalUse();
				BigDecimal totalQty = currentQty.add(qtyInternalUse);
				//	Set or Add Qty
				invLine.setQtyInternalUse(totalQty);
				invLine.saveEx();
				int idxComp = Integer.parseInt(itemProd.getId().substring(3));
				((NumberBox)itemProd.getFellow("2qtyConsum_"+idxComp)).setValue(totalQty);
			}
			else {
				int max = index+1;
				MProduct m_product = MProduct.get(ctx, M_Product_ID);
				int prodLine = posPanel.getDocumentPanel().getProdLineEndProduct();
				if(prodLine<=0)
					return;
				for (int i=index; i < max; i++) {
					invLine = new MInventoryLine(Env.getCtx(), 0, null);
					invLine.setM_Inventory_ID(currentInventory.getM_Inventory_ID());
					invLine.setM_Product_ID(m_product.getM_Product_ID());
					invLine.setQtyInternalUse(qtyInternalUse);
					invLine.setC_Charge_ID(posPanel.getM_POS().getC_Charge_ID());
					
					int locatorID = m_product.getM_Locator_ID();
					if(locatorID<=0){
					/*
						MOrgInfo orgInfo = MOrgInfo.get(Env.getCtx(), Env.getAD_Org_ID(Env.getCtx()), null);
						if(orgInfo.getM_Warehouse()!=null && orgInfo.getM_Warehouse().getM_ReserveLocator_ID()>0)
							invLine.setM_Locator_ID(orgInfo.getM_Warehouse().getM_ReserveLocator_ID());
					*/
						if(posPanel.getM_POS().getM_Warehouse()!=null && posPanel.getM_POS().getM_Warehouse().getM_ReserveLocator_ID()>0)
							locatorID = posPanel.getM_POS().getM_Warehouse().getM_ReserveLocator_ID();
					}
					invLine.setM_Locator_ID(locatorID);
					invLine.set_ValueOfColumn("M_ProductionLine_ID", prodLine);
					//	Save Line
					invLine.saveEx(); 
					
					ListItem item = new ListItem();
					item.setId("it_"+i);
					ListCell cellColumn = null;
					WSearchEditor product = createField(posPanel.getWindowNo(), MProduct.Table_Name, MProduct.COLUMNNAME_M_Product_ID, null);;
					product.getComponent().setId("1prodConsum_"+i);
					ZKUpdateUtil.setHeight(product.getComponent().getCombobox(),"35px");
					ZKUpdateUtil.setHeight(product.getComponent().getButton(),"35px");
					cellColumn = new ListCell();
					cellColumn.setId("1prodConsum1_"+i);
					cellColumn.appendChild(product.getComponent());
					product.getComponent().getCombobox().setStyle("Font-size:medium; font-weight:bold");
					product.getComponent().setEnabled(false);
					product.setValue(M_Product_ID);
					item.appendChild(cellColumn);
					NumberBox fieldQtyConsum = new NumberBox(true);
					fieldQtyConsum.setId("2qtyConsum_"+i);
					ZKUpdateUtil.setHeight(fieldQtyConsum.getDecimalbox(),"35px");
					ZKUpdateUtil.setHeight(fieldQtyConsum.getButton(),"35px");
					cellColumn = new ListCell();
					cellColumn.setId("2qtyConsum_2"+i);
					cellColumn.appendChild(fieldQtyConsum);
					fieldQtyConsum.getDecimalbox().setStyle("display: inline;Font-size:medium; font-weight:bold;");
					fieldQtyConsum.setValue(invLine.getQtyInternalUse());
					fieldQtyConsum.getDecimalbox().addEventListener(Events.ON_CHANGING, new EventListener<Event>() {
						@Override
						public void onEvent(Event e) throws Exception {
							InputEvent inpEve = (InputEvent)e;
							String t_value = inpEve.getValue();
							if(!t_value.equals("")) {
								nf = (DecimalFormat)NumberFormat.getInstance(AEnv.getLocale(Env.getCtx()));
								nf.setParseBigDecimal(true);
								BigDecimal qty = (BigDecimal)nf.parse(t_value, new ParsePosition(0));
								int idxComp = Integer.parseInt(((Decimalbox)e.getTarget()).getParent().getId().substring(11));
								MInventoryLine line = ((ListItem)((Decimalbox)e.getTarget()).getFellow("it_"+idxComp)).getValue();
								line.setQtyInternalUse(qty);
								line.saveEx();
							}
						}
					});
					item.appendChild(cellColumn);
					item.setValue(invLine);
					lst_consumables.appendChild(item);
				}				
			}
		}
	}
	
	public void addProductionLine (int M_Production_ID) {
		if(lst_productionComponents!=null) {
			String sql = "SELECT m_product_id, plannedqty, qtyused, qtydelivered "
					+ "FROM M_ProductionLine "
					+ "WHERE AD_Client_ID=? AND M_Production_id=? and (IsEndProduct is null or IsEndProduct='N')";
			List<List<Object>> listProdLine = DB.getSQLArrayObjectsEx(null, sql, Env.getAD_Client_ID(ctx), M_Production_ID);
			if(listProdLine!=null && listProdLine.size()>0) {
				int mproductID = 0;
				int idx = 1;
				for (List<Object> row : listProdLine) {
					mproductID = ((BigDecimal) row.get(0)).intValue();
					
					ListItem item = new ListItem();
					item.setId("it_"+idx);
					ListCell cellColumn = null;
					WSearchEditor product = createField(posPanel.getWindowNo(), MProduct.Table_Name, MProduct.COLUMNNAME_M_Product_ID, null);;
					product.getComponent().setId("1prodLine_"+idx);
					ZKUpdateUtil.setHeight(product.getComponent().getCombobox(),"35px");
					ZKUpdateUtil.setHeight(product.getComponent().getButton(),"35px");
					cellColumn = new ListCell();
					cellColumn.setId("1prodLine1_"+idx);
					cellColumn.appendChild(product.getComponent());
					product.getComponent().getCombobox().setStyle("Font-size:medium; font-weight:bold");
					product.getComponent().setEnabled(false);
					product.setValue(mproductID);
					item.appendChild(cellColumn);
					NumberBox fieldQty = new NumberBox(true);
					fieldQty.setId("2qty_"+idx);
					ZKUpdateUtil.setHeight(fieldQty.getDecimalbox(),"35px");
					ZKUpdateUtil.setHeight(fieldQty.getButton(),"35px");
					cellColumn = new ListCell();
					cellColumn.setId("2qty_#2"+idx);
					cellColumn.appendChild(fieldQty);
					fieldQty.getDecimalbox().setStyle("display: inline;Font-size:medium; font-weight:bold;");
					fieldQty.getDecimalbox().setDisabled(true);
					fieldQty.getButton().setVisible(false);
					fieldQty.setValue(((BigDecimal) row.get(1)));
					item.appendChild(cellColumn);
					NumberBox fieldQtyUsed = new NumberBox(true);
					fieldQtyUsed.setId("2qtyUsed_"+idx);
					ZKUpdateUtil.setHeight(fieldQtyUsed.getDecimalbox(),"35px");
					ZKUpdateUtil.setHeight(fieldQtyUsed.getButton(),"35px");
					cellColumn = new ListCell();
					cellColumn.setId("2qtyUsed_#2"+idx);
					cellColumn.appendChild(fieldQtyUsed);
					fieldQtyUsed.getDecimalbox().setStyle("display: inline;Font-size:medium; font-weight:bold;");
					fieldQtyUsed.getDecimalbox().setDisabled(true);
					fieldQtyUsed.getButton().setVisible(false);
					fieldQtyUsed.setValue(((BigDecimal) row.get(2)));
					item.appendChild(cellColumn);
					NumberBox fieldQtyDelivered = new NumberBox(true);
					fieldQtyDelivered.setId("2qtyDeliver_"+idx);
					ZKUpdateUtil.setHeight(fieldQtyDelivered.getDecimalbox(),"35px");
					ZKUpdateUtil.setHeight(fieldQtyDelivered.getButton(),"35px");
					cellColumn = new ListCell();
					cellColumn.setId("2qtyDeliver_#2"+idx);
					cellColumn.appendChild(fieldQtyDelivered);
					fieldQtyDelivered.getDecimalbox().setStyle("display: inline;Font-size:medium; font-weight:bold;");
					fieldQtyDelivered.getDecimalbox().setDisabled(true);
					fieldQtyDelivered.getButton().setVisible(false);
					fieldQtyDelivered.setValue(((BigDecimal) row.get(3)));
					item.appendChild(cellColumn);
					
					item.setValue(""+mproductID+"_"+fieldQty.getValue()+"_"+fieldQtyUsed.getValue()+"_"+fieldQtyDelivered.getValue()+"#"+idx);
					lst_productionComponents.appendChild(item);
					idx++;
				}
			}
		}
		
		
	}
	
	/**
	 * Get Panel 
	 * @return Panel
	 */
	public Panel getPanel(){
		return parameterPanel;
	}

	/**
	 * setValuesFromProduct
	 * @param productId
	 * @param imageId
     */
	public void setValuesFromProduct(int productId, BigDecimal quantity , int imageId) {
		if(productId <= 0){
			return;
		}
		
		//	Refresh Values
		ProductInfo productInfo = new ProductInfo(productId, quantity ,  imageId , 0 , 0);
		
		posPanel.updateProductPlaceholder(productInfo.name);
		
	}

	/**
	 * Refresh Product from Key
	 * @param key
	 * @return void
	 */
	public void refreshProduct(MPOSKey key ,BigDecimal quantity) {
		if(key == null) {
			return;
		}
		setValuesFromProduct(key.getM_Product_ID() , quantity , key.getAD_Image_ID());
	}

	/**
	 * Refresh from product
	 * @param productId
	 * @return void
	 */
	public void refreshProduct(int productId , BigDecimal quantity) {
		int imageId = posPanel.getProductImageId(productId, posPanel.getC_POSKeyLayout_ID());
		setValuesFromProduct(productId, quantity , imageId );
	}
	
	/**
	 * Reset Values of Info Product
	 * @return void
	 */
	public void resetValues() {
		final String NO_TEXT = "--";
		lst_consumables.getItems().clear();
		onlyProductSearch.setValue(null);
		lbl_Color.setText("COLORE: ");
		
	}

	public String getUOMSymbol()
	{
		return  "";//labelUOMSymbol.getValue();
	}
	
	public void setColor(String adColor) {
		lbl_Color.setText(lbl_Color.getValue()+adColor);
	}
	
	public MInventory_lit getCurrentInventory() {
		return currentInventory;
	}
	
	public void completeInventory() {
		if (currentInventory.getDocStatus().equalsIgnoreCase(MInventory_lit.STATUS_Invalid)) 
			currentInventory.setDocStatus(MInventory_lit.STATUS_InProgress);
		//	Set Document Action
		currentInventory.setDocAction(DocAction.ACTION_Complete);
		if (currentInventory.processIt(DocAction.ACTION_Complete)) {
			currentInventory.saveEx();
			
		}
	}
	
	public void setMaterial(int productID) {
		if(onlyProductSearch!=null) {
			onlyProductSearch.valueChange(new ValueChangeEvent(onlyProductSearch, "Change", null, productID));
		}
	}
	/**
	 * Get/create Order
	 *	@param partnerId Business Partner
	 *	@param docTypeTargetId ID of document type
	 */
	public void createInventory(int docTypeTargetId) {
		int inventoryId = getFreeM_Inventory_ID();
		//	Change Values for new Inventory
		if(inventoryId > 0) {
			currentInventory = new MInventory_lit(Env.getCtx(), inventoryId, null);
			currentInventory.setMovementDate(Env.getContextAsDate(ctx, "#Date"));
			
		} else {
			currentInventory = new MInventory_lit(Env.getCtx(), 0, null);
		}
		currentInventory.setAD_Org_ID(posPanel.getM_POS().getAD_Org_ID());
		currentInventory.getWrapper().setC_POS_ID(posPanel.getC_POS_ID());
		currentInventory.setM_Warehouse_ID(posPanel.getM_POS().getM_Warehouse_ID());
		if (docTypeTargetId != 0) {
			currentInventory.setC_DocType_ID(docTypeTargetId);
		} else {
			currentInventory.setC_DocType_ID(MDocType.getDocType(MDocType.DOCSUBTYPEINV_InternalUseInventory));
		}
		currentInventory.saveEx();
		//	Add if isInventory.getM_Inventory_ID());
	} // PosOrderModel
	
	public void deleteLineInv() {
		if(lst_consumables.getSelectedItem()!=null) {
			int indexDelete = lst_consumables.getSelectedIndex();
			MInventoryLine line = lst_consumables.getSelectedItem().getValue();
			line.deleteEx(true);
			lst_consumables.removeItemAt(indexDelete);
		}
	}
	/**
	 * Find a free order and reuse
	 * @return
	 * @return int
	 */
	private int getFreeM_Inventory_ID() {
		return DB.getSQLValue(null, "SELECT inv.M_Inventory_ID "
				+ "FROM M_Inventory inv "
				+ "WHERE inv.DocStatus = 'DR' "
				+ "AND inv.C_POS_ID = ? "
				+ "AND NOT EXISTS(SELECT 1 "
				+ "					FROM M_InventoryLine invl "
				+ "					WHERE invl.M_Inventory_ID = inv.M_Inventory_ID) "
				+ "ORDER BY inv.Updated", 
				posPanel.getC_POS_ID());
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

}

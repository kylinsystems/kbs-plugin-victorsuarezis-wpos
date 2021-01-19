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

package org.adempiere.pos;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.pos.search.WQueryBPartner;
import org.adempiere.pos.search.WQueryDocType;
import org.adempiere.pos.search.WQueryOrderHistory;
import org.adempiere.pos.service.CPOS;
import org.adempiere.pos.service.POSLookupProductInterface;
import org.adempiere.pos.service.POSPanelInterface;
import org.adempiere.pos.service.POSQueryInterface;
import org.adempiere.pos.service.POSQueryListener;
import org.adempiere.util.Callback;
import org.adempiere.webui.AdempiereWebUI;
import org.adempiere.webui.adwindow.ProcessButtonPopup;
import org.adempiere.webui.adwindow.ToolbarProcessButton;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.NumberBox;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.editor.WSearchEditor;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.event.ValueChangeListener;
import org.adempiere.webui.factory.InfoManager;
import org.adempiere.webui.info.InfoWindow;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.Lookup;
import org.compiere.model.MColumn;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MPOSKey;
import org.compiere.model.MProduct;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.zkforge.keylistener.Keylistener;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zul.Timer;

/**
 * @author Mario Calderon, mario.calderon@westfalia-it.com, Systemhaus Westfalia, http://www.westfalia-it.com
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * @author Raul Muñoz, rmunoz@erpcya.com, ERPCYA http://www.erpcya.com
 * @author victor.perez@e-evolution.com , http://www.e-evolution.com
 */
public class WPOSActionPanel extends WPOSSubPanel
		implements POSKeyListener, POSPanelInterface, POSQueryListener ,  POSLookupProductInterface{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2131406504920855582L;
	
	/**
	 * 	Constructor
	 *	@param posPanel POS Panel
	 */
	public WPOSActionPanel (WPOS posPanel) {
		super (posPanel);
	}	//	WPosSubCustomer


	/**	Buttons Command		*/
	private Button 			buttonNew;
	private Button 			buttonPrint;
	private Button 			buttonDocType;
	private Button 			buttonProduct;
	private Button 			buttonBPartner;
	private Button 			buttonProcess;
	private Button 			buttonHistory;
	private Button 			buttonBack;
	private Button 			buttonNext;
	private Button 			buttonCollect;
	private Button 			buttonCancel;
	private Button 			buttonLogout;

	/**	Is Keyboard			*/
	private boolean			isKeyboard;
	/**	For Show Product	*/
	private	Label 	fieldProductName;
	private WSearchEditor onlyProduct = null; 
	
	private NumberBox 	    fieldQuantity;
	private NumberBox 	    fieldPrice;
	private NumberBox 	    fieldDiscountPercentage;
	
	/** Find Product Timer **/
	private Timer findProductTimer;
	private WPOSLookupProduct lookupProduct;
	/**	Process Action 						*/
	private WPOSActionMenu actionProcessMenu;

	/**	Logger			*/
	private static CLogger logger = CLogger.getCLogger(WPOSActionPanel.class);

	private final String ACTION_NEW         = "New";
	private final String ACTION_PRINT       = "Print";
	private final String ACTION_DOCTYPE     = "Assignment";
	private final String ACTION_PRODUCT     = "InfoProduct";
	private final String ACTION_BPARTNER    = "BPartner";
	private final String ACTION_PROCESS     = "Process";
	private final String ACTION_HISTORY     = "History";
	private final String ACTION_BACK       	= "Parent";
	private final String ACTION_NEXT  		= "Detail";
	private final String ACTION_PAYMENT     = "Payment";
	private final String ACTION_CANCEL      = "Cancel";
	private final String ACTION_LOGOUT      = "Logout";
	
	private String errorMsg = null;
	
	private boolean evtChangeStop = false;
	
	@Override
	public void init() {

		Grid LayoutButton = GridFactory.newGridLayout();
		Row row = null;	
		Rows rows = null;
			
		isKeyboard = false;
		LayoutButton.setStyle("border: none; width:400px; height:95%;");
		
		appendChild(LayoutButton);
		rows = LayoutButton.newRows();
		LayoutButton.setStyle("border:none");
//		row = rows.newRow();
//		row.setHeight("100px");
		Label title = new Label(posPanel.getM_POS().getDescription());
		title.setStyle("-webkit-box-sizing: content-box;\n" + 
				"  -moz-box-sizing: content-box;\n" + 
				"  box-sizing: content-box;\n" + 
				"  width: 98%;\n" + 
				"  height: 50px;\n" + 
				"  border: none;\n" + 
				"  font: normal 30px/1 \"Times New Roman\", Times, serif;\n" + 
				"  color: rgba(255,255,255,1);\n" + 
				"  text-align: center;\n" + 
				"  -o-text-overflow: ellipsis;\n" + 
				"  text-overflow: ellipsis;\n" + 
				"  background: -webkit-radial-gradient(closest-side, rgba(255,255,255,1) 0, rgba(0,150,255,1) 100%);\n" + 
				"  background: -moz-radial-gradient(closest-side, rgba(255,255,255,1) 0, rgba(0,150,255,1) 100%);\n" + 
				"  background: radial-gradient(closest-side, rgba(255,255,255,1) 0, rgba(0,150,255,1) 100%);\n" + 
				"  background-position: 50% 50%;\n" + 
				"  -webkit-background-origin: padding-box;\n" + 
				"  background-origin: padding-box;\n" + 
				"  -webkit-background-clip: border-box;\n" + 
				"  background-clip: border-box;\n" + 
				"  -webkit-background-size: auto auto;\n" + 
				"  background-size: auto auto;");
//		row.appendCellChild(title, 11);
		
		row = rows.newRow();
		row.setHeight("55px");
	
		// NEW
		buttonNew = createButtonAction(ACTION_NEW, "F2");
		buttonNew.addActionListener(this);
		row.appendChild(buttonNew);

		// PRINT
		buttonPrint = createButtonAction(ACTION_PRINT, "F12");
		buttonPrint.addActionListener(this);
		row.appendChild(buttonPrint);

		// DocType 
		buttonDocType = createButtonAction(ACTION_DOCTYPE, "F10");
		buttonDocType.addActionListener(this);
		buttonDocType.setTooltiptext("F10-"+Msg.translate(ctx, "C_DocType_ID"));
		
		row.appendChild(buttonDocType);
		// PRODUCT
		buttonProduct = createButtonAction(ACTION_PRODUCT, "Alt+I");
		buttonProduct.addActionListener(this);
		buttonProduct.setTooltiptext("Alt+I-"+Msg.translate(ctx, "InfoProduct"));
		buttonProduct.setVisible(false);
		row.appendChild(buttonProduct);
		// BPartner Search
		buttonBPartner = createButtonAction(ACTION_BPARTNER, "Alt+B");
		buttonBPartner.addActionListener(this);
		buttonBPartner.setTooltiptext("Alt+B-"+Msg.translate(ctx, "IsCustomer"));
		row.appendChild(buttonBPartner);
		// PROCESS
		buttonProcess = createButtonAction(ACTION_PROCESS, "Alt+P");
		buttonProcess.setVisible(false);
		buttonProcess.addActionListener(this);
		buttonProcess.setTooltiptext("ALT+P-"+Msg.translate(ctx, "Process"));
		
		row.appendChild(buttonProcess);
		
		// HISTORY
		buttonHistory = createButtonAction(ACTION_HISTORY, "F9");
		buttonHistory.addActionListener(this);
		row.appendChild(buttonHistory);
		
		buttonBack = createButtonAction(ACTION_BACK, "Alt+Left");
		buttonBack.setTooltiptext("Alt+Left-"+Msg.translate(ctx, "Previous"));
		row.appendChild (buttonBack);
		buttonNext = createButtonAction(ACTION_NEXT, "Alt+Right");
		buttonNext.setTooltiptext("Alt+Right"+Msg.translate(ctx, "Next"));
		row.appendChild (buttonNext);
		
		// PAYMENT
		buttonCollect = createButtonAction(ACTION_PAYMENT, "F4");
		buttonCollect.addActionListener(this);
		row.appendChild(buttonCollect);
		buttonCollect.setEnabled(false);

		// Cancel
		buttonCancel = createButtonAction (ACTION_CANCEL, "F3");
		buttonCancel.addActionListener(this);
		buttonCancel.setTooltiptext("F3-"+Msg.translate(ctx, "POS.IsCancel"));
		row.appendChild (buttonCancel);
		buttonCancel.setEnabled(false);
		
		// LOGOUT
		buttonLogout = createButtonAction (ACTION_LOGOUT, "Alt+L");
		buttonLogout.addActionListener(this);
		buttonLogout.setTooltiptext("Alt+L-"+Msg.translate(ctx, "End"));
		row.appendChild (buttonLogout);
		row.setHeight("55px");

		fieldProductName = new Label(Msg.translate(Env.getCtx(), "M_Product_ID"));
		ZKUpdateUtil.setWidth(fieldProductName,"100%");
		ZKUpdateUtil.setHeight(fieldProductName,"35px");
		
		onlyProduct = createProduct(posPanel.getWindowNo());
		ZKUpdateUtil.setWidth(onlyProduct.getComponent(), "98%");
		ZKUpdateUtil.setHeight(onlyProduct.getComponent().getTextbox(),"35px");
		ZKUpdateUtil.setHeight(onlyProduct.getComponent().getButton(),"35px");
		
		Keylistener keyListener = new Keylistener();
		
    	keyListener.setCtrlKeys("#f2#f3#f4#f9#f10@b@#left@#right^l@i@p");
    	keyListener.addEventListener(Events.ON_CTRL_KEY, posPanel);
    	keyListener.addEventListener(Events.ON_CTRL_KEY, this);
    	keyListener.setAutoBlur(false);
    	
		fieldProductName.setStyle("Font-size:large; font-weight:bold");
		//fieldProductName.setValue(Msg.translate(Env.getCtx(), "M_Product_ID"));
		
		onlyProduct.getComponent().getTextbox().setStyle("Font-size:medium; font-weight:bold");
//		onlyProduct.setValue(Msg.translate(Env.getCtx(), "M_Product_ID"));
		onlyProduct.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent evt) {
				if(evt.getNewValue()!=null){
					//Entra per due volte nell'evento; bypassare la 2a volta....
					if(evtChangeStop){
						evtChangeStop = false;
						return;
					}
					else 
						evtChangeStop = true;// per prossimo richiamo dell'evento
					//
				
					Integer prod_ID = (Integer) evt.getNewValue();
//					MProduct product = new MProduct(ctx, prod_ID, null);
					
					MPOSKey key = null;
					
					try {
						ResultSet rs =DB.prepareStatement(("SELECT * FROM LIT_C_POSKey_v WHERE M_Product_ID="+prod_ID), null).executeQuery();
						if(rs!=null){
							while(rs.next()) {
								key = new MPOSKey(ctx, rs, null);
								keyReturned(key);
								
								((WSearchEditor)evt.getSource()).getComponent().getTextbox().focus();
							}
						}
						((WSearchEditor)evt.getSource()).getComponent().getTextbox().select();
						
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String where = "M_Product_ID=? AND C_POSKeyLayout_ID=?";
					MPOSKey key2 = new Query(ctx, MPOSKey.Table_Name, where, null)
							.setOnlyActiveRecords(true)
							.setClient_ID()
							.setParameters(prod_ID, posPanel.getC_POSKeyLayout_ID())
							.first();
					if(key2 != null)
						keyReturned(key2);
					else {
						addProduct(prod_ID);
					}
//					//((WSearchEditor)evt.getSource()).getComponent().focus();
					((WSearchEditor)evt.getSource()).getComponent().getTextbox().focus();
				}
			}
		});
		
		Label qtyLabel = new Label(Msg.translate(Env.getCtx(), "QtyOrdered"));
		qtyLabel.setStyle("Font-size:medium");
//		row.appendChild(qtyLabel);

		//		fieldQuantity = new POSNumberBox(false);
		fieldQuantity = new NumberBox(false);

//		row.appendChild(fieldQuantity);
		fieldQuantity.addEventListener(Events.ON_OK, posPanel.getQuantityPanel());
		fieldQuantity.addEventListener(Events.ON_CHANGE, posPanel.getQuantityPanel());
		//		fieldQuantity.setStyle("display: inline;width:65px;height:30px;Font-size:medium;");
		ZKUpdateUtil.setWidth(fieldQuantity, "85px");
		ZKUpdateUtil.setHeight(fieldQuantity.getDecimalbox(),"30px");
		ZKUpdateUtil.setHeight(fieldQuantity.getButton(),"30px");
		fieldQuantity.getDecimalbox().setStyle("display: inline;Font-size:medium;");

		Label priceLabel = new Label(Msg.translate(Env.getCtx(), "PriceActual"));
		priceLabel.setStyle("Font-size:medium");
//		row.appendChild(priceLabel);

		//		fieldPrice = new POSNumberBox(false);
		fieldPrice = new NumberBox(false);
		DecimalFormat format = DisplayType.getNumberFormat(DisplayType.Amount, AEnv.getLanguage(Env.getCtx()));
		fieldPrice.getDecimalbox().setFormat(format.toPattern());

		fieldPrice.setTooltiptext(Msg.translate(Env.getCtx(), "PriceActual"));
//		row.appendChild(fieldPrice);
		if (!posPanel.isModifyPrice())
			fieldPrice.setEnabled(false);
		else {
			fieldPrice.addEventListener(Events.ON_OK, posPanel.getQuantityPanel());
			fieldPrice.addEventListener(Events.ON_CHANGE, posPanel.getQuantityPanel());
		}
		//		fieldPrice.setStyle("display: inline;width:70px;height:30px;Font-size:medium;");
		ZKUpdateUtil.setWidth(fieldPrice, "85px");
		ZKUpdateUtil.setHeight(fieldPrice.getDecimalbox(),"30px");
		ZKUpdateUtil.setHeight(fieldPrice.getButton(),"30px");
		fieldPrice.getDecimalbox().setStyle("display: inline;Font-size:medium;");

		Label priceDiscount = new Label(Msg.translate(Env.getCtx(), "Discount"));
		priceDiscount.setVisible(false);
		priceDiscount.setStyle("Font-size:medium");
//		row.appendChild(priceDiscount);

		//		fieldDiscountPercentage = new POSNumberBox(false);
		fieldDiscountPercentage = new NumberBox(false);
//		row.appendChild(fieldDiscountPercentage);
		fieldDiscountPercentage.setTooltiptext(Msg.translate(Env.getCtx(), "Discount"));
		fieldDiscountPercentage.setVisible(false);
		if (!posPanel.isModifyPrice())
			fieldDiscountPercentage.setEnabled(false);
		else{
			fieldDiscountPercentage.addEventListener(Events.ON_OK, posPanel.getQuantityPanel());
			fieldDiscountPercentage.addEventListener(Events.ON_CHANGE, posPanel.getQuantityPanel());
		}
		//		fieldDiscountPercentage.setStyle("display: inline;width:70px;height:30px;Font-size:medium;");
		ZKUpdateUtil.setWidth(fieldDiscountPercentage, "85px");
		ZKUpdateUtil.setHeight(fieldDiscountPercentage.getDecimalbox(),"30px");
		ZKUpdateUtil.setHeight(fieldDiscountPercentage.getButton(),"30px");
		fieldDiscountPercentage.getDecimalbox().setStyle("display: inline;Font-size:medium;");

		Keylistener keyListener2 = new Keylistener();
		fieldPrice.appendChild(keyListener);
		keyListener.setCtrlKeys("@#up@#down^#f3^1^0");
		keyListener.addEventListener(Events.ON_CTRL_KEY, posPanel);
		keyListener.addEventListener(Events.ON_CTRL_KEY, this);
		keyListener.setAutoBlur(false);
		
		
		
		row = rows.newRow();
		row.setHeight("35px");
		if (posPanel.isEnableProductLookup() && !posPanel.isVirtualKeyboard()) {
//			lookupProduct = new WPOSLookupProduct(this, fieldProductName, new Long("1"));
			lookupProduct = new WPOSLookupProduct(this, null, 1);
			lookupProduct.setPriceListId(posPanel.getM_PriceList_ID());
			lookupProduct.setPartnerId(posPanel.getC_BPartner_ID());
			lookupProduct.setWarehouseId(posPanel.getM_Warehouse_ID());
			findProductTimer = new Timer(500); // , lookupProduct);
			lookupProduct.setWidth("100%");
			lookupProduct.setStyle(WPOS.FONTSTYLE+WPOS.FONTSIZELARGE);
//			fieldProductName.appendChild(keyListener);
//			fieldProductName.setVisible(false);
//			fieldProductName.setWidth("0%");
			findProductTimer.start();
			lookupProduct.addEventListener(Events.ON_CHANGE, this);
	        row.appendChild(lookupProduct);
//			row.appendChild(fieldProductName);
	        row.appendChild(onlyProduct.getComponent());
	        onlyProduct.setVisible(false);
			onlyProduct.getComponent().setWidth("0%");
		} else {
			row.appendCellChild(onlyProduct.getComponent(),3);
//			onlyProduct.getComponent().setWidth("30%");
//			fieldProductName.setWidth("40%");
			row.appendCellChild(fieldProductName,2);
			row.appendCellChild(qtyLabel);
			row.appendCellChild(fieldQuantity);
			row.appendCellChild(priceLabel);
			row.appendCellChild(fieldPrice);
			row.appendCellChild(priceDiscount);
			row.appendCellChild(fieldDiscountPercentage);

		}
		enableButton();
		actionProcessMenu = new WPOSActionMenu(posPanel);
//		
//		//	List Orders
		posPanel.listOrder();
		getMainFocus();
	}

	public WSearchEditor getOnlyProduct() {
		return this.onlyProduct;
	}

	/** 
	 * Open window Doctype 
	 */
	private void openDocType() { 
		WQueryDocType qt = new WQueryDocType(posPanel);
		qt.setVisible(true);
		qt.setAttribute(Window.MODE_KEY, Window.MODE_HIGHLIGHTED);
		AEnv.showWindow(qt);
	}
	
	private void openHistory() { 
		WQueryOrderHistory qt = new WQueryOrderHistory(posPanel);
		qt.setVisible(true);
		qt.setAttribute(Window.MODE_KEY, Window.MODE_HIGHLIGHTED);
		AEnv.showWindow(qt);
		posPanel.reloadIndex(qt.getRecord_ID());
	}
	
	private void openBPartner() {
		WQueryBPartner qt = new WQueryBPartner(posPanel);
		if(!posPanel.isBPartnerStandard())
			qt.loadData();
		qt.setAttribute(Window.MODE_KEY, Window.MODE_HIGHLIGHTED);
		AEnv.showWindow(qt);
		if (qt.getRecord_ID() > 0) {
			if(!posPanel.hasOrder()) {
				posPanel.newOrder(qt.getRecord_ID());
				posPanel.refreshPanel();
			} else {
				posPanel.configureBPartner(qt.getRecord_ID());
			}
			logger.fine("C_BPartner_ID=" + qt.getRecord_ID());
		}
	}

	@Override
	public void onEvent(Event e) throws Exception {
		try {
            if(e.getName().equals(Events.ON_CHANGE)){
                if(lookupProduct.getSelectedRecord() >= 0) {
                  lookupProduct.setText(String.valueOf(lookupProduct.getSelectedRecord()));
                    lookupProduct.captureProduct();
                }
            }

            if (Events.ON_CTRL_KEY.equals(e.getName())) {
                KeyEvent keyEvent = (KeyEvent) e;
                //F2 == 113
                if (keyEvent.getKeyCode() == 113 ) {
                    posPanel.newOrder();
                }
                //F3 == 114
                else if (keyEvent.getKeyCode() == 114 ) {
                    if (posPanel.isUserPinValid())
                        deleteOrder();
                }
                //F4 == 115
                else if (keyEvent.getKeyCode() == 115 ) {
                    payOrder();
                    return;
                }
                //F9 == 120
                else if (keyEvent.getKeyCode() == 120 ) {
                    openHistory();
                }
                //F10 == 121
                else if (keyEvent.getKeyCode() == 121 ) {
                    openDocType();
                }
                //Alt+b == 66
                else if (keyEvent.getKeyCode() == 66 ) {
                    openBPartner();
                }
                //Alt+left == 37
                else if (keyEvent.getKeyCode() == 37 ) {
                    previousRecord();
                }
                //Alt+right == 39
                else if (keyEvent.getKeyCode() == 39 ) {
                    nextRecord();
                }
                //CTL+L == 76
                else if (keyEvent.getKeyCode() == 76 ) {
                    dispose();
                    return;
                }
                //Alt+I == 73
                else if (keyEvent.getKeyCode() == 73 ) {
                    showWindowProduct(null);
                    return;
                }
                //Alt+P == 80
                else if (keyEvent.getKeyCode() == 80 ) {
                    actionProcessMenu.getPopUp().setPage(buttonProcess.getPage());
                    actionProcessMenu.getPopUp().open(150, 150);
                    return;
                }
            }
//            if(e.getTarget().equals(fieldProductName.getComponent(WPOSTextField.SECONDARY))
//                        && e.getName().equals(Events.ON_FOCUS) && !isKeyboard){
//                    if(posPanel.isDrafted() || posPanel.isInProgress())  {
//                        isKeyboard = true;
//                        if(!fieldProductName.showKeyboard()){
//                            findProduct(true);
//                        }
//                        fieldProductName.setFocus(true);
//                    }
//                }
//                if(e.getTarget().equals(fieldProductName.getComponent(WPOSTextField.PRIMARY)) && e.getName().equals(Events.ON_FOCUS)){
//                    isKeyboard = false;
//                }

            if (e.getTarget().equals(buttonNew)){
                posPanel.newOrder();
            }
			if (e.getTarget().equals(buttonPrint)){
				posPanel.printTicket();
			}
            else if (e.getTarget().equals(buttonDocType)){
                if(posPanel.isUserPinValid()) {
                    openDocType();
                }
            }
			else if (e.getTarget().equals(buttonProduct)) {
				showWindowProduct("");
			}
			else if (e.getTarget().equals(buttonBPartner)) {
				openBPartner();
			}
            else if(e.getTarget().equals(buttonCollect)){
            	if(posPanel.isReturnMaterial()) {
					completeReturn();
				} else {
					payOrder();
				}
                return;
            }
            else if(e.getTarget().equals(buttonProcess)){
                if(posPanel.isUserPinValid()) {
                    actionProcessMenu.getPopUp().setPage(this.getPage());
                    actionProcessMenu.getPopUp().open(buttonProcess);
                	
        			ProcessButtonPopup popup = new ProcessButtonPopup();
        			popup.setWidgetAttribute(AdempiereWebUI.WIDGET_INSTANCE_NAME, "processButtonPopup");

        			List<org.zkoss.zul.Button> buttonList = new ArrayList<org.zkoss.zul.Button>();
        			for(ToolbarProcessButton processButton : actionProcessMenu.getToolbarProcessButtons()) {
        				if (processButton.getButton().isVisible()) {
        					buttonList.add(processButton.getButton());
        				}
        			}

        			popup.render(buttonList);

        			popup.setWidgetAttribute(AdempiereWebUI.WIDGET_INSTANCE_NAME, "processButtonPopup");
        			if (popup.getChildren().size() > 0) {
        				popup.setPage(this.getPage());
        				popup.open(buttonProcess, "after_start");
        			}

        			return;

        		//

        		
                }
                return;
            }
            else if (e.getTarget().equals(buttonBack)){
                previousRecord();
            }
            else if (e.getTarget().equals(buttonNext)){
                nextRecord();
            }
            else if(e.getTarget().equals(buttonLogout)){
                dispose();
                return;
            }
            // Cancel
            else if (e.getTarget().equals(buttonCancel)){
                if(posPanel.isUserPinValid()) {
                    deleteOrder();
                }
            }
            //	History
            if (e.getTarget().equals(buttonHistory)) {
                openHistory();
            }
            posPanel.refreshPanel();
        } catch (Exception exception) {
            FDialog.error(posPanel.getWindowNo(), exception.getLocalizedMessage());
        }
	}
	
	/**
	 * Show Window Product
	 */
	private void showWindowProduct(String query) {
		//	Show Info
		//posPanel.getFrame().getContentPane().invalidate();
//		InfoProductWindow infoProduct = new InfoProductWindow(
//				posPanel.getWindowNo(),
//				MProduct.Table_Name,
//				"M_Product_ID",
//				"",
//				true,
//				query,
//				0,
//				true);
		InfoWindow infoProduct = (InfoWindow)InfoManager.create(0, "M_Product", "M_Product_ID", query, true, "", true);
		
		AEnv.showWindow(infoProduct);
		infoProduct.addEventListener(Events.ON_OK, posPanel.getQuantityPanel());
		infoProduct.addEventListener(Events.ON_CHANGE, posPanel.getQuantityPanel());
		infoProduct.addEventListener(Events.ON_OK, this);
		infoProduct.addEventListener(Events.ON_CHANGE, this);
		
		Object[] result = infoProduct.getSelectedKeys();
		if(result == null)
			return;
		if (infoProduct.isCancelled())
			return;
		for (Object item : result)
		{
			int productId = (Integer) item;
			if (productId > 0) {
				String value = posPanel.getProductValue(productId);
//				fieldProductName.setText(value);
				onlyProduct.getComponent().getTextbox().setText(value);
				try {
					posPanel.setAddQty(true);
					findProduct(true);
				} catch (Exception exception) {
					FDialog.error(0, this, exception.getLocalizedMessage());
				}
//				fieldProductName.setText("");
				onlyProduct.getComponent().getTextbox().setText("");
				//fieldProductName.repaint();
			}
		}
	}

	/**
	 * Get the Main Focus
	 * 
	 * @return void
	 */
	public void getMainFocus() {

		if (posPanel.isEnableProductLookup() && !posPanel.isVirtualKeyboard()) 
			lookupProduct.focus();
//		else
//			fieldProductName.focus();
		if(onlyProduct != null)
			onlyProduct.getComponent().getTextbox().focus();
		
	}

	/**************************************************************************
	 * 	Find/Set Product & Price
	 */
	public void findProduct(boolean editQty) throws Exception {
		if (getProductTimer() != null)
			getProductTimer().stop();
		String query;
		if(posPanel.isEnableProductLookup() && !posPanel.isVirtualKeyboard())
		  query = String.valueOf(lookupProduct.getText());
		else
//		  query = fieldProductName.getText();
			query = onlyProduct.getComponent().getTextbox().getText();
//		fieldProductName.setText("");
		onlyProduct.getComponent().getTextbox().setText("");
		if (query == null || query.length() == 0)
			return;
		query = query.toUpperCase();
		//	Test Number
		try {
			Integer.getInteger(query);
		} catch (Exception e) {}
		//	
		List<Vector<Object>> results = CPOS.getQueryProduct(query, posPanel.getM_Warehouse_ID(), 
				posPanel.getM_PriceList_ID() , posPanel.getC_BPartner_ID());
		//	Set Result
		if (results.size() == 1) {
			Optional<Vector<Object>> columns = results.stream().findFirst();
			if (columns.isPresent()) {
				Integer productId = (Integer) columns.get().elementAt(0);
				String productName = (String) columns.get().elementAt(2);
				posPanel.setAddQty(true);
				posPanel.addOrUpdateLine(productId, editQty? Env.ZERO: Env.ONE, posPanel.getOrder().isSOTrx());
//				fieldProductName.setText(productName);
				onlyProduct.getComponent().getTextbox().setText(productName);
			}
		} else {	//	more than one
            showWindowProduct(query);
		}
		//	Change focus
		posPanel.refreshPanel();
		posPanel.changeViewPanel();
		//	
		if(editQty)
			quantityRequestFocus();
	}	//	findProduct


	public void quantityRequestFocus() {
		posPanel.quantityRequestFocus();
	}

	/**
	 * Previous Record Order
	 */
	public void previousRecord() {
		posPanel.previousRecord();
		posPanel.refreshPanel();
	}

	/**
	 * Next Record Order
	 */
	public void nextRecord() {
		posPanel.nextRecord();
		posPanel.refreshPanel();
	}
	
	/**
	 * Complete Return Material
	 */
	private void completeReturn() {
		String errorMsg = null;
		String askMsg = "@new.customer.return.order@ @DisplayDocumentInfo@ : " + posPanel.getDocumentNo()
                + " @To@ @C_BPartner_ID@ : " + posPanel.getBPName();
		//	
		if (posPanel.isCompleted()) {
			return;
		}
		//	Show Ask
		if (FDialog.ask(posPanel.getWindowNo(), this, "StartProcess?", Msg.parseTranslation(posPanel.getCtx(), askMsg))) {
			try {
				posPanel.completeReturn();
			} catch(Exception e) {
				errorMsg = e.getLocalizedMessage();
			}
		}
		//	show if exists error
		if(errorMsg != null)
			FDialog.error(posPanel.getWindowNo(), Msg.parseTranslation(ctx, errorMsg));
		//	Update
		posPanel.refreshPanel();
	}

	/**
	 * Execute order payment
	 * If order is not processed, process it first.
	 * If it is successful, proceed to pay and print ticket
	 */
	private void payOrder() {
		//Check if order is completed, if so, print and open drawer, create an empty order and set cashGiven to zero
		if(!posPanel.hasOrder()) {
			FDialog.warn(posPanel.getWindowNo(), Msg.getMsg(ctx, "POS.MustCreateOrder"));
		} 
		else if(posPanel.hasOrder() && (posPanel.isPurchaseOrder() || posPanel.isStandardOrder()))
		{
			if(posPanel.isPurchaseOrder())
				posPanel.getOrder().setIsSOTrx(false);
				
			posPanel.processOrder(posPanel.getOrder().get_TrxName(), false, false);
			posPanel.showKeyboard();
			posPanel.setOrder(posPanel.getOrder().get_ID());
			posPanel.refreshPanel();
			posPanel.refreshProductInfo(null);
            posPanel.restoreTable();
			return;
		}
		else {
			posPanel.hideKeyboard();
			posPanel.showCollectPayment();
		}
	}  // payOrder

	/**
	 * Execute deleting an order
	 * If the order is in drafted status -> ask to delete it
	 * If the order is in completed status -> ask to void it it
	 * Otherwise, it must be done outside this class.
	 */
	private void deleteOrder() {
//		String errorMsg = null;
		errorMsg = null;
		String askMsg = "POS.DeleteOrder";
		if (posPanel.isCompleted()) {
			askMsg = "POS.OrderIsAlreadyCompleted";
		}
		//	Show Ask
		FDialog.ask(0, this, Msg.getMsg(ctx, Msg.getMsg(ctx, askMsg)), new Callback<Boolean>() {

			@Override
			public void onCallback(Boolean result) {
				errorMsg = posPanel.cancelOrder();
				posPanel.refreshPanel();
			}
			
		});
		
//		if (FDialog.ask(0, this , Msg.getMsg(ctx, Msg.getMsg(ctx, askMsg)))) {
//			//posPanel.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//			//	Cancel Order
//			errorMsg = posPanel.cancelOrder();
//			//	Set Cursor to default
//			//posPanel.getFrame().setCursor(Cursor.getDefaultCursor());
//		}
		//	show if exists error
		if(errorMsg != null)
			FDialog.error(posPanel.getWindowNo(), Msg.parseTranslation(ctx, errorMsg));
		//	Update
		posPanel.refreshPanel();
	} // deleteOrder

	@Override
	public String validatePayment() {
		return null;
	}

	@Override
	public void changeViewPanel() {
		refreshPanel();
	}

	@Override
	public void refreshPanel() {
		if(posPanel.hasOrder()) {
			if (lookupProduct != null && posPanel.isEnableProductLookup() && !posPanel.isVirtualKeyboard()) {
				lookupProduct.setPriceListId(posPanel.getM_PriceList_ID());
				lookupProduct.setPartnerId(posPanel.getC_BPartner_ID());
				lookupProduct.setWarehouseId(posPanel.getM_Warehouse_ID());
			}

			//	For Next
			buttonNext.setEnabled(!posPanel.isLastRecord() && posPanel.hasRecord());
			//	For Back
			buttonBack.setEnabled(!posPanel.isFirstRecord() && posPanel.hasRecord());
			//	For Collect
			if(posPanel.hasLines()
					&& !posPanel.isPaid()
					&& !posPanel.isVoided() && posPanel.getOrder()!=null && posPanel.getOrder().isSOTrx()) {
				//	For Credit Order
				buttonCollect.setEnabled(true);
			} else {
				buttonCollect.setEnabled(false);
			}
			// For BusinessPartner and Document Type
			if(posPanel.isDrafted() || posPanel.isInProgress()) {
				buttonDocType.setEnabled(true);
				buttonBPartner.setEnabled(true);
			} else {
				buttonDocType.setEnabled(false);
				buttonBPartner.setEnabled(false);
			} 
			//	For Cancel Action
			buttonCancel.setEnabled(!posPanel.isVoided());
			buttonNew.setEnabled(true);
			buttonHistory.setEnabled(true);
			buttonProcess.setEnabled(true);
		} else {
			buttonNew.setEnabled(true);
			buttonHistory.setEnabled(true);
			//	For Next
			buttonNext.setEnabled(!posPanel.isLastRecord() && posPanel.hasRecord());
			//	For Back
			buttonBack.setEnabled(!posPanel.isFirstRecord() && posPanel.hasRecord());
			buttonCollect.setEnabled(false);
			//	For Cancel Action
			buttonCancel.setEnabled(false);
			// For BusinessPartner and Document Type
			buttonDocType.setEnabled(false);
			buttonBPartner.setEnabled(false);
		}
		buttonNew.setEnabled(true);
		buttonHistory.setEnabled(true);
		buttonProcess.setEnabled(true);
		actionProcessMenu.initActionMenu_();
	}

	/**
	 * Enable Bttons
	 * @return void
	 */
	public void enableButton(){
		buttonNew.setEnabled(true);
		buttonCancel.setEnabled(false);
		buttonHistory.setEnabled(true);
		buttonCollect.setEnabled(false);
	}


	@Override
	public void okAction(POSQueryInterface query) {
		try
		{
			if (query.getRecord_ID() <= 0)
				return;
			//	For Ticket
			if(query instanceof WQueryOrderHistory) {
				posPanel.setOrder(query.getRecord_ID());
				posPanel.reloadIndex(query.getRecord_ID());
			} else if(query instanceof WQueryBPartner) {
				if(!posPanel.hasOrder()) {
					posPanel.newOrder(query.getRecord_ID());
					posPanel.getMainFocus();
				} else {
					posPanel.configureBPartner(query.getRecord_ID());
				}
				//
				logger.fine("C_BPartner_ID=" + query.getRecord_ID());
			} else if(query instanceof WQueryDocType) {
				if (query.getRecord_ID() > 0) {
					posPanel.setC_DocType_ID(query.getRecord_ID());
				}
			}
			//	Refresh
			posPanel.refreshPanel();
		}
		catch (AdempiereException exception) {
			FDialog.error(posPanel.getWindowNo(), this, exception.getLocalizedMessage());
		} catch (Exception exception) {
			FDialog.error(posPanel.getWindowNo(), this, exception.getLocalizedMessage());
		}

	}

	@Override
	public void cancelAction(POSQueryInterface query) {
		//	Nothing
	}

	@Override
	public void moveUp() {
	}

	@Override
	public void moveDown() {
	}	

	public void disableButtons() {
		buttonNew.setEnabled(false);
	    buttonHistory.setEnabled(false);
	    buttonNext.setEnabled(false);
	    buttonBack.setEnabled(false);
	    buttonCollect.setEnabled(false);
	    buttonCancel.setEnabled(false);
	    buttonDocType.setEnabled(false);
	    buttonBPartner.setEnabled(false); 
	    buttonProcess.setEnabled(false);
	}
	
	/**
	 * Reset Panel
	 */
	public void resetPanel() {
//		fieldProductName.setValue(Msg.translate(Env.getCtx(), "M_Product_ID"));
	}

	public Timer getProductTimer()
	{
		return  findProductTimer;
	}

	@Override
	public void keyReturned(MPOSKey key) {
		
		if(key == null)
			return;

		// processed order
		if (posPanel.hasOrder()
				&& posPanel.isCompleted()) {
			//	Show Product Info
			posPanel.refreshProductInfo(key);
			return;
		}
		// Add line
		try{
      //  Issue 139
		  posPanel.setAddQty(true);
			posPanel.addOrUpdateLine(key.getM_Product_ID(), key.getQty(), posPanel.getOrder().isSOTrx());
			posPanel.refreshPanel();
			posPanel.changeViewPanel();
//			posPanel.getMainFocus();

		} catch (Exception exception) {
			FDialog.error(posPanel.getWindowNo(), this, exception.getLocalizedMessage());
		}
		//	Show Product Info
		posPanel.refreshProductInfo(key);
		return;
	}
	
	public void addProduct(int M_Product_ID) {
		// processed order
		if (posPanel.hasOrder()
				&& posPanel.isCompleted()) {
			//	Show Product Info
			posPanel.refreshProductInfo(null);
			return;
		}
		// Add line
		try {
      //  Issue 139
			posPanel.setAddQty(true);
			posPanel.addOrUpdateLine(M_Product_ID, BigDecimal.ONE, posPanel.getOrder().isSOTrx());
			posPanel.refreshPanel();
			posPanel.changeViewPanel();
	//			posPanel.getMainFocus();
		} catch (Exception exception) {
			FDialog.error(posPanel.getWindowNo(), this, exception.getLocalizedMessage());
		}
		//	Show Product Info
		posPanel.refreshProductInfo(null);
		return;
	}
	
	public void updateProductPlaceholder(String name)
	{
		if (posPanel.isEnableProductLookup() && !posPanel.isVirtualKeyboard()) 
			lookupProduct.setText(name);
//		else
//			fieldProductName.setText(name);
	}
	
	/**
	 * @param windowNo
	 * @return WSearchEditor
	 */
	private WSearchEditor createProduct(int windowNo) {
		//int AD_Column_ID = 6862;    //  S_Resource_ID
		int AD_Column_ID = MColumn.getColumn_ID(MProduct.Table_Name, MProduct.COLUMNNAME_M_Product_ID);    //  M_Product_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), windowNo,
					0, AD_Column_ID, DisplayType.Search);
			return new WSearchEditor ("M_Product_ID", false, false, false, lookup);
		}
		catch (Exception e)
		{
			FDialog.error(posPanel.getWindowNo(), e.getLocalizedMessage());
		}
		return null;
	}
	
	private WPOSActionPanel getContent(){
		return this;
	}
	
	public void resetActionMenu(){
		actionProcessMenu.initActionMenu_();
	}
	
	public NumberBox getFieldQuantity() {
		return fieldQuantity;
	}

	public NumberBox getFieldPrice() {
		return fieldPrice;
	}

	public NumberBox getFieldDiscountPercentage() {
		return fieldDiscountPercentage;
	}
	
	public void setFieldProductName(String nameProd) {
		fieldProductName.setText(nameProd);
	}
	
}//	WPOSActionPanel
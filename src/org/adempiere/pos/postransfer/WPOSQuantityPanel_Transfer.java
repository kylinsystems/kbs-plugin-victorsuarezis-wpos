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

package org.adempiere.pos.postransfer;

import java.awt.MouseInfo;
import java.awt.Point;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.pos.search.WQueryDocType;
import org.adempiere.pos.search.WQueryUserQry;
import org.adempiere.pos.service.POSPanelInterface;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.NumberBox;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.event.DialogEvents;
import org.adempiere.webui.grid.WQuickEntryPOS;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.MMovement;
import org.compiere.model.MOrder;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.zkforge.keylistener.Keylistener;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;


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
 * 
 */
public class WPOSQuantityPanel_Transfer extends WPOSSubPanel_Transfer implements POSPanelInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = -21082207371857594L;

	public WPOSQuantityPanel_Transfer(WPOS_Transfer posPanel) {
		super(posPanel);
	}

	/** Buttons Controls  		*/
	private Button 			buttonUp;
	private Button 			buttonDown;
	private Button 			buttonDelete;
	private Button 			buttonPlus;
	private Button 			buttonMinus;
	private Button 			buttonScales;
	private Button			buttonFilter;
	private Button          btnAddInfo;
	//	private POSNumberBox 	fieldQuantity;
	
	//iDempiereConsulting __07/03/2019 --- Campi spostati su pannello superiore: WPOSActionPanel.java
	/*
		private NumberBox 	    fieldQuantity;
		//	private POSNumberBox 	fieldPrice;
		private NumberBox 	    fieldPrice;
		//	private POSNumberBox	fieldDiscountPercentage;
		private NumberBox 	    fieldDiscountPercentage;
	*/
	//iDempiereConsulting __07/03/2019 -----
	
	/**	Process Action 						*/
	private WPOSFilterMenu_Transfer actionFilterMenu;

	private final String ACTION_UP       	= "Previous";
	private final String ACTION_DOWN  		= "Next";
	private final BigDecimal CurrentQuantity =  Env.ONE;

	private Panel 		parameterPanel;
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
		ZKUpdateUtil.setHflex(row, "2");
		buttonDelete = createButtonAction("Cancel", "Ctrl+F3");
		buttonDelete.setTooltiptext("Ctrl+F3-"+Msg.translate(ctx, "DeleteLine"));
		buttonDelete.addActionListener(this);
		row.appendChild (buttonDelete);

		buttonPlus = createButtonAction("Plus", "Ctrl+1");
		buttonPlus.setTooltiptext("Ctrl+0-"+Msg.translate(ctx, "add"));
		row.appendChild(buttonPlus);

		buttonMinus = createButtonAction("Minus", "Ctrl+0");
		row.appendChild(buttonMinus);

		buttonUp = createButtonAction(ACTION_UP, "Alt+Up");
		buttonUp.setTooltiptext("Alt+Up-"+Msg.translate(ctx, "Previous"));
		row.appendChild (buttonUp);

		buttonDown = createButtonAction(ACTION_DOWN, "Alt+Down");
		buttonDown.setTooltiptext("Alt+Down-"+Msg.translate(ctx, "Next"));
		row.appendChild (buttonDown);

//		if (posPanel.isPresentElectronicScales()) {
//			buttonScales = createButtonAction("Calculator", "Ctrl+W");
//			buttonScales.setTooltiptext("ALT+down-" + Msg.translate(ctx, "Calculator"));
//			row.appendChild(buttonScales);
//			//			buttonScales.addActionListener(posPanel.getScalesListener());
//		}
		buttonFilter = createButtonAction("Filter", null);
		buttonFilter.setTooltiptext(Msg.translate(ctx, "Filter"));
		row.appendChild (buttonFilter);
		
		btnAddInfo = new Button("INFO");
		btnAddInfo.setHeight("45px");
		btnAddInfo.setWidth("55px");
		btnAddInfo.setStyle("Font-size:1.0em; font-weight:bold");
		btnAddInfo.addActionListener(this);
		row.appendChild (btnAddInfo);

		//iDempiereConsulting __07/03/2019 --- Campi spostati su pannello superiore: WPOSActionPanel.java
		/*
			Label qtyLabel = new Label(Msg.translate(Env.getCtx(), "QtyOrdered"));
			row.appendChild(qtyLabel);
	
			//		fieldQuantity = new POSNumberBox(false);
			fieldQuantity = new NumberBox(true);
	
			row.appendChild(fieldQuantity);
			fieldQuantity.addEventListener(Events.ON_OK, this);
			fieldQuantity.addEventListener(Events.ON_CHANGE, this);
			//		fieldQuantity.setStyle("display: inline;width:65px;height:30px;Font-size:medium;");
			ZKUpdateUtil.setWidth(fieldQuantity, "65px");
			ZKUpdateUtil.setHeight(fieldQuantity.getDecimalbox(),"30px");
			ZKUpdateUtil.setHeight(fieldQuantity.getButton(),"30px");
			fieldQuantity.getDecimalbox().setStyle("display: inline;Font-size:medium;");
	
			Label priceLabel = new Label(Msg.translate(Env.getCtx(), "PriceActual"));
			row.appendChild(priceLabel);
	
			//		fieldPrice = new POSNumberBox(false);
			fieldPrice = new NumberBox(false);
			DecimalFormat format = DisplayType.getNumberFormat(DisplayType.Amount, AEnv.getLanguage(Env.getCtx()));
			fieldPrice.getDecimalbox().setFormat(format.toPattern());
	
			fieldPrice.setTooltiptext(Msg.translate(Env.getCtx(), "PriceActual"));
			row.appendChild(fieldPrice);
			if (!posPanel.isModifyPrice())
				fieldPrice.setEnabled(false);
			else {
				fieldPrice.addEventListener(Events.ON_OK, this);
				fieldPrice.addEventListener(Events.ON_CHANGE, this);
			}
			//		fieldPrice.setStyle("display: inline;width:70px;height:30px;Font-size:medium;");
			ZKUpdateUtil.setWidth(fieldPrice, "70px");
			ZKUpdateUtil.setHeight(fieldPrice.getDecimalbox(),"30px");
			ZKUpdateUtil.setHeight(fieldPrice.getButton(),"30px");
			fieldPrice.getDecimalbox().setStyle("display: inline;Font-size:medium;");
	
			Label priceDiscount = new Label(Msg.translate(Env.getCtx(), "Discount"));
			row.appendChild(priceDiscount);
	
			//		fieldDiscountPercentage = new POSNumberBox(false);
			fieldDiscountPercentage = new NumberBox(false);
			row.appendChild(fieldDiscountPercentage);
			fieldDiscountPercentage.setTooltiptext(Msg.translate(Env.getCtx(), "Discount"));
			if (!posPanel.isModifyPrice())
				fieldDiscountPercentage.setEnabled(false);
			else{
				fieldDiscountPercentage.addEventListener(Events.ON_OK, this);
				fieldDiscountPercentage.addEventListener(Events.ON_CHANGE, this);
			}
			//		fieldDiscountPercentage.setStyle("display: inline;width:70px;height:30px;Font-size:medium;");
			ZKUpdateUtil.setWidth(fieldDiscountPercentage, "70px");
			ZKUpdateUtil.setHeight(fieldDiscountPercentage.getDecimalbox(),"30px");
			ZKUpdateUtil.setHeight(fieldDiscountPercentage.getButton(),"30px");
			fieldDiscountPercentage.getDecimalbox().setStyle("display: inline;Font-size:medium;");
	
			Keylistener keyListener = new Keylistener();
			fieldPrice.appendChild(keyListener);
			keyListener.setCtrlKeys("@#up@#down^#f3^1^0");
			keyListener.addEventListener(Events.ON_CTRL_KEY, posPanel);
			keyListener.addEventListener(Events.ON_CTRL_KEY, this);
			keyListener.setAutoBlur(false);
		*/
		//iDempiereConsulting __07/03/2019 -----
		actionFilterMenu = new WPOSFilterMenu_Transfer(posPanel);

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
				//iDempiereConsulting __07/03/2019 --- Campi spostati su pannello superiore: WPOSActionPanel.java

				//ctrl+f3 == 114
				if (keyEvent.getKeyCode() == 114 ) {
					posPanel.deleteLine(posPanel.getM_MovementLine_ID());
					posPanel.getActionPanel().getFieldQuantity().setValue(0.0);
				}
				//ctrl+1 == 49
				if (keyEvent.getKeyCode() == 49 ) {
					posPanel.getActionPanel().getFieldQuantity().setValue(posPanel.getActionPanel().getFieldQuantity().getValue().add(CurrentQuantity));
				}
				//ctrl+0 == 48
				if (keyEvent.getKeyCode() == 48 ) {
					posPanel.getActionPanel().getFieldQuantity().setValue(posPanel.getActionPanel().getFieldQuantity().getValue().subtract(CurrentQuantity));
				}

				//iDempiereConsulting __07/03/2019 -----
			}
			if (e.getTarget().equals(buttonUp)){
				posPanel.moveUp();
				return;
			}
			else if (e.getTarget().equals(buttonDown)){
				posPanel.moveDown();
				return;
			}
			//iDempiereConsulting __07/03/2019 --- Campi spostati su pannello superiore: WPOSActionPanel.java

			if (e.getTarget().equals(buttonMinus)){
				BigDecimal quantity = posPanel.getActionPanel().getFieldQuantity().getValue().subtract(CurrentQuantity);
				if(quantity.compareTo(Env.ZERO) == 0) {
					if(posPanel.isUserPinValid()) {
						posPanel.setQty(quantity);
					}
				} else {
					posPanel.setQty(quantity);
				}

			}
			else if (e.getTarget().equals(buttonPlus)){
				posPanel.setQty(posPanel.getActionPanel().getFieldQuantity().getValue().add(CurrentQuantity));
			}

			//iDempiereConsulting __07/03/2019 -----

			else if (e.getTarget().equals(buttonDelete)){
				if(posPanel.isUserPinValid()) {
					posPanel.deleteLine(posPanel.getM_MovementLine_ID());

					posPanel.updateLineTable();
					posPanel.refreshPanel();
					return;
				}
			}

			if (e.getTarget().equals(buttonFilter)){
				//				openFilter();
				//				Point point = MouseInfo.getPointerInfo().getLocation();
				actionFilterMenu.getPopUp().setPage(buttonFilter.getPage());
				//				actionFilterMenu.getPopUp().setPage(this.getPage());
				actionFilterMenu.getPopUp().open(buttonFilter, "after_start");
				//				actionFilterMenu.getPopUp().open(Double.valueOf(point.getX()).intValue()-140,Double.valueOf(point.getY()).intValue()-190);
				return;
			}

			BigDecimal value = Env.ZERO;
			//iDempiereConsulting __07/03/2019 --- Campi spostati su pannello superiore: WPOSActionPanel.java

			if(Events.ON_OK.equals(e.getName()) || Events.ON_CHANGE.equals(e.getName())) {

				value = posPanel.getActionPanel().getFieldQuantity().getValue();
				if(value == null)
					return;
				if(e.getTarget().equals(posPanel.getActionPanel().getFieldQuantity().getDecimalbox())) {
					if(Events.ON_OK.equals(e.getName())){
						posPanel.setQty(value);
					}
					else if(posPanel.isAddQty() 
							|| Events.ON_CHANGE.equals(e.getName())){
						//  Verify if it add or set
						//iDempiereConsulting__ 02/05/2018 -- Commentato per errata aggiunta di qta non prevista; problema dovuto a 'isAddQty' impostato a TRUE 
						//     al primissimo inserimento di un nuovo prodotto (?)  .....codice core trovato così, nessuna modifica di iDempiereConsulting...... 
						//						if(posPanel.isAddQty()) {
						//							posPanel.setQtyAdded(value);
						//						} else {

						posPanel.setQty(value);
						//						}
						//iDempiereConsulting__ 02/05/2018 -- 
					}

				}
			}

			//iDempiereConsulting __07/03/2019 -----

			posPanel.updateLineTable();
			posPanel.refreshPanel();
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
		//iDempiereConsulting __07/03/2019 --- Campi spostati su pannello superiore: WPOSActionPanel.java
		if(posPanel.getActionPanel()!=null) {
			posPanel.getActionPanel().getFieldQuantity().setEnabled(true);
		}
		//iDempiereConsulting __07/03/2019 -----
		buttonDelete.setEnabled(true);
		buttonPlus.setEnabled(true);
		buttonMinus.setEnabled(status);
		buttonDown.setEnabled(status);
		buttonUp.setEnabled(status);
	}

	@Override
	public void refreshPanel() {
		if(posPanel.hasLines()){
			buttonDown.setEnabled(true);
			buttonUp.setEnabled(true);

			// Only enable buttons if status==(drafted or in progress)
			if(posPanel.getMovement().getDocStatus().compareToIgnoreCase(MMovement.STATUS_Drafted)==0 || 
					posPanel.getMovement().getDocStatus().compareToIgnoreCase(MMovement.STATUS_InProgress)==0 ){
				buttonDelete.setEnabled(true);
				buttonPlus.setEnabled(true);
				buttonMinus.setEnabled(true);

//				if (posPanel.isPresentElectronicScales())
//					buttonScales.setEnabled(true);
//				else
					buttonScales.setVisible(false);

				//iDempiereConsulting __07/03/2019 --- Campi spostati su pannello superiore: WPOSActionPanel.java
				
				posPanel.getActionPanel().getFieldQuantity().setEnabled(true);
				
				
				//iDempiereConsulting __07/03/2019 -----
			}else {
				buttonDelete.setEnabled(false);
				buttonPlus.setEnabled(false);
				buttonMinus.setEnabled(false);

				//iDempiereConsulting __07/03/2019 --- Campi spostati su pannello superiore: WPOSActionPanel.java
				posPanel.getActionPanel().getFieldQuantity().setEnabled(false);
				//iDempiereConsulting __07/03/2019 -----
			}
		} else {
			buttonDown.setEnabled(false);
			buttonUp.setEnabled(false);
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
		//iDempiereConsulting __07/03/2019 --- Campi spostati su pannello superiore: WPOSActionPanel.java
		
		posPanel.getActionPanel().getFieldQuantity().setValue(posPanel.getQty());
		
		//iDempiereConsulting __07/03/2019 -----
	}

	public void resetPanel() {
		
		buttonDown.setEnabled(false);
		buttonUp.setEnabled(false);
		buttonDelete.setEnabled(false);
		buttonPlus.setEnabled(false);
		buttonMinus.setEnabled(false);
		//iDempiereConsulting __07/03/2019 --- Campi spostati su pannello superiore: WPOSActionPanel.java
		
		posPanel.getActionPanel().getFieldQuantity().setValue(0);
		posPanel.getActionPanel().getFieldQuantity().setEnabled(false);
		
		//iDempiereConsulting __07/03/2019 -----
	}

	public void setQuantity(BigDecimal value) {
		//iDempiereConsulting __07/03/2019 --- Campi spostati su pannello superiore: WPOSActionPanel.java
		
		posPanel.getActionPanel().getFieldQuantity().setValue(value);
		posPanel.getActionPanel().getFieldQuantity().focus();
		
		//iDempiereConsulting __07/03/2019 -----
	}

	public void requestFocus() {
		//iDempiereConsulting __07/03/2019 --- Campi spostati su pannello superiore: WPOSActionPanel.java
		
		posPanel.getActionPanel().getFieldQuantity().focus();
		
		//iDempiereConsulting __07/03/2019 -----
	}

	/**
	 * Get Panel 
	 * @return Panel
	 */
	public Panel getPanel(){
		return parameterPanel;
	}

	/** 
	 * Open window Filter 
	 */
//	private void openFilter() { 
//		WQueryUserQry qt = new WQueryUserQry(posPanel);
//		qt.setVisible(true);
//		qt.setAttribute(Window.MODE_KEY, Window.MODE_HIGHLIGHTED);
//		AEnv.showWindow(qt);
//	}

}
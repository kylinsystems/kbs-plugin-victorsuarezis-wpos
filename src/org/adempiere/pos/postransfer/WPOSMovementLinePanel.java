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

package org.adempiere.pos.postransfer;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigDecimal;
import java.util.List;

import org.adempiere.pos.WPOSTable;
import org.adempiere.pos.postransfer.service.POSMovementLineTableHandle;
import org.adempiere.pos.service.POSPanelInterface;
import org.adempiere.webui.component.ListHeader;
import org.adempiere.webui.component.ListModelTable;
import org.adempiere.webui.event.WTableModelEvent;
import org.adempiere.webui.event.WTableModelListener;
import org.compiere.minigrid.IDColumn;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.event.ListDataEvent;

/**
 * Button panel supporting multiple linked layouts
 * @author Mario Calderon, mario.calderon@westfalia-it.com, Systemhaus Westfalia, http://www.westfalia-it.com
 * @author Raul Muñoz, rmunoz@erpcya.com, ERPCYA http://www.erpcya.com
 *  <li><a href="https://github.com/adempiere/adempiere/issues/533">
 *           BF/FR [ 533 ] Update Fields when selected line</a>
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * @author victor.perez@e-evolution.com , http://www.e-evolution.com
 */
public class WPOSMovementLinePanel extends WPOSSubPanel_Transfer implements WTableModelListener, POSPanelInterface,FocusListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4023538043556457231L;

	/**
	 * Constructor
	 * 
	 * @param posPanel POS Panel
	 */
	public WPOSMovementLinePanel(WPOS_Transfer posPanel, boolean showPanelDescProd) {
		super(posPanel);
		this.showPanelProd = showPanelDescProd;
	}
	/**	Current Movement Line			*/
	private int 					movementLineId = 0;
	/** The Table					*/
	private WPOSTable 				posTable;
	/** The Table					*/
	public 	POSMovementLineTableHandle lineTableHandle;
	/**	Logger				*/
	private static CLogger logger = CLogger.getCLogger(WPOSMovementLinePanel.class);
	
	public boolean isFilter = false;
	
	public boolean showPanelProd = false;

	@Override
	protected void init() {
		posTable = new WPOSTable();
		//	
		lineTableHandle = new POSMovementLineTableHandle(posTable);
		lineTableHandle.prepareTable();

		posTable.setColumnClass(4, BigDecimal.class, true);
		appendChild(posTable);
		posTable.repaint();
		posTable.setWidth("99%");
		if(showPanelProd)
			posTable.setHeight("47%");
		else
			posTable.setHeight("65%");
		posTable.addActionListener(this);
		posTable.addEventListener(Events.ON_CLICK, this);
		posTable.getModel().addTableModelListener(this);
		posTable.setClass("Table-OrderLine");
		posTable.setStyle("overflow-y: scroll; zoom:1.4;");
		posTable.setColumnReadOnly(POSMovementLineTableHandle.POSITION_MOVEMENTQTY, true);
		
		List<ListHeader> lisss = posTable.getListHead().getChildren();
		for (ListHeader listHeader : lisss) {
			listHeader.setStyle("zoom: 0.8");
		}
		
	}

	@Override
	public void refreshPanel() {
	
		if (!posPanel.hasMovement()) {
			posTable.loadTable(new PO[0]);
		}
		//	Load Data
		lineTableHandle.loadTable(posPanel.getM_Movement_ID());
		//	
		for (int i = 0; i < posTable.getRowCount(); i ++ ) {
			IDColumn key = (IDColumn) posTable.getModel().getValueAt(i, 0);
			if ( key != null && movementLineId > 0 && key.getRecord_ID() == movementLineId)
			{
				posTable.setSelectedIndex(i);
				posPanel.changeViewPanel();
				showProductInfo(i);
				break;
			}
			// Select first row, if end of table and no row has been selected
			if(i==posTable.getRowCount()-1)	 {
        //  Set Current Order Line
        movementLineId = key.getRecord_ID();
				posTable.setSelectedIndex(0);
				posPanel.changeViewPanel();
				showProductInfo(0);
			}
		}
		
		
		return;
	}
	
	/**
	 * Disable Table 
	 */
	public void disableTable() {
		posTable.setEnabled(false);
		lineTableHandle.setEditable(false, false);
		posTable.removeActionListener(this);
		posTable.removeEventListener(Events.ON_CLICK, this);
	}
	
	/**
	 * Enable Table
	 */
	public void enableTable() {
		posTable.setEnabled(true);
		lineTableHandle.setEditable(true, true);
		posTable.addActionListener(this);
		posTable.addEventListener(Events.ON_CLICK, this);
	}
	
	@Override
	public void onEvent(Event arg0) throws Exception {
		String action = arg0.getTarget().getId();
//		if (action == null || action.length() == 0)
//			return;
		posTable.getModel().removeTableModelListener(this);
		logger.info( "POSOrderLinePanel - actionPerformed: " + action);
		
		if(arg0.getName().equals(Events.ON_SELECT)) {
			selectLine();
		}
		
		//	Refresh All
		posPanel.refreshPanel();
		posTable.getModel().addTableModelListener(this);
		
		int numItem = posTable.getItems().size();
		for (int i = 0; i < numItem; i++) {
			if(isFilter)
				posTable.getItems().get(i).setStyle("background:yellow !important");
			else
				posTable.getItems().get(i).setStyle(null);
		}
		
	}
	
	public void selectLine(){
		lineTableHandle.setEditable(true, true);
		IDColumn key = (IDColumn) posTable.getModel().getValueAt(posTable.getSelectedRow(), 0);
		movementLineId = key.getRecord_ID();
		showProductInfo(posTable.getSelectedRow());
		posPanel.changeViewPanel();
	}
	@Override
	public void tableChanged(WTableModelEvent event) {
		boolean isUpdate = (event.getType() == ListDataEvent.CONTENTS_CHANGED);
		int col = event.getColumn();
		int row = posTable.getSelectedRow();
		/*if(event.getColumn() == POSOrderLineTableHandle.POSITION_DELETE){
			posTable.getModel().removeTableModelListener(this);
			ListModelTable m_model = (ListModelTable)event.getModel();
			for(int x = 0; x < posTable.getRowCount(); x++){
				String value = m_model.getValueAt(x, 1).toString();
				if(value.length() == 0){
					IDColumn key = (IDColumn) m_model.getValueAt(x, 0);
					orderLineId = key.getRecord_ID();
					posPanel.deleteLine(orderLineId);
					posTable.getModel().remove(x);
				}
			}

			posTable.getModel().addTableModelListener(this);
			posPanel.refreshHeader();
			return;
		}*/
		if (!isUpdate || (col != POSMovementLineTableHandle.POSITION_MOVEMENTQTY)) {
			return;
		}
		if (event.getModel().equals(posTable.getModel())){ //Add Minitable Source Condition
				if (row != -1) {	
					ListModelTable model = posTable.getModel();
					IDColumn key = (IDColumn) model.getValueAt(row, 0);
					posTable.getModel().removeTableModelListener(this);
				
				//	Validate Key
				if (key != null) {
					//	Set Current Order Line
					movementLineId = key.getRecord_ID();
					BigDecimal m_QtyOrdered       = (BigDecimal) posTable.getValueAt(row, POSMovementLineTableHandle.POSITION_MOVEMENTQTY);
					m_QtyOrdered = m_QtyOrdered.setScale(0);
					posPanel.setQty(m_QtyOrdered);
					updateLine();
				}
			}
			
		}
	}
	
	public void updateLine() {
		int row = posTable.getSelectedRow();
			//	Remove Listener
			posTable.getModel().removeTableModelListener(this);
			//	Remove line
			if(posPanel.getQty() != null && posPanel.getQty().signum() < 0) {
				if (movementLineId > 0 && !posPanel.isAddQty())
				if(posPanel.isRequiredPIN() && posPanel.isUserPinValid()) {
					posPanel.deleteLine(movementLineId);
				}
				if (row >= 0) {
					((ListModelTable) posTable.getModel()).remove(row);
					posTable.getModel().addTableModelListener(this);
					posPanel.refreshPanel();
				}
				//	Exit
				return;
			}
			
			//	Get Order Line
			BigDecimal[] m_Summary = posPanel.updateLine(
					movementLineId,
					posPanel.getQty().add(posPanel.getQtyAdded()));
			
			posTable.getModel().addTableModelListener(this);
			//	Only Refresh Header
			posPanel.refreshHeader();
			posPanel.refreshPanel();
			
			return;
	}
	
	public void moveDown() {
//		if((posTable.getRowCount()-1) > posTable.getSelectedRow() && posTable.getRowCount() != 0){
//			if((posTable.getRowCount()-1)>posTable.getSelectedRow()+4)
//				posTable.setSelectedIndex(posTable.getSelectedRow()+4);
//			else
//				posTable.setSelectedIndex(0);
//		}else
		if((posTable.getRowCount()-1) > posTable.getSelectedRow() && posTable.getRowCount() != 0)
				posTable.setSelectedIndex(posTable.getSelectedRow()+1);
		else
			posTable.setSelectedIndex(0);
		selectLine();
		posPanel.changeViewPanel();
		showProductInfo(posTable.getSelectedIndex());
		return;
	}
	
	public void moveUp() {
//		if((posTable.getRowCount()-1) >= posTable.getSelectedRow() && posTable.getSelectedRow() != 0){
//			if((posTable.getRowCount()-1) >= posTable.getSelectedRow()-4 && posTable.getSelectedRow()-4>=0)
//				posTable.setSelectedIndex(posTable.getSelectedRow()-4);
//			else 
//				posTable.setSelectedIndex(posTable.getRowCount()-1);
//		}else
		if((posTable.getRowCount()-1) >= posTable.getSelectedRow() && posTable.getSelectedRow() != 0)
			posTable.setSelectedIndex(posTable.getSelectedRow()-1);
		else	
			posTable.setSelectedIndex(posTable.getRowCount()-1);
		selectLine();		
		posPanel.changeViewPanel();
		showProductInfo(posTable.getSelectedIndex());
		return;
	}
	
	/**
	 * Seek in record from Product
	 * @param p_M_Product_ID
	 */
	public void seekFromProduct(int p_M_Product_ID) {
		int m_M_MovementLine_ID = getM_MovementLine_ID(p_M_Product_ID);
		if(m_M_MovementLine_ID <= 0)
			return;
		//	
		movementLineId = m_M_MovementLine_ID;
		//	Iterate
		for (int i = 0; i < posTable.getRowCount(); i ++ ) {
			IDColumn key = (IDColumn) posTable.getModel().getValueAt(i, 0);
			if ( key != null && movementLineId > 0 && key.getRecord_ID() == movementLineId) {
				posTable.setSelectedIndex(i);
				selectLine();
				break;
			}
		}
	}
	/**
	 * 	Focus Gained
	 *	@param e
	 */
	public void focusGained (FocusEvent e) {
		logger.info("POSMovementLinePanel - focusGained: " + e);
	}	//	focusGained
		

	/**
	 * 	Focus Lost
	 *	@param e
	 */
	public void focusLost (FocusEvent e) {
		if (e.isTemporary())
			return;
		logger.info( "POSDocumentPanel - focusLost");
		posPanel.refreshPanel();
	}	//	focusLost

	@Override
	public String validatePayment() {
		return null;
	}

	@Override
	public void changeViewPanel() {
		int row = posTable.getSelectedRow();
		if (row != -1 &&  row < posTable.getRowCount()) {
			//	Set Current Order Line
			BigDecimal qtyOrdered = (BigDecimal) posTable.getValueAt(row, POSMovementLineTableHandle.POSITION_MOVEMENTQTY);
			
			posPanel.setQty(qtyOrdered);
		}
		else {
			posPanel.setQty(Env.ZERO);
		}
	}
	
	/**
	 * Show Product Info
	 * @param row
	 * @return void
	 */
	private void showProductInfo(int row) {
		Object data = posTable.getModel().getValueAt(row, 0);
		if ( data != null )	{
			Integer id = (Integer) ((IDColumn)data).getRecord_ID();
			posPanel.setMovementLineId(id);

			BigDecimal quantity = (BigDecimal) posTable.getModel().getValueAt(row, POSMovementLineTableHandle.POSITION_MOVEMENTQTY);
			String name = (String)posTable.getModel().getValueAt(row, 1);

			//	Refresh
			posPanel.setQty(quantity);
			posPanel.getActionPanel().setFieldProductName(name);
			posPanel.changeViewPanel();
			posPanel.refreshProductInfo(posPanel.getM_Product_ID(posPanel.getMovementLineId()));

			//	posPanel.refreshProductInfo(m_M_Product_ID);

		}
	}

	/**
	 * Get Movement Line from Product
	 * @param p_M_Product_ID
	 * @return
	 */
	private int getM_MovementLine_ID(int p_M_Product_ID) {
		return DB.getSQLValue(null, "SELECT ml.M_MovementLine_ID "
				+ "FROM M_MovementLine ml "
				+ "WHERE ml.M_Product_ID = ? AND ml.M_Movement_ID = ?", 
				p_M_Product_ID, posPanel.getM_Movement_ID());
	}
	
	public int getM_MovementLine_ID()
	{
		return movementLineId;
	}
	
	public WPOSTable posTable(){
		return posTable;
	}
	
}
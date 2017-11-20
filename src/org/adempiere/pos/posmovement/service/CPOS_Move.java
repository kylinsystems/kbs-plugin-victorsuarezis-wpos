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
 * Copyright (C) 2003-2013 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Yamel Senih www.erpcya.com                                 *
 *****************************************************************************/
package org.adempiere.pos.posmovement.service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Vector;

import org.adempiere.model.MInventory_lit;
import org.adempiere.pos.AdempierePOSException;
import org.compiere.model.I_AD_User;
import org.compiere.model.MDocType;
import org.compiere.model.MInventoryLine;
import org.compiere.model.MLocator;
import org.compiere.model.MOrg;
import org.compiere.model.MPOSKey;
import org.compiere.model.MProduct;
import org.compiere.model.MSequence;
import org.compiere.model.MUser;
import org.compiere.model.MWarehouse;
import org.compiere.model.X_M_Inventory;
import org.compiere.process.DocAction;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.idempiere.model.MPOS;

/**
 * @author Mario Calderon, mario.calderon@westfalia-it.com, Systemhaus Westfalia, http://www.westfalia-it.com
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * @author victor.perez@e-evolution.com , http://www.e-evolution.com
 */
public class CPOS_Move {
	
	/**
	 * 
	 * *** Constructor ***
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 */
	public CPOS_Move() {
		ctx = Env.getCtx();
		decimalFormat = DisplayType.getNumberFormat(DisplayType.Amount);
		dateFormat = DisplayType.getDateFormat(DisplayType.Date);
		today = Env.getContextAsDate(ctx, "#Date");
	}
	
	/**	POS Configuration		*/
	private MPOS 				entityPOS;
	/**	Current Inventory		*/
	private MInventory_lit		currentInventory;
	/** Sequence Doc 			*/
	private MSequence 			documentSequence;
	/** Context					*/
	protected Properties 		ctx;
	/**	Today's (login) date	*/
	private Timestamp 			today;
	/**	Inventory List			*/
	private ArrayList<Integer>  inventoryList;
	/**	Order List Position		*/
	private int 				recordPosition;
	/**	Is Inventory Completed	*/
	private boolean 			isToPrint;
	/**	Logger					*/
	private CLogger 			log = CLogger.getCLogger(getClass());
	/**	Internal Use Qty		*/
	private BigDecimal 			qtyInternalUse = BigDecimal.ZERO;
	/**	Internal Use Qty		*/
	private BigDecimal 			quantityAdded = BigDecimal.ZERO;
	/** is new line **/
	private boolean				isAddQty = false;
	/** InventoryLine id 		*/
	private int 				inventoryLineId = 0;
	/**	Format					*/
	private DecimalFormat 		decimalFormat;
	/**	Date Format				*/
	private SimpleDateFormat	dateFormat;
	/**	Window No				*/
	private int 				windowNo;
	
	/**
	 * 	Set MPOS
	 * @param salesRepId
	 * @return true if found/set
	 */
	public void setPOS(int salesRepId, String typePos) {
		//List<MPOS> poss = getPOSs(p_SalesRep_ID);
		List<MPOS> poss = getPOSByOrganization(Env.getAD_Org_ID(getCtx()));
		//
		if (poss.size() == 0) {
			throw new AdempierePOSException("@NoPOSForUser@");
		} 
//		else if (poss.size() == 1) {
//			entityPOS = poss.get(0);
//		}
		else {
			
			for (MPOS mpos : poss) {
				
				if(mpos.get_ValueAsString("LIT_POSType").equals(typePos)){
					entityPOS = mpos;
					break;
				}
			}			
		}
	}	//	setMPOS
	
	/**
	 * Set POS
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @param pos
	 * @return void
	 */
	public void setM_POS(org.idempiere.model.MPOS pos) {
		entityPOS = pos;
	}
	
	/**
	 * Get number format
	 * @return
	 * @return DecimalFormat
	 */
	public DecimalFormat getNumberFormat() {
		return decimalFormat;
	}
	
	/**
	 * Get Date Format
	 * @return
	 */
	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}
	
	/**
	 * Get Window Number
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return int
	 */
	public int getWindowNo() {
		return windowNo;
	}
	
	/**
	 * Set window No
	 * @param windowNo
	 */
	public void setWindowNo(int windowNo) {
		this.windowNo = windowNo;
	}
	
	/**
	 * Validate if is Order Completed
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return boolean
	 */
	public boolean isCompleted() {
		if(!hasInventory()) {
			return false;
		}
		//	
		return currentInventory.isProcessed()
				&& X_M_Inventory.DOCSTATUS_Completed.equals(currentInventory.getDocStatus());
	}
	
	/**
	 * Get Sequence
	 * @return
	 * @return int
	 */
	public int getAD_Sequence_ID() {
		if (entityPOS.getC_DocType_ID() > 0)
			return entityPOS.getC_DocType().getDocNoSequence_ID();
		else
			throw new AdempierePOSException("@C_POS_ID@ @C_DocType_ID @NotFound@");
	}
	
	/**
	 * Get Organization
	 * @return
	 * @return int
	 */
	public int getAD_Org_ID() {
		return entityPOS.getAD_Org_ID();
	}


	/**
	 * Validate if is voided
	 * @return
	 * @return boolean
	 */
	public boolean isClosed() {
		if(!hasInventory()) {
			return false;
		}
		//
		return X_M_Inventory.DOCSTATUS_Closed.equals(currentInventory.getDocStatus());
	}

	/**
	 * Validate if is voided
	 * @return
	 * @return boolean
	 */
	public boolean isVoided() {
		if(!hasInventory()) {
			return false;
		}
		//	
		return X_M_Inventory.DOCSTATUS_Voided.equals(currentInventory.getDocStatus());
	}
	
	/**
	 * Validate if is drafted
	 * @return
	 * @return boolean
	 */
	public boolean isDrafted() {
		if(!hasInventory()) {
			return false;
		}
		//	
		return !isCompleted() 
				&& !isVoided() 
				&& X_M_Inventory.DOCSTATUS_Drafted.equals(currentInventory.getDocStatus());
	}
	
	/**
	 * Validate if is "In Process"}
	 * @return
	 * @return boolean
	 */
	public boolean isInProgress() {
		if(!hasInventory()) {
			return false;
		}
		//	
		return !isCompleted() 
				&& !isVoided() 
				&& X_M_Inventory.DOCSTATUS_InProgress.equals(currentInventory.getDocStatus());
	}
	
	/**
	 * Validate if is "Invalid"}
	 * @return
	 * @return boolean
	 */
	public boolean isInvalid() {
		if(!hasInventory()) {
			return false;
		}
		//	
		return !isCompleted() 
				&& !isVoided() 
				&& X_M_Inventory.DOCSTATUS_Invalid.equals(currentInventory.getDocStatus());
	}
	
	/**
	 * Validate if has lines
	 * @return
	 * @return boolean
	 */
	public boolean hasLines() {
		if(!hasInventory()) {
			return false;
		}
		//	
		return currentInventory.getLines(true).length > 0;
	}
	
	/**
	 * Validate if is InternalUseInventory
	 * @return
	 * @return boolean
	 */
	public boolean isInternalUseInventory() {
		if(!hasInventory()) {
			return false;
		}

		return MDocType.DOCSUBTYPEINV_InternalUseInventory.equals(getDocSubTypeInv());
	}
	
	/**
	 * Validate if is PhysicalInventory
	 * @return
	 * @return boolean
	 */
	public boolean isPhysicalInventory() {
		if(!hasInventory()) {
			return false;
		}

		return MDocType.DOCSUBTYPEINV_PhysicalInventory.equals(getDocSubTypeInv());
	}
	
	/**
	 * Validate if is CostAdjustment
	 * @return
	 * @return boolean
	 */
	public boolean isCostAdjustment() {
		if(!hasInventory()) {
			return false;
		}

		return MDocType.DOCSUBTYPEINV_CostAdjustment.equals(getDocSubTypeInv());
	}
	
	/**
	 * Get Document Sub Type SO
	 * @return
	 * @return String
	 */
	private String getDocSubTypeInv() {
		//	
		MDocType docType = MDocType.get(getCtx(), getC_DocType_ID());
		if(docType != null) {
			if(docType.getDocSubTypeInv() != null) {
				return docType.getDocSubTypeInv();
			}
		}
		//	
		return "";
	}
	
	/**
	 * Get Document Type from Inventory
	 * @return
	 * @return int
	 */
	public int getC_DocType_ID() {
		if(!hasInventory()) {
			return 0;
		}
		//	
		return currentInventory.getC_DocType_ID();
	}
	
	/**
	 * Validate if is to print invoice
	 * @return
	 * @return boolean
	 */
	public boolean isToPrint() {
		return isToPrint;
	}

	/**
	 * Validate if is to print invoice
	 * @return
	 * @return boolean
	 */
	public void setIsToPrint(boolean isToPrint) {
		this.isToPrint= isToPrint;
	}
	
	/**
	 * Get Current Inventory
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return MInventory
	 */
	public MInventory_lit getInventory() {
		return currentInventory;
	}
	
	/**
	 * Has Order
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return boolean
	 */
	public boolean hasInventory() {
		return currentInventory != null
				&& currentInventory.getM_Inventory_ID() != 0;
	}
				
	/**
	 * 	Get BPartner Contact
	 *	@return AD_User_ID
	 */
	public int getAD_User_ID () {
		return Env.getAD_User_ID(Env.getCtx());
	}	//	getAD_User_ID
	
	/**
	 * Get Auto Delay
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return int
	 */
	public int getAutoLogoutDelay() {
		if (entityPOS != null)
			return entityPOS.getAutoLogoutDelay();
		return 0;
	}

	/**
	 * Get PIN Entry Timeout
	 * @return
	 * @return int
	 */
	public int getPINEntryTimeout() {
		if (entityPOS != null)
			return entityPOS.getPINEntryTimeout();
		return 0;
	}

	/**
	 * Get Sales Rep. Name
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return String
	 */
	public String getSalesRepName() {
		MUser salesRep = MUser.get(ctx);
		if(salesRep == null) {
			return null;
		}
		//	Default Return
		return salesRep.getName();
	}
	
	/**
	 * Get Sales Representative
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return int
	 */
	public int getSalesRep_ID() {
		return entityPOS.getSalesRep_ID();
	}
	
	/**
	 * Get POS Configuration
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return MPOS
	 */
	public MPOS getM_POS() {
		return entityPOS;
	}
	
	/**
	 * Get POS Name
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return String
	 */
	public String getPOSName() {
		return entityPOS.getName();
	}
	
	/**
	 * Get POS Identifier
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return int
	 */
	public int getC_POS_ID() {
		return entityPOS.getC_POS_ID();
	}


	/**
	 * Is Enable Product Lookup
	 * @return
     */
	public boolean isEnableProductLookup() {
		return entityPOS.isEnableProductLookup();
	}

	/**
	 * Is POS Required PIN
	 * @return
     */
	public boolean isRequiredPIN(){
		return entityPOS.isPOSRequiredPIN();
	}

	/**
	 * 	New Inventory
	 *  @param partnerId
	 */
	public void newInventory(/*int partnerId*/) {
		log.info( "PosPanel.newInventory");
		currentInventory = null;
		int docTypeId = entityPOS.getC_DocType_ID();
		//	Create Inventory
		createInventory(docTypeId);
		//	
		reloadInventory();
	}	//	newOrder
	
	/**
	 * Set Custom Document Type
	 * @param docTypeTargetId
	 * @return void
	 */
	public void setC_DocType_ID(int docTypeTargetId) {
		//	Valid if has a Order
		if(!isDrafted())
			return;
		//	Set Document Type
		currentInventory.setC_DocType_ID(docTypeTargetId);
		//	Set Sequenced No
		String value = DB.getDocumentNo(getC_DocType_ID(), null, false, currentInventory);
		if (value != null) {
			currentInventory.setDocumentNo(value);
		}
		currentInventory.saveEx();
	}
	
	
	/**
	 * Get/create Order
	 *	@param partnerId Business Partner
	 *	@param docTypeTargetId ID of document type
	 */
	private void createInventory(int docTypeTargetId) {
		int inventoryId = getFreeM_Inventory_ID();
		//	Change Values for new Inventory
		if(inventoryId > 0) {
			currentInventory = new MInventory_lit(Env.getCtx(), inventoryId, null);
			currentInventory.setMovementDate(getToday());
			
		} else {
			currentInventory = new MInventory_lit(Env.getCtx(), 0, null);
		}
		currentInventory.setAD_Org_ID(entityPOS.getAD_Org_ID());
		currentInventory.getWrapper().setC_POS_ID(entityPOS.getC_POS_ID());
		currentInventory.setM_Warehouse_ID(entityPOS.getM_Warehouse_ID());
		if (docTypeTargetId != 0) {
			currentInventory.setC_DocType_ID(docTypeTargetId);
		} else {
			currentInventory.setC_DocType_ID(MDocType.getDocType(MDocType.DOCSUBTYPEINV_InternalUseInventory));
		}
		currentInventory.saveEx();
		//	Add if is new
		if(inventoryId < 0) {
			//	Add To List
			inventoryList.add(currentInventory.getM_Inventory_ID());
		}
		//  Add record
		reloadIndex(currentInventory.getM_Inventory_ID());
	} // PosOrderModel
	
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
				getC_POS_ID());
	}
	
	/**
	 * 	Get POSs for specific Sales Rep or all
	 *	@return array of POS
	 */
	public List<MPOS> getPOSByOrganization (int orgId) {
		return MPOS.getByOrganization(ctx, orgId, null);
	}

	/**************************************************************************
	 * 	Get Today's date
	 *	@return date
	 */
	public Timestamp getToday() {
		return today;
	}	//	getToday
	
	/**
	 * @param orderId
	 */
	public void setInventory(int inventoryId) {
		currentInventory = new MInventory_lit(ctx, inventoryId, null);
		//	
		reloadInventory();
	}
	
	/**
	 * Update line
	 * @param inventoryLineId
	 * @param qtyOrdered
	 * @return
	 */
	public BigDecimal [] updateLine(int inventoryLineId,
									BigDecimal qtyInternalUse
									) {
		//	Valid if has a Order
		if(!isDrafted())
			return null;
		//	
		MInventoryLine[] inventoryLines = currentInventory.getLines(true);
		
		//	Search Line
		for(MInventoryLine inventoryLine : inventoryLines) {
			//	Valid No changes
			if(qtyInternalUse.compareTo(inventoryLine.getQtyInternalUse()) == 0 ) {
				return null;
			}
			
			inventoryLine.setQtyInternalUse(qtyInternalUse);
			inventoryLine.saveEx();
		}
		//	Return Value
		return new BigDecimal[]{qtyInternalUse};
	}

	/**
	 * Create new Line
	 * @param product
	 * @param qtyInternalUse
     * @return
     */
	public MInventoryLine addOrUpdateLine(MProduct product, BigDecimal qtyInternalUse) {
		//	Valid Complete
		if (!isDrafted())
			return null;
		// catch Exceptions at order.getLines()
		MInventoryLine[] lines = currentInventory.getLines(true);
		for (MInventoryLine line : lines) {
			if (line.getM_Product_ID() == product.getM_Product_ID()) {
				//increase qty
				setInventoryLineId(line.getM_InventoryLine_ID());
				BigDecimal currentQty = line.getQtyInternalUse();
				BigDecimal totalQty = currentQty.add(qtyInternalUse);
				//	Set or Add Qty
				line.setQtyInternalUse(isAddQty()? totalQty: qtyInternalUse);
				line.saveEx();
				return line;
			}
		}
        //create new line
		MInventoryLine line = new MInventoryLine(Env.getCtx(), 0, null);
		line.setM_Inventory_ID(currentInventory.getM_Inventory_ID());
		line.setM_Product_ID(product.getM_Product_ID());
		line.setQtyInternalUse(qtyInternalUse);
		int chargeID = DB.getSQLValue(null, "SELECT C_Charge_ID FROM C_Charge WHERE IsActive='Y' AND AD_Client_ID=? AND Name=?", Env.getAD_Client_ID(getCtx()),"InternalInventory");
		if(chargeID>0)
			line.setC_Charge_ID(chargeID);
		
		int locatorID = product.getM_Locator_ID();
		if(locatorID<=0){
			MWarehouse warehouse = new MWarehouse(getCtx(), Env.getContextAsInt(getCtx(), "#M_Warehouse_ID"),null);
			if(warehouse!=null)
				line.setM_Locator_ID(warehouse.getDefaultLocator().getM_Locator_ID());
		}
		//	Save Line
		line.saveEx();
		setInventoryLineId(line.getM_InventoryLine_ID());
		
		return line;
			
	} //	addOrUpdateLine

	/**
	 *  Save Line
	 * @param productId
	 * @param qtyInternalUse
     * @return
     */
	public String addOrUpdate(int productId, BigDecimal qtyInternalUse) {
		String errorMessage = null;
		try {
			MProduct product = MProduct.get(ctx, productId);
			if (product == null)
				return "@No@ @InfoProduct@";
			//	Validate if exists a order
			if (hasInventory()) {
				addOrUpdateLine(product, qtyInternalUse);
			} else {
				return "@POS.MustCreateOrder@";
			}
		} catch (Exception e) {
			errorMessage = e.getMessage();
		}
		//	
		return errorMessage;
	} //	saveLine
	
	/**
	 * 	Call Inventory void process 
	 *  Only if Inventory is "Drafted", "In Progress" or "Completed"
	 * 
	 *  @return true if inventory voided; false otherwise
	 */
	private boolean voidOrder() {
		if (!(currentInventory.getDocStatus().equals(MInventory_lit.STATUS_Drafted)
				|| currentInventory.getDocStatus().equals(DocAction.STATUS_InProgress)
				|| currentInventory.getDocStatus().equals(DocAction.STATUS_Completed)))
			return false;
		
		// Standard way of voiding an order
		currentInventory.setDocAction(MInventory_lit.DOCACTION_Void);
		if (currentInventory.processIt(MInventory_lit.DOCACTION_Void) ) {
			currentInventory.setDocAction(MInventory_lit.DOCACTION_None);
			currentInventory.setDocStatus(MInventory_lit.STATUS_Voided);
			currentInventory.saveEx();
			return true;
		} else {
			return false;
		}
	} // cancelOrder
	
	/**
	 * Execute deleting an inventory
	 * If the inventory is in drafted status -> ask to delete it
	 * If the inventory is in completed status -> ask to void it it
	 * Otherwise, it must be done outside this class.
	 */
	public String cancelInventory() {
		String errorMsg = null;
		try {
			//	Get Index
			int currentIndex = inventoryList.indexOf(currentInventory.getM_Inventory_ID());
			if (!hasInventory()) {
				throw new AdempierePOSException("@POS.MustCreateOrder@");
			} else if (!isCompleted()) {
				//	Delete Order
				currentInventory.deleteEx(true);
			} else if (isCompleted()) {	
				voidOrder();
			} else {
				throw new AdempierePOSException("@POS.OrderIsNotProcessed@");
			}
			//	Remove from List
			if(currentIndex >= 0) {
				inventoryList.remove(currentIndex);
			}
			//	
			currentInventory = null;
			//	Change to Next
			if(hasRecord()){
				if(isFirstRecord()) {
					firstRecord();
				} else if(isLastRecord()) {
					lastRecord();
				} else {
					previousRecord();
				}
			}
		} catch(Exception e) {
			errorMsg = e.getMessage();
		}
		//	Default Return
		return errorMsg;
	} // cancelInventory
	
	/** 
	 * Delete one inventory line
	 * To erase one line from inventory
	 * 
	 */
	public void deleteLine (int inventoryLineId) {
		if ( inventoryLineId != -1 && currentInventory != null ) {
			for ( MInventoryLine line : currentInventory.getLines(true) ) {
				if ( line.getM_InventoryLine_ID() == inventoryLineId ) {
					line.deleteEx(true);	
				}
			}
		}
	} //	deleteLine

	/**
	 * Get Data List Order
	 */
	public void listOrder() {
		String sql = new String("SELECT inv.M_Inventory_ID "
					+ "FROM M_Inventory inv "
					+ "WHERE inv.Processed = 'N' "
					+ "AND inv.AD_Client_ID = ? "
					+ "AND inv.C_POS_ID = ? "
					+ "ORDER BY inv.Updated");
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		inventoryList = new ArrayList<Integer>();
		try {
			//	Set Parameter
			preparedStatement= DB.prepareStatement(sql, null);
			preparedStatement.setInt (1, Env.getAD_Client_ID(Env.getCtx()));
			preparedStatement.setInt (2, getC_POS_ID());
			//	Execute
			resultSet = preparedStatement.executeQuery();
			//	Add to List
			while(resultSet.next()){
				inventoryList.add(resultSet.getInt(1));
			}
		} catch(Exception e) {
			log.severe("SubInventory.listInventory: " + e + " -> " + sql);
		} finally {
			DB.close(resultSet);
			DB.close(preparedStatement);
		}
		//	Seek Position
		if(hasRecord())
			recordPosition = inventoryList.size() -1;
		else 
			recordPosition = -1;
	}
	
	/**
	 * Verify if has record in list
	 * @return
	 * @return boolean
	 */
	public boolean hasRecord(){
		return !inventoryList.isEmpty();
	}
	
	/**
	 * Verify if is first record in list
	 * @return
	 * @return boolean
	 */
	public boolean isFirstRecord() {
		return recordPosition == 0;
	}
	
	/**
	 * Verify if is last record in list
	 * @return
	 * @return boolean
	 */
	public boolean isLastRecord() {
		return recordPosition == inventoryList.size() - 1;
	}
	
	/**
	 * Previous Record Order
	 */
	public void previousRecord() {
		if(recordPosition > 0) {
			setInventory(inventoryList.get(--recordPosition));
		}
	}

	/**
	 * Next Record Order
	 */
	public void nextRecord() {
		if(recordPosition < inventoryList.size() - 1) {
			setInventory(inventoryList.get(++recordPosition));
		}
	}
	
	/**
	 * Reload List Index
	 * @param orderId
	 * @return void
	 */
	public void reloadIndex(int orderId) {
		int position = inventoryList.indexOf(orderId);
		if(position >= 0) {
			recordPosition = position;
		}
	}
	
	/**
	 * Seek to last record
	 * @return void
	 */
	public void lastRecord() {
		recordPosition = inventoryList.size();
		if(recordPosition != 0) {
			--recordPosition;
		}
	}
	
	/**
	 * Seek to first record
	 * @return void
	 */
	public void firstRecord() {
		recordPosition = inventoryList.size();
		if(recordPosition != 0) {
			recordPosition = 0;
		}
	}
	
	/**
	 * Process Inventory
	 * For status "Drafted" or "In Progress": process order
	 * For status "Completed": do nothing as it can be pre payment or payment on credit
	 * @param trxName
	 * @return true if inventory processed or pre payment/on credit; otherwise false
	 * 
	 */
	public boolean processInventory(String trxName) {
		//Returning inventoryCompleted to check for inventory completeness
		boolean inventoryCompleted = isCompleted();
		// check if order completed OK
		if (inventoryCompleted) {	//	Inventory already completed -> default nothing
			setIsToPrint(false);
		} else {	//	Complete Order
			//	Replace
			if(trxName == null) {
				trxName = currentInventory.get_TrxName();
			} else {
				currentInventory.set_TrxName(trxName);
			}
			// In case the Order is Invalid, set to In Progress; otherwise it will not be completed
			if (currentInventory.getDocStatus().equalsIgnoreCase(MInventory_lit.STATUS_Invalid)) 
				currentInventory.setDocStatus(MInventory_lit.STATUS_InProgress);
			//	Set Document Action
			currentInventory.setDocAction(DocAction.ACTION_Complete);
			if (currentInventory.processIt(DocAction.ACTION_Complete)) {
				currentInventory.saveEx();
				inventoryCompleted = true;
				setIsToPrint(true);
			} else {
				log.info( "Process Order FAILED " + currentInventory.getProcessMsg());
				currentInventory.saveEx();
				return inventoryCompleted;
			}
		}

		return inventoryCompleted;
	}	// processInventory
					
	/**
	 * Get Process Message
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return String
	 */
	public String getProcessMsg() {
		return currentInventory.getProcessMsg();
	}
			
	/**
	 * Get Document No
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return String
	 */
	public String getDocumentNo() {
		return currentInventory.getDocumentNo();
	}

//	/**
//	 * get Default Delivery Rule from POS Terminal
//	 * @return
//     */
//	public String getDeliveryRule()
//	{
//		return entityPOS.getDeliveryRule();
//	}
//
//	/**
//	 * get default invoice rule from POS Terminal
//	 * @return
//     */
//	public String getInvoiceRule()
//	{
//		return entityPOS.getInvoiceRule();
//	}
		
	/**
	 * 	Load Order
	 */
	public void reloadInventory() {
		if (currentInventory == null) {
			
			if(recordPosition != -1
					&& recordPosition < inventoryList.size()) {
				setInventory(inventoryList.get(recordPosition));
			}
			//	
			return;
		}
		currentInventory.load(currentInventory.get_TrxName());
		currentInventory.getLines(true);
	}
	
	/**
	 * Get Warehouse Identifier
	 * @return
	 * @return int
	 */
	public int getM_Warehouse_ID() {
		return entityPOS.getM_Warehouse_ID();
	}
	
	/**
	 * Valid Locator
	 * @return
	 * @return String
	 */
	public void validLocator() {
		MWarehouse warehouse = MWarehouse.get(ctx, getM_Warehouse_ID());
		MLocator[] locators = warehouse.getLocators(true);
		for (MLocator mLocator : locators) {
			if (mLocator.isDefault()) {
				return ;
			}
		}

		throw new AdempierePOSException("@M_Locator_ID@ @default@ "
				+ "@not.found@ @M_Warehouse_ID@: " 
				+ warehouse.getName());
	}
	
	/**
	 * Get Warehouse Name
	 * @return
	 * @return String
	 */
	public String getWarehouseName() {
		if(getM_Warehouse_ID() > 0) {
			MWarehouse.get(ctx, getM_Warehouse_ID()).getName();
		}
		//	Default
		return "";
	}
	
	/**
	 * Get Document Type Name
	 * @return
	 * @return String
	 */
	public String getDocumentTypeName() {
		if(hasInventory()) {
			MDocType m_DocType = MDocType.get(getCtx(), currentInventory.getC_DocType_ID());
			if(m_DocType != null) {
				return m_DocType.getName();
			}
		}
		//	Default None
		return "";
	}
	
	/**
	 * Get Date Movement
	 * @return
	 */
	public Timestamp getMovementDate() {
		if(hasInventory()) {
			return currentInventory.getMovementDate();
		}
		//	Default
		return null;
	}
	
	/**
	 * Get Context
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> Aug 31, 2015, 8:23:54 PM
	 * @return
	 * @return Properties
	 */
	public Properties getCtx() {
		return ctx;
	}
	
	/**
	 * Get POS Key Layout Identifier
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return int
	 */
	public int getOSKeyLayout_ID() {
		if(entityPOS != null) {
			return entityPOS.getOSK_KeyLayout_ID();
		}
		//	Default Return
		return 0;
	}
	
	/**
	 * Get Key Layout
	 * @return
	 * @return int
	 */
	public int getC_POSKeyLayout_ID() {
		if(entityPOS != null) {
			return entityPOS.getC_POSKeyLayout_ID();
		}
		//	Default Return
		return 0;
	}
		
	/**
	 * Get Order Identifier
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return int
	 */
	public int getM_Inventory_ID() {
		int m_M_Inventory_ID = 0;
		if(hasInventory()) {
			m_M_Inventory_ID = currentInventory.getM_Inventory_ID();
		}
		//	Default
		return m_M_Inventory_ID;
	}
	
	/**
	 * Save Current Next Sequence
	 * @param trxName
	 * @return void
	 */
	public void saveNextSeq(String trxName){
		int next = documentSequence.getCurrentNext() + documentSequence.getIncrementNo();
		documentSequence.setCurrentNext(next);
		documentSequence.saveEx(trxName);
	}
	
	/**
	 * Get Sequence Document
	 * @param trxName
	 * @return String
	 */
	public String getSequenceDoc(String trxName){
		documentSequence = new MSequence(Env.getCtx(), getAD_Sequence_ID() , trxName);
		return documentSequence.getPrefix() + documentSequence.getCurrentNext();
	}
	
	/**
	 * Get Quantity of Product
	 * @return quantity
	 */
	public BigDecimal getQty() {
		return qtyInternalUse;
	}

	/**
	 * Set Quantity of Product
	 * @param qty
	 */
	public void setQty(BigDecimal qty) {
		this.qtyInternalUse = qty;
	}
	
	/**
	 * Get Quantity of Product
	 * @return quantity
	 */
	public BigDecimal getQtyAdded() {
		BigDecimal quantityAddedReturn = quantityAdded;
		quantityAdded = Env.ZERO;
		setAddQty(false);
		return quantityAddedReturn;
	}

	/**
	 * Set Quantity of Product
	 * @param qtyAdded
	 */
	public void setQtyAdded(BigDecimal qtyAdded) {
		this.quantityAdded = qtyAdded;
	}
	
	/**
	 * 
	 * @return
	 * @return String
	 */
	public String getElectronicScales()
	{
		if (entityPOS != null)
			return entityPOS.getElectronicScales();
		return null;
	}

	public String getMeasureRequestCode()
	{
		if (entityPOS != null)
			return entityPOS.getMeasureRequestCode();
		return null;
	}

	public boolean isPresentElectronicScales()
	{
		if (getElectronicScales() != null)
			if (getElectronicScales().length() > 0)
				return true;
			else
				return false;
		else
			return false;
	}

	public boolean IsShowLineControl() {
		return true;
	}

	/**
	 * get if POS using a virtual Keyboard
	 * @return
	 */
	public boolean isVirtualKeyboard()
	{
		if (getOSKeyLayout_ID() > 0)
			return true;

		return false;
	}

	/**
	 * Get Product Image
	 * Right now, it is only possible a two-stage lookup.
	 * A general lookup has to be implemented, where more than 2 stages are considered.
	 * @param productId
	 * @param posKeyLayoutId
	 * @return int
	 */
	public int getProductImageId(int productId , int posKeyLayoutId) {
		int imageId = 0;

		//	Valid Product
		if(productId == 0)
			return imageId;

		//	Get POS Key
		int m_C_POSKey_ID = DB.getSQLValue(null, "SELECT pk.C_POSKey_ID "
				+ "FROM C_POSKey pk "
				+ "WHERE pk.C_POSKeyLayout_ID = ? "
				+ "AND pk.M_Product_ID = ? "
				+ "AND pk.IsActive = 'Y'", posKeyLayoutId , productId);
		
		if(m_C_POSKey_ID > 0) {
			//	Valid POS Key
			MPOSKey key =  new MPOSKey(ctx, m_C_POSKey_ID, null);
			imageId = key.getAD_Image_ID();
		}
		else  {
			//	No record has been found for a product in the current Key Layout. Try it in the Subkey Layout.
			m_C_POSKey_ID = DB.getSQLValue(null, "SELECT pk2.C_POSKey_ID "
					+ "FROM C_POSKey pk1 "
					+ "INNER JOIN C_POSKey pk2 ON pk1.subkeylayout_id=pk2.c_poskeylayout_id AND pk1.subkeylayout_id IS NOT NULL "
					+ "WHERE pk2.M_Product_ID = ? "
					+ "AND pk1.IsActive = 'Y' AND pk2.IsActive = 'Y'", productId);
			//	Valid POS Key
			if(m_C_POSKey_ID > 0) {
				MPOSKey key =  new MPOSKey(ctx, m_C_POSKey_ID, null);
				imageId = key.getAD_Image_ID();
			}
		}
		return imageId;
	}

	/**
	 * Get Product ID from Inventory Line ID
	 * @param inventoryLineId
	 * @return
	 * @return int
	 */
	public int getM_Product_ID(int inventoryLineId) {
		return DB.getSQLValue(null, "SELECT il.M_Product_ID "
				+ "FROM M_InventoryLine il "
				+ "WHERE il.M_InventoryLine_ID = ?", inventoryLineId);
	}

	/**
	 * Validate User PIN
	 * @param userPin
     */
	public boolean isValidUserPin(char[] userPin) {
		if(userPin==null || userPin.length==0)
			return false;
		MUser user = MUser.get(getCtx() ,getAD_User_ID());
		Optional<I_AD_User> optionalSuperVisor = Optional.of(user.getSupervisor());
		I_AD_User superVisor = optionalSuperVisor.orElseThrow(() -> new AdempierePOSException("@Supervisor@ @NotFound@"));
		Optional<String> superVisorName = Optional.ofNullable(superVisor.getName());
		if (superVisor.getUserPIN() == null || superVisor.getUserPIN().isEmpty())
			throw new AdempierePOSException("@Supervisor@ \"" + superVisorName.orElse("") + "\": @UserPIN@ @NotFound@");

		char[] correctPassword = superVisor.getUserPIN().toCharArray();
		boolean isCorrect = true;
		if (userPin.length != correctPassword.length) {
			isCorrect = false;
		} else {
			for (int i = 0; i < userPin.length; i++) {
				if (userPin[i] != correctPassword[i]) {
					isCorrect = false;
				}
			}
		}
		//Zero out the password.
		for (int i = 0; i < correctPassword.length; i++) {
			correctPassword[i] = 0;
		}

		return isCorrect;
	}

	/**
	 * Get Product Value from ID
	 * @param productId
	 * @return
	 * @return String
	 */
	public String getProductValue(int productId) {
		return DB.getSQLValueString(null, "SELECT Value FROM M_Product WHERE M_Product_ID = ? " , productId);
	}

	/**
	 * Get Product Name from ID
	 * @param productId
	 * @return
	 * @return String
	 */
	public String getProductName(int productId) {
		return DB.getSQLValueString(null, "SELECT name FROM M_Product WHERE M_Product_ID = ? " , productId);
	}

	/**
	 * Get Query for Product
	 * @param productCode
	 * @param warehouseId
	 * @param priceListId
	 * @param partnerId
	 * @return
	 * @return List<Vector<Object>>
	 */
	public static List<Vector<Object>> getQueryProduct(String productCode, int warehouseId) {
		ArrayList<Vector<Object>> rows = new ArrayList<>();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT M_Product_ID , p.Value, p.Name,")
				.append("   BomQtyAvailable(M_Product_ID, ? , 0 ) AS QtyAvailable FROM M_Product p ")
				.append(" WHERE ValidFrom <= getDate() ")
				.append("AND p.AD_Client_ID=? AND p.IsStocked=? AND p.IsSummary=? ")
				.append("AND UPPER(p.Name)  LIKE UPPER('").append("%").append(productCode.replace(" ","%")).append("%").append("')")
				.append(" OR UPPER(p.Value) LIKE UPPER('").append("%").append(productCode.replace(" ","%")).append("%").append("')")
				.append(" OR UPPER(p.UPC)   LIKE UPPER('").append("%").append(productCode.replace(" ","%")).append("%").append("')")
				.append(" OR UPPER(p.SKU)   LIKE UPPER('").append("%").append(productCode.replace(" ","%")).append("%").append("')")
				.append(" ORDER BY p.Value");
		PreparedStatement statement = null;
		try{
			statement = DB.prepareStatement(sql.toString(), null);
			int count = 1;
//			statement.setInt(count, warehouseId);
//			count ++;
			statement.setInt(count, Env.getAD_Client_ID(Env.getCtx()));
			count++;
			statement.setString(count, "Y");
			count++;
			statement.setString(count, "N");

			ResultSet resultSet = statement.executeQuery();

			while (resultSet.next()) {
				Vector<Object> columns = new Vector<>();
				Integer productId = resultSet.getInt(1);
				String  productValue = resultSet.getString(2).trim();
				String  productName = resultSet.getString(3).trim();
				String  qtyAvailable = resultSet.getBigDecimal(4) != null ? resultSet.getBigDecimal(4).toString().trim() : "0";
				columns.add(productId);
				columns.add(productValue);
				columns.add(productName);
				columns.add(qtyAvailable);
				rows.add(columns);
			}

			DB.close(resultSet,statement);
			statement = null;
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
		return rows;
	}

//	/**
//	 * 	Print Ticket
//	 *
//	 */
//	public void printTicket() {
//		if (!hasInventory())
//			return;
//		else if(hasInventory() && !isCompleted())
//		{
//			FDialog.info(getWindowNo(), null, "No Order Completed");
//			return;
//		}
//		//	Print
//		POSTicketHandler ticketHandler = POSTicketHandler.getTicketHandler(this);
//		if(ticketHandler == null)
//			return;
//		//	
//		ticketHandler.printTicket();
//	}

	/**
	 * Set if the quantity is set or add to it
	 * @param isAddQty
	 * @return void
	 */
	public void setAddQty(boolean isAddQty) {
		this.isAddQty = isAddQty;
	}

	/**
	 * Verify if set Quatity or Add
	 * @return
	 * @return boolean
	 */
	public boolean isAddQty() {
		return isAddQty;
	}

	/**
	 * Get Inventory Line ID
	 * @return
	 * @return int
	 */
	public int getInventoryLineId() {
		return inventoryLineId;
	}

	/**
	 * Set Inventory Line ID
	 * @param inventoryLineId
	 * @return void
	 */
	public void setInventoryLineId(int inventoryLineId) {
		this.inventoryLineId = inventoryLineId;
	}
		
	/**
	 * Get Date Ordered for view
	 * @return
	 */
	public String getDateOrderedForView() {
		return getDateFormat().format(getMovementDate());
	}
	
	/**
	 * Get Class name for ticket handler
	 * @return
	 */
	public String getTicketHandlerClassName() {
		return entityPOS.getTicketClassName();
	}
	
	public String get_TrxName() {
		if(!hasInventory())
			return null;
		//	Default
		return currentInventory.get_TrxName();
	}
}

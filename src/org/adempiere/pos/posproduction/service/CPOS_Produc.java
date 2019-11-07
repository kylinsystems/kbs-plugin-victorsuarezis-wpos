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
package org.adempiere.pos.posproduction.service;

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
import org.adempiere.model.MPPCostCollector_lit;
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
import org.eevolution.model.X_PP_Cost_Collector;
import org.idempiere.model.MPOS;

/**
 * @author Mario Calderon, mario.calderon@westfalia-it.com, Systemhaus Westfalia, http://www.westfalia-it.com
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * @author victor.perez@e-evolution.com , http://www.e-evolution.com
 */
public class CPOS_Produc {
	
	/**
	 * 
	 * *** Constructor ***
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 */
	public CPOS_Produc() {
		ctx = Env.getCtx();
		decimalFormat = DisplayType.getNumberFormat(DisplayType.Amount);
		dateFormat = DisplayType.getDateFormat(DisplayType.Date);
		today = Env.getContextAsDate(ctx, "#Date");
	}
	
	/**	POS Configuration		*/
	private MPOS 				entityPOS;
	/**	Current Production		*/
	private MPPCostCollector_lit currentProduction;
	/** Sequence Doc 			*/
	private MSequence 			documentSequence;
	/** Context					*/
	protected Properties 		ctx;
	/**	Today's (login) date	*/
	private Timestamp 			today;
	/**	Production List			*/
	private ArrayList<Integer>  productionList;
	/**	Order List Position		*/
	private int 				recordPosition;
	/**	Is Production Completed	*/
	private boolean 			isToPrint;
	/**	Logger					*/
	private CLogger 			log = CLogger.getCLogger(getClass());
	/**	Internal Use Qty		*/
	private BigDecimal 			qtyInternalUse = BigDecimal.ZERO;
	/**	Internal Use Qty		*/
	private BigDecimal 			quantityAdded = BigDecimal.ZERO;
	/** is new line **/
	private boolean				isAddQty = false;
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
				
				if(mpos.get_ValueAsString("LIT_POSType").equals(typePos) && mpos.getAD_User_ID()==salesRepId && mpos.get_ValueAsString("LabelPrint").contains(Env.getContext(Env.getCtx(), "##FormName_"))){
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
		if(!hasProduction()) {
			return false;
		}
		//	
		return currentProduction.isProcessed()
				&& X_PP_Cost_Collector.DOCSTATUS_Completed.equals(currentProduction.getDocStatus());
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
		if(!hasProduction()) {
			return false;
		}
		//
		return X_PP_Cost_Collector.DOCSTATUS_Closed.equals(currentProduction.getDocStatus());
	}

	/**
	 * Validate if is voided
	 * @return
	 * @return boolean
	 */
	public boolean isVoided() {
		if(!hasProduction()) {
			return false;
		}
		//	
		return X_PP_Cost_Collector.DOCSTATUS_Voided.equals(currentProduction.getDocStatus());
	}
	
	/**
	 * Validate if is drafted
	 * @return
	 * @return boolean
	 */
	public boolean isDrafted() {
		if(!hasProduction()) {
			return false;
		}
		//	
		return !isCompleted() 
				&& !isVoided() 
				&& X_PP_Cost_Collector.DOCSTATUS_Drafted.equals(currentProduction.getDocStatus());
	}
	
	/**
	 * Validate if is "In Process"}
	 * @return
	 * @return boolean
	 */
	public boolean isInProgress() {
		if(!hasProduction()) {
			return false;
		}
		//	
		return !isCompleted() 
				&& !isVoided() 
				&& X_PP_Cost_Collector.DOCSTATUS_InProgress.equals(currentProduction.getDocStatus());
	}
	
	/**
	 * Validate if is "Invalid"}
	 * @return
	 * @return boolean
	 */
	public boolean isInvalid() {
		if(!hasProduction()) {
			return false;
		}
		//	
		return !isCompleted() 
				&& !isVoided() 
				&& X_PP_Cost_Collector.DOCSTATUS_Invalid.equals(currentProduction.getDocStatus());
	}
	
	
	/**
	 * Validate if is Manufacturing Cost Collector
	 * @return
	 * @return boolean
	 */
	public boolean isManufacturingCostCollector() {
		if(!hasProduction()) {
			return false;
		}

		return MDocType.DOCBASETYPE_ManufacturingCostCollector.equals(getDocBaseTypeProduc());
	}
	
	/**
	 * Get Document Sub Type SO
	 * @return
	 * @return String
	 */
	private String getDocBaseTypeProduc() {
		//	
		MDocType docType = MDocType.get(getCtx(), getC_DocType_ID());
		if(docType != null) {
			if(docType.getDocBaseType() != null) {
				return docType.getDocBaseType();
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
		if(!hasProduction()) {
			return 0;
		}
		//	
		return currentProduction.getC_DocType_ID();
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
	public MPPCostCollector_lit getProduction() {
		return currentProduction;
	}
	
	public void resetProduction(){
		currentProduction = null;
	}
	
	/**
	 * Has Production
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return boolean
	 */
	public boolean hasProduction() {
		return currentProduction != null
				&& currentProduction.getPP_Cost_Collector_ID() != 0;
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
	public void newProduction(/*int partnerId*/) {
		log.info( "PosPanel.newProduction");
		currentProduction = null;
		int docTypeId = entityPOS.getC_DocType_ID();
		//	Create Production
		createProduction(docTypeId);
		//	
//		reloadProduction();
	}	//	newProduction
	
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
		currentProduction.setC_DocType_ID(docTypeTargetId);
		//	Set Sequenced No
		String value = DB.getDocumentNo(getC_DocType_ID(), null, false, currentProduction);
		if (value != null) {
			currentProduction.setDocumentNo(value);
		}
		currentProduction.saveEx();
	}
	
	
	/**
	 * Get/create Activity Control Report
	 *	@param docTypeTargetId ID of document type
	 */
	private void createProduction(int docTypeTargetId) {
		int productionId = getFreePP_Cost_Collector_ID();
		//	Change Values for new Inventory
		if(productionId > 0) {
			currentProduction = new MPPCostCollector_lit(Env.getCtx(), productionId, null);
			currentProduction.setMovementDate(getToday());
			
		} else {
			currentProduction = new MPPCostCollector_lit(Env.getCtx(), 0, null);
		}
		currentProduction.setAD_Org_ID(entityPOS.getAD_Org_ID());
		currentProduction.getWrapper().setC_POS_ID(entityPOS.getC_POS_ID());
		currentProduction.setM_Warehouse_ID(entityPOS.getM_Warehouse_ID());
		if (docTypeTargetId != 0) {
			currentProduction.setC_DocType_ID(docTypeTargetId);
		} else {
			currentProduction.setC_DocType_ID(MDocType.getDocType(MDocType.DOCBASETYPE_ManufacturingCostCollector));
		}
//		currentProduction.saveEx();
		//	Add if is new
		if(productionId < 0) {
			//	Add To List
			productionList.add(currentProduction.getPP_Cost_Collector_ID());
		}
		//  Add record
		reloadIndex(currentProduction.getPP_Cost_Collector_ID());
	} // PosProductionModel
	
	/**
	 * Find a free production and reuse
	 * @return
	 * @return int
	 */
	private int getFreePP_Cost_Collector_ID() {
		return DB.getSQLValue(null, "SELECT pp.PP_Cost_Collector_ID "
				+ "FROM PP_Cost_Collector pp "
				+ "WHERE pp.DocStatus = 'DR' "
				+ "AND pp.C_POS_ID = ? "
				+ "ORDER BY pp.Updated", 
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
	 * @param productionId
	 */
	public void setProduction(int productionId) {
		currentProduction = new MPPCostCollector_lit(ctx, productionId, null);
		//	
		reloadProduction();
	}
	
	/**
	 * 	Call Production void process 
	 *  Only if Production is "Drafted", "In Progress" or "Completed"
	 * 
	 *  @return true if Production voided; false otherwise
	 */
	private boolean voidProduction() {
		if (!(currentProduction.getDocStatus().equals(MPPCostCollector_lit.STATUS_Drafted)
				|| currentProduction.getDocStatus().equals(DocAction.STATUS_InProgress)
				|| currentProduction.getDocStatus().equals(DocAction.STATUS_Completed)))
			return false;
		
		// Standard way of voiding an order
		currentProduction.setDocAction(MPPCostCollector_lit.DOCACTION_Void);
		if (currentProduction.processIt(MPPCostCollector_lit.DOCACTION_Void) ) {
			currentProduction.setDocAction(MPPCostCollector_lit.DOCACTION_None);
			currentProduction.setDocStatus(MPPCostCollector_lit.STATUS_Voided);
			currentProduction.saveEx();
			return true;
		} else {
			return false;
		}
	} // 
	
	/**
	 * Execute deleting an Production
	 * If the Production is in drafted status -> ask to delete it
	 * If the Production is in completed status -> ask to void it it
	 * Otherwise, it must be done outside this class.
	 */
	public String cancelProduction() {
		String errorMsg = null;
		try {
			//	Get Index
			int currentIndex = productionList.indexOf(currentProduction.getPP_Cost_Collector_ID());
			if (!hasProduction()) {
				throw new AdempierePOSException("@POS.MustCreateOrder@");
			} else if (!isCompleted()) {
				//	Delete Production
				currentProduction.deleteEx(true);
			} else if (isCompleted()) {	
				voidProduction();
			} else {
				throw new AdempierePOSException("@POS.ProdutionIsNotProcessed@");
			}
			//	Remove from List
			if(currentIndex >= 0) {
				productionList.remove(currentIndex);
			}
			//	
			currentProduction = null;
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
	 * Get Data List Production
	 */
	public void listProduction() {
		String sql = new String("SELECT pp.PP_Cost_Collector_ID "
					+ "FROM PP_Cost_Collector pp "
					+ "WHERE pp.Processed = 'N' "
					+ "AND pp.AD_Client_ID = ? "
					+ "AND pp.C_POS_ID = ? "
					+ "ORDER BY pp.Updated");
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		productionList = new ArrayList<Integer>();
		try {
			//	Set Parameter
			preparedStatement= DB.prepareStatement(sql, null);
			preparedStatement.setInt (1, Env.getAD_Client_ID(Env.getCtx()));
			preparedStatement.setInt (2, getC_POS_ID());
			//	Execute
			resultSet = preparedStatement.executeQuery();
			//	Add to List
			while(resultSet.next()){
				productionList.add(resultSet.getInt(1));
			}
		} catch(Exception e) {
			log.severe("SubProduction.listProduction: " + e + " -> " + sql);
		} finally {
			DB.close(resultSet);
			DB.close(preparedStatement);
		}
		//	Seek Position
		if(hasRecord())
			recordPosition = productionList.size() -1;
		else 
			recordPosition = -1;
	}
	
	/**
	 * Verify if has record in list
	 * @return
	 * @return boolean
	 */
	public boolean hasRecord(){
		return !productionList.isEmpty();
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
		return recordPosition == productionList.size() - 1;
	}
	
	/**
	 * Previous Record Production
	 */
	public void previousRecord() {
		if(recordPosition > 0) {
			setProduction(productionList.get(--recordPosition));
		}
	}

	/**
	 * Next Record Production
	 */
	public void nextRecord() {
		if(recordPosition < productionList.size() - 1) {
			setProduction(productionList.get(++recordPosition));
		}
	}
	
	/**
	 * Reload List Index
	 * @param productionId
	 * @return void
	 */
	public void reloadIndex(int productionId) {
		int position = productionList.indexOf(productionId);
		if(position >= 0) {
			recordPosition = position;
		}
	}
	
	/**
	 * Seek to last record
	 * @return void
	 */
	public void lastRecord() {
		recordPosition = productionList.size();
		if(recordPosition != 0) {
			--recordPosition;
		}
	}
	
	/**
	 * Seek to first record
	 * @return void
	 */
	public void firstRecord() {
		recordPosition = productionList.size();
		if(recordPosition != 0) {
			recordPosition = 0;
		}
	}
	
	/**
	 * Process Production
	 * For status "Drafted" or "In Progress": process order
	 * For status "Completed": do nothing as it can be pre payment or payment on credit
	 * @param trxName
	 * @return true if Production processed or pre payment/on credit; otherwise false
	 * 
	 */
	public boolean processProduction(String trxName) {
		//Returning productionCompleted to check for production completeness
		boolean productionCompleted = isCompleted();
		// check if order completed OK
		if (productionCompleted) {	//	Production already completed -> default nothing
			setIsToPrint(false);
		} else {	//	Complete Production
			//	Replace
			if(trxName == null) {
				trxName = currentProduction.get_TrxName();
			} else {
				currentProduction.set_TrxName(trxName);
			}
			// In case the Order is Invalid, set to In Progress; otherwise it will not be completed
			if (currentProduction.getDocStatus().equalsIgnoreCase(MPPCostCollector_lit.STATUS_Invalid)) 
				currentProduction.setDocStatus(MPPCostCollector_lit.STATUS_InProgress);
			//	Set Document Action
			currentProduction.setDocAction(DocAction.ACTION_Complete);
			if (currentProduction.processIt(DocAction.ACTION_Complete)) {
				currentProduction.saveEx();
				productionCompleted = true;
				setIsToPrint(true);
			} else {
				log.info( "Process Production FAILED " + currentProduction.getProcessMsg());
				currentProduction.saveEx();
				return productionCompleted;
			}
		}

		return productionCompleted;
	}	// processInventory
					
	/**
	 * Get Process Message
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return String
	 */
	public String getProcessMsg() {
		return currentProduction.getProcessMsg();
	}
			
	/**
	 * Get Document No
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @return
	 * @return String
	 */
	public String getDocumentNo() {
		return currentProduction.getDocumentNo();
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
	public void reloadProduction() {
		if (currentProduction == null) {
			
			if(recordPosition != -1
					&& recordPosition < productionList.size()) {
				setProduction(productionList.get(recordPosition));
			}
			//	
			return;
		}
		currentProduction.load(currentProduction.get_TrxName());
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
		if(hasProduction() || currentProduction !=null) {
			MDocType m_DocType = MDocType.get(getCtx(), currentProduction.getC_DocTypeTarget_ID());
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
		if(hasProduction() || currentProduction !=null) {
			return currentProduction.getMovementDate();
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
	public int getPP_Cost_Collector_ID() {
		int m_PP_Cost_Collector_ID = 0;
		if(hasProduction()) {
			m_PP_Cost_Collector_ID = currentProduction.getPP_Cost_Collector_ID();
		}
		//	Default
		return m_PP_Cost_Collector_ID;
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
	 * Get Product ID from Production Line ID
	 * @param inventoryLineId
	 * @return
	 * @return int
	 */
	public int getM_Product_ID(int productionId) {
		return DB.getSQLValue(null, "SELECT pp.M_Product_ID "
				+ "FROM PP_Cost_Collector pp "
				+ "WHERE pp.PP_Cost_Collector_ID = ?", productionId);
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
		if(!hasProduction())
			return null;
		//	Default
		return currentProduction.get_TrxName();
	}
}

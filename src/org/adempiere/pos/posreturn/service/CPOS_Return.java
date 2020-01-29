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
package org.adempiere.pos.posreturn.service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Vector;

import org.adempiere.pos.AdempierePOSException;
import org.adempiere.webui.apps.WProcessCtl;
import org.compiere.model.I_AD_User;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MCurrency;
import org.compiere.model.MDocType;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MLocator;
import org.compiere.model.MOrder;
import org.compiere.model.MOrgInfo;
import org.compiere.model.MPOSKey;
import org.compiere.model.MProcess;
import org.compiere.model.MProduct;
import org.compiere.model.MQuery;
import org.compiere.model.MSequence;
import org.compiere.model.MTab;
import org.compiere.model.MTable;
import org.compiere.model.MUser;
import org.compiere.model.MWarehouse;
import org.compiere.model.MWindow;
import org.compiere.model.PrintInfo;
import org.compiere.model.X_M_InOut;
import org.compiere.print.MPrintFormat;
import org.compiere.print.ReportCtl;
import org.compiere.print.ReportEngine;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfo;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.idempiere.model.MPOS;

/**
 * 
 */
public class CPOS_Return {
	
	
	public CPOS_Return() {
		ctx = Env.getCtx();
		decimalFormat = DisplayType.getNumberFormat(DisplayType.Amount);
		dateFormat = DisplayType.getDateFormat(DisplayType.Date);
		today = Env.getContextAsDate(ctx, "#Date");
	}
	
	/**	POS Configuration		*/
	private MPOS 				entityPOS;
	/**	Current Return			*/
	private MInOut				currentReturn;
	/** Sequence Doc 			*/
	private MSequence 			documentSequence;
	/**	The Business Partner	*/
	private MBPartner 			partner;
	/**	Price List Version		*/
	private int 				priceListVersionId;
	/**	Price List Id		*/
	private int 				priceListId;
	/** Context					*/
	protected Properties 		ctx;
	/**	Today's (login) date	*/
	private Timestamp 			today;
	/**	Movement List			*/
	private ArrayList<Integer>  returnList;
	/**	Order List Position		*/
	private int 				recordPosition;
	/**	Is Payment Completed	*/
	private boolean 			isToPrint;
	/**	Logger					*/
	private CLogger 			log = CLogger.getCLogger(getClass());
	/**	Quantity Ordered		*/
	private BigDecimal 			quantity = BigDecimal.ZERO;
	/**	Quantity Ordered		*/
	private BigDecimal 			quantityAdded = BigDecimal.ZERO;
	/** is new line **/
	private boolean				isAddQty = false;
	/** MovementLine id **/
	private int 				returnLineId = 0;
	/**	Format					*/
	private DecimalFormat 		decimalFormat;
	/**	Date Format				*/
	private SimpleDateFormat	dateFormat;
	/**	Window No				*/
	private int 				windowNo;
	
	private String             p_nameOfForm;
	
	/**
	 * 	Set MPOS
	 * @param salesRepId
	 * @return true if found/set
	 */
	public void setPOS(int userId, String typePos) {
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
				
				if(mpos.get_ValueAsString("LIT_POSType").equals(typePos) && mpos.getAD_User_ID()==userId && mpos.get_ValueAsString("LabelPrint").contains(Env.getContext(Env.getCtx(), "##FormName_"))){
					entityPOS = mpos;
					break;
				}
			}
			
			if(entityPOS == null)
				throw new AdempierePOSException("@NoPOSForUser@");
			
		}
	}	//	setMPOS
	
	/**
	 * Set POS
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
		if(!hasReturn()) {
			return false;
		}
		//	
		return currentReturn.isProcessed()
				&& X_M_InOut.DOCSTATUS_Completed.equals(currentReturn.getDocStatus());
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
		if(!hasReturn()) {
			return false;
		}
		//
		return X_M_InOut.DOCSTATUS_Closed.equals(currentReturn.getDocStatus());
	}

	/**
	 * Validate if is voided
	 * @return
	 * @return boolean
	 */
	public boolean isVoided() {
		if(!hasReturn()) {
			return false;
		}
		//	
		return X_M_InOut.DOCSTATUS_Voided.equals(currentReturn.getDocStatus());
	}
	
	/**
	 * Validate if is drafted
	 * @return
	 * @return boolean
	 */
	public boolean isDrafted() {
		if(!hasReturn()) {
			return false;
		}
		//	
		return !isCompleted() 
				&& !isVoided() 
				&& X_M_InOut.DOCSTATUS_Drafted.equals(currentReturn.getDocStatus());
	}
	
	/**
	 * Validate if is "In Process"}
	 * @return
	 * @return boolean
	 */
	public boolean isInProgress() {
		if(!hasReturn()) {
			return false;
		}
		//	
		return !isCompleted() 
				&& !isVoided() 
				&& X_M_InOut.DOCSTATUS_InProgress.equals(currentReturn.getDocStatus());
	}
	
	/**
	 * Validate if is "Invalid"}
	 * @return
	 * @return boolean
	 */
	public boolean isInvalid() {
		if(!hasReturn()) {
			return false;
		}
		//	
		return !isCompleted() 
				&& !isVoided() 
				&& X_M_InOut.DOCSTATUS_Invalid.equals(currentReturn.getDocStatus());
	}
	
	/**
	 * Validate if has lines
	 * @return
	 * @return boolean
	 */
	public boolean hasLines() {
		if(!hasReturn()) {
			return false;
		}
		//	
		return currentReturn.getLines(true).length > 0;
	}
	
	/**
	 * Validate if is POS Return
	 * @return
	 * @return boolean
	 */
	public boolean isPOSReturn() {
		if(!hasReturn()) {
			return false;
		}
		//	
		return MDocType.DOCBASETYPE_MaterialReceipt.equals(getDocSubTypeSO());
	}
	
	
	/**
	 * Validate if is In Transit
	 * @return
	 * @return boolean
	 */
	public boolean isInTransit() {
		if(!hasReturn()) {
			return false;
		}
		//	
		return currentReturn.isInTransit();
	}
	
	/**
	 * Get Document Sub Type SO
	 * @return
	 * @return String
	 */
	private String getDocSubTypeSO() {
		//	
		MDocType docType = MDocType.get(getCtx(), getC_DocType_ID());
		if(docType != null) {
			if(docType.getDocSubTypeSO() != null) {
				return docType.getDocSubTypeSO();
			}
		}
		//	
		return "";
	}
	
	/**
	 * Get Document Type from Return
	 * @return
	 * @return int
	 */
	public int getC_DocType_ID() {
		if(!hasReturn()) {
			return 0;
		}
		//	
		return currentReturn.getC_DocType_ID();
		
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
	 * Get Current Return
	 * @return
	 * @return MInOut
	 */
	public MInOut getReturn() {
		return currentReturn;
	}
	
	/**
	 * Has Return
	 * @return
	 * @return boolean
	 */
	public boolean hasReturn() {
		return currentReturn != null
				&& currentReturn.getM_InOut_ID() != 0;
	}
	
	/**
	 * Has Business Partner
	 * @return
	 * @return boolean
	 */
	public boolean hasBPartner() {
		return partner != null;
	}
	
	/**
	 * Compare BP Name
	 * @param name
	 * @return
	 * @return boolean
	 */
	public boolean compareBPName(String name) {
		return partner.getName().equals(name);
	}
	
	/**
	 * 	Get BPartner
	 *	@return C_BPartner_ID
	 */
	public int getC_BPartner_ID () {
		if (hasBPartner())
			return partner.getC_BPartner_ID();
		return 0;
	}	//	getC_BPartner_ID

	/**
	 * Get Bank Account Id
	 * @return
     */
	public int getC_BankAccount_ID()
	{
		return entityPOS.getC_BankAccount_ID();
	}
	
	
	/**
	 * Get Business Partner Name
	 * @return
	 * @return String
	 */
	public String getBPName() {
		if (hasBPartner())
			return partner.getName();
		return null;
	}
	
	/**
	 * Get Currency Identifier
	 * @return
	 * @return int
	 */
	public int getC_Currency_ID() {
		if (hasBPartner()
				&& currentReturn != null) {
			return currentReturn.getC_Currency_ID();
		}
		//	Default
		return 0;
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
	 * @return
	 * @return String
	 */
	public String getSalesRepName() {
		MUser salesRep = null;
		if(hasReturn())
			return MUser.getNameOfUser(getSalesRep_ID());
		else{
			if(entityPOS.getSalesRep_ID()>0)
				return MUser.getNameOfUser(entityPOS.getSalesRep_ID());
			else{
			
				salesRep = MUser.get(ctx); 
				if(salesRep == null) {
					return null;
				}
				//	Default Return
				return salesRep.getName();
			}
		}
	}
	
	/**
	 * Get Sales Representative
	 * @return
	 * @return int
	 */
	public int getSalesRep_ID() {
		if(hasReturn()){
			return currentReturn.getSalesRep_ID();
		}
		return entityPOS.getSalesRep_ID();
	}
	
	/**
	 * Get POS Configuration
	 * @return
	 * @return MPOS
	 */
	public MPOS getM_POS() {
		return entityPOS;
	}
	
	/**
	 * Get POS Name
	 * @return
	 * @return String
	 */
	public String getPOSName() {
		return entityPOS.getName();
	}
	
	/**
	 * Get POS Identifier
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
		if(entityPOS!=null)
			return entityPOS.isEnableProductLookup();
		else
			return false;
	}

	/**
	 * Is POS Required PIN
	 * @return
     */
	public boolean isRequiredPIN(){
		return entityPOS.isPOSRequiredPIN();
	}

	/**
	 * 	New Return
	 *  @param partnerId
	 */
	public void newReturn(int partnerId) {
		log.info( "PosPanel.newReturn");
		currentReturn = null;
		int docTypeId = entityPOS.getC_DocType_ID();
		//	Create Return
		createReturn(partnerId, docTypeId);
		//	
		reloadReturn();
	}	//	newReturn
	
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
		currentReturn.setC_DocType_ID(docTypeTargetId);
		//	Set Sequenced No
		String value = DB.getDocumentNo(getC_DocType_ID(), null, false, currentReturn);
		if (value != null) {
			currentReturn.setDocumentNo(value);
		}
		
		currentReturn.saveEx();
	}
	
	
	/**
	 * Get/create Return
	 *	@param partnerId Business Partner
	 *	@param docTypeTargetId ID of document type
	 */
	private void createReturn(int partnerId, int docTypeTargetId) {
		int returnId = getFreeM_InOut_ID();
		//	Change Values for new Movement
		if(returnId > 0) {
			currentReturn = new MInOut(Env.getCtx(), returnId, null);
			//currentReturn.setDateReceived(getToday());
			currentReturn.setMovementDate(getToday());
			//currentReturn.setDatePromised(getToday());
		} else {
			currentReturn = new MInOut(Env.getCtx(), 0, null);
			Timestamp dateMovement = currentReturn.getMovementDate();
			LocalDateTime dd = dateMovement.toLocalDateTime().plusDays(1);
			dateMovement = Timestamp.valueOf(dd);
			currentReturn.setMovementDate(dateMovement);
		}
		currentReturn.setAD_Org_ID(entityPOS.getAD_Org_ID());
		
		currentReturn.set_ValueNoCheck("C_POS_ID",entityPOS.getC_POS_ID());
//		if (entityPOS.getDeliveryRule() != null)
//			currentDDT.setDeliveryRule(getDeliveryRule());
		
		currentReturn.set_ValueNoCheck("M_Warehouse_ID",entityPOS.getM_Warehouse_ID()); 
		currentReturn.setC_DocType_ID(docTypeTargetId);
		String movementTypeShipment = null;
		MDocType dtShipment = new MDocType(Env.getCtx(), docTypeTargetId, null); 
		if (dtShipment.getDocBaseType().equals(MDocType.DOCBASETYPE_MaterialDelivery)) 
			movementTypeShipment = dtShipment.isSOTrx() ? MInOut.MOVEMENTTYPE_CustomerShipment : MInOut.MOVEMENTTYPE_VendorReturns; 
		else if (dtShipment.getDocBaseType().equals(MDocType.DOCBASETYPE_MaterialReceipt)) 
			movementTypeShipment = dtShipment.isSOTrx() ? MInOut.MOVEMENTTYPE_CustomerReturns : MInOut.MOVEMENTTYPE_VendorReceipts;  
		currentReturn.setMovementType (movementTypeShipment);
		currentReturn.setIsSOTrx(dtShipment.isSOTrx());
		
		//	Set BPartner
		configureBPartner(partnerId);
		//	Add if is new
		if(returnId < 0) {
			//	Add To List
			returnList.add(currentReturn.getM_InOut_ID());
		}
		//  Add record
		reloadIndex(currentReturn.getM_InOut_ID());
	} // PosReturnModel
	
	/**
	 * Find a free return and reuse
	 * @return
	 * @return int
	 */
	private int getFreeM_InOut_ID() {
		return DB.getSQLValue(null, "SELECT m.M_InOut_ID "
				+ "FROM M_InOut m "
				+ "WHERE m.DocStatus = 'DR' "
				+ "AND m.C_POS_ID = ? "
				+ "AND m.SalesRep_ID = ? "
				+ "AND NOT EXISTS(SELECT 1 "
				+ "					FROM M_InOutLine ml "
				+ "					WHERE ml.M_InOut_ID = m.M_InOut_ID) "
				+ "ORDER BY m.Updated", 
				getC_POS_ID(), getSalesRep_ID());
	}
	
	/**
	 * Is BPartner Standard 
	 * @return boolean
	 */
	public boolean isBPartnerStandard() {
		int partnerId = currentReturn != null ? currentReturn.getC_BPartner_ID() : 0 ;
		if(entityPOS.getC_BPartnerCashTrx_ID() == partnerId)
			return true;
		else
			return false;
	}
	
	/**
	 * 	Set BPartner, update price list and locations
	 *  Configuration of Business Partner has priority over POS configuration
	 *	@param p_C_BPartner_ID id
	 */
	
	/**
	 * set BPartner and save
	 */
	public void configureBPartner(int partnerId) {
		//	Valid if has a Order
		if(isCompleted()
				|| isVoided())
			return;
		log.fine( "CPOS_Return.setC_BPartner_ID=" + partnerId);
		boolean isSamePOSPartner = false;
		//	Validate BPartner
		if (partnerId == 0) {
			isSamePOSPartner = true;
			partnerId = entityPOS.getC_BPartnerCashTrx_ID();
		}
		//	Get BPartner
		partner = MBPartner.get(ctx, partnerId);
		if (partner == null || partner.get_ID() == 0) {
			throw new AdempierePOSException("POS.NoBPartnerForOrder");
		} else {
			log.info("CPOS_Return.SetC_BPartner_ID -" + partner);
			currentReturn.setC_BPartner_ID(partner.getC_BPartner_ID());
			//	
			MBPartnerLocation [] partnerLocations = partner.getLocations(true);
			if(partnerLocations.length > 0) {
				for(MBPartnerLocation partnerLocation : partnerLocations) {
					if(partnerLocation.isShipTo()) {
						currentReturn.setC_BPartner_Location_ID(partnerLocation.getC_BPartner_Location_ID());
						break;
					}
					
				}				
			}
			//	Set Sales Representative
			currentReturn.setSalesRep_ID(entityPOS.getSalesRep_ID());
			//	Save Header
			currentReturn.saveEx();
		}
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
	public void setReturn(int returnId) {
		currentReturn = new MInOut(ctx, returnId, null);
		//	
		reloadReturn();
	}
	
	/**
	 * 
	 * 
	 * @param returnLineId
	 * @param qtyEntered
	 * @return
	 */
	public BigDecimal [] updateLine(int returnLineId, BigDecimal qtyEntered) {
		//	Valid if has a Order
		if(!isDrafted())
			return null;
		//	
		MInOutLine[] returnLines = currentReturn.getLines(true);
		
		//	Search Line
		for(MInOutLine returnLine : returnLines) {
			if(returnLineId==returnLine.getM_InOutLine_ID()) {
				//	Valid No changes
				if(qtyEntered.compareTo(returnLine.getQtyEntered()) == 0) 
					return null;
	
				returnLine.setQty(qtyEntered);
				returnLine.saveEx();
			}

		}
		//	Return Value
		return new BigDecimal[]{qtyEntered};
	}

	/**
	 * Create new Line
	 * @param product
	 * @param qtyEntered
	 * @param productPricing
     * @return
     */
	public MInOutLine addOrUpdateLine(MProduct product, BigDecimal qtyEntered) {
		//	Valid Complete
		if (!isDrafted())
			return null;
		// catch Exceptions at order.getLines()
		MInOutLine[] returnLines = currentReturn.getLines(true);
		for(MInOutLine returnLine : returnLines) {
			if (returnLine.getM_Product_ID() == product.getM_Product_ID()) {
				//increase qty
				setReturnLineId(returnLine.getM_InOutLine_ID());
				BigDecimal currentQty = returnLine.getQtyEntered();
				BigDecimal totalQty = currentQty.add(qtyEntered);
				//	Set or Add Qty
				returnLine.setQty(isAddQty()? totalQty: qtyEntered);
				returnLine.saveEx();
				return returnLine;
			}
		}
        //create new line
		MInOutLine line = new MInOutLine(currentReturn);
		line.setM_Product_ID(product.getM_Product_ID());
		line.setQty(qtyEntered);
		
		MOrgInfo orgInfo = MOrgInfo.get(getCtx(), Env.getAD_Org_ID(getCtx()), null);
		line.setM_Locator_ID(orgInfo.getM_Warehouse().getM_ReserveLocator_ID());
		
		//	
		//	Save Line
		setReturnLineId(line.getM_InOutLine_ID());
		line.saveEx();
		return line;
			
	} //	addOrUpdateLine

	/**
	 *  Save Line
	 * @param productId
	 * @param qtyOrdered
     * @return
     */
	public String addOrUpdate(int productId, BigDecimal qtyMovement) {
		String errorMessage = null;
		try {
			MProduct product = MProduct.get(ctx, productId);
			if (product == null)
				return "@No@ @InfoProduct@";

			//	Validate if exists a order
			if (hasReturn()) {
				addOrUpdateLine(product, qtyMovement);
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
	 * 	Call Order void process 
	 *  Only if Order is "Drafted", "In Progress" or "Completed"
	 * 
	 *  @return true if order voided; false otherwise
	 */
	private boolean voidReturn() {
		if (!(currentReturn.getDocStatus().equals(MOrder.STATUS_Drafted)
				|| currentReturn.getDocStatus().equals(DocAction.STATUS_InProgress)
				|| currentReturn.getDocStatus().equals(DocAction.STATUS_Completed)))
			return false;
		
		// Standard way of voiding an order
		currentReturn.setDocAction(MInOut.DOCACTION_Void);
		if (currentReturn.processIt(MInOut.DOCACTION_Void) ) {
			currentReturn.setDocAction(MInOut.DOCACTION_None);
			currentReturn.setDocStatus(MInOut.STATUS_Voided);
			currentReturn.saveEx();
			return true;
		} else {
			return false;
		}
	} // cancelReturn
	
	/**
	 * Execute deleting an return
	 * If the order is in drafted status -> ask to delete it
	 * If the order is in completed status -> ask to void it it
	 * Otherwise, it must be done outside this class.
	 */
	public String cancelReturn() {
		String errorMsg = null;
		try {
			//	Get Index
			int currentIndex = returnList.indexOf(currentReturn.getM_InOut_ID());
			if (!hasReturn()) {
				throw new AdempierePOSException("@POS.MustCreateOrder@");
			} else if (!isCompleted()) {
				//	Delete Order
				currentReturn.deleteEx(true);
			} else if (isCompleted()) {	
				voidReturn();
			} else {
				throw new AdempierePOSException("@POS.OrderIsNotProcessed@");
			}
			//	Remove from List
			if(currentIndex >= 0) {
				returnList.remove(currentIndex);
			}
			//	
			currentReturn = null;
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
	} // cancelOrder
	
	/** 
	 * Delete one order line
	 * To erase one line from order
	 * 
	 */
	public void deleteLine (int returnLineId) {
		if ( returnLineId != -1 && currentReturn != null ) {
			for ( MInOutLine line : currentReturn.getLines(true) ) {
				if ( line.getM_InOutLine_ID() == returnLineId ) {
					line.deleteEx(true);	
				}
			}
		}
	} //	deleteLine

	/**
	 * Get Data List Return
	 */
	public void listReturn() {
		String sql = new String("SELECT m.M_InOut_ID "
					+ "FROM M_InOut m "
					+ "WHERE m.Processed = 'N' "
					+ "AND m.AD_Client_ID = ? "
					+ "AND m.C_POS_ID = ? "
					+ "AND m.SalesRep_ID = ? "
					+ "AND m.C_DocType_ID = ? "
					+ "ORDER BY m.Updated");
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		returnList = new ArrayList<Integer>();
		try {
			//	Set Parameter
			preparedStatement= DB.prepareStatement(sql, null);
			preparedStatement.setInt (1, Env.getAD_Client_ID(Env.getCtx()));
			preparedStatement.setInt (2, getC_POS_ID());
			preparedStatement.setInt (3, getSalesRep_ID());
			preparedStatement.setInt (4, entityPOS.getC_DocType_ID());
			//	Execute
			resultSet = preparedStatement.executeQuery();
			//	Add to List
			while(resultSet.next()){
				returnList.add(resultSet.getInt(1));
			}
		} catch(Exception e) {
			log.severe("SubReturn.listReturn: " + e + " -> " + sql);
		} finally {
			DB.close(resultSet);
			DB.close(preparedStatement);
		}
		//	Seek Position
		if(hasRecord())
			recordPosition = returnList.size() -1;
		else 
			recordPosition = -1;
	}
	
	/**
	 * Verify if has record in list
	 * @return
	 * @return boolean
	 */
	public boolean hasRecord(){
		return !returnList.isEmpty();
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
		return recordPosition == returnList.size() - 1;
	}
	
	/**
	 * Previous Record Movement
	 */
	public void previousRecord() {
		if(recordPosition > 0) {
			setReturn(returnList.get(--recordPosition));
		}
	}

	/**
	 * Next Record Movement
	 */
	public void nextRecord() {
		if(recordPosition < returnList.size() - 1) {
			setReturn(returnList.get(++recordPosition));
		}
	}
	
	/**
	 * Reload List Index
	 * @param orderId
	 * @return void
	 */
	public void reloadIndex(int movementId) {
		int position = returnList.indexOf(movementId);
		if(position >= 0) {
			recordPosition = position;
		}
	}
	
	/**
	 * Seek to last record
	 * @return void
	 */
	public void lastRecord() {
		recordPosition = returnList.size();
		if(recordPosition != 0) {
			--recordPosition;
		}
	}
	
	/**
	 * Seek to first record
	 * @return void
	 */
	public void firstRecord() {
		recordPosition = returnList.size();
		if(recordPosition != 0) {
			recordPosition = 0;
		}
	}
	
	/**
	 * Process Return
	 * For status "Drafted" or "In Progress": process order
	 * For status "Completed": do nothing as it can be pre payment or payment on credit
	 * @param trxName
	 * @param isPrepayment
	 * @param isPaid
	 * @return true if order processed or pre payment/on credit; otherwise false
	 * 
	 */
	public boolean processReturn(String trxName) {
		//Returning orderCompleted to check for order completeness
		boolean returnCompleted = isCompleted();
		// check if order completed OK
		if (returnCompleted) {	//	Return already completed -> default nothing
			setIsToPrint(false);
		} else {	//	Complete Order
			//	Replace
			if(trxName == null) {
				trxName = currentReturn.get_TrxName();
			} else {
				currentReturn.set_TrxName(trxName);
			}
			// In case the Order is Invalid, set to In Progress; otherwise it will not be completed
			if (currentReturn.getDocStatus().equalsIgnoreCase(MInOut.STATUS_Invalid)) 
				currentReturn.setDocStatus(MInOut.STATUS_InProgress);
			//	Set Document Action
			currentReturn.setDocAction(DocAction.ACTION_Complete);
			if (currentReturn.processIt(DocAction.ACTION_Complete)) {
				currentReturn.saveEx();
				returnCompleted = true;
				setIsToPrint(true);
			} else {
				log.info( "Process Return FAILED " + currentReturn.getProcessMsg());
				currentReturn.saveEx();
				return returnCompleted;
			}
		}

		return returnCompleted;
	}	// processOrder
			
	
	/**
	 * Get Process Message
	 * @return
	 * @return String
	 */
	public String getProcessMsg() {
		return currentReturn.getProcessMsg();
	}

	
	/**
	 * Get Document No
	 * @return
	 * @return String
	 */
	public String getDocumentNo() {
		return currentReturn.getDocumentNo();
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
	 * 	Load Return
	 */
	public void reloadReturn() {
		if (currentReturn == null) {
			
			if(recordPosition != -1
					&& recordPosition < returnList.size()) {
				setReturn(returnList.get(recordPosition));
			}
			//	
			return;
		}
		currentReturn.load(currentReturn.get_TrxName());
		currentReturn.getLines(true);
		partner = MBPartner.get(getCtx(), currentReturn.getC_BPartner_ID());
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
		if(hasReturn()) {
			MDocType m_DocType = MDocType.get(getCtx(), currentReturn.getC_DocType_ID());
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
		if(hasReturn()) {
			return currentReturn.getMovementDate();
		}
		//	Default
		return null;
	}
	
	/**
	 * Get Date Received
	 * @return
	 */
	public Timestamp getDateReceived() {
		if(hasReturn()) {
			return currentReturn.getDateReceived();
		}
		//	Default
		return null;
	}
	
	/**
	 * Get Currency Symbol
	 * @return
	 * @return String
	 */
	public String getCurSymbol() {
		int currencyId = getC_Currency_ID();
		if(currencyId > 0) {
			MCurrency currency = MCurrency.get(getCtx(), currencyId);
			if(currency != null) {
				return currency.getCurSymbol();
			}
		}
		//	Default
		return "";
	}
		
	/**
	 * Get Context
	 * @return
	 * @return Properties
	 */
	public Properties getCtx() {
		return ctx;
	}
	
	/**
	 * Get POS Key Layout Identifier
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
	 * Get Return Identifier
	 * @return
	 * @return int
	 */
	public int getM_InOut_ID() {
		int m_M_InOut_ID = 0;
		if(hasReturn()) {
			m_M_InOut_ID = currentReturn.getM_InOut_ID();
		}
		//	Default
		return m_M_InOut_ID;
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
	
//	/**
//	 * Set Purchase Order Reference 
//	 * @param documentNo
//	 * @return void
//	 */
//	public void setPOReference(String documentNo) {
//		String trxName = currentDDT.get_TrxName();
//		Trx trx = Trx.get(trxName, true);
//		currentDDT.setPOReference(documentNo);
//		currentDDT.saveEx(trx.getTrxName());
//		trx.close();
//		
//	}

	/**
	 * Get Quantity of Product
	 * @return quantity
	 */
	public BigDecimal getQty() {
		return quantity;
	}

	/**
	 * Set Quantity of Product
	 * @param qty
	 */
	public void setQty(BigDecimal qty) {
		this.quantity = qty;
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
	
	public String getMeasureRequestCode()
	{
		if (entityPOS != null)
			return entityPOS.getMeasureRequestCode();
		return null;
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
	 * Get Product ID from Return Line ID
	 * @param returnLineId
	 * @return
	 * @return int
	 */
	public int getM_Product_ID(int returnLineId) {
		return DB.getSQLValue(null, "SELECT ml.M_Product_ID "
				+ "FROM M_InOutLine ml "
				+ "WHERE ml.M_InOutLine_ID = ?", returnLineId);
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
	public static List<Vector<Object>> getQueryProduct(String productCode, int warehouseId , int priceListId , int partnerId) {
		ArrayList<Vector<Object>> rows = new ArrayList<>();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT ON ( ProductPricing.M_Product_ID , p.Value, p.Name ) ProductPricing.M_Product_ID , p.Value, p.Name,")
				.append("   BomQtyAvailable(ProductPricing.M_Product_ID, ? , 0 ) AS QtyAvailable , PriceStd , PriceList FROM M_Product p INNER JOIN (")
					.append("	SELECT pl.M_PriceList_ID , ValidFrom , 0 AS BreakValue , null AS C_BPartner_ID,")
					.append("   p.M_Product_ID,")
					.append("	bomPriceStd(p.M_Product_ID,plv.M_PriceList_Version_ID) AS PriceStd,")
					.append("	bomPriceList(p.M_Product_ID,plv.M_PriceList_Version_ID) AS PriceList,")
					.append("	bomPriceLimit(p.M_Product_ID,plv.M_PriceList_Version_ID) AS PriceLimit")
					.append("	FROM M_Product p")
					.append("	INNER JOIN M_ProductPrice pp ON (p.M_Product_ID=pp.M_Product_ID)")
					.append("	INNER JOIN M_PriceList_Version plv ON (pp.M_PriceList_Version_ID=plv.M_PriceList_Version_ID)")
					.append("	INNER JOIN M_PriceList pl ON (plv.M_PriceList_ID=pl.M_PriceList_ID)")
					.append("	WHERE pl.M_PriceList_ID=? AND plv.IsActive='Y'AND pp.IsActive='Y'")
				.append("	UNION	")
					.append("	SELECT pl.M_PriceList_ID , plv.ValidFrom , pp.BreakValue , pp.C_BPartner_ID,")
					.append("   p.M_Product_ID,")
					.append("   pp.PriceStd, pp.PriceList, pp.PriceLimit")
					.append("	FROM M_Product p")
					.append("	INNER JOIN M_ProductPriceVendorBreak pp ON (p.M_Product_ID=pp.M_Product_ID)")
					.append("	INNER JOIN M_PriceList_Version plv ON (pp.M_PriceList_Version_ID=plv.M_PriceList_Version_ID)")
					.append("	INNER JOIN M_PriceList pl ON (plv.M_PriceList_ID=pl.M_PriceList_ID)")
					.append("	WHERE pl.M_PriceList_ID=? AND plv.IsActive='Y' AND pp.IsActive='Y'AND pp.BreakValue IN (0,1)")
					.append("  ORDER BY ValidFrom DESC, BreakValue DESC , C_BPartner_ID ASC")
					.append(") ProductPricing  ON (p.M_Product_ID=ProductPricing.M_Product_ID)")
				.append(" WHERE M_PriceList_ID=? AND ValidFrom <= getDate() ");
				if (partnerId > 0 )
					sql.append("AND (C_BPartner_ID IS NULL OR C_BPartner_ID =?) ");
				else
					sql.append( "AND C_BPartner_ID IS NULL ");

				sql.append("AND p.AD_Client_ID=? AND p.IsSold=? AND p.Discontinued=? ")
				.append("AND UPPER(p.Name)  LIKE UPPER('").append("%").append(productCode.replace(" ","%")).append("%").append("')")
				.append(" OR UPPER(p.Value) LIKE UPPER('").append("%").append(productCode.replace(" ","%")).append("%").append("')")
				.append(" OR UPPER(p.UPC)   LIKE UPPER('").append("%").append(productCode.replace(" ","%")).append("%").append("')")
				.append(" OR UPPER(p.SKU)   LIKE UPPER('").append("%").append(productCode.replace(" ","%")).append("%").append("')");
		PreparedStatement statement = null;
		try{
			statement = DB.prepareStatement(sql.toString(), null);
			int count = 1;
			statement.setInt(count, warehouseId);
			count ++;
			statement.setInt(count, priceListId);
			count ++;
			statement.setInt(count, priceListId);
			count ++;
			statement.setInt(count, priceListId);
			count ++;
			if (partnerId > 0) {
				statement.setInt(count, partnerId);
				count++;
			}
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
				String  priceStd = resultSet.getBigDecimal(5) != null ? resultSet.getBigDecimal(5).setScale(2, BigDecimal.ROUND_UP).toString().trim() :  "0";
				String  priceList = resultSet.getBigDecimal(6) != null ? resultSet.getBigDecimal(6).setScale(2, BigDecimal.ROUND_UP).toString().trim() : "0 ";
				columns.add(productId);
				columns.add(productValue);
				columns.add(productName);
				columns.add(qtyAvailable);
				columns.add(priceStd);
				columns.add(priceList);
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

	/**
	 * 	Print Ticket
	 *
	 */
	public void printTicket() {
		if (!hasReturn())
			return;
//		else if(hasOrder() && !isCompleted())
//		{
//			FDialog.info(getWindowNo(), null, "No Order Completed");
//			return;
//		}
		
		//10/04/2018 Vecchia gestione -----
//		//	Print
//		POSTicketHandler ticketHandler = POSTicketHandler.getTicketHandler(this);
//		if(ticketHandler == null)
//			return;
//		//	
//		ticketHandler.printTicket();
		//-----
		
		//Anteprima Report(tasto 'stampante'), come da maschera....
		int processID = 0;
		if(getQuickAD_Tab_ID()>0){
			MTab tab = new MTab(ctx, getQuickAD_Tab_ID(), null);
			processID = tab.getAD_Process_ID();
		}

		if(processID>0){
			String nameProc = MProcess.get(ctx, processID).getName();
			ProcessInfo pi = new ProcessInfo(nameProc, processID, getReturn().get_Table_ID(), getM_InOut_ID());
			
			WProcessCtl.process(getWindowNo(), pi, null);
		}
		else{
			MPrintFormat pf = MPrintFormat.createFromTable(ctx, getReturn().get_Table_ID());
			String tableName = getReturn().get_TableName();
			MQuery query = new MQuery(tableName);
			query.addRestriction(tableName+"_ID", MQuery.EQUAL, getM_InOut_ID());
			PrintInfo info = new PrintInfo(tableName,  MTable.getTable_ID(tableName), getM_InOut_ID());
			ReportEngine re = new ReportEngine(getCtx(), pf, query, info);
			ReportCtl.preview(re);
		}
		
		///////
		
		
	}

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
	 * Get Return Line ID
	 * @return
	 * @return int
	 */
	public int getReturnLineId() {
		return returnLineId;
	}

	/**
	 * Set Return Line ID
	 * @param p_returnLineId
	 * @return void
	 */
	public void setReturnLineId(int p_returnLineId) {
		this.returnLineId = p_returnLineId;
	}
	
//	/**
//	 * Get Total Lines for view in POS with format
//	 * @return
//	 */
//	public String getTotaLinesForView() {
//		return getNumberFormat().format(getTotalLines());
//	}
		
	/**
	 * Get Date Movement for view
	 * @return
	 */
	public String getMovementDateForView() {
		return getDateFormat().format(getMovementDate());
	}
	
	/**
	 * Get Date Promised for view
	 * @return
	 */
	public String getDateReceivedForView() {
		return getDateFormat().format(getDateReceived());
	}
	
	/**
	 * Get Class name for ticket handler
	 * @return
	 */
	public String getTicketHandlerClassName() {
		return entityPOS.getTicketClassName();
	}
	
	public String get_TrxName() {
		if(!hasReturn())
			return null;
		//	Default
		return currentReturn.get_TrxName();
	}
	
	public void setNameFormPOS(String name){
		p_nameOfForm = name;
	}
	
	public int getQuickAD_Window_ID(){
		return entityPOS.getAD_Window_ID();
	}
	
	public int getQuickAD_Tab_ID(){
		return entityPOS.getAD_Tab_ID();
	}
	
	public boolean isSOTrx_Win_POS(){
		if(getQuickAD_Window_ID()>0){
			int adWin_ID = getQuickAD_Window_ID(); 
			MWindow win_for_POS = new MWindow(Env.getCtx(), adWin_ID, null);
			return win_for_POS.isSOTrx();
		}
		return false;
	}
	
	public boolean showPanelDescProduct(){
		return entityPOS.isLIT_isShowWindowProduct();
	}
}

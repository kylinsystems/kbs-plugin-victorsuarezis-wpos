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
 * Copyright (C) 2003-2016 E.R.P. Consultores y Asociados.                    *
 * All Rights Reserved.                                                       *
 * Contributor(s): Yamel Senih www.erpcya.com                                 *
 *****************************************************************************/
package org.adempiere.pos.util;

import org.adempiere.pos.AdempierePOSException;
import org.adempiere.pos.service.CPOS;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.print.MPrintFormat;
import org.compiere.print.ReportCtlPOS;
import org.compiere.print.ReportEngine;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.Env;

//import it.cnet.idempiere.utilPDF.utility.PrinterUtil;

/**
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * 		<a href="https://github.com/adempiere/adempiere/issues/672">
 * 		@see FR [ 672 ] Add abstract class for basic operation of POS (Generic Ticket Handler)</a>
 */
public class POSGenericTicketHandler extends POSTicketHandler {

	/**
	 * Default Constructor
	 * @param pos
	 */
	public POSGenericTicketHandler(CPOS pos) {
		super(pos);
	}
	
	@Override
	public void printTicket() {
		try {
			ProcessInfo info = null;
			if(!getPOS().isInvoiced()) {
				
				info = new ProcessInfo ("", 0, getPOS().getOrder().Table_ID, getPOS().getC_Order_ID());
				info.setTransactionName(getPOS().get_TrxName());
				
				MPrintFormat printformat = null;
				if(getPOS().getOrder().isSOTrx()){
					int LIT_POS_SOPrintForm_ID = DB.getSQLValue(null, "SELECT LIT_POS_SOPrintForm_ID FROM AD_PrintForm WHERE IsActive='Y' AND AD_CLIENT_ID=?", Env.getAD_Client_ID(Env.getCtx()));
					printformat = new MPrintFormat(Env.getCtx(), LIT_POS_SOPrintForm_ID, null);
					info.setAD_Process_ID(printformat.getJasperProcess_ID());
				}
				else{
					int LIT_POS_POPrintForm_ID = DB.getSQLValue(null, "SELECT LIT_POS_POPrintForm_ID FROM AD_PrintForm WHERE IsActive='Y' AND AD_CLIENT_ID=?", Env.getAD_Client_ID(Env.getCtx()));
					printformat = new MPrintFormat(Env.getCtx(), LIT_POS_POPrintForm_ID, null);
					info.setAD_Process_ID(printformat.getJasperProcess_ID());
				}
				
//				ReportCtlPOS.startDocumentPrint(
//						ReportEngine.ORDER, 
//						printformat, 
//						getPOS().getC_Order_ID(), 
//						null, 
//						getPOS().getWindowNo(), 
//						false, 
//						null, 
//						info);
//			PrinterUtil printerUtil = new PrintDirect(info);
//			printerUtil.setDirectPrint(true);
//			printerUtil.print();
				
				
			} else {
				for (MInvoice invoice :  getPOS().getOrder().getInvoices()) {
					info = new ProcessInfo ("", 0, invoice.Table_ID, invoice.getC_Invoice_ID());
					info.setTransactionName(getPOS().get_TrxName());
					
//					int LIT_POS_InvoicePrintForm_ID = DB.getSQLValue(null, "SELECT LIT_POS_InvoicePrintForm_ID FROM AD_PrintForm WHERE IsActive='Y' AND AD_CLIENT_ID=?", Env.getAD_Client_ID(Env.getCtx()));
//					MPrintFormat printformat = new MPrintFormat(Env.getCtx(), LIT_POS_InvoicePrintForm_ID, null);
//					ReportCtlPOS.startDocumentPrint(
//							ReportEngine.INVOICE, 
//							printformat, 
//							invoice.getC_Invoice_ID(), 
//							null, 
//							getPOS().getWindowNo(), 
//							false, 
//							null, 
//							info);
//					PrinterUtil printerUtil = new PrintDirect(info);
//					printerUtil.setDirectPrint(true);
//					printerUtil.print();
                }
			}
		} catch (Exception e) {
			throw new AdempierePOSException("PrintTicket - Error Printing Ticket");
		}
	}

	@Override
	public void openDrawer() {
		//		
	}

	@Override
	public void showMessage(String message) {
		//	
	}
}

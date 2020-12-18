package org.adempiere.pos.util;

//import org.compiere.model.MProcess;
//import org.compiere.model.PrintInfo;
//import org.compiere.print.MPrintFormat;
//import org.compiere.print.ReportEngine;
//import org.compiere.print.ServerReportCtl;
import org.compiere.process.ProcessInfo;
//import org.compiere.util.Env;

//import it.cnet.idempiere.utilPDF.utility.PrinterUtil;

public class PrintDirect //extends PrinterUtil 
{

	public PrintDirect() {
	}

	public PrintDirect(ProcessInfo prInfo) {
		//super(prInfo);
	}

//	@Override
//	public void print() {
//		int recordID = getProcessInfo().getRecord_ID();
//		int processID = getProcessInfo().getAD_Process_ID();
//		
//		PrintInfo info = new PrintInfo (getProcessInfo());
//		info.setAD_Table_ID(getProcessInfo().getTable_ID());
//		
//		MProcess process = MProcess.get(Env.getCtx(), processID);
//		MPrintFormat format = new MPrintFormat(Env.getCtx(), process.getAD_PrintFormat_ID(), null);
//		format.setJasperProcess_ID(processID);
//		getProcessInfo().setTransactionName("trx_000");
//		
//		ReportEngine repo = new ReportEngine(Env.getCtx(), format, null, info, false, "");
//				
//		if (repo.getPrintFormat()!=null)
//		{
//			format = repo.getPrintFormat();
//			String printerName = format.getPrinterName();
//			//stampa jasperReport
//			boolean result = ServerReportCtl.runJasperProcess(recordID, repo, true, printerName);
//				
//		}
//		
//	}

}

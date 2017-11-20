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

package org.adempiere.pos.posproduction.search;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import org.adempiere.pos.WPOS;
import org.adempiere.pos.WPOSKeyboard;
import org.adempiere.pos.WPOSTextField;
import org.adempiere.pos.posproduction.WPOS_Produc;
import org.adempiere.pos.service.POSQueryInterface;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Checkbox;
import org.adempiere.webui.component.Datebox;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListboxFactory;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.compiere.minigrid.ColumnInfo;
import org.compiere.minigrid.IDColumn;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Center;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.North;
import org.zkoss.zul.Space;

/**
 * @author Mario Calderon, mario.calderon@westfalia-it.com, Systemhaus Westfalia, http://www.westfalia-it.com
 * @author Raul Muñoz, rmunoz@erpcya.com, ERPCYA http://www.erpcya.com
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * @author victor.perez@e-evolution.com , http://www.e-evolution.com
 */
public class WQueryProductionHistory extends WPOSQuery_Produc implements POSQueryInterface
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9106643432072753397L;

	/**
	 * 	Constructor
	 */
	public WQueryProductionHistory (WPOS_Produc posPanel)
	{
		super(posPanel);
	}	//	PosQueryProduct

	/** Fields 				*/
	private WPOSTextField 	fieldDocumentNo;
	private Datebox 		fieldDateTo;
	private Datebox 		fieldDateFrom;
	private Checkbox 		fieldProcessed;
	private Checkbox 		fieldAllowDate;

	private Date 			dateTo;
	private Date 			dateFrom;
	private int 			productionId;
	private boolean			isKeyboard;
	
	static final private String DOCUMENTNO      = "DocumentNo";
	static final private String DOCTYPE         = "C_DocType_ID";  // Just display of column name. The actual doctype will be target doctype
	static final private String PROCESSED       = "Processed";
	static final private String DATE	        = "Date";
	static final private String DATEMOVEFROM 	= "From";
	static final private String DATEMOVETO   	= "To";
	static final private String DATEMOVEMENT    = "MovementDate";
	static final private String QUERY           = "Query";
	
	/**	Table Column Layout Info			*/
	private static ColumnInfo[] columnInfos = new ColumnInfo[] {
		new ColumnInfo(" ", "PP_Cost_Collector_ID", IDColumn.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), DOCUMENTNO), DOCUMENTNO, String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), DOCTYPE), DOCTYPE, String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), DATEMOVEMENT), DATEMOVEMENT, Date.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), PROCESSED), PROCESSED, Boolean.class), 
	};

	/**
	 * 	Set up Panel
	 */
	protected void init()
	{
		setTitle(Msg.translate(Env.getCtx(), "PP_Cost_Collector_ID"));
		Panel panel = new Panel();
		setVisible(true);
		Panel mainPanel = new Panel();
		Grid productLayout = GridFactory.newGridLayout();
		
		Groupbox groupPanel = new Groupbox();
		Caption v_TitleBorder = new Caption(Msg.getMsg(ctx, QUERY));
		
		//	Set title window
		this.setClosable(true);
		// add listener on 'ENTER' key 
        addEventListener(Events.ON_OK,this);
		
		appendChild(panel);
		northPanel = new Panel();
		mainPanel.appendChild(mainLayout);
		groupPanel.appendChild(v_TitleBorder);
		mainPanel.setStyle("width: 100%; height: 100%; padding: 0; margin: 0");
		mainLayout.setHeight("100%");
		mainLayout.setWidth("100%");
		Center center = new Center();
		//
		North north = new North();
		north.setStyle("border: none");
		mainLayout.appendChild(north);
		north.appendChild(groupPanel);
		groupPanel.appendChild(productLayout);
		appendChild(mainPanel);
		productLayout.setWidth("100%");
		Rows rows = null;
		Row row = null;
		rows = productLayout.newRows();
		row = rows.newRow();
		
		Label labelDocumentNo = new Label(Msg.translate(ctx, DOCUMENTNO));
		labelDocumentNo.setStyle(WPOS.FONTSIZESMALL);
		row.setHeight("20px");
		row.appendChild(labelDocumentNo.rightAlign());
		fieldDocumentNo = new WPOSTextField("", null);
		row.appendChild(fieldDocumentNo);
		fieldDocumentNo.addEventListener(this);
		fieldDocumentNo.setWidth("120px");
		fieldDocumentNo.setStyle(WPOS.FONTSIZESMALL);
		//
		Label labelDateFrom = new Label(Msg.translate(ctx, DATEMOVEFROM));
		labelDateFrom.setStyle(WPOS.FONTSIZESMALL);
		row.appendChild(labelDateFrom.rightAlign());
		fieldDateFrom = new Datebox();
		fieldDateFrom.setValue(Env.getContextAsDate(Env.getCtx(), "#Date"));
		fieldDateFrom.addEventListener("onBlur",this);
		fieldDateFrom.setStyle(WPOS.FONTSIZESMALL);
		row.appendChild(fieldDateFrom);

		fieldAllowDate = new Checkbox();
		fieldAllowDate.setLabel(Msg.translate(ctx, DATE));
		fieldAllowDate.setSelected(false);
		row.appendChild(fieldAllowDate);
		fieldAllowDate.addActionListener(this);
		fieldAllowDate.setStyle(WPOS.FONTSIZESMALL);
		
		row = rows.newRow();
		row.appendChild(new Space());
		row.appendChild(new Space());
		
		Label labelDateTo = new Label(Msg.translate(ctx, DATEMOVETO));
		labelDateTo.setStyle(WPOS.FONTSIZESMALL);
		row.appendChild(labelDateTo.rightAlign());
		fieldDateTo = new Datebox();
		fieldDateTo.setValue(Env.getContextAsDate(Env.getCtx(), "#Date"));
		fieldDateTo.addEventListener("onBlur",this);
		fieldDateTo.setStyle(WPOS.FONTSIZESMALL);
		row.appendChild(fieldDateTo);
		
		fieldProcessed = new Checkbox();
		fieldProcessed.setLabel(Msg.translate(ctx, PROCESSED));
		fieldProcessed.setSelected(false);
		row.appendChild(fieldProcessed);
		fieldProcessed.addActionListener(this);
		fieldProcessed.setStyle(WPOS.FONTSIZESMALL);
		
		//	Center
		posTable = ListboxFactory.newDataTable();
		posTable.prepareTable (columnInfos, "PP_Cost_Collector",
				"C_POS_ID = " + posPanel.getC_POS_ID()
				, false, "PP_Cost_Collector");

		enableButtons();
		center = new Center();
		center.setStyle("border: none");
//		posTable.setWidth("100%");
//		posTable.setHeight("99%");
		ZKUpdateUtil.setWidth(posTable, "100%");
		ZKUpdateUtil.setHeight(posTable, "99%");
		posTable.addActionListener(this);
		center.appendChild(posTable);
		mainLayout.appendChild(center);
		posTable.setClass("Table-ProductionLine");
		posTable.autoSize();
		posTable.addEventListener(Events.ON_DOUBLE_CLICK, this);
		refresh();
	}	//	init
	
	/**
	 * 
	 */
	@Override
	public void reset()
	{
		fieldProcessed.setSelected(false);
		fieldDocumentNo.setText(null);
		fieldDateFrom.setValue(Env.getContextAsDate(Env.getCtx(), "#Date"));
		fieldDateTo.setValue(Env.getContextAsDate(Env.getCtx(), "#Date"));
		refresh();
	}

	/**
	 * 	Set/display Results
	 *	@param results results
	 */
	public void setResults (Properties ctx, boolean processed, String doc, Date dateFrom, Date dateTo, boolean aDate)
	{
		StringBuffer sql = new StringBuffer();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try  {
			sql.append(" SELECT coll.PP_Cost_Collector_ID, coll.DocumentNo, dt.Name AS C_DocType_ID ,")
				.append(" TRUNC(coll.movementdate,'DD') as datemovement, ")
				// priority for open amounts: invoices, allocations, order
			    .append(" coll.Processed ")
				.append(" FROM PP_Cost_Collector coll ");
			
			if (Env.isBaseLanguage(Env.getAD_Language(ctx), "C_DocType"))
				sql.append(" INNER JOIN C_DocType      dt ON (coll.C_DocType_ID = dt.C_DocType_ID)");
			else
				sql.append(" INNER JOIN C_DocType_trl  dt ON (coll.C_DocType_ID = dt.C_DocType_ID AND dt.AD_LANGUAGE = '" + Env.getAD_Language(ctx) + "')");
			
				sql.append(" WHERE  coll.DocStatus <> 'VO'")
				.append(" AND coll.C_POS_ID = ?")
				.append(" AND coll.Processed= ?");
			if (doc != null && !doc.equalsIgnoreCase(""))
				sql.append(" AND (coll.DocumentNo LIKE '%" + doc + "%')");
			if ( dateFrom != null && aDate) {
				if ( dateTo != null && !dateTo.equals(dateFrom))
					sql.append(" AND coll.movementdate >= ? AND coll.movementdate <= ?");						
				else
					sql.append(" AND coll.movementdate = ? ");	
			}
			//	Group By
			sql.append(" GROUP BY coll.PP_Cost_Collector_ID, coll.DocumentNo, dt.Name , coll.Processed");
			sql.append(" ORDER BY coll.Updated");
			int i = 1;			
			preparedStatement = DB.prepareStatement(sql.toString(), null);
			//	POS
			preparedStatement.setInt(i++, posPanel.getC_POS_ID());
			//	Processed
			preparedStatement.setString(i++, processed? "Y": "N");
			//	Date From and To
			if (dateFrom != null && aDate) {				
				preparedStatement.setDate(i++, dateFrom);
				if (dateTo != null 
						&& !dateTo.equals(dateFrom)) {
					preparedStatement.setDate(i++, dateTo);
				}
			}
			//	
			resultSet = preparedStatement.executeQuery();
			posTable.loadTable(resultSet);
			int rowNo = posTable.getRowCount();
			if (rowNo > 0) {
				if(rowNo == 1) {
					select();
				}
			}
		} catch(Exception e) {
			logger.severe("QueryTicket.setResults: " + e + " -> " + sql);
		} finally {
			DB.close(resultSet);
			DB.close(preparedStatement);
		}
	}	//	setResults

	/**
	 * 	Enable/Set Buttons and set ID
	 */
	protected void enableButtons()
	{
		productionId = -1;
		int row = posTable.getSelectedRow();
		boolean enabled = row != -1;
		if (enabled)
		{
			Integer ID = posTable.getSelectedRowKey();
			if (ID != null)
			{
				productionId = ID.intValue();
			}
		}
		logger.info("ID=" + productionId);
	}	//	enableButtons

	/**
	 * 	Close.
	 * 	Set Values on other panels and close
	 */
	@Override
	protected void close()
	{
		logger.info("PP_Cost_Collector_ID=" + productionId);
		
		if (productionId > 0)
		{
			posPanel.setProduction(productionId);

		}
		posPanel.refreshPanel();
		dispose();
	}	//	close


	@Override
	public void onEvent(Event e) throws Exception {
		if(e.getTarget().equals(fieldDocumentNo.getComponent(WPOSTextField.SECONDARY)) && !isKeyboard) {
			isKeyboard = true;
			//	Get Keyboard Panel
			WPOSKeyboard keyboard = fieldDocumentNo.getKeyboard();
			
			if(keyboard != null) {
				
				//	Set Title
				keyboard.setTitle(Msg.translate(Env.getCtx(), DOCUMENTNO).substring(1));
				keyboard.setPosTextField(fieldDocumentNo);
				AEnv.showWindow(keyboard);
				
			}

			fieldDocumentNo.setFocus(true);
			refresh();
		}
		else if(e.getTarget().equals(fieldDocumentNo.getComponent(WPOSTextField.PRIMARY))) {
			 isKeyboard = false;
		}
		else if(e.getTarget().getId().equals("Refresh")) {
			refresh();
		}
		if ( e.getTarget().equals(fieldProcessed) || e.getTarget().equals(fieldAllowDate)
				|| e.getTarget().equals(fieldDateTo) || e.getTarget().equals(fieldDateFrom)) {
				refresh();
				return;
		}
		else if (Events.ON_OK.equals(e.getName()) 
				|| e.getTarget().equals(posTable) 
				&& e.getName().equals(Events.ON_DOUBLE_CLICK)) {
			close();
		}
		else if(e.getTarget().getId().equals("Ok")){
			close();
		}
		else if(e.getTarget().getId().equals("Cancel")){
			close();
		}		else if(e.getTarget().getId().equals("Reset")){
			reset();
		}
		enableButtons();
	}

	@Override
	public void refresh() {
		lockUI();
		fieldDateTo.setEnabled(fieldAllowDate.isSelected());
		fieldDateFrom.setEnabled(fieldAllowDate.isSelected());
		if(fieldDateTo.getValue()!=null) {
			dateTo = new Date(fieldDateTo.getValue().getTime());
		}	
		else {
			dateTo = null;
		}
		if(fieldDateFrom.getValue()!=null) {
			dateFrom = new Date(fieldDateFrom.getValue().getTime());
		}
		else {
			dateFrom = null;
		}
		setResults(ctx, fieldProcessed.isSelected(), fieldDocumentNo.getText(), dateFrom, dateTo,fieldAllowDate.isSelected());
		unlockUI();
	}

	@Override
	protected void select() {
		productionId = -1;
		int row = posTable.getSelectedRow();
		boolean enabled = row != -1;
		if (enabled)
		{
			Integer ID = posTable.getSelectedRowKey();
			if (ID != null)
			{
				productionId = ID.intValue();
			}
		}
		logger.info("ID=" + productionId);
	}
	@Override
	protected void cancel() {
		productionId = -1;
		dispose();
	}
	
	@Override
	public int getRecord_ID() {
		return productionId;
	}
	
	@Override
	public String getValue() {
		return null;
	}

	@Override
	public void showView() {
		// TODO Auto-generated method stub
		
	}
	
}	//	PosQueryProduct

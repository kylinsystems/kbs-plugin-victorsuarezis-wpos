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

package org.adempiere.pos.postransfer.search;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import org.adempiere.pos.WPOS;
import org.adempiere.pos.WPOSKeyboard;
import org.adempiere.pos.WPOSTextField;
import org.adempiere.pos.postransfer.WPOS_Transfer;
import org.adempiere.pos.service.POSQueryInterface;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Checkbox;
import org.adempiere.webui.component.Datebox;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListItem;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.ListboxFactory;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.WListboxPOS;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.compiere.minigrid.ColumnInfo;
import org.compiere.minigrid.IDColumn;
import org.compiere.model.MUser;
import org.compiere.model.MValRule;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Center;
import org.zkoss.zul.North;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Groupbox;

/**
 * @author Mario Calderon, mario.calderon@westfalia-it.com, Systemhaus Westfalia, http://www.westfalia-it.com
 * @author Raul Muñoz, rmunoz@erpcya.com, ERPCYA http://www.erpcya.com
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * @author victor.perez@e-evolution.com , http://www.e-evolution.com
 */
public class WQueryMovementHistory extends WPOSQuery_Transfer implements POSQueryInterface
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7713957495649128816L;
	/**
	 * 	Constructor
	 */
	public WQueryMovementHistory (WPOS_Transfer posPanel)
	{
		super(posPanel);
	}	//	PosQueryProduct

	/** Fields 				*/
	private Listbox         fieldUser;
	private WPOSTextField 	fieldDocumentNo;
	private WPOSTextField 	fieldBPartner;
	private Datebox 		fieldDateTo;
	private Datebox 		fieldDateFrom;
	private Checkbox 		fieldProcessed;
	private Checkbox 		fieldAllowDate;

	private Date 			dateTo;
	private Date 			dateFrom;
	private int 			movementId;
	private boolean			isKeyboard;
	
	static final private String DOCUMENTNO       = "DocumentNo";
	static final private String DOCTYPE          = "C_DocType_ID";  // Just display of column name. The actual doctype will be target doctype
	static final private String DOCSTATUS        = "DocStatus";
	static final private String NAME             = "SalesRep_ID";
	static final private String BPARTNERID       = "C_BPartner_ID";
	static final private String PROCESSED        = "Processed";
	static final private String DATE	         = "Date";
	static final private String DATEMOVEMENTFROM = "From";
	static final private String DATEMOVEMENTTO   = "To";
	static final private String DATEMOVEMENT      = "MovementDate";
	static final private String QUERY            = "Query";
	
	/**	Table Column Layout Info			*/
	private static ColumnInfo[] columnInfos = new ColumnInfo[] {
		new ColumnInfo(" ", "C_Order_ID", IDColumn.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), DOCUMENTNO), DOCUMENTNO, String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), DOCTYPE), DOCTYPE, String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), DOCSTATUS), DOCSTATUS, String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), NAME), NAME, String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), BPARTNERID), BPARTNERID, String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), DATEMOVEMENT), DATEMOVEMENT, Date.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), PROCESSED), PROCESSED, Boolean.class) 
	};

	/**
	 * 	Set up Panel
	 */
	protected void init()
	{
		setTitle(Msg.translate(Env.getCtx(), "M_Movement_ID"));
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
		
		Label labelUser = new Label(Msg.translate(ctx, NAME));
		labelUser.setStyle(WPOS.FONTSIZESMALL);
		row.setHeight("20px");
		row.appendChild(labelUser.rightAlign());
		
		//////
		KeyNamePair[] listUserSaleRep = null;
		String sqlUserActive = "";
		if(posPanel.getM_POS()!=null && posPanel.getM_POS().getAD_Val_Rule_ID()>0){
			MValRule valRule = MValRule.get(ctx, posPanel.getM_POS().getAD_Val_Rule_ID());
			sqlUserActive = "SELECT AD_USER_ID, Name FROM AD_USER WHERE "+valRule.getCode();
		}
		else
			sqlUserActive = "SELECT AD_USER_ID, Name FROM AD_USER WHERE AD_CLIENT_ID="+Env.getAD_Client_ID(Env.getCtx())+" AND ISACTIVE='Y' ORDER BY Name";

		listUserSaleRep = DB.getKeyNamePairs(sqlUserActive, true);
		fieldUser = new Listbox(listUserSaleRep);
		fieldUser.setMold("select");
		
		if(posPanel.getSalesRep_ID()>0){
			int key = posPanel.getSalesRep_ID();
			String name = MUser.getNameOfUser(posPanel.getSalesRep_ID());
			KeyNamePair pp = new KeyNamePair(key, name);
			fieldUser.setSelectedKeyNamePair(pp);
		}
		///--
		
		row.appendChild(fieldUser);
		
		fieldUser.addActionListener(this);
		fieldUser.setWidth("120px");
		fieldUser.setStyle(WPOS.FONTSIZESMALL);
		
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
		Label labelDateFrom = new Label(Msg.translate(ctx, DATEMOVEMENTFROM));
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
		
		Label labelBPartner = new Label(Msg.translate(ctx, BPARTNERID));
		labelBPartner.setStyle(WPOS.FONTSIZESMALL);
		row.setHeight("60px");
		row.appendChild(labelBPartner.rightAlign());
		fieldBPartner = new WPOSTextField("", null);
		row.appendChild(fieldBPartner);
		fieldBPartner.addEventListener(this);
		fieldBPartner.setWidth("120px");
		fieldBPartner.setStyle(WPOS.FONTSIZESMALL);
		
		Label labelDateTo = new Label(Msg.translate(ctx, DATEMOVEMENTTO));
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
		posTable.prepareTable (columnInfos, "M_Movement",
				"C_POS_ID = " + posPanel.getC_POS_ID()
				, false, "M_Movement");

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
		posTable.setClass("Table-OrderLine");
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
	 * @param object 
	 *	@param results results
	 */
	public void setResults (Properties ctx, boolean processed, String doc, Date dateFrom, Date dateTo, String bPartner, boolean aDate, ListItem valueSaleRep)
	{
		
		StringBuffer sql = new StringBuffer();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try  {
			sql.append(" SELECT m.M_Movement_ID, m.DocumentNo, dt.Name AS C_DocType_ID, refTrl.Name AS DocStatus, au.Name,")
				.append(" b.Name, TRUNC(m.MovementDate,'DD') as MovementDate, ")
			    .append(" m.Processed ")
				.append(" FROM M_Movement m ")
				.append(" INNER JOIN C_BPartner      b ON (m.C_BPartner_ID = b.C_BPartner_ID)");
			
			if (Env.isBaseLanguage(Env.getAD_Language(ctx), "C_DocType"))
				sql.append(" INNER JOIN C_DocType      dt ON (m.C_DocType_ID = dt.C_DocType_ID)");
			else
				sql.append(" INNER JOIN C_DocType_trl  dt ON (m.C_DocType_ID = dt.C_DocType_ID AND dt.AD_LANGUAGE = '" + Env.getAD_Language(ctx) + "')");
			if(Env.isBaseLanguage(Env.getAD_Language(ctx), "AD_Ref_List"))
				sql.append(" INNER JOIN AD_Ref_List ref ON (m.DocStatus = ref.Value AND ref.AD_Reference_ID=131)");
			else
				sql.append(" INNER JOIN AD_Ref_List ref ON (m.DocStatus = ref.Value AND ref.AD_Reference_ID=131)");
				sql.append(" INNER JOIN AD_Ref_List_Trl refTrl ON (ref.AD_Ref_List_ID = refTrl.AD_Ref_List_ID AND refTrl.AD_LANGUAGE = '" + Env.getAD_Language(ctx) + "')");

				sql.append(" LEFT JOIN AD_User au ON (au.AD_User_ID = m.SalesRep_ID)")
				.append(" WHERE  m.DocStatus <> 'VO'")
				.append(" AND m.C_POS_ID = ?")
				.append(" AND m.Processed= ?");
			if (doc != null && !doc.equalsIgnoreCase(""))
				sql.append(" AND m.DocumentNo LIKE '%" + doc + "%' ");
			if ( dateFrom != null && aDate) {
				if ( dateTo != null && !dateTo.equals(dateFrom))
					sql.append(" AND m.MovementDate >= ? AND m.MovementDate <= ?");						
				else
					sql.append(" AND m.MovementDate = ? ");	
			}
			if (bPartner != null && !bPartner.equalsIgnoreCase(""))
				sql.append(" AND (UPPER(b.name) LIKE '%" + bPartner + "%' OR UPPER(b.value) LIKE '%" + bPartner + "%' )");
			//
			if(valueSaleRep!=null && valueSaleRep.getValue()!=null && ((Integer)valueSaleRep.getValue())>0){
				sql.append(" AND m.SalesRep_ID = "+((Integer)valueSaleRep.getValue()));
			}
				
			//	Group By
//			sql.append(" GROUP BY o.C_Order_ID, o.DocumentNo, au.Name, dt.Name , refTrl.Name, b.Name, o.GrandTotal, o.Processed, i.IsPaid ");
			sql.append(" ORDER BY m.Updated");
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
		movementId = -1;
		int row = posTable.getSelectedRow();
		boolean enabled = row != -1;
		if (enabled)
		{
			Integer ID = posTable.getSelectedRowKey();
			if (ID != null)
			{
				movementId = ID.intValue();
			}
		}
		logger.info("ID=" + movementId);
	}	//	enableButtons

	/**
	 * 	Close.
	 * 	Set Values on other panels and close
	 */
	@Override
	protected void close()
	{
		logger.info("M_Movement_ID=" + movementId);
		
		if (movementId > 0)
		{
			posPanel.setMovement(movementId);

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
		}else if(e.getTarget().equals(fieldBPartner.getComponent(WPOSTextField.SECONDARY)) && !isKeyboard) {
			isKeyboard = true;
			//	Get Keyboard Panel
			WPOSKeyboard keyboard = fieldBPartner.getKeyboard();
			
			if(keyboard != null) {
				
				//	Set Title
				keyboard.setTitle(Msg.translate(Env.getCtx(), BPARTNERID));
				keyboard.setPosTextField(fieldBPartner);
				AEnv.showWindow(keyboard);
				
			}
			fieldBPartner.setFocus(true);
			refresh();
		}
		else if(e.getTarget().equals(fieldBPartner.getComponent(WPOSTextField.PRIMARY))) {
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
		setResults(ctx, fieldProcessed.isSelected(), fieldDocumentNo.getText(), dateFrom, dateTo, 
					fieldBPartner.getText().toUpperCase(), fieldAllowDate.isSelected(), fieldUser.getSelectedItem());
		unlockUI();
	}

	@Override
	protected void select() {
		movementId = -1;
		int row = posTable.getSelectedRow();
		boolean enabled = row != -1;
		if (enabled)
		{
			Integer ID = posTable.getSelectedRowKey();
			if (ID != null)
			{
				movementId = ID.intValue();
			}
		}
		logger.info("ID=" + movementId);
	}
	@Override
	protected void cancel() {
		movementId = -1;
		dispose();
	}
	
	@Override
	public int getRecord_ID() {
		return movementId;
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

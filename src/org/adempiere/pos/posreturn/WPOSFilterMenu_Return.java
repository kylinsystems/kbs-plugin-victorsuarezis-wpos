/** ****************************************************************************
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
 * Copyright (C) 2003-2016 e-Evolution,SC. All Rights Reserved.               *
 * Contributor(s): Victor Perez www.e-evolution.com                           *
 * ****************************************************************************/
package org.adempiere.pos.posreturn;

import java.util.List;

import org.adempiere.pos.command.Command;
import org.adempiere.pos.command.CommandManager;
//import org.adempiere.pos.search.QueryBPartner;
import org.adempiere.pos.search.WPOSQuery;
import org.adempiere.pos.service.POSQueryInterface;
import org.adempiere.pos.service.POSQueryListener;
import org.compiere.model.MTable;
import org.compiere.model.MUserQuery;
import org.compiere.model.Query;
import org.compiere.util.Env;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;

/**
 * Class that execute business logic from POS
 * eEvolution author Victor Perez <victor.perez@e-evolution.com> 
 * Raul Muñoz, rmunoz@erpcya.com, ERPCYA http://www.erpcya.com
 * @contributor Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * 		<a href="https://github.com/adempiere/adempiere/issues/670">
 * 		@see FR [ 670 ] Standard process for return material on POS</a>
 */
public class WPOSFilterMenu_Return implements  POSQueryListener, EventListener<Event>{

	private WPOS_Return pos;
	private WPOSQuery queryPartner;
	private Menupopup popupMenu;
	private CommandManager commandManager;
	private Command currentCommand;
	public static final String EVENT_ATTRIBUTE = "EVENT";
	public static final String TABLE_ID = "TABLE";
	public static final String CODE = "CODE";

	private static final String FIELD_SEPARATOR = "<^>";
	private static final String SEGMENT_SEPARATOR = "<~>";

	public WPOSFilterMenu_Return(WPOS_Return pos)
	{
		this.pos = pos;
		this.popupMenu =  new Menupopup();
		this.popupMenu.setStyle("background: #E8E3E3 repeat-y scroll 0 0 !important;");

		List<MUserQuery> listQrFilter = new Query(Env.getCtx(), MUserQuery.Table_Name, "Name LIKE ?", null)
				.setClient_ID()
				.setOnlyActiveRecords(true)
				.setParameters("POS_%")
				.list();
		MUserQuery all = new MUserQuery(Env.getCtx(), 0, null);
		all.setName("All");
		addMenuItem(all);

		for (MUserQuery mUserQuery : listQrFilter) {
			addMenuItem(mUserQuery);
		}

	}

	private void addMenuItem(MUserQuery usrQuery)
	{
		String optionName = usrQuery.getName();
		Menuitem menuItem =  new Menuitem(optionName, null);
		popupMenu.appendChild(menuItem);
		menuItem.setAttribute(EVENT_ATTRIBUTE, optionName);
		menuItem.setAttribute(TABLE_ID, usrQuery.getAD_Table_ID());
		menuItem.setAttribute(CODE, usrQuery.getCode());
		menuItem.addEventListener(Events.ON_CLICK, this);
	}

	@Override
	public void onEvent(Event actionEvent) throws Exception {

		//popupMenu.setVisible(false);
		String queryFilter = (String)actionEvent.getTarget().getAttribute(EVENT_ATTRIBUTE);
		String code = (String)actionEvent.getTarget().getAttribute(CODE);
		int tableID = (int)actionEvent.getTarget().getAttribute(TABLE_ID);
		String tableName = MTable.getTableName(Env.getCtx(), tableID);

		if(queryFilter.equals("All")){
			pos.getOrdLinePanel().lineTableHandle.prepareTable();
			pos.getOrdLinePanel().refreshPanel();
			int numItem = pos.getOrdLinePanel().posTable().getItems().size();
			for (int i = 0; i < numItem; i++) {
				pos.getOrdLinePanel().posTable().getItems().get(i).setStyle(null);
			}
			pos.getOrdLinePanel().isFilter = false;
		}
		else{
			String whereClause = "";
		// =====! STEP_1 gestione del filtro con le query specificate(e parsificate...) sul campo 'AD_UserQuery.code'	....da rivedere TODO
//			String[] segments = code.split(Pattern.quote(SEGMENT_SEPARATOR));
//			String[] fields = segments[0].split(Pattern.quote(FIELD_SEPARATOR));
//			String whereClause = "";
//			String colName = fields[0];
//			String operator = fields[1];
//			String value = fields[2];
//
//			if(colName!=null && colName.trim().length()>0 && operator!=null && operator.trim().length()>0){
//
//				whereClause = colName+" "+operator;
//				MColumn col = MColumn.get(Env.getCtx(), tableName, colName);
//				if (col.getAD_Reference_ID() == DisplayType.Amount || DisplayType.isNumeric (col.getAD_Reference_ID ())
//						|| col.getAD_Reference_ID() == DisplayType.Integer || col.getAD_Reference_ID() == DisplayType.Search){
//					whereClause = whereClause+" "+value; 
//				}
//				else if(col.getAD_Reference_ID() == DisplayType.Date || col.getAD_Reference_ID() == DisplayType.DateTime){
//					whereClause = whereClause+" "+DisplayType.getDateFormat(DisplayType.DateTime, Env.getLanguage(Env.getCtx())).format(Timestamp.valueOf (value));
//				}
//				else{
//					if(value!= null && value.trim().length()>0)
//						whereClause = whereClause+" '"+value+"'";
//
//					//        	(fields[0]+fields[1]+fields[2]);
//				}
//
//				
//				String sqlWhereBase = pos.getOrdLinePanel().lineTableHandle.getSqlWhere();
//				pos.getOrdLinePanel().lineTableHandle.setSqlWhere(sqlWhereBase+" AND "+whereClause);
//				pos.getOrdLinePanel().lineTableHandle.prepareTable();
//				pos.getOrdLinePanel().refreshPanel();
//				pos.getOrdLinePanel().lineTableHandle.setSqlWhere(sqlWhereBase);
//			}
		// =====!!! FINE
			
			//TEST
			//whereClause = "productname LIKE 'Azalea%'";
			//
		// =====! STEP_2 utilizzo del campo 'AD_UserQuery.code' per specificare una query filtro sulla lista degli orderLine
			// es. --> productname in (select name from m_product where M_Product_Category_ID in (Select M_Product_Category_ID from M_Product_Category where name ='Bushes'))
			whereClause = code;
			String sqlWhereBase = pos.getOrdLinePanel().lineTableHandle.getSqlWhere();
			pos.getOrdLinePanel().lineTableHandle.setSqlWhere(sqlWhereBase+" AND "+whereClause);
			pos.getOrdLinePanel().lineTableHandle.prepareTable();
			pos.getOrdLinePanel().refreshPanel();
			pos.getOrdLinePanel().lineTableHandle.setSqlWhere(sqlWhereBase);
			
			int numItem = pos.getOrdLinePanel().posTable().getItems().size();
			for (int i = 0; i < numItem; i++) {
				pos.getOrdLinePanel().posTable().getItems().get(i).setStyle("background:yellow !important;");      
			}
			pos.getOrdLinePanel().isFilter = true;
		// =====!!! FINE
			
		}
	}

	public Menupopup getPopUp()
	{
		return popupMenu;
	}

	@Override
	public void okAction(POSQueryInterface query) {
		if (query.getRecord_ID() <= 0)
			return;
		//	For Ticket
		//        if(query instanceof WQueryBPartner) {
		//            executeCommand(currentCommand);
		//        }
	}

	@Override
	public void cancelAction(POSQueryInterface query) {
	}

}

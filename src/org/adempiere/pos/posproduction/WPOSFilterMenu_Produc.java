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
package org.adempiere.pos.posproduction;

//import java.sql.Timestamp;
import java.util.List;
//import java.util.regex.Pattern;

import org.adempiere.pos.command.Command;
import org.adempiere.pos.command.CommandManager;
//import org.adempiere.pos.search.QueryBPartner;
import org.adempiere.pos.search.WPOSQuery;
import org.adempiere.pos.service.POSQueryInterface;
import org.adempiere.pos.service.POSQueryListener;
//import org.compiere.model.MColumn;
//import org.compiere.model.MQuery;
import org.compiere.model.MTable;
import org.compiere.model.MUserQuery;
import org.compiere.model.Query;
//import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
//import org.zkoss.zul.ListModelList;
//import org.zkoss.zul.Listcell;
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
public class WPOSFilterMenu_Produc implements  POSQueryListener, EventListener<Event>{

	private WPOS_Produc pos;
	private WPOSQuery queryPartner;
	private Menupopup popupMenu;
	private CommandManager commandManager;
	private Command currentCommand;
	public static final String EVENT_ATTRIBUTE = "EVENT";
	public static final String TABLE_ID = "TABLE";
	public static final String CODE = "CODE";

	private static final String FIELD_SEPARATOR = "<^>";
	private static final String SEGMENT_SEPARATOR = "<~>";

	public WPOSFilterMenu_Produc(WPOS_Produc pos)
	{
		this.pos = pos;
		this.popupMenu =  new Menupopup();
		this.popupMenu.setStyle("background: #E8E3E3 repeat-y scroll 0 0 !important; zoom:1.2;");

		List<MUserQuery> listQrFilter = new Query(Env.getCtx(), MUserQuery.Table_Name, "Name LIKE ?", null)
				.setClient_ID()
				.setOnlyActiveRecords(true)
				.setParameters("POSpp_%")
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

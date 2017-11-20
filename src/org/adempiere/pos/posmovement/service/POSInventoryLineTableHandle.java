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
 * Contributor: Yamel Senih www.erpcya.com                                    *
 * Contributor: Mario Calderon www.westfalia-it.com                           *
 *****************************************************************************/
package org.adempiere.pos.posmovement.service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import org.compiere.minigrid.ColumnInfo;
//import org.compiere.minigrid.ColumnInfoPOS;
import org.compiere.minigrid.IDColumn;
import org.compiere.minigrid.IMiniTable;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;

/**
 * @author Mario Calderon, mario.calderon@westfalia-it.com, Systemhaus Westfalia, http://www.westfalia-it.com
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * eEvolution author Victor Perez <victor.perez@e-evolution.com>, Created by e-Evolution on 29/01/16.
 * <li> Define columns of order line table
 * <li> Handle the Load from DB
 */
public class POSInventoryLineTableHandle {
	
	/**
	 * 
	 * *** Constructor ***
	 * @param p_InventoryLineTable
	 */
	public POSInventoryLineTableHandle (IMiniTable p_InventoryLineTable) {
		table = p_InventoryLineTable;
	}
	
	/**	Table Name			*/
	private static final String	TABLE_NAME	= "POS_InventoryLine_v";
	
	/**	Column Names		*/
	public static final String 	PRODUCTNAME	= "ProductName";
	public static final String 	QTYINTERUSE = "QtyInternalUse";
	public static final String 	UOM    		= "C_UOM_ID";
	public static final String  LOCATOR     = "M_Locator_ID";
	/**	Columns Quantity	*/
	public static final int	COLUMN_QTY = 10;
	
	/**	Column Position		*/
	public static final int POSITION_M_INVENTORYLINE_ID = 0;
	//public static final int	POSITION_DELETE	 		= 1;
	public static final int	POSITION_QTYINTERUSE 	= 2;
	public static final int POSITION_LOCATOR        = 4;
	
	/**	Table Column Layout Info	*/
	private ColumnInfo[] columns = new ColumnInfo[] {
		new ColumnInfo(" ", "M_InventoryLine_ID", IDColumn.class,false,true,null), 
		//new ColumnInfo("", "C_OrderLine_ID", DeleteColumn.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), PRODUCTNAME), PRODUCTNAME, String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "Qty"), QTYINTERUSE , BigDecimal.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), UOM), "UOMSymbol", String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), LOCATOR), LOCATOR, Integer.class)
	};
	
	/**	From Clause					*/
	private final String sqlFrom = "POS_InventoryLine_v";
	/** Where Clause				*/
	private String sqlWhere = "M_Inventory_ID=?";
	/** The Query SQL				*/
	private String sqlStatement;
	/**	Table						*/
	private IMiniTable table;
	/**	Logger						*/
	private static CLogger 	log = CLogger.getCLogger(POSInventoryLineTableHandle.class);
	
	/**
	 * Prepare Table
	 * @return boolean
	 */
	public boolean prepareTable() {
		if(table == null)
			return false;
		//	Default Prepare
		sqlStatement = table.prepareTable (columns, sqlFrom,
				sqlWhere, false, TABLE_NAME);
		//	Default Return
		return true;
	}
	
	/**
	 * Set Editable Quantity
	 * @param isDrafted
	 * @return void
	 */
	public void setEditable(boolean isDrafted) {
		table.setColumnClass(POSITION_QTYINTERUSE, BigDecimal.class, !isDrafted);
	}
	
	/**
	 * Load Table from SQL
	 * @param orderId
	 * @return boolean
	 */
	public boolean loadTable(int orderId) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = DB.prepareStatement (sqlStatement, null);
			statement.setInt (1, orderId);
			resultSet = statement.executeQuery ();
			if (resultSet != null)
				table.loadTable(resultSet);
			//	is Ok
			return true;
		} catch (Exception e) {
			log.log(Level.SEVERE, sqlStatement, e);
		} finally {
			DB.close(resultSet, statement);
			resultSet = null; statement = null;
		}
		//	Return
		return false;
	}

	public String getSqlWhere() {
		return sqlWhere;
	}

	public void setSqlWhere(String sqlWhere) {
		this.sqlWhere = sqlWhere;
	}
	
	
}
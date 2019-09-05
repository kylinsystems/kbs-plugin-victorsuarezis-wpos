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
package org.adempiere.pos.posreturn.service;

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
 * <li> Define columns of order line table
 * <li> Handle the Load from DB
 */
public class POSReturnLineTableHandle {
	
	/**
	 * 
	 * *** Constructor ***
	 * @param p_OrderLineTable
	 */
	public POSReturnLineTableHandle (IMiniTable p_ReturnLineTable) {
		table = p_ReturnLineTable;
	}
	
	/**	Table Name			*/
	private static final String	TABLE_NAME	= "POS_ReturnLine_v";
	
	/**	Column Names		*/
	public static final String 	PRODUCTNAME	= "ProductName";
	public static final String 	QTYENTERED = "QtyEntered";
	public static final String 	UOM    		= "C_UOM_ID";
	
	/**	Columns Quantity	*/
	public static final int	COLUMN_QTY = 10;
	
	/**	Column Position		*/
	public static final int POSITION_M_RETURNLINE_ID = 0;
	//public static final int	POSITION_DELETE	 		= 1;
	public static final int	POSITION_QTYENTERED 	= 2;
	
	/**	Table Column Layout Info	*/
	private ColumnInfo[] columns = new ColumnInfo[] {
		new ColumnInfo(" ", "M_InOutLine_ID", IDColumn.class,false,true,null), 
		//new ColumnInfo("", "C_OrderLine_ID", DeleteColumn.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), PRODUCTNAME), PRODUCTNAME, String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "QtyEntered"), QTYENTERED , BigDecimal.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), UOM), "UOMSymbol", String.class)
	};
	
	/**	From Clause					*/
	private final String sqlFrom = "POS_ReturnLine_v";
	/** Where Clause				*/
	private String sqlWhere = "M_InOut_ID=?";
	/** The Query SQL				*/
	private String sqlStatement;
	/**	Table						*/
	private IMiniTable table;
	/**	Logger						*/
	private static CLogger 	log = CLogger.getCLogger(POSReturnLineTableHandle.class);
	
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
	 * Set Editable Quantity and Price
	 * @param isModifyPrice
	 * @param isDrafted
	 * @return void
	 */
	public void setEditable(boolean isModifyPrice, boolean isDrafted) {
		//table.setColumnClass(POSITION_QTYORDERED, BigDecimal.class, !isDrafted);
		table.setColumnClass(POSITION_QTYENTERED, BigDecimal.class, true);
		//table.setColumnClass(POSITION_PRICE, BigDecimal.class, !(isModifyPrice && isDrafted));
		//table.setColumnClass(POSITION_PRICE, BigDecimal.class, true);
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
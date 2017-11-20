package org.adempiere.model;

import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.model.MInventory;
import org.compiere.model.MWarehouse;

public class MInventory_lit extends MInventory {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6749266722851030718L;

	public MInventory_lit(Properties ctx, int M_Inventory_ID, String trxName) {
		super(ctx, M_Inventory_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MInventory_lit(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	public MInventory_lit(MWarehouse wh, String trxName) {
		super(wh, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public I_M_Inventory_lit getWrapper()
	{
		return POWrapper.create(this,I_M_Inventory_lit.class);
	}

}

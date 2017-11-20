package org.adempiere.model;

import java.sql.ResultSet;
import java.util.Properties;

import org.libero.model.MPPCostCollector;
import org.libero.model.MPPOrder;

public class MPPCostCollector_lit extends MPPCostCollector {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4837614383214656031L;

	public MPPCostCollector_lit(Properties ctx, int PP_Cost_Collector_ID, String trxName) {
		super(ctx, PP_Cost_Collector_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MPPCostCollector_lit(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	public MPPCostCollector_lit(MPPOrder order) {
		super(order);
		// TODO Auto-generated constructor stub
	}
	
	public I_PP_Cost_Collector_lit getWrapper()
	{
		return POWrapper.create(this,I_PP_Cost_Collector_lit.class);
	}

}

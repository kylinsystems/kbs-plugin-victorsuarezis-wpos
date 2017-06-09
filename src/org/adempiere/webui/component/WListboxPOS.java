package org.adempiere.webui.component;

import org.compiere.minigrid.ColumnInfo;
import org.compiere.util.Util;

public class WListboxPOS extends WListbox {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2263449524344671953L;

	public WListboxPOS() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 *  Add Table Column and specify the column header.
	 *
	 *  @param header	name of column header
	 */
	public void addColumn (String header)
	{
		WListItemRenderer renderer = (WListItemRenderer)getItemRenderer();
		renderer.addColumn(Util.cleanAmp(header));
		getModel().addColumn();

		return;
	}   //  addColumn

	/**
	 *  Add Table Column and specify the column header.
	 *
	 *  @param info	ColumInfo class for the column
	 */
	public void addColumn (ColumnInfo info)
	{
		WListItemRenderer renderer = (WListItemRenderer) getItemRenderer();
		renderer.addColumn(info);
		getModel().addColumn();

		return;
	}   //  addColumn
	
	public void setKeyColumnIndex(int keyColumnIndex) {
		m_keyColumnIndex = keyColumnIndex;
	}

}

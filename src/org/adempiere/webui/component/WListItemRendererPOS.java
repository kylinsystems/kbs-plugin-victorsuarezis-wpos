package org.adempiere.webui.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.compiere.minigrid.ColumnInfo;
import org.compiere.util.Util;

public class WListItemRendererPOS extends WListItemRenderer {
	
	/**	Array of table details. */
	private ArrayList<WTableColumn> m_tableColumns = new ArrayList<WTableColumn>();

	public WListItemRendererPOS() {
		// TODO Auto-generated constructor stub
	}

	public WListItemRendererPOS(List<? extends String> columnNames) {
		super(columnNames);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 *  Add Table Column.
	 *  after adding a column, you need to set the column classes again
	 *  (DefaultTableModel fires TableStructureChanged, which calls
	 *  JTable.tableChanged .. createDefaultColumnsFromModel
	 *  @param ColumnInfo for the column
	 */
	public void addColumn(ColumnInfo info)
	{
		WTableColumn tableColumn;

		tableColumn = new WTableColumn();
		tableColumn.setHeaderValue(Util.cleanAmp(info.getColHeader()));
		setColumnVisibility(tableColumn,info.getVisibility());
		m_tableColumns.add(tableColumn);

		return;
	}   //  addColumn
	
	/**
	 * Hide or show column
	 * @param column
	 * @param visible
	 */
	public void setColumnVisibility(WTableColumn column, boolean visible) 
	{

		if (visible)
		{
			if (isColumnVisible(column)) return;
			ColumnAttributes attributes = columnAttributesMap.get(column);
			if (attributes == null) return;
			
			column.setMinWidth(attributes.minWidth);
			column.setMaxWidth(attributes.maxWidth);
			column.setPreferredWidth(attributes.preferredWidth);
			columnAttributesMap.remove(column);
			hiddenColumns.remove(column);
		}
		else 
		{
			if (!isColumnVisible(column)) return;

			ColumnAttributes attributes = new ColumnAttributes();
			attributes.minWidth = column.getMinWidth();
			attributes.maxWidth = column.getMaxWidth();
			attributes.preferredWidth = column.getPreferredWidth();
			columnAttributesMap.put(column, attributes);			
			column.setMinWidth(0);
			column.setMaxWidth(0);            	
			column.setPreferredWidth(0);
        	hiddenColumns.add(column);
		}
	}

    private List<WTableColumn> hiddenColumns = new ArrayList<WTableColumn>();
	/**
     * 
     * @param column
     * @return boolean
     */
	public boolean isColumnVisible(WTableColumn column) {
		return !hiddenColumns.contains(column);
	}
	
    private Map<WTableColumn, ColumnAttributes> columnAttributesMap
	= new HashMap<WTableColumn, ColumnAttributes>();

    class ColumnAttributes {

		protected Object headerValue;

		protected int minWidth;

		protected int maxWidth;

		protected int preferredWidth;
	}

}

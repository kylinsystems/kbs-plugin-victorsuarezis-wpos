package org.adempiere.webui.component;

import org.compiere.util.KeyNamePair;

public class ListboxPOS extends Listbox {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6585398043335968352L;

	public ListboxPOS() {
		// TODO Auto-generated constructor stub
	}

	public ListboxPOS(KeyNamePair[] pairs) {
		super(pairs);
		// TODO Auto-generated constructor stub
	}
	
	   /** 
     * Get selected item for the list box based on the value of list item
     * @return Value of selected ListItem
     */
    public Object getValue()
    {
    	ListItem item = getSelectedItem();
    	
    	return item.getValue();
    	
    }

}

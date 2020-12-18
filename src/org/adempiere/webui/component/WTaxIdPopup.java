/******************************************************************************
 * Copyright (C) 2012 Elaine Tan                                              *
 * Copyright (C) 2012 Trek Global
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package org.adempiere.webui.component;

//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

//import org.adempiere.exceptions.DBException;
import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.apps.BusyDialog;
import org.adempiere.webui.event.DialogEvents;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.adempiere.webui.window.FDialog;
import org.adempiere.webui.window.WAutoCompleterCity;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MCountry;
import org.compiere.model.MLocation;
import org.compiere.model.MRegion;
import org.compiere.model.Query;
import org.compiere.util.DB;
//import org.compiere.grid.PaymentForm;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
//import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.South;
import org.zkoss.zul.Space;

//import it.cnet.impl.editorNatIDNumber.model.MLIT_BPTaxIDInfo;
//import it.cnet.impl.editorNatIDNumber.vies_client.ViesVatRegistration;
//import it.cnet.impl.editorNatIDNumber.vies_client.ViesVatService;
//import it.cnet.impl.editorNatIDNumber.vies_client.ViesVatServiceException;

/**
 * 
 * @author Elaine
 *
 */
public class WTaxIdPopup extends Window implements EventListener<Event>, DialogEvents {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2710316463655831868L;

//	private PaymentForm paymentForm;
	private int windowNo;
	
	private Panel mainPanel = new Panel();
	//private Panel centerPanel = new Panel();
	private Grid centerPanel = GridFactory.newGridLayout();
	
	private Borderlayout mainLayout = new Borderlayout();
	private ConfirmPanel confirmPanel = new ConfirmPanel(true);
	
	private boolean m_isLocked = false;
	private boolean initOK = false;
	private BusyDialog progressWindow;
	
	private org.zkoss.zul.Label lblRegion;
	
	private Listbox lstCountryCode;
	private Textbox txtVATNumber;
	private WAutoCompleterCity txtCity;
	
	private Label lblVATNumber;
	
	private Hashtable<String, String> listCode;
	
//	private boolean isChange = false;
//	private boolean onSaveError = false;
//	private boolean inOKAction;
//	private boolean     m_change = false;
//	private boolean firstChange;
	private boolean isCountryCodeMandatory = false;
	private boolean isVATNumberMandatory = false;
//	private boolean inCountryAction;
	
	
	private static final String LABEL_STYLE = "white-space: nowrap;";
	
	//private MLIT_BPTaxIDInfo   m_taxIDNumber;
	private MLocation   m_location;

	private String name;
	private String address;
	private String postal;
	private String city;
	private String regionName;
	private String countryName;
	
	private int bpartnerID=0;
	
	private boolean successOK = false;
	
	public WTaxIdPopup(String valueTxt)
	{
		super();
		

		try {
			
			zkInit();
//			initOK = dynInit(); // Null Pointer if order/invoice not saved yet
			
			//loadData(valueTxt);
			
//			m_taxIDNumber = getRecord(valueTxt);
//			if(m_taxIDNumber!=null)
//				initDataForm();
			
		} catch (Exception ex) {
			FDialog.error(windowNo, this, ex.getMessage() == null ? ex.toString() : ex.getMessage());
			initOK = false;
		}
		
		this.setTitle(Msg.getMsg(Env.getCtx(), "TaxIDNumberNew"));
		this.setSclass("popup-dialog");
		this.setBorder("normal");
		ZKUpdateUtil.setWidth(this, "500px");
		this.setShadow(true);
		this.setAttribute(Window.MODE_KEY, Window.MODE_HIGHLIGHTED);
	}
	
//	private void loadData(String valueTxt){
//		if(valueTxt!=null && valueTxt.trim().length()>0)
//			txtVATNumber.setText(valueTxt);
//		else
//			return;
//		
//		String okNoDuplicate = duplicateCtrl(valueTxt);
//		
//		ViesVatService vl = new ViesVatService();
//		String countryCode = ((MCountry)lstCountryCode.getSelectedItem().getValue()).getCountryCode();
////		String countryCode = "IT";
//		String VATNumber = valueTxt;
//		ViesVatRegistration resDk = null;
//		try
//		{
//			resDk = vl.lookup(countryCode, VATNumber);
//		}catch(ViesVatServiceException ex)
//		{
//			FDialog.error(0, this, "Error_Bussiness_Service", Msg.parseTranslation(Env.getCtx(), ex.getMessage()));
//			return;
//		}
        
        //System.out.println("vl.lookup(\"IT\", \"00488410010\") = "+ resDk);
        
//        if(resDk !=null){
//        	
//        	StringBuffer result = new StringBuffer();
//        	
//        	
//        	name = resDk.getName();
//        	result.append(name).append("\n");
//        	address = resDk.getAddress().substring(0, resDk.getAddress().indexOf("\n"));
//        	result.append("\n"+" --- " +address).append("\n");
//
//        	postal = resDk.getAddress().substring(resDk.getAddress().indexOf("\n")+1);
//        	
//        	MRegion rr = null;
//        	MCountry c = new Query(Env.getCtx(), MCountry.Table_Name, MCountry.COLUMNNAME_CountryCode+"=?", null)
//        			.setOnlyActiveRecords(true)
//        			.setParameters(countryCode)
//        			.first();
//        	
//        	result.append("\n"+" --- " +postal.substring(0, 5)).append("\n");
//        	
////        	if(countryCode.equals("IT"))
////        	{
////        		city= postal.substring(6, postal.lastIndexOf("\n")-3);
////        		result.append("\n"+" --- " +city).append("\n");
////        		
////        		txtCity.setValue(city);
////        		
////        		rr = new Query(Env.getCtx(), MRegion.Table_Name, 
////	        			MRegion.COLUMNNAME_Name+"=? AND "+MRegion.COLUMNNAME_C_Country_ID+"=?",
////	        			null)
////	        			.setParameters(postal.substring(postal.lastIndexOf("\n")-3,postal.lastIndexOf("\n")).trim(), c.getC_Country_ID())
////	        			.first();
////        	}
////        	else{
//        		result.append("\n"+" --- " +postal.substring(6).replace("\n", "")).append("\n");
//        	//}
//        	regionName = rr.getName();
//        	countryName = c.getName();
//        	result.append("\n"+" --- " +regionName).append("\n");
//        	result.append("\n"+" --- " + countryName).append("\n");
//			
////			if(!okNoDuplicate.equals("OK"))
////			{
////				throw new WrongValueException(txtVATNumber, "Business Partner Duplicated...");
////			}
//			
//        	lblRegion.setMultiline(true);
//        	lblRegion.setValue(result.toString());
//			
//        }
//        else{
//        	FDialog.error(-1, "Error_Bussiness_Service");
//        }
//	}
	
//	private MLIT_BPTaxIDInfo getRecord(String valueTxt) {
//
//		String whereClause = MLIT_BPTaxIDInfo.COLUMNNAME_LIT_VATNumber+"=? ";
//		return new Query(Env.getCtx(), MLIT_BPTaxIDInfo.Table_Name, whereClause, null)
//				.setClient_ID()
//				.setOnlyActiveRecords(true)
//				.setParameters(valueTxt)
//				.first();
//		
//	}

	private void zkInit() throws Exception {
		this.appendChild(mainPanel);
		mainPanel.appendChild(mainLayout);
		Center center = new Center();
		center.setSclass("dialog-content");
		mainLayout.appendChild(center);
		ZKUpdateUtil.setHflex(mainLayout, "1");
		ZKUpdateUtil.setVflex(mainLayout, "min");	
		center.appendChild(centerPanel = getPanel());
		LayoutUtils.addSclass("payment-form-content", centerPanel);
		ZKUpdateUtil.setVflex(centerPanel, "min");
		ZKUpdateUtil.setHflex(centerPanel, "1");
		center.setAutoscroll(true);
		
		////////////
		Label lblCountryCode     = new Label(Msg.getElement(Env.getCtx(), "CountryCode"));
		lblCountryCode.setStyle(LABEL_STYLE);
		lblVATNumber       = new Label(Msg.getElement(Env.getCtx(), "LIT_VATNumber"));
		lblVATNumber.setStyle(LABEL_STYLE);
		lblRegion    = new Label(Msg.getElement(Env.getCtx(), "C_Region_ID"));
		lblRegion.setStyle(LABEL_STYLE);
		
		// --
	
		lstCountryCode    = new Listbox();
		lstCountryCode.setMold("select");
		lstCountryCode.setWidth("154px");
		lstCountryCode.setRows(0);
		
		txtVATNumber = new Textbox();
		txtVATNumber.setCols(20);
		txtVATNumber.setEnabled(false);

		
		Rows rows = centerPanel.newRows();
		
		Row pnlCountryCode = rows.newRow();
		pnlCountryCode.appendChild(lblCountryCode.rightAlign());
		pnlCountryCode.appendChild(lstCountryCode);
//		lstCountryCode.setHflex("1");
		ZKUpdateUtil.setHflex(lstCountryCode,"1");

		Row pnlVATNumber = rows.newRow();
		pnlVATNumber.appendChild(lblVATNumber.rightAlign());
		pnlVATNumber.appendChild(txtVATNumber);
//		txtVATNumber.setHflex("1");
		ZKUpdateUtil.setHflex(txtVATNumber,"1");
		
		Row pnlRegion    = rows.newRow();
		pnlRegion.appendChild(new Space());
		pnlRegion.appendChild(lblRegion);
//		pnlRegion.appendChild(lstRegion);
////		lstRegion.setHflex("1");
		ZKUpdateUtil.setHflex(lblRegion,"5");
		
		///////////
		
		//
		South south = new South();
		south.setSclass("dialog-footer");		
		mainLayout.appendChild(south);
		south.appendChild(confirmPanel);
		confirmPanel.addActionListener(this);
		
		
		txtCity = new WAutoCompleterCity(windowNo);
		txtCity.setCols(20);
		txtCity.setAutodrop(true);
		txtCity.setAutocomplete(true);
		
		
//      Current Country
		List<MCountry> countries = new Query(Env.getCtx(), MCountry.Table_Name, "", null)
				.setOnlyActiveRecords(true)
				.list();
		for (MCountry country:countries)
		{
			lstCountryCode.appendItem(country.getCountryCode(), country);
		}
		
		setCountry();
		txtCity.fillList();
		lstCountryCode.addEventListener(Events.ON_SELECT,this);
	}
		
	private void setCountry()
	{
		List<?> listCountry = lstCountryCode.getChildren();
		Iterator<?> iter = listCountry.iterator();
		
		MCountry country = new MCountry(Env.getCtx(), Env.getContextAsInt(Env.getCtx(), "#C_Country_ID"), null);
		
		while (iter.hasNext())
		{
			ListItem listitem = (ListItem)iter.next();
			if (country.equals(listitem.getValue()))
			{
				lstCountryCode.setSelectedItem(listitem);
			}
		}
	}

	
	public /*Panel*/Grid getPanel() {
		return centerPanel;
	}
	
	/**************************************************************************
	 * Action Listener
	 * 
	 * @param e
	 *            event
	 */
	public void onEvent(Event event) {
		if (event.getTarget() == confirmPanel.getButton(ConfirmPanel.A_OK)) 
		{
			
//			if(m_taxIDNumber==null)
//				m_taxIDNumber = new MLIT_BPTaxIDInfo(Env.getCtx(), 0, null);
//			
//			m_taxIDNumber.setCountryCode(((MCountry)lstCountryCode.getSelectedItem().getValue()).getName().toUpperCase());
//			m_taxIDNumber.setC_Country_ID(((MCountry)lstCountryCode.getSelectedItem().getValue()).get_ID());
//			m_taxIDNumber.setLIT_TradeName(name);
//			m_taxIDNumber.setLIT_VATNumber(txtVATNumber.getValue());
//			m_taxIDNumber.setName(txtVATNumber.getValue()+"_"+name);
//			m_taxIDNumber.setAddress1(address);
//			m_taxIDNumber.setC_City_ID(txtCity.getC_City_ID()); 
//			m_taxIDNumber.setCity(city);
//			m_taxIDNumber.setPostal(postal);
//			m_taxIDNumber.saveEx();
			
			MBPartner partner = new MBPartner(Env.getCtx(), 0, null);
			partner.setName(name);
			partner.set_ValueOfColumn("TaxID", txtVATNumber.getValue());
			partner.saveEx();
			
//			m_taxIDNumber.setC_BPartner_ID(partner.getC_BPartner_ID());
//			m_taxIDNumber.saveEx();
			bpartnerID = partner.getC_BPartner_ID();
			
			Trx trx = Trx.get(Trx.createTrxName("WTaxIdNumberDialog"), true);
			m_location = new MLocation(Env.getCtx(), 0, null);
			m_location.setAddress1(address);
			m_location.setAddress2("");
			m_location.setAddress3("");
			m_location.setAddress4("");
			m_location.setC_City_ID(txtCity.getC_City_ID()); 
			m_location.setCity(txtCity.getValue());
			m_location.setPostal(postal);
			m_location.setPostal_Add("");
			//  Country/Region
			MCountry country = (MCountry)lstCountryCode.getSelectedItem().getValue();
			m_location.setCountry(country);
			
			for (MRegion region : MRegion.getRegions(Env.getCtx(), country.getC_Country_ID()))
			{
				if(region.getName().equals(regionName)){
					m_location.setRegion(region);
					break;
				}
					
			}
			
			boolean success = m_location.save();
			if (success){
				int bplID = DB.getSQLValueEx(null, "SELECT C_BPartner_Location_ID FROM C_BPartner_Location WHERE C_Location_ID = " + m_location.getC_Location_ID());
    			if (bplID>0)
    			{
    				MBPartnerLocation bpl = new MBPartnerLocation(Env.getCtx(), bplID, null);
    				bpl.setName(bpl.getBPLocName(m_location));
    				success = bpl.save();
    			}
    			else{
    				MBPartnerLocation bpl = new MBPartnerLocation(Env.getCtx(), 0, null);
    				bpl.setC_BPartner_ID(partner.getC_BPartner_ID());
    				bpl.setC_Location_ID(m_location.get_ID());
    				bpl.setName(bpl.getBPLocName(m_location));
    				success = bpl.save();
    			}
			}
			
			if (success) {
				trx.commit();
			} else {
				trx.rollback();
			}
			trx.close();
			
			setSuccessOK(success);
			this.dispose();
		}
		else if (event.getTarget() == confirmPanel.getButton(ConfirmPanel.A_CANCEL))
		{
//			m_change = false;
			this.dispose();
		}
		else if (lstCountryCode.equals(event.getTarget()))
		{
			
			//loadData(txtVATNumber.getValue());
//			//  refresh
//			try {
//				dynInit();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			lstCountryCode.focus();
		}	
		else if ("onSaveError".equals(event.getName())) {
//			onSaveError = false;
			doPopup();
			focus();			
		}

	} // actionPerformed
	
//	public void runProcessOnline() {
//		try {
//			paymentForm.processOnline();
//		} finally {
//			unlockUI();
//		}
//	}
	
	private void setSuccessOK(boolean success) {
		successOK = success;
		
	}
	
	public boolean getSuccessOK(){
		return successOK;
	}
	
	public int getBP_ID(){
		return bpartnerID;
	}

	public void unlockUI() {
		if (!m_isLocked) return;
		
		m_isLocked = false;
		hideBusyDialog();
		updateUI();
	}
	
	public void lockUI() {
		if (m_isLocked) return;
		
		m_isLocked = true;
		
		showBusyDialog();
	}
	
	private void hideBusyDialog() {
		if (progressWindow != null) {
			progressWindow.dispose();
			progressWindow = null;
		}
	}

	private void updateUI() {
//		if (paymentForm.isApproved())
			dispose();
	}
	
	private void showBusyDialog() {
		progressWindow = new BusyDialog();
		progressWindow.setPage(this.getPage());
		progressWindow.doHighlighted();
	}
	
	public boolean isInitOK()
	{
		return initOK;
	}
	
	public String getValueTaxID(){
		if(txtVATNumber.getValue()!=null)
			return txtVATNumber.getValue();
		else
			return "";
	}
	
//	private String duplicateCtrl(String cf)
//	{
//			
//		String sql = "SELECT "+MLIT_BPTaxIDInfo.COLUMNNAME_LIT_C_BPartner_TaxIDInfo_ID 
//				+ " FROM "+MLIT_BPTaxIDInfo.Table_Name 
//				+ " WHERE "+MLIT_BPTaxIDInfo.COLUMNNAME_LIT_VATNumber+"=? "
//				+ "AND "+MLIT_BPTaxIDInfo.COLUMNNAME_AD_Client_ID+"=?";
//		
//		int result = DB.getSQLValue(null, sql, cf, Env.getAD_Client_ID(Env.getCtx()));
//		
//		if(result >0)
//			return Msg.getMsg(Env.getCtx(), "TaxCode_Duplicated", true);
//		
//		return "OK";
//	}
}

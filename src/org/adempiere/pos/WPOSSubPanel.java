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
 * Copyright (C) 2003-2014 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Raul Muñoz www.erpcya.com					              *
 *****************************************************************************/

package org.adempiere.pos;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.theme.ThemeManager;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;


/**
 * @author Mario Calderon, mario.calderon@westfalia-it.com, Systemhaus Westfalia, http://www.westfalia-it.com
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * @author Raul Muñoz, rmunoz@erpcya.com, ERPCYA http://www.erpcya.com
 * @author victor.perez@e-evolution.com , http://www.e-evolution.com
 */
public abstract class WPOSSubPanel extends Panel 
	implements EventListener<Event>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -158167614949876569L;

	/**
	 * 	Constructor
	 *	@param posPanel POS Panel
	 */
	public WPOSSubPanel (WPOS posPanel) {
		super();
		this.posPanel = posPanel;
		setHeight("100%");
		init();
	}	//	PosSubPanel
	
	/** POS Panel						*/
	protected WPOS 				posPanel;
	/** Context							*/
	protected Properties 		ctx = Env.getCtx();
	

	/** Button Width = 55			*/
	private static final int	WIDTH = 75;	
	/** Button Height = 55			*/
	private static final int	HEIGHT = 75;
	
	/**
	 * 	Initialize
	 */
	protected abstract void init();
	
	/**
	 * 	Dispose - Free Resources
	 */
	public void dispose() {
		SessionManager.getAppDesktop().closeActiveWindow();
	}	//	dispose

	
	/**
	 * 	Create Action Button
	 *	@param action action 
	 *	@return button
	 */
	protected Button createButtonAction (String action, String accelerator)
	{
		Button button = new Button();
		if (ThemeManager.isUseFontIconForImage()) {
    		String iconSclass = "z-icon-" + action;
    		button.setIconSclass(iconSclass);
    		LayoutUtils.addSclass("font-icon-toolbar-button", button);
    	} else {
    		button.setImage(ThemeManager.getThemeResource("images/"+action+"24.png"));
    	}
		button.setTooltiptext(accelerator+"-"+Msg.translate(ctx, action));
		button.setWidth(WIDTH+"px");
		button.setHeight(HEIGHT+"px");
		button.addActionListener(this);
		if(action != null || accelerator != null)
			posPanel.addStatusBarInfo("("+action + accelerator + ") ");
		return button;
	}	//	getButtonAction
	
	/**
	 * 	Create Action Button
	 *	@param action action
	 *	@param icon Path of Icon 
	 *	@return button
	 */
	protected Button createButtonAction (String action, String accelerator, String iconPath) {
		BufferedImage bffImg = new BufferedImage(24, 24, BufferedImage.TYPE_INT_RGB);
		try {
			bffImg = ImageIO.read(getClass().getResource(iconPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(bffImg == null)
			return createButtonAction(action, accelerator);
		Button button = new Button();
		String icon = iconPath;
		if (ThemeManager.isUseFontIconForImage()) {
    		String iconSclass = "z-icon-" + action;
    		button.setIconSclass(iconSclass);
    		LayoutUtils.addSclass("font-icon-toolbar-button", button);
    	} else {
			try {
				button.setImage(getClass().getResource(iconPath).toURI().toString());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		button.setTooltiptext(accelerator+"-"+Msg.translate(ctx, action));
		button.setWidth(WIDTH+"px");
		button.setHeight(HEIGHT+"px");
		button.addActionListener(this);
		if(action != null || accelerator != null)
			posPanel.addStatusBarInfo("("+action + accelerator + ") ");
		return button;
	}	//	getButtonAction
	
	/**
	 * 	Create Action Button
	 *	@param action action 
	 *	@return button
	 */
	protected Button createButtonAction (String action, int m_OSK_KeyLayout_ID)
	{
		
		Button button = new Button();
		if (ThemeManager.isUseFontIconForImage()) {
    		String iconSclass = "z-icon-" + action;
    		button.setIconSclass(iconSclass);
    		LayoutUtils.addSclass("font-icon-toolbar-button", button);
    	} else {
    		button.setImage(ThemeManager.getThemeResource("images/"+action+"24.png"));
    	}
		button.setTooltiptext(Msg.translate(ctx, action));
		button.setId(m_OSK_KeyLayout_ID+"");
		button.setWidth(WIDTH+"px");
		button.setHeight(HEIGHT+"px");
		button.addActionListener(this);
		return button;
	}	//	getButtonAction
	
	/**
	 * 	Create Standard Button
	 *	@param text text
	 *	@return button
	 */
	protected Button createButton (String text)
	{
	//	if (text.indexOf("<html>") == -1)
	//		text = "<html><h4>" + text + "</h4></html>";
		Button button = new Button(text);
		button.addActionListener(this);
//		button.setPreferredSize(new Dimension(WIDTH, HEIGHT));

//		button.setFocusable(false);
		return button;
	}	//	getButton

}	//	PosSubPanel

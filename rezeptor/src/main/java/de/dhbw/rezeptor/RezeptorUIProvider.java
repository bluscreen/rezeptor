package de.dhbw.rezeptor;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;

/**
 * Der UI-Provider gibt dem Client das jeweilig Platformabhängige UI (in unserem
 * Fall gibt es erst mal nur eins... mehr braucht man da erst mal nicht zu
 * wissen...
 * 
 * @author dhammacher
 * 
 */
public class RezeptorUIProvider extends UIProvider {

	@Override
	public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
		if (event.getRequest().getParameter("mobile") != null
				&& event.getRequest().getParameter("mobile").equals("false")) {
			return RezeptorUI.class;
		}

		if (event.getRequest().getHeader("user-agent").toLowerCase()
				.contains("mobile")
				&& !event.getRequest().getHeader("user-agent").toLowerCase()
						.contains("ipad")) {
			return MobileCheckUI.class;
		}

		return RezeptorUI.class;
	}
}
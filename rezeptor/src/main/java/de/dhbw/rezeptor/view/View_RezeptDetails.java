package de.dhbw.rezeptor.view;

import java.util.Properties;

import com.vaadin.data.util.BeanItem;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.dhbw.rezeptor.RezeptorUI;
import de.dhbw.rezeptor.container.Container_RezeptDetails;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Rezept;

/**
 * Dieser View dient dem anzeigen der eigenen rezepte.... Verwendet af�r
 * Container_RezeptDetails.java
 * 
 * @author dhammacher
 * 
 */
public class View_RezeptDetails extends VerticalLayout implements View {

	private final static Properties prop = SystemProperties.getProperties();
	private RezeptorUI rezui = (RezeptorUI) UI.getCurrent();

	private VerticalLayout mainVL = new VerticalLayout();

	public View_RezeptDetails() {
		setSpacing(true);
		setMargin(true);
		addComponent(mainVL);
		setExpandRatio(mainVL, 1);
	}

	public void generateDetails(long rezeptId) {
		HorizontalLayout top = new HorizontalLayout();
		mainVL.setSizeFull();
		mainVL.addComponent(top);

		BeanItem<Rezept> item = null;
		Rezept fragmentRezept = null;
		if (rezeptId != -1) {
			fragmentRezept = rezui.dataProvider.getRezeptById(rezeptId);
		}
		// wenn fragment == null dann randomrezept suchen
		if (fragmentRezept == null) {
			fragmentRezept = rezui.dataProvider.getRandomRezept();
		}
		if (fragmentRezept != null) {
			item = new BeanItem<Rezept>(fragmentRezept);
		}

		if (item != null) {
			Label title = new Label(
					prop.getProperty("ui.caption.rezeptDetails") + " '"
							+ item.getBean().getBezeichnung() + "' von "
							+ item.getBean().getVerfasser());
			VerticalLayout rezeptDetails = new Container_RezeptDetails(item);
			mainVL.addComponent(rezeptDetails);
			mainVL.setExpandRatio(rezeptDetails, 1);
			title.setSizeUndefined();
			title.addStyleName("h1");
			top.setWidth("100%");
			top.addComponent(title);
			top.setComponentAlignment(title, Alignment.TOP_LEFT);
		} else {
			Notification n = new Notification(prop.getProperty("error"),
					Type.WARNING_MESSAGE);
			n.setDelayMsec(1500);
			n.setDescription(prop.getProperty("error.noRezeptFound"));
			n.show(rezui.getPage());
		}
	}

	@Override
	public void enter(ViewChangeEvent event) {
		long rezeptId = -1;
		if (event.getParameters() != null) {
			String[] msgs = event.getParameters().split("/");
			if (msgs.length > 0 && msgs[0].length() > 0) {
				Long dieId = Long.decode(msgs[0]);
				if (dieId != null)
					rezeptId = dieId.longValue();
			}
		}
		generateDetails(rezeptId);
	}
}

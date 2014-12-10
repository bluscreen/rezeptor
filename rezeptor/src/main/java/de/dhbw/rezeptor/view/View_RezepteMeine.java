package de.dhbw.rezeptor.view;

import java.util.Properties;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.dhbw.rezeptor.RezeptorUI;
import de.dhbw.rezeptor.container.Container_Rezepte;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Person;
/**
 * Dieser View dient dem anzeigen der eigenen rezepte....
 * Verwendet af�r Container_Rezepte.java
 * @author dhammacher
 *
 */
public class View_RezepteMeine extends VerticalLayout implements View {

	private final static Properties prop = SystemProperties.getProperties();
	private Person person = new Person();
	private RezeptorUI rezui = (RezeptorUI) UI.getCurrent();
	private Container_Rezepte container_RezeptZutaten;
	
	private VerticalLayout mainVL = new VerticalLayout();

	public View_RezepteMeine() {
		setSpacing(true);
		setMargin(true);
		addComponent(mainVL);
		setExpandRatio(mainVL, 1);
		
		HorizontalLayout top = new HorizontalLayout();
		top.setWidth("100%");
		top.setSpacing(true);
		top.setMargin(true);
		final Label title = new Label(prop.getProperty("ui.caption.meineRezepte"));
		title.setSizeUndefined();
		title.addStyleName("h1");
		top.addComponent(title);
		top.setComponentAlignment(title, Alignment.TOP_LEFT);	

		// Session auslesen
		person = (Person) VaadinSession.getCurrent().getAttribute("User");
		container_RezeptZutaten = new Container_Rezepte(person,false);

		mainVL.setSizeFull();
		mainVL.addComponent(top);
		mainVL.addComponent(container_RezeptZutaten);
		mainVL.setExpandRatio(container_RezeptZutaten, 1);
	}

	@Override
	public void enter(ViewChangeEvent event) {
	}

}

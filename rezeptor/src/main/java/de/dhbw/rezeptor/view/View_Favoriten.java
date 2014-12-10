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
import de.dhbw.rezeptor.container.Container_Zutaten;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Person;
import de.dhbw.rezeptor.util.Hr;
/**
 * Dieser View dient dem anzeigen der eigenen rezepte....
 * Verwendet af�r Container_Rezepte.java
 * @author dhammacher
 *
 */
public class View_Favoriten extends VerticalLayout implements View {

	private final static Properties prop = SystemProperties.getProperties();
	Person person = (Person) VaadinSession.getCurrent().getAttribute(prop.getProperty("session.user"));
	
	RezeptorUI rezui = (RezeptorUI) UI.getCurrent();
	Container_Rezepte container_Rezepte;
	Container_Zutaten container_Zutaten;
	
	VerticalLayout mainVL = new VerticalLayout();

	public View_Favoriten() {
		setSpacing(true);
		setMargin(true);
		addComponent(mainVL);
		setExpandRatio(mainVL, 1);
		
		HorizontalLayout top = new HorizontalLayout();
		HorizontalLayout top2 = new HorizontalLayout();
		top.setWidth("100%");
		top2.setWidth("100%");
		top.setSpacing(true);
		top.setMargin(true);
		top2.setSpacing(true);
		top2.setMargin(true);
		final Label title = new Label(prop.getProperty("ui.caption.lieblingsrezepte"));
		final Label title2 = new Label(prop.getProperty("ui.caption.lieblingszutaten"));
		title.setSizeUndefined();
		title2.setSizeUndefined();
		title.addStyleName("h1");
		title2.addStyleName("h1");
		top.addComponent(title);
		top2.addComponent(title2);
		top.setComponentAlignment(title, Alignment.TOP_LEFT);	
		top2.setComponentAlignment(title2, Alignment.TOP_LEFT);	

		container_Rezepte = new Container_Rezepte(person, true);
		container_Zutaten = new Container_Zutaten(person, true);

		mainVL.setSizeFull();
		mainVL.addComponent(top);
		mainVL.addComponent(container_Rezepte);
		mainVL.setExpandRatio(container_Rezepte, 2);
		mainVL.addComponent(new Hr());
		mainVL.addComponent(top2);
		mainVL.addComponent(container_Zutaten);
		mainVL.setExpandRatio(container_Zutaten, 2);
		
	}

	@Override
	public void enter(ViewChangeEvent event) {
	}

}

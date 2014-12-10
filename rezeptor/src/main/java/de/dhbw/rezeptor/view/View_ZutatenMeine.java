package de.dhbw.rezeptor.view;

import java.util.Properties;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import de.dhbw.rezeptor.container.Container_Zutaten;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Person;

/**
 * Klasse zur anzeige der eigenen zutaten... TODO merge mit view_warenkunde.java
 * 
 * @author dhammacher
 * 
 */
public class View_ZutatenMeine extends VerticalLayout implements View {

	private final static Properties prop = SystemProperties.getProperties();
	private Person person = new Person();
	private Container_Zutaten container_Zutaten;

	private VerticalLayout mainVL = new VerticalLayout();

	public View_ZutatenMeine() {
		setSpacing(true);
		setMargin(true);
		addComponent(mainVL);
		setExpandRatio(mainVL, 1);

		HorizontalLayout top = new HorizontalLayout();
		top.setWidth("100%");
		top.setMargin(true);
		top.setSpacing(true);
		final Label title = new Label("Meine Zutaten");
		title.setSizeUndefined();
		title.addStyleName("h1");
		top.addComponent(title);
		top.setComponentAlignment(title, Alignment.TOP_LEFT);

		// Session auslesen
		person = (Person) VaadinSession.getCurrent().getAttribute(
				prop.getProperty("session.user"));
		container_Zutaten = new Container_Zutaten(person,false);

		mainVL.setSizeFull();
		mainVL.addComponent(top);
		mainVL.addComponent(container_Zutaten);
		mainVL.setExpandRatio(container_Zutaten, 1);
	}

	@Override
	public void enter(ViewChangeEvent event) {
	}

}

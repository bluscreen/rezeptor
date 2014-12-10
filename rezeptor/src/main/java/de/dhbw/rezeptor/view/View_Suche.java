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
 * diese klasse ist identisch mit View_rezeptemeine.java
 * TODO hier das ganze mergen!!!
 * @author dhammacher
 *
 */
public class View_Suche extends VerticalLayout implements View {

	private final static Properties prop = SystemProperties.getProperties();
	private Container_Rezepte container_RezeptZutaten;
	
	private VerticalLayout mainVL = new VerticalLayout();

	public View_Suche() {
		setSpacing(true);
		setMargin(true);
		addComponent(mainVL);
		setExpandRatio(mainVL, 1);
		
		HorizontalLayout top = new HorizontalLayout();
		top.setWidth("100%");
		top.setSpacing(true);
		top.setMargin(true);
		final Label title = new Label(prop.getProperty("ui.caption.suche"));
		title.setSizeUndefined();
		title.addStyleName("h1");
		top.addComponent(title);
		top.setComponentAlignment(title, Alignment.TOP_LEFT);	

		container_RezeptZutaten = new Container_Rezepte(null,false);

		mainVL.setSizeFull();
		mainVL.addComponent(top);
		mainVL.addComponent(container_RezeptZutaten);
		mainVL.setExpandRatio(container_RezeptZutaten, 1);
	}

	@Override
	public void enter(ViewChangeEvent event) {
	}

}

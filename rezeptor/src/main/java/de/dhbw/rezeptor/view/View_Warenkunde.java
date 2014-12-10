package de.dhbw.rezeptor.view;

import java.util.Properties;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import de.dhbw.rezeptor.container.Container_Zutaten;
import de.dhbw.rezeptor.data.SystemProperties;
/**
 * Die Klasse zeigt die Zutaten an... identisch mit View_ZutatenMeine.java
 * TODO Merge!!!!
 * @author dhammacher
 *
 */
public class View_Warenkunde extends VerticalLayout implements View {

	private final static Properties prop = SystemProperties.getProperties();
	private Container_Zutaten container_Zutaten;
	
	private VerticalLayout mainVL = new VerticalLayout();

	public View_Warenkunde() {
		setSpacing(true);
		setMargin(true);
		addComponent(mainVL);
		setExpandRatio(mainVL, 1);
		
		HorizontalLayout top = new HorizontalLayout();
		top.setWidth("100%");
		top.setSpacing(true);
		top.setMargin(true);
		final Label title = new Label(prop.getProperty("ui.caption.warenkunde"));
		title.setSizeUndefined();
		title.addStyleName("h1");
		top.addComponent(title);
		top.setComponentAlignment(title, Alignment.TOP_LEFT);	

		container_Zutaten = new Container_Zutaten(null,false);

		mainVL.setSizeFull();
		mainVL.addComponent(top);
		mainVL.addComponent(container_Zutaten);
		mainVL.setExpandRatio(container_Zutaten, 1);
	}

	@Override
	public void enter(ViewChangeEvent event) {
	}

}

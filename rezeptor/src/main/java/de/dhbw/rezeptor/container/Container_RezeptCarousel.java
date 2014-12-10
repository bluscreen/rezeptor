package de.dhbw.rezeptor.container;

import java.io.File;
import java.util.Properties;

import org.vaadin.teemu.ratingstars.RatingStars;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Rezept;

/**
 * Ein Container zur Anzeige eines Rezepts im Carousel..
 * @author dhammacher
 *
 */

public class Container_RezeptCarousel extends VerticalLayout {

	private final static Properties prop = SystemProperties.getProperties();

	public Container_RezeptCarousel(Rezept dasRezept) {
		HorizontalLayout rezeptFormImageLayout = new HorizontalLayout();
		VerticalLayout rezeptKopfLayout = new VerticalLayout();

		rezeptKopfLayout.setSpacing(false);

		rezeptFormImageLayout.setMargin(true);
		rezeptFormImageLayout.setSpacing(true);

		setSpacing(true);
		setMargin(true);
		setSizeFull();

		Image image = new Image();
		image.setVisible(false);
		image.setWidth(200, UNITS_PIXELS);

		String thumburl = dasRezept.getThumburl();
		if (thumburl != null) {
			File thumb = new File(prop.getProperty("config.dir.rezept")
					+ thumburl);
			if (thumb.canRead()) {
				image.setSource(new FileResource(thumb));
				image.setVisible(true);
			}
		}
		Label title = new Label(dasRezept.getBezeichnung());
		title.setStyleName("h2");

		// Kopfdaten UI Komponenten deklarieren und initialwerte zuweisen
		Label metaInf = new Label(prop.getProperty("ui.caption.kategorie")
				+ ": " + dasRezept.getKategorie().getBezeichnung());
		Label metaInf2 = new Label(prop.getProperty("ui.caption.dauer") + ": "
				+ dasRezept.getDauer());
		Label metaInf3 = new Label(prop.getProperty("ui.caption.anzPersonen")
				+ ": " + dasRezept.getAnzahlPersonen());

		Label verfasser = new Label(dasRezept.getAnlageDatum() + " "
				+ prop.getProperty("ui.caption.von") + " "
				+ dasRezept.getVerfasser().toString());

		RatingStars rs = new RatingStars();
		rs.setValue(dasRezept.getRating());
		rezeptKopfLayout.addComponents(title, rs, metaInf, metaInf2, metaInf3,
				verfasser);

		rezeptFormImageLayout.addComponent(rezeptKopfLayout);
		rezeptFormImageLayout.addComponent(image);
		rezeptFormImageLayout.setWidth(100, Unit.PERCENTAGE);
		rezeptFormImageLayout.setExpandRatio(rezeptKopfLayout, 2);
		rezeptFormImageLayout.setExpandRatio(image, 2);
		rezeptFormImageLayout.setComponentAlignment(image, Alignment.TOP_RIGHT);
		addComponent(rezeptFormImageLayout);
//		Label spacer = new Label("");
//		spacer.setHeight("1em");
//		addComponent(spacer);
	}
}

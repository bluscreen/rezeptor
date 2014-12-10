package de.dhbw.rezeptor.container;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.vaadin.teemu.ratingstars.RatingStars;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.dhbw.rezeptor.RezeptorUI;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Bewertung;
import de.dhbw.rezeptor.domain.Favorit;
import de.dhbw.rezeptor.domain.Person;
import de.dhbw.rezeptor.domain.Rezept;
import de.dhbw.rezeptor.domain.RezeptZutat;
import de.dhbw.rezeptor.domain.Zutat;
import de.dhbw.rezeptor.util.Hr;

/**
 * UI Container zur Anzeige der ZutatDetails... Läd Bewertungen mit rein
 * @author dhammacher
 *
 */
public class Container_ZutatDetails extends VerticalLayout {

	private RezeptorUI rezui = (RezeptorUI) UI.getCurrent();
	private final static Properties prop = SystemProperties.getProperties();

	private Person person = (Person) VaadinSession.getCurrent().getAttribute(
			prop.getProperty("session.user"));
	
	private Container_Bewertung container_Bewertungen;

	public Container_ZutatDetails(BeanItem<Zutat> item) {

		FormLayout zutatKopfLayout = new FormLayout();
		HorizontalLayout zutatFormImageLayout = new HorizontalLayout();

		zutatFormImageLayout.setMargin(true);
		zutatFormImageLayout.setSpacing(true);

		setSpacing(true);
		setMargin(true);
		setSizeFull();

		Image image = new Image("Bild");
		image.setVisible(false);
		image.setWidth(200, UNITS_PIXELS);

		Zutat dieZutat = item.getBean();
		final Favorit fav = (Favorit) rezui.dataProvider.getFavoritZutatPerson(
				dieZutat, person);
		VaadinSession.getCurrent().setAttribute(
				prop.getProperty("session.lastzutatid"), dieZutat.getId());
		String thumburl = dieZutat.getThumburl();
		if (thumburl != null) {
			File thumb = new File(prop.getProperty("config.dir.zutat")
					+ thumburl);
			if (thumb.canRead()) {
				image.setSource(new FileResource(thumb));
				image.setVisible(true);
			}
		}

		// Kopfdaten
		Label bezeichnung = new Label(dieZutat.getBezeichnung());
		bezeichnung.setCaption("Bezeichnung");

		Label vegetarisch = new Label(dieZutat.isVeggie() ? "ja" : "nein");
		vegetarisch.setCaption("Vegetarisch");

		Label mengeneinheit = new Label(dieZutat.getMengeneinheit()
				.getBezeichnung());
		mengeneinheit.setCaption("Mengeneinheit");

		Label verfasser = new Label(dieZutat.getVerfasser().getVorname() + " "
				+ dieZutat.getVerfasser().getNachname());
		verfasser.setCaption("Verfasser");

		Label anlageDatum = new Label(dieZutat.getAnlageDatum()
				.toLocaleString());
		anlageDatum.setCaption("Anlagedatum");
		final Button favorit = new Button("");
		
		zutatKopfLayout.addComponents(bezeichnung, vegetarisch, mengeneinheit,
				verfasser, anlageDatum, favorit);
		zutatKopfLayout.setCaption("Zutat");

		// Beschreibung
		Label beschreibung = new Label(dieZutat.getBeschreibung());
		beschreibung.setCaption("Wissenswertes");
		beschreibung.setContentMode(ContentMode.HTML);

		zutatFormImageLayout.addComponent(zutatKopfLayout);
		zutatFormImageLayout.addComponent(image);
		addComponent(zutatFormImageLayout);
		addComponent(beschreibung);

		if (fav == null) {
			final Favorit favzwo = new Favorit();

			favorit.setCaption(prop
					.getProperty("ui.caption.favoritHinzufuegen"));
			favzwo.setZutat(dieZutat);
			favzwo.setAnlageDatum(new Date());
			favzwo.setVerfasser(person);
			favorit.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					// Favorit hinzufï¿½gen action
					favorit.setVisible(false);

					Notification n = new Notification(prop
							.getProperty("ui.caption.geschafft"));
					n.setDelayMsec(1500);
					String statusMessage = rezui.dataProvider
							.persistFavorit(favzwo);
					n.setDescription(statusMessage);
					n.show(rezui.getPage());

				}
			});
		} else {
			favorit.setCaption(prop.getProperty("ui.caption.favoritEntfernen"));
			favorit.addClickListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					// Favorit entfernen action
					favorit.setVisible(false);

					Notification n = new Notification(prop
							.getProperty("ui.caption.geschafft"));
					n.setDelayMsec(1500);

					String statusMessage = rezui.dataProvider
							.deleteFavorit(fav);
					n.setDescription(statusMessage);
					n.show(rezui.getPage());
					// rezeptContainer.removeAllItems();
					rezui.nav.navigateTo(prop.getProperty("url.favoriten"));
				}
			});
		}
		// Bewertungen
				Bewertung initialBewertung = new Bewertung();
				initialBewertung.setZutat(dieZutat);
				container_Bewertungen = new Container_Bewertung(rezui.dataProvider
						.getZutatBewertungen(dieZutat), initialBewertung, prop.getProperty("ui.caption.zutatBewerten"));
				
				addComponent(container_Bewertungen);
	}
}

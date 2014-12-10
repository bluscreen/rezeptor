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
import com.vaadin.server.ClientConnector.AttachEvent;
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
import de.dhbw.rezeptor.util.Hr;

/**
 * Dient der Detailansicht eines Rezepts.
 * @author dhammacher
 *
 */

public class Container_RezeptDetails extends VerticalLayout {

	private RezeptorUI rezui = (RezeptorUI) UI.getCurrent();
	private final static Properties prop = SystemProperties.getProperties();
	private BeanItemContainer<RezeptZutat> rezeptZutatenContainer = new BeanItemContainer<RezeptZutat>(
			RezeptZutat.class);
	
	private Container_Bewertung container_Bewertungen;

	private Person person = (Person) VaadinSession.getCurrent().getAttribute(
			prop.getProperty("session.user"));

	public Container_RezeptDetails(BeanItem<Rezept> item) {

		HorizontalLayout rezeptFormImageLayout = new HorizontalLayout();
		FormLayout rezeptKopfLayout = new FormLayout();

		rezeptFormImageLayout.setMargin(true);
		rezeptFormImageLayout.setSpacing(true);

		setSpacing(true);
		setMargin(true);
		setSizeFull();

		Image image = new Image(prop.getProperty("ui.caption.bild"));
		image.setVisible(false);
		image.setWidth(200, UNITS_PIXELS);


		// BeanItem in bean mappen
		Rezept dasRezept = item.getBean();

		// clicks um 1 erhoehen
		rezui.dataProvider.clickRezept(dasRezept);

		VaadinSession.getCurrent().setAttribute(
				prop.getProperty("session.lastrezeptid"), dasRezept.getId());

		String thumburl = dasRezept.getThumburl();
		if (thumburl != null) {
			File thumb = new File(prop.getProperty("config.dir.rezept")
					+ thumburl);
			if (thumb.canRead()) {
				image.setSource(new FileResource(thumb));
				image.setVisible(true);
			}
		}

		final Favorit fav = (Favorit) rezui.dataProvider
				.getFavoritRezeptPerson(dasRezept, person);

		final Button favorit = new Button("");
		if (fav == null) {
			final Favorit favzwo = new Favorit();

			favorit.setCaption(prop
					.getProperty("ui.caption.favoritHinzufuegen"));
			favzwo.setRezept(dasRezept);
			favzwo.setAnlageDatum(new Date());
			favzwo.setVerfasser(person);
			favorit.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					// Favorit hinzuf�gen action
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

		// Kopfdaten UI Komponenten deklarieren und initialwerte zuweisen
		Label bezeichnung = new Label(dasRezept.getBezeichnung());
		bezeichnung.setCaption(prop.getProperty("ui.caption.bezeichnung"));

		Label kategorie = new Label(dasRezept.getKategorie().getBezeichnung());
		kategorie.setCaption(prop.getProperty("ui.caption.kategorie"));

		Label dauer = new Label(Double.toString(dasRezept.getDauer()));
		dauer.setCaption(prop.getProperty("ui.caption.dauerInMinuten"));

		Label anzahlPersonen = new Label(Double.toString(dasRezept
				.getAnzahlPersonen()));
		anzahlPersonen
				.setCaption(prop.getProperty("ui.caption.anzahlPersonen"));

		Label verfasser = new Label(dasRezept.getVerfasser().getVorname() + " "
				+ dasRezept.getVerfasser().getNachname());
		verfasser.setCaption(prop.getProperty("ui.caption.verfasser"));

		Label anlageDatum = new Label(dasRezept.getAnlageDatum()
				.toLocaleString());	
		anlageDatum.setCaption(prop.getProperty("ui.caption.anlageDatum"));
		rezeptKopfLayout.addComponents(bezeichnung, kategorie, anzahlPersonen,
				dauer, anzahlPersonen, verfasser, anlageDatum, favorit);
		rezeptKopfLayout.setCaption(prop.getProperty("ui.caption.rezept"));

		rezeptZutatenContainer.removeAllItems();
		// Zutaten aufbauen
		rezeptZutatenContainer.addAll(dasRezept.getRezeptZutaten());
		Table zutaten = new Table(prop.getProperty("ui.caption.zutaten"),
				rezeptZutatenContainer);
		zutaten.setVisibleColumns(new String[] { "zutat", "menge",
				"mengeneinheit" });
		zutaten.setColumnHeaders(new String[] {
				prop.getProperty("ui.caption.zutat"),
				prop.getProperty("ui.caption.menge"),
				prop.getProperty("ui.caption.mengeneinheit") });
		zutaten.setSelectable(true);
		zutaten.setSortEnabled(true);
		zutaten.setWidth("100%");
		int tableheight = rezeptZutatenContainer.getItemIds().size()*2;
		tableheight += 3;
		zutaten.setHeight(tableheight, Unit.EM);

		// Beschreibung
		Label beschreibung = new Label(dasRezept.getBeschreibung());
		beschreibung.setCaption(prop.getProperty("ui.caption.rezeptText"));
		beschreibung.setContentMode(ContentMode.HTML);

		rezeptFormImageLayout.addComponent(rezeptKopfLayout);
		rezeptFormImageLayout.addComponent(image);
		rezeptFormImageLayout.setWidth(100, Unit.PERCENTAGE);
		rezeptFormImageLayout.setExpandRatio(rezeptKopfLayout, 2);
		rezeptFormImageLayout.setExpandRatio(image, 2);
		rezeptFormImageLayout.setComponentAlignment(image, Alignment.TOP_RIGHT);
		addComponent(rezeptFormImageLayout);
		addComponent(beschreibung);
		addComponent(zutaten);
//		rezeptFormImageLayout.addAttachListener(new AttachListener() {
//			
//			@Override
//			public void attach(AttachEvent event) {
//				HorizontalLayout hl = (HorizontalLayout)event.getSource();
//				hl.getc
//			}
//		});

		// Bewertungen
		Bewertung initialBewertung = new Bewertung();
		initialBewertung.setRezept(dasRezept);
		container_Bewertungen = new Container_Bewertung(rezui.dataProvider
				.getRezeptBewertungen(dasRezept), initialBewertung, prop.getProperty("ui.caption.rezeptBewerten"));
		
		addComponent(container_Bewertungen);
	}
}

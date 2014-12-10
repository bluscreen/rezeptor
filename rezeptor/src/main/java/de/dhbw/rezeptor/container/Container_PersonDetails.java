package de.dhbw.rezeptor.container;

import java.io.File;
import java.util.Properties;

import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.dhbw.rezeptor.RezeptorUI;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Bewertung;
import de.dhbw.rezeptor.domain.Person;
import de.dhbw.rezeptor.domain.Rezept;
import de.dhbw.rezeptor.domain.Zutat;

/**
 * Container zur Person Detailansicht, zeigt dessen rezepte, zutaten und
 * bewertungen
 * 
 * @author dhammacher
 * 
 */

public class Container_PersonDetails extends VerticalLayout {

	private RezeptorUI rezui = (RezeptorUI) UI.getCurrent();
	private final static Properties prop = SystemProperties.getProperties();
	private BeanItemContainer<Rezept> rezeptContainer = new BeanItemContainer<Rezept>(
			Rezept.class);
	private BeanItemContainer<Zutat> zutatContainer = new BeanItemContainer<Zutat>(
			Zutat.class);
	private BeanItemContainer<Bewertung> bewertungContainer = new BeanItemContainer<Bewertung>(
			Bewertung.class);

	private Person person;

	public Container_PersonDetails(BeanItem<Person> item) {
		person = (Person) VaadinSession.getCurrent().getAttribute(
				prop.getProperty("session.user"));

		HorizontalLayout personHorizontalLayout = new HorizontalLayout();
		FormLayout personKopfLayout = new FormLayout();

		personHorizontalLayout.setMargin(true);
		personHorizontalLayout.setSpacing(true);

		setSpacing(true);
		setMargin(true);
		setSizeFull();
		setWidth("100%");

		Image image = new Image(prop.getProperty("ui.caption.bild"));
		image.setVisible(false);
		image.setWidth(200, UNITS_PIXELS);

		// BeanItem in bean mappen
		Person diePerson = (Person) item.getBean();

		VaadinSession.getCurrent().setAttribute(
				prop.getProperty("session.lastpersonid"), diePerson.getId());

		String thumburl = diePerson.getThumburl();
		if (thumburl != null) {
			File thumb = new File(prop.getProperty("config.dir.user")
					+ thumburl);
			if (thumb.canRead()) {
				image.setSource(new FileResource(thumb));
				image.setVisible(true);
			}
		}

		// Kopfdaten UI Komponenten deklarieren und initialwerte zuweisen
		Label personName = new Label(diePerson.getVorname() + " "
				+ diePerson.getNachname());
		personName.setCaption(prop.getProperty("ui.caption.name"));

		Label email = new Label(diePerson.getEmail());
		email.setCaption(prop.getProperty("ui.caption.email"));

		java.util.Date gebDat = diePerson.getGeburtsdatum();
		Label geburtsdatum = new Label();
		if (gebDat != null) {
			geburtsdatum.setValue(gebDat.toGMTString());
		}

		geburtsdatum.setCaption(prop.getProperty("ui.caption.geburtsdatum"));

		personKopfLayout.addComponents(personName, email, geburtsdatum);
		personKopfLayout.setCaption(prop.getProperty("ui.caption.benutzer"));

		Table rezepte = renderPersonRezepteTable(diePerson);
		Table zutaten = renderPersonZutatenTable(diePerson);
		Table bewertungen = renderPersonBewertungenTable(diePerson);

		personHorizontalLayout.addComponent(personKopfLayout);
		personHorizontalLayout.addComponent(image);
		personHorizontalLayout.setWidth(100, Unit.PERCENTAGE);
		personHorizontalLayout.setExpandRatio(personKopfLayout, 2);
		personHorizontalLayout.setExpandRatio(image, 2);
		personHorizontalLayout
				.setComponentAlignment(image, Alignment.TOP_RIGHT);
		addComponent(personHorizontalLayout);
		addComponent(rezepte);
		addComponent(zutaten);
		addComponent(bewertungen);

	}

	private Table renderPersonRezepteTable(Person diePerson) {
		rezeptContainer.removeAllItems();
		rezeptContainer.addAll(rezui.dataProvider.getPersonRezepte(diePerson));
		Table rezepte = new Table(prop.getProperty("ui.caption.rezepte"),
				rezeptContainer);
		rezepte.setVisibleColumns(new String[] { "bezeichnung", "kategorie",
				"clicks", "rating" });
		rezepte.setColumnHeaders(new String[] {
				prop.getProperty("ui.caption.rezept"),
				prop.getProperty("ui.caption.kategorie"),
				prop.getProperty("ui.caption.views"),
				prop.getProperty("ui.caption.sterne") });
		rezepte.setSelectable(true);
		rezepte.setSortEnabled(true);
		rezepte.setWidth("100%");
		int tableheight = rezeptContainer.getItemIds().size()*2;
		tableheight += 3;
		rezepte.setHeight(tableheight, Unit.EM);
		rezepte.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				// Linksklick auf tabellenzeile baut rechte spalte auf...
				if (MouseButton.LEFT.getName().equals(event.getButtonName())) {
					if (event.getItem() != null) {
						Rezept r = (Rezept) ((BeanItem<Rezept>) event.getItem())
								.getBean();
						String fragment = prop.getProperty("url.rezeptdetails");
						rezui.clearMenuSelection();
						rezui.viewNameToMenuButton.get(fragment).addStyleName(
								"selected");
						rezui.nav.navigateTo(fragment + "/"
								+ String.valueOf(r.getId()));
					}
				}
			}
		});
		return rezepte;
	}

	private Table renderPersonZutatenTable(Person diePerson) {
		zutatContainer.removeAllItems();
		zutatContainer.addAll(rezui.dataProvider.getPersonZutaten(diePerson));
		Table zutaten = new Table(prop.getProperty("ui.caption.zutaten"),
				zutatContainer);
		zutaten.setVisibleColumns(new String[] { "bezeichnung",
				"mengeneinheit", "rating" });
		zutaten.setColumnHeaders(new String[] {
				prop.getProperty("ui.caption.bezeichnung"),
				prop.getProperty("ui.caption.mengeneinheit"),
				prop.getProperty("ui.caption.sterne") });
		zutaten.setSelectable(true);
		zutaten.setSortEnabled(true);
		zutaten.setWidth("100%");
		int tableheight = zutatContainer.getItemIds().size()*2;
		tableheight += 3;
		zutaten.setHeight(tableheight, Unit.EM);
		zutaten.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				// Linksklick auf tabellenzeile baut rechte spalte auf...
				if (MouseButton.LEFT.getName().equals(event.getButtonName())) {
					if (event.getItem() != null) {
						Zutat r = (Zutat) ((BeanItem<Zutat>) event.getItem())
								.getBean();
						VaadinSession.getCurrent().setAttribute(
								prop.getProperty("session.lastzutatid"),
								r.getId());
						rezui.nav.navigateTo(prop.getProperty("url.warenkunde"));
					}
				}
			}
		});
		return zutaten;
	}

	private Table renderPersonBewertungenTable(Person diePerson) {
		bewertungContainer.removeAllItems();
		bewertungContainer.addAll(rezui.dataProvider
				.getPersonBewertungen(diePerson));
		Table bewertungen = new Table(
				prop.getProperty("ui.caption.bewertungen"), bewertungContainer);
		bewertungen.setVisibleColumns(new String[] { "rezept", "zutat",
				"bezeichnung", "rating" });
		bewertungen.setColumnHeaders(new String[] {
				prop.getProperty("ui.caption.rezept"),
				prop.getProperty("ui.caption.zutat"),
				prop.getProperty("ui.caption.titel"),
				prop.getProperty("ui.caption.sterne") });
		bewertungen.setSelectable(true);
		bewertungen.setSortEnabled(true);
		bewertungen.setWidth("100%");
		int tableheight = bewertungContainer.getItemIds().size()*2;
		tableheight += 3;
		bewertungen.setHeight(tableheight, Unit.EM);
		bewertungen.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				// Linksklick auf tabellenzeile baut rechte spalte auf...
				if (MouseButton.LEFT.getName().equals(event.getButtonName())) {
					if (event.getItem() != null) {
						Bewertung r = (Bewertung) ((BeanItem<Bewertung>) event
								.getItem()).getBean();
						if (r.getZutat() != null) {
							Long zutatId = r.getZutat().getId();
							VaadinSession.getCurrent().setAttribute(
									prop.getProperty("session.lastzutatid"),
									zutatId);
							rezui.nav.navigateTo(prop
									.getProperty("url.warenkunde"));
						}
						// es handelt sich also um ein rezept...
						else {
							String fragment = prop
									.getProperty("url.rezeptdetails");
							rezui.clearMenuSelection();
							rezui.viewNameToMenuButton.get(fragment)
									.addStyleName("selected");
							rezui.nav.navigateTo(fragment + "/"
									+ String.valueOf(r.getRezept().getId()));
						}
					}
				}
			}
		});
		return bewertungen;
	}
}

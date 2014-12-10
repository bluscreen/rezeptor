package de.dhbw.rezeptor.container;

import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.tepi.filtertable.FilterTable;

import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.dhbw.rezeptor.RezeptorUI;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Person;
import de.dhbw.rezeptor.domain.Rezept;
import de.dhbw.rezeptor.domain.Zutat;
import de.dhbw.rezeptor.util.Berechtigor;
import de.dhbw.rezeptor.window.Window_DeleteConfirm;

/**
 * Der Rezeptcontainer stellt eine UI-Komponente zum Anzeigen der Rezepte
 * bereit: linke seite: Tabelle mit allen/meinen eigenen rezepten... diese hat
 * ein kontext menu zum zufügen, löschen (noch zu implementieren) und
 * bearbeiten der rezepte 
 * 
 * 
 * rechte seite: container_rezeptdetails.java
 * 
 * @author dhammacher
 * 
 */
public class Container_Rezepte extends VerticalLayout {

	private final static Properties prop = SystemProperties.getProperties();
	private final static Action ACTION_ADD = new Action(
			prop.getProperty("ui.caption.rezeptHinzufuegen"));
	// TODO Implementierung
	private final static Action ACTION_DELETE = new Action(
			prop.getProperty("ui.caption.rezeptLoeschen"));
	private final static Action ACTION_EDIT = new Action(
			prop.getProperty("ui.caption.rezeptBearbeiten"));

	private BeanItemContainer<Rezept> rezeptContainer = new BeanItemContainer<Rezept>(
			Rezept.class);
	private BeanItemContainer<Zutat> zutatFilterContainer = new BeanItemContainer<Zutat>(
			Zutat.class);
	private BeanItemContainer<Zutat> zutatenListeContainer = new BeanItemContainer<Zutat>(
			Zutat.class);

	private RezeptorUI rezui = (RezeptorUI) UI.getCurrent();

	private FilterTable table;
	private HorizontalSplitPanel tabellenPanel = new HorizontalSplitPanel();
	private VerticalLayout mainVL = new VerticalLayout();
	private boolean favoritesOnly = false;

	private Person person = (Person) VaadinSession.getCurrent().getAttribute(
			prop.getProperty("session.user"));
	private Person p;

	public Container_Rezepte(Person p, boolean favoritesOnly) {
		rezeptContainer.removeAllItems();
		zutatenListeContainer.removeAllItems();
		zutatFilterContainer.removeAllItems();

		// Wenn ein Rezept mit kommt, mï¿½ssen dessen Zutaten ï¿½ber den
		// Dataprovider beschafft und in einen BeanItemContainer gebolzt werden
		this.favoritesOnly = favoritesOnly;
		this.p = p;

		// TODO: FILTER
		List<Rezept> rezepte = null;
		if (favoritesOnly) {
			rezepte = rezui.dataProvider.getFavoritRezepteByPerson(p);
		} else {
			if (p == null) {
				rezepte = rezui.dataProvider.getAlleRezepte();
			} else {
				rezepte = rezui.dataProvider.getPersonRezepte(p);
			}
		}

		rezeptContainer.addAll(rezepte);
		// Und jetzt die Tabelle bauen... ogoddogodd

		tabellenPanel.setSplitPosition(100, Unit.PERCENTAGE);
		table = new FilterTable(prop.getProperty("ui.caption.rezepte"));
		table.removeAllItems();
		table.setContainerDataSource(rezeptContainer);
		table.setLocale(Locale.GERMAN);
		table.setFilterBarVisible(true);
		table.setVisibleColumns(new String[] { "clicks", "rating", "kategorie",
				"bezeichnung", "anzahlPersonen", "dauer", "anlageDatum" });
		table.setColumnHeaders(new String[] {
				prop.getProperty("ui.caption.views"),
				prop.getProperty("ui.caption.sterne"),
				prop.getProperty("ui.caption.kategorie"),
				prop.getProperty("ui.caption.bezeichnung"),
				prop.getProperty("ui.caption.anzPersonen"),
				prop.getProperty("ui.caption.dauer"),
				prop.getProperty("ui.caption.anlageDatum") });
		table.setSelectable(true);
		table.setSortEnabled(true);
		table.setSizeUndefined();
		table.setWidth(100, Unit.PERCENTAGE);

		if (table.firstItemId() != null) {
			Long rezeptId = (Long) VaadinSession.getCurrent().getAttribute(
					prop.getProperty("session.lastrezeptid"));
			Rezept rez = null;
			BeanItem<Rezept> rezBean = null;

			if (rezeptId != null) {
				// ACHTUNG: rez ist eine Instanz, die NICHT in "table" enthalten
				// ist!
				rez = rezui.dataProvider.getRezeptById(rezeptId);

				for (Object id : table.getItemIds()) {
					if (((Rezept) id).getId().equals(rezeptId)) {
						rez = (Rezept) id;
						rezBean = (BeanItem<Rezept>) table.getItem(id);
						break;
					}
				}
			} else {
				rez = (Rezept) table.firstItemId();
				rezBean = (BeanItem<Rezept>) table.getItem(rez);
			}

			table.setValue(rez);
			if (rezBean != null && rez != null) {
				tabellenPanel.setSecondComponent(new Container_RezeptDetails(
						rezBean));
				tabellenPanel.setSplitPosition(50, Unit.PERCENTAGE);
			}
		}

		// hier wird die rechte seite mit kontextinfos des in der tabelle
		// ausgewï¿½hlten objekts aufgebaut
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				// Linksklick auf tabellenzeile baut rechte spalte auf...
				if (MouseButton.LEFT.getName().equals(event.getButtonName())) {
					if (event.getItem() != null) {
						tabellenPanel
								.setSecondComponent(new Container_RezeptDetails(
										(BeanItem<Rezept>) event.getItem()));
					} else if (table.firstItemId() != null) {
						tabellenPanel
								.setSecondComponent(new Container_RezeptDetails(
										(BeanItem<Rezept>) table.getItem(table
												.firstItemId())));
					}
					tabellenPanel.setSplitPosition(50, Unit.PERCENTAGE);
				}
			}
		});
		// kontextmenu fï¿½r die rezepttabelle aufbauen
		table.addActionHandler(new Handler() {
			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if (ACTION_DELETE == action) {
					if (target instanceof Rezept) {
						Rezept t = (Rezept) target;
						Window w = new Window_DeleteConfirm(t, person);
						UI.getCurrent().addWindow(w);
						w.focus();
					}
				}
				if (ACTION_ADD == action) {
					VaadinSession.getCurrent().setAttribute(
							prop.getProperty("session.rezeptid"), null);
					rezui.nav.navigateTo(prop.getProperty("url.rezeptedit"));
				}
				if (ACTION_EDIT == action) {
					if (target instanceof Rezept) {
						Rezept t = (Rezept) target;
						if (Berechtigor.darfRan(person, t)) {
							VaadinSession.getCurrent().setAttribute(
									prop.getProperty("session.rezeptid"),
									t.getId());
							rezui.nav.navigateTo(prop
									.getProperty("url.rezeptedit"));
						} else {
							Notification.show(prop
									.getProperty("error.berechtigung"));
						}
					}
				}
			}

			@Override
			public Action[] getActions(Object target, Object sender) {
				return new Action[] { ACTION_ADD, ACTION_DELETE, ACTION_EDIT };
			}
		});
		// linke spalte fï¿½llen
		tabellenPanel.setFirstComponent(table);
		mainVL.setSizeUndefined();
		mainVL.setWidth("100%");
		mainVL.addComponent(createFilterLayout());
		mainVL.addComponent(tabellenPanel);
		addComponent(mainVL);
	}

	private Layout createFilterLayout() {
		HorizontalLayout filterLayout = new HorizontalLayout();
		filterLayout.setMargin(true);
		filterLayout.setSpacing(true);
		filterLayout.setSizeUndefined();

		zutatFilterContainer.addAll(rezui.dataProvider.getAlleZutaten());

		// Kombobox zum auswï¿½hlen der hinzuzufï¿½genden zutat
		final ComboBox zutatFilterBox = new ComboBox(
				prop.getProperty("ui.caption.zutatenFilter"));
		zutatFilterBox.setContainerDataSource(zutatFilterContainer);
		final ListSelect zutatenFilterListe = new ListSelect(
				prop.getProperty("ui.caption.zutatenListe"));
		zutatenFilterListe.setSizeUndefined();
		zutatenFilterListe.setWidth(20, Unit.EM);
		zutatenFilterListe.setRows(3);
		zutatenFilterListe.setContainerDataSource(zutatenListeContainer);
		zutatenListeContainer.removeAllItems();
		zutatenFilterListe.removeItem(null);
		zutatenFilterListe.setNullSelectionAllowed(false);
		zutatFilterBox.removeItem(null);
		zutatFilterBox.setNullSelectionAllowed(false);
		zutatFilterBox.setValue(zutatFilterContainer.firstItemId());

		final Button addZutat = new Button(
				prop.getProperty("ui.caption.hinzufuegen"));
		addZutat.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				// RezeptZutat rz = new RezeptZutat();
				Zutat z = (Zutat) zutatFilterBox.getValue();
				// rz.setZutat(z);
				// rz.setMengeneinheit(z.getMengeneinheit());
				zutatFilterContainer.removeItem(z);
				zutatenListeContainer.addBean(z);
				zutatFilterBox.setValue(zutatFilterContainer.firstItemId());
				zutatenFilterListe.setValue(z);
				zutatenFilterListe.select(z);
			}
		});

		final Button delZutat = new Button(
				prop.getProperty("ui.caption.loeschen"));
		delZutat.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				// RezeptZutat rz = new RezeptZutat();
				zutatFilterContainer.addItem(zutatenFilterListe.getValue());
				// TODO
				// zutatFilterContainer.sort();
				String propIds[] = { "bezeichnung", "mengeneinheit" };
				boolean sortOrders[] = { true, true };
				zutatFilterContainer.sort(propIds, sortOrders);
				zutatenFilterListe.removeItem(zutatenFilterListe.getValue());
			}
		});

		// hier geht die filtermagic ab...
		zutatenFilterListe
				.addItemSetChangeListener(new ItemSetChangeListener() {

					@Override
					public void containerItemSetChange(ItemSetChangeEvent event) {
						// wir bauen ein nativequery auf, dafï¿½r gibt es 2
						// params, anzahl der zutaten und das set der zutaten
						// als string
						int anzahl = event.getContainer().getItemIds().size();
						if (anzahl > 0) {
							String zutaten = "(";
							int i = 1;
							for (Object id : event.getContainer().getItemIds()) {
								Zutat z = (Zutat) id;
								zutaten += z.getId();
								if (i < anzahl) {
									zutaten += ",";
								}
								i++;
							}
							zutaten += ")";
							List<Rezept> filteredRezepte = null;
							// und ab in das monsterdao..
							if (!favoritesOnly) {
								filteredRezepte = rezui.dataProvider
										.getFilteredRezepte(zutaten, anzahl,
												null);
							} else {
								filteredRezepte = rezui.dataProvider
										.getFilteredRezepte(zutaten, anzahl,
												person);
							}

							// jetzt mï¿½ssen wir noch den rezeptcontainer
							// refreshen...
							rezeptContainer.removeAllItems();
							rezeptContainer.addAll(filteredRezepte);
						} else {
							List<Rezept> rezepte = null;
							rezeptContainer.removeAllItems();
							if (favoritesOnly) {
								rezepte = rezui.dataProvider
										.getFavoritRezepteByPerson(p);
							} else {
								if (p == null) {

									rezepte = rezui.dataProvider
											.getAlleRezepte();
								} else {
									rezepte = rezui.dataProvider
											.getPersonRezepte(p);
								}
							}
							rezeptContainer.addAll(rezepte);
						}
					}
				});

		filterLayout.addComponent(zutatFilterBox);
		filterLayout.addComponent(addZutat);
		filterLayout.addComponent(zutatenFilterListe);
		filterLayout.addComponent(delZutat);
		filterLayout.setComponentAlignment(zutatFilterBox, Alignment.TOP_LEFT);
		filterLayout.setComponentAlignment(addZutat, Alignment.MIDDLE_LEFT);
		filterLayout.setComponentAlignment(delZutat, Alignment.MIDDLE_LEFT);

		return filterLayout;
	}
}

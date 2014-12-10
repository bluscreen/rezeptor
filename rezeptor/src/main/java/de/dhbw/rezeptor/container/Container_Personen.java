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
import de.dhbw.rezeptor.window.Window_PersonEdit;

/**
 *
 *  Ähnlich Container REzepte, nur mit Personen
 * 
 * rechte seite: container_persondetails.java
 * 
 * @author dhammacher
 * 
 */
public class Container_Personen extends VerticalLayout {

	private final static Properties prop = SystemProperties.getProperties();
	private final static Action ACTION_ADD = new Action(
			prop.getProperty("ui.caption.personHinzufuegen"));
	// TODO Implementierung
	private final static Action ACTION_DELETE = new Action(
			prop.getProperty("ui.caption.personLoeschen"));
	private final static Action ACTION_EDIT = new Action(
			prop.getProperty("ui.caption.personBearbeiten"));

	private BeanItemContainer<Person> personContainer = new BeanItemContainer<Person>(
			Person.class);

	private RezeptorUI rezui = (RezeptorUI) UI.getCurrent();

	private FilterTable table;
	private HorizontalSplitPanel tabellenPanel = new HorizontalSplitPanel();
	private VerticalLayout mainVL = new VerticalLayout();

	private Person person = (Person) VaadinSession.getCurrent().getAttribute(
			prop.getProperty("session.user"));

	public Container_Personen() {
		personContainer.removeAllItems();
		personContainer.addAll(rezui.dataProvider.getAllePersonen());
		// Und jetzt die Tabelle bauen... ogoddogodd

		tabellenPanel.setSplitPosition(100, Unit.PERCENTAGE);
		table = new FilterTable(prop.getProperty("ui.caption.user"));
		table.removeAllItems();
		table.setContainerDataSource(personContainer);
		table.setLocale(Locale.GERMAN);
		table.setFilterBarVisible(true);
		table.setVisibleColumns(new String[] { "vorname", "nachname",
				"geburtsdatum", "anzRezepte", "anzZutaten", "anzBewertungen",
				"lastLoginDate" });
		table.setColumnHeaders(new String[] {
				prop.getProperty("ui.caption.vorname"),
				prop.getProperty("ui.caption.nachname"),
				prop.getProperty("ui.caption.geburtsdatum"),
				prop.getProperty("ui.caption.rezepte"),
				prop.getProperty("ui.caption.zutaten"),
				prop.getProperty("ui.caption.bewertungen"),
				prop.getProperty("ui.caption.letzterLogin") });
		table.setSelectable(true);
		table.setSortEnabled(true);
		table.setSizeUndefined();
		table.setWidth(100, Unit.PERCENTAGE);

		// hier wird die rechte seite mit kontextinfos des in der tabelle
		// ausgewï¿½hlten objekts aufgebaut
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				// Linksklick auf tabellenzeile baut rechte spalte auf...
				if (MouseButton.LEFT.getName().equals(event.getButtonName())) {
					if (event.getItem() != null) {
						tabellenPanel
								.setSecondComponent(new Container_PersonDetails(
										(BeanItem<Person>) event.getItem()));
					} else if (table.firstItemId() != null) {
						tabellenPanel
								.setSecondComponent(new Container_PersonDetails(
										(BeanItem<Person>) table.getItem(table
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
					if (target instanceof Person) {
						Person t = (Person) target;
						Window w = new Window_DeleteConfirm(t, person);
						UI.getCurrent().addWindow(w);
						w.focus();
					}
				}
				if (ACTION_ADD == action) {
					Window w = new Window_PersonEdit(null);
					UI.getCurrent().addWindow(w);
					w.focus();
				}
				if (ACTION_EDIT == action) {
					if (target instanceof Person) {
						Person t = (Person) target;
						if (Berechtigor.darfRan(person, t)) {
							Window w = new Window_PersonEdit(t);
							UI.getCurrent().addWindow(w);
							w.focus();
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
		mainVL.setSizeFull();
		mainVL.addComponent(tabellenPanel);
		addComponent(mainVL);
	}
}

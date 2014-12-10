package de.dhbw.rezeptor.container;

import java.util.List;
import java.util.Properties;

import org.tepi.filtertable.FilterTable;

import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.dhbw.rezeptor.RezeptorUI;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Person;
import de.dhbw.rezeptor.domain.Zutat;
import de.dhbw.rezeptor.util.Berechtigor;
import de.dhbw.rezeptor.util.ZutatenFilterDecorator;
import de.dhbw.rezeptor.window.Window_DeleteConfirm;

/**
 * Dieser UI-Container dient der anzeige der zutaten ... er funktioniert
 * prinzipiell wie die klasse Container_Rezepte.java... daher könnten diese
 * beiden komponenten in späterer version zu einer generischen klasse zusammen
 * geführt werden
 * 
 * die erkläung bitte aus container_rezepte.java entnehmen!!
 * 
 * @author dhammacher
 * 
 */
public class Container_Zutaten extends VerticalLayout {

	private final static Properties prop = SystemProperties.getProperties();

	private final static Action ACTION_ADD = new Action(
			prop.getProperty("ui.caption.zutatHinzufuegen"));
	private final static Action ACTION_DELETE = new Action(
			prop.getProperty("ui.caption.zutatLoeschen"));
	private final static Action ACTION_EDIT = new Action(
			prop.getProperty("ui.caption.zutatBearbeiten"));

	private BeanItemContainer<Zutat> zutatenContainer = new BeanItemContainer<Zutat>(
			Zutat.class);

	private RezeptorUI rezui = (RezeptorUI) UI.getCurrent();

	private final FilterTable table;
	private HorizontalSplitPanel tabellenPanel = new HorizontalSplitPanel();
	private boolean favoritesOnly = false;
	private Person person = (Person) VaadinSession.getCurrent().getAttribute(
			prop.getProperty("session.user"));
	private Person p;

	public Container_Zutaten(Person p, boolean favoritesOnly) {
		zutatenContainer.removeAllItems();

		this.favoritesOnly = favoritesOnly;
		this.p = p;

		// TODO: FILTER
		List<Zutat> zutatenListe = null;
		if (favoritesOnly) {
			zutatenListe = rezui.dataProvider.getFavoritZutatenByPerson(p);
		} else {
			if (p == null)
				zutatenListe = rezui.dataProvider.getAlleZutaten();
			else
				zutatenListe = rezui.dataProvider.getPersonZutaten(p);
		}
		zutatenContainer.addAll(zutatenListe);
		// Und jetzt die Tabelle bauen... ogoddogodd

		tabellenPanel.setSplitPosition(100, Unit.PERCENTAGE);
		table = new FilterTable("Zutaten");
		table.setContainerDataSource(zutatenContainer);
		table.setFilterDecorator(new ZutatenFilterDecorator());
		table.setFilterBarVisible(true);
		table.setVisibleColumns(new String[] { "bezeichnung", "veggie",
				"mengeneinheit", "rating" });
		table.setColumnHeaders(new String[] {
				prop.getProperty("ui.caption.bezeichnung"),
				prop.getProperty("ui.caption.vegetarisch"),
				prop.getProperty("ui.caption.mengeneinheit"),
				prop.getProperty("ui.caption.sterne") });
		table.setSelectable(true);
		table.setSortEnabled(true);
		table.setSizeFull();
		if (table.firstItemId() != null) {
			Long zutatId = (Long) VaadinSession.getCurrent().getAttribute(
					prop.getProperty("session.lastzutatid"));
			Zutat zut = null;
			BeanItem<Zutat> zutBean = null;

			if (zutatId != null) {
				// ACHTUNG: rez ist eine Instanz, die NICHT in "table" enthalten
				// ist!
				zut = rezui.dataProvider.getZutatById(zutatId);

				for (Object id : table.getItemIds()) {
					if (((Zutat) id).getId().equals(zutatId)) {
						zut = (Zutat) id;
						zutBean = (BeanItem<Zutat>) table.getItem(id);
						break;
					}
				}
			} else {
				zut = (Zutat) table.firstItemId();
				zutBean = (BeanItem<Zutat>) table.getItem(zut);
			}

			table.setValue(zut);
			tabellenPanel.setSplitPosition(50, Unit.PERCENTAGE);
			tabellenPanel
					.setSecondComponent(new Container_ZutatDetails(zutBean));
		}
		table.addItemClickListener(new ItemClickListener() {

			@Override
			public void itemClick(ItemClickEvent event) {
				if (MouseButton.LEFT.getName().equals(event.getButtonName())) {
					tabellenPanel
							.setSecondComponent(new Container_ZutatDetails(
									(BeanItem<Zutat>) event.getItem()));
					tabellenPanel.setSplitPosition(50, Unit.PERCENTAGE);
				}
			}
		});

		table.addActionHandler(new Handler() {
			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if (ACTION_DELETE == action) {
					if (target instanceof Zutat) {
						Zutat t = (Zutat) target;
						Window w = new Window_DeleteConfirm(t, person);
						UI.getCurrent().addWindow(w);
						w.focus();
					}
					// zutatenContainer.removeItem(target);
				}
				if (ACTION_ADD == action) {
					VaadinSession.getCurrent().setAttribute(
							prop.getProperty("session.zutatid"), null);
					rezui.nav.navigateTo("/zutatedit");
				}
				if (ACTION_EDIT == action) {
					if (target instanceof Zutat) {
						Zutat t = (Zutat) target;
						if (Berechtigor.darfRan(person, t)) {
							VaadinSession.getCurrent().setAttribute(
									prop.getProperty("session.zutatid"),
									t.getId());
							rezui.nav.navigateTo("/zutatedit");
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

		tabellenPanel.setFirstComponent(table);
		addComponent(tabellenPanel);
	}
}

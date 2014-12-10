package de.dhbw.rezeptor.container;

import java.util.Properties;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.dhbw.rezeptor.RezeptorUI;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Mengeneinheit;
import de.dhbw.rezeptor.domain.Rezept;
import de.dhbw.rezeptor.domain.RezeptZutat;
import de.dhbw.rezeptor.domain.Zutat;

/**
 * Dieser UI-Container dient dem Verwalten der Zutaten bei anlage/bearbeiten des
 * rezeptes
 * 
 * @author dhammacher
 * 
 */
public class Container_RezeptZutaten extends VerticalLayout {

	private final static Properties prop = SystemProperties.getProperties();

	private final static Action ACTION_ADD = new Action(
			prop.getProperty("ui.caption.zutatHinzufuegen"));
	private final static Action ACTION_DELETE = new Action(
			prop.getProperty("ui.caption.zutatLoeschen"));

	private BeanItemContainer<Zutat> zutatenHinzufuegenContainer = new BeanItemContainer<Zutat>(
			Zutat.class);
	private BeanItemContainer<Zutat> rezeptZutatAnpassenContainer = new BeanItemContainer<Zutat>(
			Zutat.class);
	private BeanItemContainer<Mengeneinheit> mengeneinheitenContainer = new BeanItemContainer<Mengeneinheit>(
			Mengeneinheit.class);
	private BeanItemContainer<RezeptZutat> rezeptZutatenContainer = new BeanItemContainer<RezeptZutat>(
			RezeptZutat.class);

	private final Table table;
	private HorizontalSplitPanel tabellenPanel = new HorizontalSplitPanel();

	private RezeptorUI rezui = (RezeptorUI) UI.getCurrent();
	private Rezept rez;

	public Container_RezeptZutaten(Rezept rezept) {

		rezeptZutatAnpassenContainer.removeAllItems();
		zutatenHinzufuegenContainer.removeAllItems();
		mengeneinheitenContainer.removeAllItems();
		rezeptZutatAnpassenContainer.removeAllItems();

		mengeneinheitenContainer.addAll(rezui.dataProvider
				.getAlleMengeneinheiten());
		zutatenHinzufuegenContainer.addAll(rezui.dataProvider.getAlleZutaten());
		rezeptZutatAnpassenContainer
				.addAll(rezui.dataProvider.getAlleZutaten());

		// Wenn ein Rezept mit kommt, müssen dessen Zutaten über den
		// Dataprovider beschafft und in einen BeanItemContainer gebolzt werden
		if (rezept instanceof Rezept && rezept.getId() != null
				&& !rezept.getId().equals("")) {
			rez = rezept;
			rezeptZutatenContainer.addAll(rez.getRezeptZutaten());

		} else {
			rez = new Rezept();
			tabellenPanel.setSplitPosition(100, Unit.PERCENTAGE);
		}

		// Jetzt die Selectbox zum Auswählen der neuen Zutat aufbauen
		// TODO Wenn noch Zeit ist Drag & Drop oder multiselect mal
		// ausprobieren...

		// Kombobox zum ausw�hlen der hinzuzuf�genden zutat
		final ComboBox zutatHinzufuegenComboBox = renderZutatHinzufuegenComboBox();

		final Button addZutat = new Button(
				prop.getProperty("ui.caption.hinzufuegen"));
		addZutat.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				Zutat z = (Zutat) zutatHinzufuegenComboBox.getValue();
				boolean found = false;
				for (RezeptZutat r : rezeptZutatenContainer.getItemIds()) {
					if (r.getZutat().getId() == z.getId()) {
						found = true;
						break;
					}
				}
				if (found == false) {
					RezeptZutat rz = new RezeptZutat();
					rz.setZutat(z);
					rz.setMengeneinheit(z.getMengeneinheit());
					rezeptZutatenContainer.addBean(rz);
//					int tableheight = rezeptZutatenContainer.getItemIds()
//							.size()*2;
//					tableheight += 3;
//					table.setHeight(tableheight, Unit.EM);
				}
			}
		});

		// Die SelectBox und den button der geilheit in das layout
		// kloppen
		HorizontalLayout zutatenAddLayout = new HorizontalLayout();
		zutatenAddLayout.setSpacing(true);
		zutatenAddLayout.setMargin(true);
		zutatenAddLayout.addComponent(zutatHinzufuegenComboBox);
		zutatenAddLayout.addComponent(addZutat);
		zutatenAddLayout.setComponentAlignment(addZutat, Alignment.BOTTOM_LEFT);

		// Und jetzt die Tabelle bauen... ogoddogodd

		table = new Table(prop.getProperty("ui.caption.zutaten"),
				rezeptZutatenContainer);
		table.setVisibleColumns(new String[] { "zutat", "menge",
				"mengeneinheit" });
		table.setColumnHeaders(new String[] {
				prop.getProperty("ui.caption.zutat"),
				prop.getProperty("ui.caption.menge"),
				prop.getProperty("ui.caption.mengeneinheit") });
		table.setSelectable(true);
//		table.setWidth(100, Unit.PERCENTAGE);
		table.setSizeFull();
		// dieser clicklistener h�rt auf einen linksklick auf ein tabellenitem
		// und baut die rechte spalte zum editieren der werte des item auf
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if (MouseButton.LEFT.getName().equals(event.getButtonName())) {
					if (event.getItem() != null) {
						tabellenPanel
								.setSecondComponent(createForm((BeanItem<RezeptZutat>) event
										.getItem()));
					} else if (table.firstItemId() != null) {
						tabellenPanel
								.setSecondComponent(createForm((BeanItem<RezeptZutat>) table
										.getItem(table.firstItemId())));
					}
					tabellenPanel.setSplitPosition(60, Unit.PERCENTAGE);
				}
			}
		});

		// kontextmenu f�r rezeptzutatentabelle
		table.addActionHandler(new Handler() {
			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if (ACTION_DELETE == action) {
					rezeptZutatenContainer.removeItem(target);
				}
				if (ACTION_ADD == action) {
					addZutat.click();
				}
			}

			@Override
			public Action[] getActions(Object target, Object sender) {
				return new Action[] { ACTION_ADD, ACTION_DELETE };
			}
		});

//		int tableheight = rezeptZutatenContainer.getItemIds().size();
//		tableheight += 5;
//		table.setHeight(tableheight, Unit.EM);

		tabellenPanel.setFirstComponent(table);
		tabellenPanel.setWidth("100%");
		tabellenPanel.setHeight(300, Unit.PIXELS);
		addComponent(zutatenAddLayout);
		addComponent(tabellenPanel);

	}

	private FormLayout createForm(BeanItem<RezeptZutat> item) {

		// diese methode baut die rechte seite des panels auf
		// TODO beim editieren werden die werte des tabellenitems nicht rein
		// gelesen...
		RezeptZutat dieRezeptZutat = item.getBean();
		FormLayout rezeptZutatEditLayout = new FormLayout();
		rezeptZutatEditLayout.setSpacing(true);
		rezeptZutatEditLayout.setMargin(true);
		final BeanFieldGroup<RezeptZutat> rezeptZutatFieldGroup = new BeanFieldGroup<RezeptZutat>(
				RezeptZutat.class);

		rezeptZutatFieldGroup.setItemDataSource(item);
		try {
			rezeptZutatFieldGroup.commit();
		} catch (CommitException e1) {
			Notification.show(prop.getProperty("error.commitFG")
					+ e1.getCause().getMessage(), Type.WARNING_MESSAGE);
		}

		// Zuerst den Commitbutton...
		Button rezeptZutatCommit = new Button(
				prop.getProperty("ui.caption.speichern"));
		rezeptZutatCommit.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					rezeptZutatFieldGroup.commit();
				} catch (CommitException e) {
					Notification.show(prop.getProperty("error.commitFG") + "\n"
							+ e.getCause().getMessage(), Type.ERROR_MESSAGE);
				}
			}
		});

		// über die properties des items laufen... UI komponenten bauen,
		// container zuweisen... an fieldgroup ud layout binden
		for (Object propertyId : rezeptZutatFieldGroup.getUnboundPropertyIds()) {
			if (propertyId.equals("zutat")) {
				ComboBox rezeptZutat = renderRezeptZutatAnpassenComboBox(dieRezeptZutat);
				rezeptZutatFieldGroup.bind(rezeptZutat, propertyId);
				rezeptZutatEditLayout.addComponent(rezeptZutat);
			} else if (propertyId.equals("mengeneinheit")) {
				ComboBox rezeptZutatMengeneinheit = renderMengenEinheitenComboBox(dieRezeptZutat);
				rezeptZutatFieldGroup
						.bind(rezeptZutatMengeneinheit, propertyId);
				rezeptZutatEditLayout.addComponent(rezeptZutatMengeneinheit);
			} else if (propertyId.equals("menge")) {
				rezeptZutatEditLayout.addComponent(rezeptZutatFieldGroup
						.buildAndBind(propertyId));
			}
			// rezeptZutatCommit.click();
		}
		rezeptZutatEditLayout.addComponent(rezeptZutatCommit);
		return rezeptZutatEditLayout;
	}

	private ComboBox renderZutatHinzufuegenComboBox() {
		ComboBox zutatenBox = new ComboBox(prop.getProperty("ui.caption.zutat"));
		zutatenBox.setImmediate(true);
		zutatenBox.setInvalidAllowed(false);
		zutatenBox.setNullSelectionAllowed(false);
		zutatenBox.setContainerDataSource(zutatenHinzufuegenContainer);
		zutatenBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		zutatenBox.setItemCaptionPropertyId("bezeichnung");
		zutatenBox.setValue(zutatenHinzufuegenContainer.firstItemId());

		return zutatenBox;
	}

	private ComboBox renderMengenEinheitenComboBox(RezeptZutat zutat) {
		ComboBox mengenEinheitenBox = new ComboBox(
				prop.getProperty("ui.caption.mengeneinheit"));
		mengenEinheitenBox.setImmediate(true);
		mengenEinheitenBox.setInvalidAllowed(false);
		mengenEinheitenBox.setNullSelectionAllowed(false);
		mengenEinheitenBox.setContainerDataSource(mengeneinheitenContainer);
		mengenEinheitenBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		mengenEinheitenBox.setItemCaptionPropertyId("bezeichnung");
		// Aktuelle Mengeneinheit der Zutat lesen
		Mengeneinheit mengenEinheit = zutat.getMengeneinheit();

		// Wenn diese null ist...
		if (mengenEinheit == null) {
			// Einen Defaultwert annehmen
			BeanItem<Mengeneinheit> mengenItem = mengeneinheitenContainer
					.getItem(mengeneinheitenContainer.firstItemId());
			zutat.setMengeneinheit(mengenItem.getBean());
		} else {
			// mengenitem holen
			BeanItem<Mengeneinheit> mengenItem = null, tempItem;
			for (Mengeneinheit item : mengeneinheitenContainer.getItemIds()) {
				tempItem = mengeneinheitenContainer.getItem(item);
				if (tempItem.getBean().getId() == mengenEinheit.getId()) {
					mengenItem = tempItem;
					break;
				}
			}

			zutat.setMengeneinheit(mengenItem.getBean());
		}
		return mengenEinheitenBox;
	}

	private ComboBox renderRezeptZutatAnpassenComboBox(RezeptZutat zutat) {
		ComboBox zutatenBox = new ComboBox(prop.getProperty("ui.caption.zutat"));
		zutatenBox.setImmediate(true);
		zutatenBox.setInvalidAllowed(false);
		zutatenBox.setNullSelectionAllowed(false);
		zutatenBox.setContainerDataSource(rezeptZutatAnpassenContainer);
		zutatenBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		zutatenBox.setItemCaptionPropertyId("bezeichnung");

		// Aktuelle Zutat aus der Rezeptzutat lesen
		Zutat dieZutat = zutat.getZutat();

		// Wenn diese null ist...
		if (dieZutat == null) {
			// ... einen Defaultwert annehmen
			BeanItem<Zutat> zutatItem = rezeptZutatAnpassenContainer
					.getItem(rezeptZutatAnpassenContainer.firstItemId());
			zutat.setZutat(zutatItem.getBean());
		} else {
			// ... sonst aktuelle zutat lesen
			BeanItem<Zutat> zutatItem = null, tempItem;

			for (Zutat item : rezeptZutatAnpassenContainer.getItemIds()) {
				tempItem = rezeptZutatAnpassenContainer.getItem(item);
				if (tempItem.getBean().getId() == dieZutat.getId()) {
					zutatItem = tempItem;
					break;
				}
			}
			zutat.setZutat(zutatItem.getBean());
		}
		return zutatenBox;
	}

	public Table getTable() throws CommitException {
		if (table.getItemIds().size() < 1)
			throw new CommitException(prop.getProperty("error.rezeptZutatReq"),
					new Throwable(prop.getProperty("error.rezeptZutatReq")));

		return table;
	}
}

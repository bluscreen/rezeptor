package de.dhbw.rezeptor.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

import de.dhbw.rezeptor.RezeptorUI;
import de.dhbw.rezeptor.container.Container_RezeptZutaten;
import de.dhbw.rezeptor.data.ImageUploader;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Kategorie;
import de.dhbw.rezeptor.domain.Person;
import de.dhbw.rezeptor.domain.Rezept;
import de.dhbw.rezeptor.domain.RezeptZutat;

/**
 * Die RezeptEdit view stellt UI-Komponenten zum anlegen und bearbeiten von
 * rezepten bereit sie implementiert Container_RezeptZutaten.java
 * 
 * @author dhammacher
 * 
 */
public class View_RezeptEdit extends VerticalLayout implements View {

	private final static Properties prop = SystemProperties.getProperties();
	// container und was man sonst noch so braucht
	private BeanItemContainer<Kategorie> kategorienContainer = new BeanItemContainer<Kategorie>(
			Kategorie.class);
	private Person user = (Person) VaadinSession.getCurrent().getAttribute(
			prop.getProperty("session.user"));
	private Long rezeptId = (Long) VaadinSession.getCurrent().getAttribute(
			prop.getProperty("session.rezeptid"));
	private Rezept rezept = new Rezept();
	private String filename = "X";
	private List<RezeptZutat> rezeptZutatenListe = new ArrayList<RezeptZutat>();
	private RezeptorUI rezui = (RezeptorUI) UI.getCurrent();
	private Container_RezeptZutaten container_RezeptZutaten;

	private VerticalLayout mainVL = new VerticalLayout();

	private void doLayoutSettings() {
		setSpacing(true);
		setMargin(true);
		addComponent(mainVL);
		setExpandRatio(mainVL, 1);
	}

	public View_RezeptEdit() {
		rezui.clearMenuSelection();

		doLayoutSettings();

		HorizontalLayout titelLeiste = new HorizontalLayout();
		titelLeiste.setSpacing(true);
		titelLeiste.setMargin(true);
		HorizontalLayout rezeptKopfDatenHorizontalLayout = new HorizontalLayout();
		FormLayout rezeptKopfDatenFormLayout = new FormLayout();

		TextField bezeichnung = new TextField(
				prop.getProperty("ui.caption.titel"));
		TextField anzahlPersonen = new TextField(
				prop.getProperty("ui.caption.anzahlPersonen"));
		TextField dauer = new TextField(
				prop.getProperty("ui.caption.dauerInMinuten"));
		Label spacer = new Label("");
		spacer.setHeight("1em");
		Label titel = new Label("");
		final Button commitButton = new Button(
				prop.getProperty("ui.caption.rezeptSpeichern"));
		RichTextArea beschreibung = new RichTextArea();
		final Image image = new Image(prop.getProperty("ui.caption.bild"));
		final ImageUploader receiver;
		final Upload upload = new Upload();

		// wir wissen noch nicht ob das rezept ein bild hat..
		image.setVisible(false);

		// Rezept(Huelle beim anlegen) in einen container laden
		if (rezeptId == null) {
			// Rezept hinzufügen
			rezept.setVerfasser(user);
			rezept.setAnzahlPersonen(2);
			rezept.setBeschreibung("");
			rezept.setBezeichnung("");
			rezept.setDauer(15);
			rezept.setAnlageDatum(new Date());
		} else {
			// Rezept bearbeiten
			if (rezui != null) {
				// UI mit dataProvider gefunden. Hole Rezept aus DB
				rezept = rezui.dataProvider.getRezeptById(rezeptId);
				String thumburl = rezept.getThumburl();
				if (thumburl != null) {
					File thumb = new File(prop.getProperty("config.dir.rezept")
							+ thumburl);
					if (thumb.canRead()) {
						image.setSource(new FileResource(thumb));
						image.setVisible(true);
					}
				}
			}
		}
		titel.setValue((rezept == null || rezept.getBezeichnung() == null || rezept
				.getBezeichnung().equals("")) ? prop
				.getProperty("ui.caption.titelRezeptErstellen") : prop
				.getProperty("ui.caption.titelRezeptBearbeiten")
				+ " "
				+ rezept.getBezeichnung());

		image.setWidth(200, UNITS_PIXELS);
		receiver = new ImageUploader(image, filename,
				prop.getProperty("config.dir.rezept"));
		upload.setReceiver(receiver);
		upload.setButtonCaption(prop.getProperty("ui.caption.hochladen"));
		upload.setLocale(Locale.GERMAN);
		upload.addSucceededListener(receiver);
		upload.setEnabled(rezept.getBezeichnung() != "");

		titel.setSizeUndefined();
		titel.addStyleName("h1");

		titelLeiste.setWidth("100%");
		titelLeiste.addComponent(titel);
		titelLeiste.setComponentAlignment(titel, Alignment.TOP_LEFT);

		beschreibung.setLocale(Locale.GERMAN);
		beschreibung.setCaption(prop.getProperty("ui.caption.beschreibung"));

		// RezeptZutatenContainer aufbauen
		container_RezeptZutaten = new Container_RezeptZutaten(rezept);

		// Selectbox fuer Auswahl der Kategorie
		ComboBox kategorienComboBox = renderKategorienComboBox();

		// TODO FLAG public/private veröffentlichungsfunktion für rezepte

		// UI-Komponenten an die Item Properties binden
		final BeanFieldGroup<Rezept> rezeptDatenFieldGroup = new BeanFieldGroup<Rezept>(
				Rezept.class);
		rezeptDatenFieldGroup.setItemDataSource(rezept);
		rezeptDatenFieldGroup.bind(bezeichnung, "bezeichnung");
		rezeptDatenFieldGroup.bind(anzahlPersonen, "anzahlPersonen");
		rezeptDatenFieldGroup.bind(beschreibung, "beschreibung");
		rezeptDatenFieldGroup.bind(dauer, "dauer");
		rezeptDatenFieldGroup.bind(kategorienComboBox, "kategorie");

		// Komponenten an das Layout binden
		rezeptKopfDatenFormLayout.setCaption("Rezept");
		rezeptKopfDatenFormLayout.addComponents(bezeichnung,
				kategorienComboBox, dauer, anzahlPersonen);

		rezeptKopfDatenHorizontalLayout.setSizeUndefined();
		rezeptKopfDatenHorizontalLayout.setSpacing(true);
		rezeptKopfDatenHorizontalLayout.setMargin(true);
		rezeptKopfDatenHorizontalLayout.addComponent(rezeptKopfDatenFormLayout);

		rezeptKopfDatenFormLayout.addComponent(spacer);
		rezeptKopfDatenFormLayout.addComponent(commitButton);
		rezeptKopfDatenFormLayout.setComponentAlignment(commitButton,
				Alignment.BOTTOM_RIGHT);

		TextChangeListener textChangeListener = new TextChangeListener() {
			@Override
			public void textChange(TextChangeEvent event) {
				// TODO hier noch validierung einbauen... note:
				// https://vaadin.com/forum/#!/thread/367582
				// AbstractTextField source = (AbstractTextField)
				// event.getSource();
				// System.err.println(event.getText() + " - " +
				// source.getValue());

				// wir leiten den filename des bildes von der bezeichnung der
				// zutat ab und erweitern ihn um timestamp und ein bisschen
				// random um doppelte dateinamen auszuschließen
				if (event.getText().length() > 0) {
					upload.setEnabled(true);
					filename = event.getText();
					receiver.setFilename(filename);
				} else
					upload.setEnabled(false);
			}
		};

		bezeichnung.setImmediate(true);
		bezeichnung.setRequired(true);
		bezeichnung.setRequiredError(prop.getProperty("error.bezeichnungReq"));
		bezeichnung.addTextChangeListener(textChangeListener);

		VerticalLayout uploadVerticalLayout = new VerticalLayout();
		uploadVerticalLayout.setMargin(true);
		uploadVerticalLayout.setSpacing(true);
		uploadVerticalLayout.addComponents(beschreibung, upload);

		rezeptKopfDatenHorizontalLayout.addComponent(uploadVerticalLayout);
		rezeptKopfDatenHorizontalLayout.addComponent(image);

		// Listener für die Buttons implementieren
		commitButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String statusMessage = "";
				String title = "";
				boolean persisted = false;
				// Commit der Formularfelder
				try {
					rezeptDatenFieldGroup.commit();
					Long id = rezept.getId();
					Rezept dbZ = rezui.dataProvider
							.getRezeptByBezeichnung(rezept.getBezeichnung());
					if (dbZ != null && !id.equals(dbZ.getId())) {

						throw new CommitException(new Throwable(prop
								.getProperty("error.doubleBez")));
					}

					// TODO das geht bestimmt auch schoeer!!
					// bei zeiten mal drueber schauen....
					Table table = container_RezeptZutaten.getTable();
					// Iterate over the item identifiers of the table.
					for (Iterator i = table.getItemIds().iterator(); i
							.hasNext();) {
						rezeptZutatenListe.add((RezeptZutat) i.next());
					}

					// File aus dem reciever holen
					File theFile = receiver.getFile();
					if (theFile != null && theFile.canRead()) {
						// wenn das file gelesen werden kann dann den filename
						// (und zwar nur den filename!)
						// in die thumburl property laden
						String theFileName = theFile.getAbsoluteFile()
								.getName();
						rezept.setThumburl(theFileName);
						image.setSource(new FileResource(new File(theFileName)));
					}

					rezept.setRezeptZutaten(rezeptZutatenListe);

					statusMessage = rezui.dataProvider.persistRezept(rezept);
					title = prop.getProperty("status.geschafft");
					persisted = true;
				} catch (CommitException e) {
					statusMessage = e.getCause().getMessage();
					title = prop.getProperty("status.oops");
				}

				Notification n = new Notification(title);
				n.setDescription(statusMessage);
				n.setDelayMsec(1500);
				n.show(rezui.getPage());

				if (persisted) {
					VaadinSession.getCurrent().setAttribute(
							prop.getProperty("session.rezeptid"), null);
					rezui.nav.navigateTo(prop.getProperty("url.meinerezepte"));
				}
			}
		});
		mainVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		mainVL.setSizeFull();
		mainVL.addComponent(titelLeiste);
		mainVL.addComponent(rezeptKopfDatenHorizontalLayout);
		mainVL.addComponent(container_RezeptZutaten);
		mainVL.addComponent(spacer);
		mainVL.setExpandRatio(rezeptKopfDatenHorizontalLayout, 1);
		mainVL.setExpandRatio(container_RezeptZutaten, 1);
	}

	private ComboBox renderKategorienComboBox() {
		kategorienContainer.addAll(rezui.dataProvider.getAlleKategorien());

		ComboBox kategorienBox = new ComboBox(
				prop.getProperty("ui.caption.kategorie"));
		kategorienBox.setImmediate(true);
		kategorienBox.setInvalidAllowed(false);
		kategorienBox.setNullSelectionAllowed(false);
		kategorienBox.setContainerDataSource(kategorienContainer);
		kategorienBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		kategorienBox.setItemCaptionPropertyId("bezeichnung");
		// Aktuelle Mengeneinheit der Zutat lesen
		Kategorie kategorie = rezept.getKategorie();

		// Wenn diese null ist...
		if (kategorie == null) {
			// Einen Defaultwert annehmen
			BeanItem<Kategorie> kategorieItem = kategorienContainer
					.getItem(kategorienContainer.firstItemId());
			rezept.setKategorie(kategorieItem.getBean());
			kategorienBox.setValue(kategorieItem);
		} else {
			// neuer versuch
			BeanItem<Kategorie> kategorieItem = null, tempItem;
			for (Kategorie item : kategorienContainer.getItemIds()) {
				tempItem = kategorienContainer.getItem(item);
				if (tempItem.getBean().getId() == kategorie.getId()) {
					kategorieItem = tempItem;
					break;
				}
			}
			rezept.setKategorie(kategorieItem.getBean());
			kategorienBox.setValue(kategorieItem);
		}
		return kategorienBox;
	}

	@Override
	public void enter(ViewChangeEvent event) {
	}

}

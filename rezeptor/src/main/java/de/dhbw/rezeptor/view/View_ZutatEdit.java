package de.dhbw.rezeptor.view;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

import de.dhbw.rezeptor.RezeptorUI;
import de.dhbw.rezeptor.data.ImageUploader;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Mengeneinheit;
import de.dhbw.rezeptor.domain.Person;
import de.dhbw.rezeptor.domain.Zutat;

/**
 * View zum anlegen/bearbeitgen von zutaten...
 * 
 * @author dhammacher
 * 
 */
public class View_ZutatEdit extends VerticalLayout implements View {

	private final static Properties prop = SystemProperties.getProperties();

	private BeanItemContainer<Mengeneinheit> mengeneinheitenContainer = new BeanItemContainer<Mengeneinheit>(
			Mengeneinheit.class);
	private Person person = (Person) VaadinSession.getCurrent().getAttribute(
			prop.getProperty("session.user"));
	private Zutat zutat = new Zutat();
	private String filename = "X";
	private boolean neuAnlage = true;
	private Long zutatId = (Long) VaadinSession.getCurrent().getAttribute(
			prop.getProperty("session.zutatid"));

	private RezeptorUI rezui = (RezeptorUI) UI.getCurrent();

	private VerticalLayout mainVL = new VerticalLayout();

	private void doLayoutSettings() {
		setSpacing(true);
		setMargin(true);
		addComponent(mainVL);
		setExpandRatio(mainVL, 1);
	}

	public View_ZutatEdit() {
		rezui.clearMenuSelection();

		doLayoutSettings();
		HorizontalLayout titelLeiste = new HorizontalLayout();
		titelLeiste.setSpacing(true);
		titelLeiste.setMargin(true);

		FormLayout zutatenFormLayout = new FormLayout();

		Label spacer = new Label("");
		Label titel = new Label("");
		final Image image = new Image(prop.getProperty("ui.caption.bild"));
		final ImageUploader receiver;
		final Upload upload = new Upload();
		RichTextArea beschreibung = new RichTextArea();
		// TODO warum wird hier keine caption gesetzt???
		ComboBox mengeneinheitenComboBox = null;

		Button commitButton = new Button(
				prop.getProperty("ui.caption.zutatSpeichern"));
		TextField bezeichnung = new TextField(
				prop.getProperty("ui.caption.bezeichnung"));
		bezeichnung.addValidator(new StringLengthValidator(prop
				.getProperty("error.zutatLen"), 1, 40, false));
		bezeichnung.focus();
		CheckBox vegetarisch = new CheckBox(
				prop.getProperty("ui.caption.vegetarisch"));

		// ID der Zutat aus der Session laden

		// wir wissen noch nicht ob die zutat ein bild hat..
		image.setVisible(false);

		if (zutatId == null) {
			zutat.setVerfasser(person);
			zutat.setAnlageDatum(new Date());
			zutat.setBeschreibung("");
			zutat.setBezeichnung("");
			zutat.setVeggie(true);
		} else {
			neuAnlage = true;
			// Zutat bearbeiten
			// UI mit dataProvider gefunden. Hole Zutat aus DB
			zutat = rezui.dataProvider.getZutatById(zutatId);
			String thumburl = zutat.getThumburl();
			if (thumburl != null) {
				File thumb = new File(prop.getProperty("config.dir.zutat")
						+ thumburl);
				if (thumb.canRead()) {
					image.setSource(new FileResource(thumb));
					image.setVisible(true);
				}
			}
		}
		
		final BeanFieldGroup<Zutat> zutatFieldGroup = new BeanFieldGroup<Zutat>(
				Zutat.class);
		zutatFieldGroup.setItemDataSource(zutat);
		spacer.setHeight("1em");

		titel.setSizeUndefined();
		titel.addStyleName("h1");

		image.setWidth(200, UNITS_PIXELS);
		receiver = new ImageUploader(image, filename,
				prop.getProperty("config.dir.zutat"));
		upload.setReceiver(receiver);
		upload.setButtonCaption(prop.getProperty("ui.caption.hochladen"));
		upload.setLocale(Locale.GERMAN);
		upload.addSucceededListener(receiver);
		upload.setEnabled(false);

		titelLeiste.setWidth("100%");
		titelLeiste.addComponent(titel);
		titelLeiste.setComponentAlignment(titel, Alignment.TOP_LEFT);

		beschreibung.setLocale(Locale.GERMAN);
		beschreibung.setCaption(prop.getProperty("ui.caption.beschreibung"));

		titel.setValue((zutat == null || zutat.getBezeichnung() == null || zutat
				.getBezeichnung().equals("")) ? prop
				.getProperty("ui.caption.neueZutatErstellen") : prop
				.getProperty("ui.caption.zutatBearbeiten")
				+ ": "
				+ zutat.getBezeichnung());

		// ComboBox bauen
		mengeneinheitenComboBox = renderMengenEinheitenComboBox();

		// UI-Komponenten an die Item Properties binden
		zutatFieldGroup.bind(bezeichnung, "bezeichnung");
		zutatFieldGroup.bind(vegetarisch, "veggie");
		zutatFieldGroup.bind(beschreibung, "beschreibung");
		zutatFieldGroup.bind(mengeneinheitenComboBox, "mengeneinheit");

		// Komponenten an das FormLayout binden
		zutatenFormLayout.setCaption(prop.getProperty("ui.caption.zutat"));
		zutatenFormLayout.addComponents(bezeichnung, mengeneinheitenComboBox,
				vegetarisch);

		TextChangeListener textChangeListener = new TextChangeListener() {
			@Override
			public void textChange(TextChangeEvent event) {
				// TODO hier noch validierung einbauen... note:
				// https://vaadin.com/forum/#!/thread/367582
				AbstractTextField source = (AbstractTextField) event
						.getSource();
				try {
					source.validate();

					// wir leiten den filename des bildes von der bezeichnung
					// der
					// zutat ab und erweitern ihn um timestamp und ein bisschen
					// random um doppelte dateinamen auszuschließen
					upload.setEnabled(true);
					filename = event.getText();
					receiver.setFilename(filename);
				} catch (InvalidValueException e) {
					upload.setEnabled(false);
				}
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

		HorizontalLayout zutatHorizontalLayout = new HorizontalLayout();
		zutatHorizontalLayout.setSizeUndefined();
		zutatHorizontalLayout.setSpacing(true);
		zutatHorizontalLayout.setMargin(true);
		zutatHorizontalLayout.addComponent(zutatenFormLayout);
		zutatHorizontalLayout.addComponent(uploadVerticalLayout);
		zutatHorizontalLayout.addComponent(image);

		zutatenFormLayout.addComponent(spacer);
		zutatenFormLayout.addComponent(commitButton);
		zutatenFormLayout.setComponentAlignment(commitButton,
				Alignment.BOTTOM_RIGHT);
		// Listener für die Buttons implementieren
		commitButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String statusMessage;

				try {
					// Commit der Formularfelder
					zutatFieldGroup.commit();
					zutat = zutatFieldGroup.getItemDataSource().getBean();
					Zutat dbZ = rezui.dataProvider.getZutatByBezeichnung(zutat
							.getBezeichnung());
					if (dbZ != null && !zutat.getId().equals(dbZ.getId())) {
						throw new CommitException(new Throwable(prop
								.getProperty("error.doubleBez")));
					}

					// File aus dem reciever holen
					File theFile = receiver.getFile();
					if (theFile != null && theFile.canRead()) {
						// wenn das file gelesen werden kann dann den filename
						// (und zwar nur den filename!)
						// in die thumburl property laden
						String theFileName = theFile.getAbsoluteFile()
								.getName();
						zutat.setThumburl(theFileName);
						image.setSource(new FileResource(new File(theFileName)));
					}
					statusMessage = rezui.dataProvider.persistZutat(zutat);
				} catch (CommitException e) {
					statusMessage = prop.getProperty("error.commitFG") + "\n"
							+ e.getCause().getMessage();
				}
				Notification n = new Notification(prop
						.getProperty("ui.caption.geschafft"));
				n.setDescription(statusMessage);
				n.setDelayMsec(1500);
				n.show(rezui.getPage());
				if (neuAnlage) {
					rezui.nav.navigateTo(prop.getProperty("url.zutatedit"));
				} else {
					rezui.nav.navigateTo(prop.getProperty("url.meinezutaten"));
				}
			}
		});
		mainVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		mainVL.setSizeFull();
		mainVL.addComponent(titelLeiste);
		mainVL.addComponent(zutatHorizontalLayout);
		mainVL.addComponent(spacer);
		mainVL.setExpandRatio(zutatHorizontalLayout, 1);
	}

	private PropertysetItem buildZutatItem(Zutat z) {
		// TODO Auto-generated method stub
		PropertysetItem itemZutat = new PropertysetItem();
		itemZutat.addItemProperty("veggie",
				new ObjectProperty<Boolean>(zutat.isVeggie()));
		itemZutat.addItemProperty("beschreibung", new ObjectProperty<String>(
				zutat.getBeschreibung()));
		itemZutat.addItemProperty("bezeichnung", new ObjectProperty<String>(
				zutat.getBezeichnung()));
		itemZutat.addItemProperty("mengeneinheit",
				new ObjectProperty<Mengeneinheit>(zutat.getMengeneinheit()));
		return itemZutat;
	}

	private ComboBox renderMengenEinheitenComboBox() {
		mengeneinheitenContainer.addAll(rezui.dataProvider
				.getAlleMengeneinheiten());

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
			mengenEinheitenBox.setValue(mengenItem);
		} else {
			// neuer versuch
			BeanItem<Mengeneinheit> mengenItem = null, tempItem;
			for (Mengeneinheit item : mengeneinheitenContainer.getItemIds()) {
				tempItem = mengeneinheitenContainer.getItem(item);
				if (tempItem.getBean().getId() == mengenEinheit.getId()) {
					mengenItem = tempItem;
					break;
				}
			}
			zutat.setMengeneinheit(mengenItem.getBean());
			mengenEinheitenBox.setValue(mengenItem);
		}
		return mengenEinheitenBox;
	}

	@Override
	public void enter(ViewChangeEvent event) {
	}
}

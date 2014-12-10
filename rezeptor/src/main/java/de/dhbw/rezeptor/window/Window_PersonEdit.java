package de.dhbw.rezeptor.window;

import java.util.Properties;

import javax.persistence.PersistenceException;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.dhbw.rezeptor.RezeptorUI;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Person;
import de.dhbw.rezeptor.util.Berechtigor;

/**
 * Fenster zum Bearbeiten der eigenen nutzerdaten
 * 
 * @author dhammacher
 * 
 */
public class Window_PersonEdit extends Window {

	private final static Properties prop = SystemProperties.getProperties();
	private Person user = (Person) VaadinSession.getCurrent().getAttribute(
			prop.getProperty("session.user"));
	private RezeptorUI rezui = (RezeptorUI) UI.getCurrent();

	public Window_PersonEdit(Person p) {
		final boolean selbst = user.equals(p) ? true : false;
		final boolean isNeu = p == null ? true : false;

		if (selbst) {
			setCaption("Account bearbeiten");
			p = user;
		} else if (isNeu) {
			setCaption("Benutzer anlegen");
			p = new Person();
			p.setVorname("");
			p.setNachname("");
			p.setEmail("");
			p.setPasswort("");

		} else {
			setCaption("Benutzer bearbeiten: " + p);
		}

		// Vertikales Layout als Root
		final VerticalLayout mainVerticalLayout = new VerticalLayout();
		mainVerticalLayout.setSpacing(true);
		mainVerticalLayout.setMargin(true);
		setContent(mainVerticalLayout);
		setCloseShortcut(KeyCode.ESCAPE, null);
		setClosable(true);
		setModal(true);
		setResizable(true);
		center();
		addStyleName("no-vertical-drag-hints");
		addStyleName("no-horizontal-drag-hints");

		// Neues Layout f�r den allgemeinen Teil der PersonDaten
		FormLayout personDatenFormLayout = new FormLayout();

		TextField vorname = new TextField("Vorname");
		vorname.addValidator(new StringLengthValidator(prop
				.getProperty("error.vornameLen"), 2, 30, false));
		TextField nachname = new TextField("Nachname");
		nachname.addValidator(new StringLengthValidator(prop
				.getProperty("error.nameLen"), 2, 30, false));
		TextField email = new TextField("E-Mail");
		email.addValidator(new EmailValidator(prop
				.getProperty("error.emailInvalid")));
		email.setRequired(true);
		email.setRequiredError(prop.getProperty("error.emailReq"));
		DateField geburtsdatum = new DateField("Geburtsdatum");
		// TODO button noch kaputt...

		// UI-Komponenten an PropertyItem binden
		final BeanFieldGroup<Person> personDatenFieldGroup = new BeanFieldGroup<Person>(
				Person.class);
		p.setPasswort_alt("");
		p.setPasswort_neu1("");
		p.setPasswort_neu2("");
		personDatenFieldGroup.setItemDataSource(p);
		personDatenFieldGroup.bind(vorname, "vorname");
		personDatenFieldGroup.bind(nachname, "nachname");
		personDatenFieldGroup.bind(email, "email");
		personDatenFieldGroup.bind(geburtsdatum, "geburtsdatum");

		// Komponenten an das Layout binden
		personDatenFormLayout.setCaption("Allgemeine Daten");
		personDatenFormLayout.addComponents(vorname, nachname, email,
				geburtsdatum);

		// Nochmal fuer PW
		FormLayout passwortDatenFormLayout = new FormLayout();

		PasswordField passwort_alt = new PasswordField("altes Passwort");
		passwort_alt.setVisible(false);
		PasswordField passwort_neu1 = new PasswordField("neues Passwort");
		PasswordField passwort_neu2 = new PasswordField("nochmal bitte");
		TextField berechtigung = new TextField("Berechtigung");
		berechtigung.setVisible(false);
		if (Berechtigor.darfRan(user, p) && p.getBerechtigung() < 100) {
			berechtigung.setVisible(true);
		}

		if (selbst)
			passwort_alt.setVisible(true);

		personDatenFieldGroup.bind(passwort_alt, "passwort_alt");
		personDatenFieldGroup.bind(passwort_neu1, "passwort_neu1");
		personDatenFieldGroup.bind(passwort_neu2, "passwort_neu2");
		personDatenFieldGroup.bind(berechtigung, "berechtigung");
		final Button sichern = new Button("Speichern");

		passwortDatenFormLayout.setCaption("Passwort aendern");
		passwortDatenFormLayout.addComponents(passwort_alt, passwort_neu1,
				passwort_neu2, berechtigung, sichern);

		// Die UI-FormLayouts horizontal anordnen
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.addComponent(personDatenFormLayout);
		horizontalLayout.addComponent(passwortDatenFormLayout);
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true);
		mainVerticalLayout.addComponent(horizontalLayout);

		// VaadinSession.getCurrent().setAttribute("PersonDatenSichernItem",
		// personPropertySetItem);

		sichern.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				// Commit der Formularfelder mappt formularwerte in das
				Person finPers = null;
				boolean fensterZu = false;
				String statusMessage = "";
				try {
					personDatenFieldGroup.commit();
					// Zurückmappen der ItemProperties in Person
					finPers = personDatenFieldGroup.getItemDataSource()
							.getBean();
					statusMessage = rezui.dataProvider.persistPerson(finPers,
							selbst, isNeu);
					fensterZu = true;
				} catch (CommitException e) {
					Notification.show(prop.getProperty("error.commitFG") + "\n"
							+ e.getCause().getMessage(), Type.WARNING_MESSAGE);
				} catch (PersistenceException e) {
					Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
				} catch (NullPointerException e) {
					Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
				}
				if (fensterZu) {
					Notification n = new Notification("Geschafft!");
					n.setDescription(statusMessage);
					n.setDelayMsec(1500);
					n.show(rezui.getPage());
					close();
					rezui.nav.navigateTo(prop.getProperty("url.mitglieder"));
				}
			}
		});
	}
}

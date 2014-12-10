package de.dhbw.rezeptor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.dhbw.rezeptor.data.DataProvider;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Person;
import de.dhbw.rezeptor.view.View_Favoriten;
import de.dhbw.rezeptor.view.View_Mitglieder;
import de.dhbw.rezeptor.view.View_RezeptDetails;
import de.dhbw.rezeptor.view.View_RezeptEdit;
import de.dhbw.rezeptor.view.View_RezepteMeine;
import de.dhbw.rezeptor.view.View_Rezeptor;
import de.dhbw.rezeptor.view.View_Suche;
import de.dhbw.rezeptor.view.View_Warenkunde;
import de.dhbw.rezeptor.view.View_ZutatEdit;
import de.dhbw.rezeptor.view.View_ZutatenMeine;
import de.dhbw.rezeptor.window.Window_PersonEdit;

// TODO Theme anpassen
@Theme("dashboard")
@Title("DHBW.Rezeptor")
public class RezeptorUI extends UI {

	private final static Properties prop = SystemProperties.getProperties();
	private static final long serialVersionUID = 1L;
	// Referenz auf die DB (siehe persistence.xml)
	public static final String PERSISTENCE_UNIT = "de.dhbw";
	// Stellt unsere Daten bereit (quasi ein erweitertes DAO)
	public DataProvider dataProvider = new DataProvider();

	public HashMap<String, Button> viewNameToMenuButton = new HashMap<String, Button>();
	public Navigator nav;
	private HelpManager helpManager;

	CssLayout root = new CssLayout();
	CssLayout menu = new CssLayout();
	CssLayout content = new CssLayout();

	VerticalLayout loginLayout;

	// Setzen der URI-Fragmente und Mapping der zugehoerigen View
	HashMap<String, Class<? extends View>> routes = new HashMap<String, Class<? extends View>>() {
		{
			put(prop.getProperty("url.rezeptor"), View_Rezeptor.class);
			put(prop.getProperty("url.rezeptedit"), View_RezeptEdit.class);
			put(prop.getProperty("url.zutatedit"), View_ZutatEdit.class);
			put(prop.getProperty("url.meinerezepte"), View_RezepteMeine.class);
			put(prop.getProperty("url.meinezutaten"), View_ZutatenMeine.class);
			put(prop.getProperty("url.suche"), View_Suche.class);
			put(prop.getProperty("url.favoriten"), View_Favoriten.class);
			put(prop.getProperty("url.warenkunde"), View_Warenkunde.class);
			put(prop.getProperty("url.rezeptdetails"), View_RezeptDetails.class);
			put(prop.getProperty("url.mitglieder"), View_Mitglieder.class);
		}
	};

	// Diese Klasse wird bei Initialisierung eines VaadinRequests aufgerufen,
	// also immer dann, wenn der User erstmalig einen HTTP Request an die URL
	// des Rezeptors sendet, also bspw. auch wenn der User einen Refresh (F5)
	// auf die Seite macht. Hier wird auch entschieden, ob der LoginView oder
	// der
	// MainView aufgebaut wird
	@Override
	protected void init(VaadinRequest request) {
		// UI an die Session binden
		VaadinSession.getCurrent().addUI(this);
		helpManager = new HelpManager(this);
		// getSession().setConverterFactory(new MyConverterFactory());

		// Root Layout binden
		setLocale(Locale.GERMAN);
		setContent(root);
		root.addStyleName("root");
		root.setSizeFull();

		// Login View aufbauen, wenn noch nicht eingeloggt...
		Person user = (Person) getSession().getAttribute(
				prop.getProperty("session.user"));
		if (user == null || user.getId() == null) {
			buildLoginView(false);
		} else {
			buildMainView();
		}
	}

	// Diese MEthode baut den LoginView auf.
	private void buildLoginView(boolean exit) {
		Label bg = new Label();
		bg.setSizeUndefined();
		bg.addStyleName("login-bg");
		root.addComponent(bg);
		// Bei verlassen der Application alle komponenten vom root layout
		// trennen
		if (exit) {
			root.removeAllComponents();
		}
		helpManager.closeAll();

		HelpOverlay w = helpManager
				.addOverlay(
						"Willkommen bei Rezeptor",
						"<p>Um das Portal nutzen zu können, musst Du dich einloggen.</p>",
						"login");
		w.center();
		addWindow(w);

		addStyleName("login");
		loginLayout = new VerticalLayout();
		loginLayout.setSizeFull();
		loginLayout.addStyleName("login-layout");
		root.addComponent(loginLayout);

		final CssLayout loginPanel = new CssLayout();
		loginPanel.addStyleName("login-panel");

		HorizontalLayout labels = new HorizontalLayout();
		labels.setWidth("100%");
		labels.setMargin(true);
		labels.addStyleName("labels");
		loginPanel.addComponent(labels);

		Label welcome = new Label("Geheimrezeptor!");
		welcome.setSizeUndefined();
		welcome.addStyleName("h4");
		labels.addComponent(welcome);
		labels.setComponentAlignment(welcome, Alignment.MIDDLE_LEFT);

		Label title = new Label("bitte einloggen!");
		title.setSizeUndefined();
		title.addStyleName("h2");
		title.addStyleName("light");
		labels.addComponent(title);
		labels.setComponentAlignment(title, Alignment.MIDDLE_RIGHT);

		HorizontalLayout fields = new HorizontalLayout();
		fields.setSpacing(true);
		fields.setMargin(true);
		fields.addStyleName("fields");

		final TextField email = new TextField("E-Mail");
		email.focus();
		email.addValidator(new EmailValidator(prop
				.getProperty("error.emailInvalid")));
		fields.addComponent(email);

		final PasswordField password = new PasswordField("Passwort");
		fields.addComponent(password);

		final Button signin = new Button("Login");
		signin.addStyleName("default");
		fields.addComponent(signin);
		fields.setComponentAlignment(signin, Alignment.BOTTOM_LEFT);

		// Shortcutlistener definieren... reagiert auf druecken der Enter Taste
		final ShortcutListener enter = new ShortcutListener("Login",
				KeyCode.ENTER, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				// Shortcutlistener an den Klicklistener binden...
				signin.click();
			}
		};

		// Clcklistener fuer den login implementieren
		signin.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				// TU ES!!!
				// Person result = dataProvider.getPersonById(1L);
				Object result = null;
				try {
					email.validate();
				} catch (InvalidValueException e) {
					result = e.getMessage();
				}
				if (result == null) {
					result = dataProvider.loginPerson(email.getValue(),
							password.getValue());
				}
				// Wenn Person zurï¿½ck kommt, dann login erfolgreich
				if (result instanceof Person) {
					VaadinSession.getCurrent().setAttribute(
							prop.getProperty("session.user"), (Person) result);
					// Shortcutlistener wieder entfernen... nicht dass wir uns
					// nochmal einloggen...
					signin.removeShortcutListener(enter);
					helpManager.closeAll();
					removeStyleName("login");
					root.removeComponent(loginLayout);
					// Hauptview aufbauen
					buildMainView();
				}
				// login nicht erfolgreich...
				else {
					// wenn es schon eine alte fehlermeldung gab lï¿½sche diese
					// erst
					if (loginPanel.getComponentCount() > 2) {
						loginPanel.removeComponent(loginPanel.getComponent(2));
					}

					Label error = new Label(result.toString(), ContentMode.HTML);
					error.addStyleName("error");
					error.setSizeUndefined();
					error.addStyleName("light");
					// AHNIMIIIERN
					error.addStyleName("v-animate-reveal");
					loginPanel.addComponent(error);
					email.focus();
				}
			}
		});
		// Shortcutlistener an den login button binden
		signin.addShortcutListener(enter);
		loginPanel.addComponent(fields);
		loginLayout.addComponent(loginPanel);
		loginLayout.setComponentAlignment(loginPanel, Alignment.MIDDLE_CENTER);
	}

	// Diese Methode baut den MainView auf, dafür muss der User erst eingeloggt
	// sein, Hier wird ebenfalls der Navigationsfluss über den Navigator
	// implementiert
	private void buildMainView() {
		// Unser Navigator gibt den content an.... er reagiert auf URL Action
		nav = new Navigator(this, content);
		nav.addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				helpManager.closeAll();
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
				View newView = event.getNewView();
				helpManager.showHelpFor(newView);
			}
		});

		for (String route : routes.keySet()) {
			nav.addView(route, routes.get(route));
		}

		// Zuerst eine Horizontale Trennung -- links menu, rechts inhalt
		root.addComponent(renderMainView());

		// Hier kommt die eigentliche Logik des Navigators
		// erst mal das URI-Fragment aus der Page lesen
		String f = Page.getCurrent().getUriFragment();
		// wenn ein weiteres fragment daran klebt, den string splitten
		if (f != null && f.startsWith("!")) {
			f = f.substring(1);
		}
		// Wenn kein view gesetzt ist, gehe auf den hauptview
		if (f == null || f.equals("") || f.equals("/")) {
			nav.navigateTo(prop.getProperty("url.rezeptor"));
			menu.getComponent(0).addStyleName("selected");
			helpManager.showHelpFor(View_Rezeptor.class);
		}
		// wenn ein view gesetzt ist navigiere da hin
		else {
			nav.navigateTo(f);
			// TODO ... na wo isser denn...?
			helpManager.showHelpFor(routes.get(f));

			if (viewNameToMenuButton.get(f) != null)
				viewNameToMenuButton.get(f).addStyleName("selected");
		}
	}

	// Diese Methode rendert das MainLayout (Also Links MenuBar, rechts
	// View-Komponente)
	private HorizontalLayout renderMainView() {
		HorizontalLayout linksRechts = new HorizontalLayout();
		linksRechts.setSizeFull();
		linksRechts.addStyleName("main-view");
		// In die linke Spalte kommt die Seitenleiste
		linksRechts.addComponent(renderLeftMenuBar());

		// rechts wird unser inhalt reingeladen
		linksRechts.addComponent(content);
		content.setSizeFull();
		content.addStyleName("view-content");
		linksRechts.setExpandRatio(content, 1);
		return linksRechts;
	}

	// Rendern der Menubar
	private VerticalLayout renderLeftMenuBar() {
		VerticalLayout menuBar = new VerticalLayout();

		// Hier bauen wir das Hauptmenu auf... dazu erst mal alle loeschen
		menu.removeAllComponents();
		String iconString[] = new String[] { "rezeptor", "suche", "warenkunde",
				"favoriten", "details", "mitglieder" };
		// Buttons generieren...
		for (final String view : iconString) {
			// Button hinzufuegen
			Button b = new NativeButton(view.substring(0, 1).toUpperCase()
					+ view.substring(1).replace('-', ' '));

			b.addStyleName("icon-" + view);
			// Die buttons poebeln den navigator an.
			b.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					// TODO reset von bestimmten view gebundenen session
					// params...

					// button vom alten view wieder zurücksetzen
					clearMenuSelection();
					// aktuell ausgewählten button anpöbeln
					event.getButton().addStyleName("selected");
					if (!nav.getState().equals("/" + view))
						nav.navigateTo("/" + view);
				}
			});
			// button ans menu kloppen
			menu.addComponent(b);
			viewNameToMenuButton.put("/" + view, b);
		}
		menu.addStyleName("menu");
		menu.setHeight("100%");

		// Badge oben rechts.. News.....
		viewNameToMenuButton.get("/rezeptor").setHtmlContentAllowed(true);
		viewNameToMenuButton.get("/rezeptor").setCaption(
				"Rezeptor<span class=\"badge\">2</span>");

		menuBar.addStyleName("sidebar");
		menuBar.setWidth(null);
		menuBar.setHeight("100%");

		// LOOGOOOO
		menuBar.addComponent(renderLogoLayout());

		// Hauptmenu
		menuBar.addComponent(menu);
		menuBar.setExpandRatio(menu, 1);

		// User menu (unten)
		menuBar.addComponent(renderUserMenu());
		return menuBar;

	}

	// Hier könnte noch ein Logo rein...
	private CssLayout renderLogoLayout() {
		CssLayout logoLayout = new CssLayout();
		logoLayout.addStyleName("branding");
		Label logo = new Label("<span>DHBW</span> Rezeptor", ContentMode.HTML);
		logo.setSizeUndefined();
		logoLayout.addComponent(logo);

		// Image img = new Image(null, new ThemeResource("img/rezeptor.png"));
		// logoLayout.addComponent(img);
		return logoLayout;
	}

	// Hier wird der untere Teil der Menubar gerendert, also das Usermenu...
	private VerticalLayout renderUserMenu() {
		VerticalLayout userMenu = new VerticalLayout();
		userMenu.setSizeUndefined();
		userMenu.addStyleName("user");

		// TODO ... aus dem user laden
		Image profilePic = new Image(null, new ThemeResource(
				"img/profile-pic.png"));
		profilePic.setWidth("34px");
		userMenu.addComponent(profilePic);

		final Person user = (Person) VaadinSession.getCurrent().getAttribute(
				prop.getProperty("session.user"));
		Label userName = new Label(user.getVorname() + " " + user.getNachname());
		userName.setSizeUndefined();
		userMenu.addComponent(userName);

		// Hier wird das Benutzer Kontext-Menue
		// Implementiert
		Command cmd = new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				if (selectedItem.getText().equals(
						prop.getProperty("ui.caption.accountBearbeiten"))) {
					Window w = new Window_PersonEdit(user);
					UI.getCurrent().addWindow(w);
					w.focus();
				} else if (selectedItem.getText().equals(
						prop.getProperty("ui.caption.rezeptErstellen"))) {
					VaadinSession.getCurrent().setAttribute(
							prop.getProperty("session.rezeptid"), null);
					clearMenuSelection();
					nav.navigateTo(prop.getProperty("url.rezeptedit"));
				} else if (selectedItem.getText().equals(
						prop.getProperty("ui.caption.zutatErstellen"))) {
					clearMenuSelection();
					VaadinSession.getCurrent().setAttribute(
							prop.getProperty("session.zutatid"), null);
					nav.navigateTo(prop.getProperty("url.zutatedit"));
				} else if (selectedItem.getText().equals(
						prop.getProperty("ui.caption.meineZutaten"))) {
					clearMenuSelection();
					nav.navigateTo(prop.getProperty("url.meinezutaten"));
				} else if (selectedItem.getText().equals(
						prop.getProperty("ui.caption.meineRezepte"))) {
					clearMenuSelection();
					nav.navigateTo(prop.getProperty("url.meinerezepte"));
				} else if (selectedItem.getText().equals(
						prop.getProperty("ui.caption.favoriten"))) {
					clearMenuSelection();
					viewNameToMenuButton.get(prop.getProperty("url.favoriten"))
							.addStyleName("selected");
					nav.navigateTo(prop.getProperty("url.favoriten"));
				} else {
					Notification.show(prop.getProperty("status.notSupported"));
				}

			}
		};

		// Hier wird der Sichtbare Teil des Menus
		// implementiert
		MenuBar settings = new MenuBar();
		MenuItem settingsMenu = settings.addItem("", null);
		settingsMenu.setStyleName("icon-cog");
		settingsMenu.addItem(prop.getProperty("ui.caption.favoriten"), cmd);
		settingsMenu.addItem(prop.getProperty("ui.caption.meineRezepte"), cmd);
		settingsMenu.addItem(prop.getProperty("ui.caption.meineZutaten"), cmd);
		settingsMenu.addItem(prop.getProperty("ui.caption.rezeptErstellen"),
				cmd);
		settingsMenu
				.addItem(prop.getProperty("ui.caption.zutatErstellen"), cmd);
		settingsMenu.addSeparator();
		settingsMenu.addItem(prop.getProperty("ui.caption.accountBearbeiten"),
				cmd);
		userMenu.addComponent(settings);

		Button exit = new NativeButton(prop.getProperty("ui.caption.logout"));
		exit.addStyleName("icon-cancel");
		exit.setDescription(prop.getProperty("ui.caption.logout"));
		userMenu.addComponent(exit);
		// Listener fuer den exit button...
		exit.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				VaadinSession.getCurrent().setAttribute(
						prop.getProperty("session.user"), null);
				VaadinSession.getCurrent().close();
				buildLoginView(true);
				getPage().setLocation(prop.getProperty("url.rezeptor"));
				System.out.println("wtfWTF");
			}
		});
		return userMenu;
	}

	// Methode zum zuruecksetzen des "selected"-Styles der aktiven menubuttons
	// der menubar
	public void clearMenuSelection() {
		for (Iterator<Component> it = menu.getComponentIterator(); it.hasNext();) {
			Component next = it.next();
			if (next instanceof NativeButton) {
				next.removeStyleName("selected");
			}
		}
	}

	public void updateReportsButtonBadge(String badgeCount) {
		viewNameToMenuButton.get("/reports").setHtmlContentAllowed(true);
		viewNameToMenuButton.get("/reports").setCaption(
				"Reports<span class=\"badge\">" + badgeCount + "</span>");
	}

	public void clearRezeptorButtonBadge() {
		viewNameToMenuButton.get("/rezeptor").setCaption("Rezeptor");
	}

	public HelpManager getHelpManager() {
		return helpManager;
	}
}

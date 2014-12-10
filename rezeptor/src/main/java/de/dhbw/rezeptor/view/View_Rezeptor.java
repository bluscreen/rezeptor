package de.dhbw.rezeptor.view;

import java.util.Locale;
import java.util.Properties;

import org.tepi.filtertable.FilterTable;
import org.vaadin.virkki.carousel.HorizontalCarousel;
import org.vaadin.virkki.carousel.client.widget.gwt.ArrowKeysMode;
import org.vaadin.virkki.carousel.client.widget.gwt.CarouselLoadMode;

import com.example.balling.Baller;
import com.example.balling.BallerRecordEvent;
import com.example.balling.BallerRecordListener;
import com.example.balling.BallerTouchEvent;
import com.example.balling.BallerTouchListener;
import com.example.balling.client.baller.AllowedWidgets;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
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
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.dhbw.rezeptor.RezeptorUI;
import de.dhbw.rezeptor.container.Container_RezeptCarousel;
import de.dhbw.rezeptor.data.Generator;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Person;
import de.dhbw.rezeptor.domain.Rezept;

/**
 * 
 * Rezeptor View... mit Carousel
 * 
 * TODO Push aktivieren für notifications und zum automatischen scrolling der
 * carousel componenten
 * https://vaadin.com/book/vaadin7/-/page/advanced.push.html
 * https://vaadin.com/forum/#!/thread/2944531
 * 
 * @author dhammacher
 * 
 */
public class View_Rezeptor extends VerticalLayout implements View {

	private final static Properties prop = SystemProperties.getProperties();
	private RezeptorUI rezui = (RezeptorUI) UI.getCurrent();
	Window notifications;
	private Person person = null;
	private boolean balls = false;
	public Baller bExt;

	public View_Rezeptor() {
		setSizeFull();
		getBalls();
		addStyleName("dashboard-view");

		person = (Person) VaadinSession.getCurrent().getAttribute(
				prop.getProperty("session.user"));

		HorizontalLayout top = new HorizontalLayout();
		top.setWidth("100%");
		top.setSpacing(true);
		top.addStyleName("toolbar");
		addComponent(top);
		final Label title = new Label(prop.getProperty("ui.caption.homeScreen"));
		title.setSizeUndefined();
		title.addStyleName("h1");
		top.addComponent(title);
		top.setComponentAlignment(title, Alignment.MIDDLE_LEFT);
		top.setExpandRatio(title, 1);

		// Template für Notification-Implementierung... dem Zugrunde liegt
		// allerdings zunächst ein gescheites Logging, was aus Zeitgründen erst
		// mal nichts wird
		Button notify = new Button("2");
		notify.setDescription("Notifications (2 unread)");
		// notify.addStyleName("borderless");
		notify.addStyleName("notifications");
		notify.addStyleName("unread");
		notify.addStyleName("icon-only");
		notify.addStyleName("icon-bell");
		notify.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				event.getButton().removeStyleName("unread");
				event.getButton().setDescription("Notifications");

				if (notifications != null && notifications.getUI() != null)
					notifications.close();
				else {
					buildNotifications(event);
					getUI().addWindow(notifications);
					notifications.focus();
					((CssLayout) getUI().getContent())
							.addLayoutClickListener(new LayoutClickListener() {
								@Override
								public void layoutClick(LayoutClickEvent event) {
									notifications.close();
									((CssLayout) getUI().getContent())
											.removeLayoutClickListener(this);
									rezui.clearRezeptorButtonBadge();
								}
							});
				}

			}
		});
		top.addComponent(notify);
		top.setComponentAlignment(notify, Alignment.MIDDLE_LEFT);

		final Button edit = new Button();
		edit.addStyleName("icon-edit");
		edit.addStyleName("icon-only");
		top.addComponent(edit);
		edit.setDescription("Edit Rezeptor");
		final ShortcutListener esc = new ShortcutListener("NOBALLS",
				KeyCode.F1, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				// Shortcutlistener an den Klicklistener binden...
				edit.click();
			}
		};
		edit.addShortcutListener(esc);
		edit.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				balls();
			}
		});

		top.setComponentAlignment(edit, Alignment.MIDDLE_LEFT);

		HorizontalLayout row = new HorizontalLayout();
		row.setSizeFull();
		row.setMargin(new MarginInfo(true, true, false, true));
		row.setSpacing(true);
		addComponent(row);
		setExpandRatio(row, 1.5f);

		Image img = new Image(null, new ThemeResource("img/rezeptor.png"));
		img.setSizeFull();
		final HorizontalCarousel carousel = new HorizontalCarousel();

		carousel.setArrowKeysMode(ArrowKeysMode.ALWAYS);
		carousel.setLoadMode(CarouselLoadMode.LAZY);
		carousel.setSizeFull();
		carousel.setMouseDragEnabled(false);

		// bezieht sich irrtümlicherweise nicht auf einen auto-scroll effekt
		// sondern auf die zeit, die beim hin und her scrollen von einem zum
		// anderen element gebraucht wird... implementierung recht verwirrend...
		// wird erstmal so gelassen
		carousel.setTransitionDuration(500);
		carousel.setCaption(prop.getProperty("ui.caption.bestBewertet"));
		carousel.addComponent(img);
		carousel.setTouchDragEnabled(false);

		for (Rezept r : rezui.dataProvider.getRezepteByAvgRating()) {
			Container_RezeptCarousel cr = new Container_RezeptCarousel(r);
			final Rezept rez = r;
			cr.addLayoutClickListener(new LayoutClickListener() {

				@Override
				public void layoutClick(LayoutClickEvent event) {
					String fragment = prop.getProperty("url.rezeptdetails");

					rezui.clearMenuSelection();
					rezui.viewNameToMenuButton.get(fragment).addStyleName(
							"selected");

					rezui.nav.navigateTo(fragment + "/"
							+ String.valueOf(rez.getId()));
				}
			});
			carousel.addComponent(cr);
		}
		//
		// int i = 0;
		// final int anzComp = carousel.getComponentCount();
		//
		// ScheduledExecutorService exec = Executors
		// .newSingleThreadScheduledExecutor();
		// exec.scheduleAtFixedRate(new Runnable() {
		// @Override
		// public void run() {
		// carousel.scroll(2);
		// if (i < anzComp) {
		// i.;
		// } else {
		// i = 0;
		// }
		// }
		// }, 0, 5, TimeUnit.SECONDS);

		CssLayout panel2 = createPanel(carousel);
		panel2.addStyleName("carousel");
		row.addComponent(panel2);

		final TextArea notes = new TextArea("Notizen");
		notes.setImmediate(true);
		if (person.getNotizen() == null || person.getNotizen().equals("")) {
			person.setNotizen(prop.getProperty("ui.def.notizen"));
		}
		notes.setSizeFull();
		notes.setValue(person.getNotizen());
		notes.setMaxLength(500);
		CssLayout panel = createPanel(notes);
		panel.addStyleName("notes");
		row.addComponent(panel);

		notes.addTextChangeListener(new TextChangeListener() {

			@Override
			public void textChange(TextChangeEvent event) {

				// TODO gefahhhhr..
				// if (event.getText().contains("\"")
				// || event.getText().contains("<")
				// || event.getText().contains(">")
				// || event.getText().contains("\\")) {
				// notes.setConversionError(prop
				// .getProperty("error.conversion"));
				// } else {

				System.err.println(event.getText());
				person.setNotizen(event.getText());
				rezui.dataProvider.persistPerson(person, true, false);
				// }
			}
		});

		row = new HorizontalLayout();
		row.setMargin(true);
		row.setSizeFull();
		row.setSpacing(true);
		addComponent(row);
		setExpandRatio(row, 1);
		row.addComponent(createPanel(createLatestRezepteTable()));
	}

	private FilterTable createLatestRezepteTable() {
		BeanItemContainer<Rezept> rezeptContainer = new BeanItemContainer<Rezept>(
				Rezept.class);
		rezeptContainer.addAll(rezui.dataProvider.getLatestRezepte());
		FilterTable table = new FilterTable(
				prop.getProperty("ui.caption.neuesteRezepte"));
		table.removeAllItems();
		table.setContainerDataSource(rezeptContainer);
		table.setLocale(Locale.GERMAN);
		table.addStyleName("plain");
		table.addStyleName("borderless");
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
		table.addItemClickListener(new ItemClickListener() {
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
		return table;
	}

	private CssLayout createPanel(Component content) {
		CssLayout panel = new CssLayout();
		panel.addStyleName("layout-panel");
		panel.setSizeFull();

		// Button configure = new Button();
		// configure.addStyleName("configure");
		// configure.addStyleName("icon-cog");
		// configure.addStyleName("icon-only");
		// configure.addStyleName("borderless");
		// configure.setDescription("Configure");
		// configure.addStyleName("small");
		// configure.addClickListener(new ClickListener() {
		// @Override
		// public void buttonClick(ClickEvent event) {
		// Notification.show("Not implemented in this demo");
		// }
		// });
		// panel.addComponent(configure);

		panel.addComponent(content);
		return panel;
	}

	@Override
	public void enter(ViewChangeEvent event) {
	}

	private void buildNotifications(ClickEvent event) {
		notifications = new Window("Neuigkeiten");
		VerticalLayout l = new VerticalLayout();
		l.setMargin(true);
		l.setSpacing(true);
		notifications.setContent(l);
		notifications.setWidth("300px");
		notifications.addStyleName("notifications");
		notifications.setClosable(false);
		notifications.setResizable(false);
		notifications.setDraggable(false);
		notifications.setPositionX(event.getClientX() - event.getRelativeX());
		notifications.setPositionY(event.getClientY() - event.getRelativeY());
		notifications.setCloseShortcut(KeyCode.ESCAPE, null);

		// Hier könnten wir einen kleinen Newsfeed einbauen.
		Label label = new Label(
				"<hr><b>"
						+ "irgendjemand hat eine neue Zutat erstellt</b><br><span>vor 25 Minuten</span><br>"
						+ Generator.randomText(18), ContentMode.HTML);
		l.addComponent(label);

		label = new Label(
				"<hr><b>jemand hat ein Rezept Bewertet</b><br><span>vor 2 Tagen</span><br>"
						+ Generator.randomText(10), ContentMode.HTML);
		l.addComponent(label);
	}

	public void balls() {
		if (balls == true) {
			balls = false;
		} else {
			balls = true;
		}
		bExt.setWicked(balls); // use this flag to turn the wickedness on/off
	}

	public void getBalls() {
		bExt = new Baller();
		bExt.addWickedWidgetType(AllowedWidgets.BUTTON);
		bExt.addWickedWidgetType(AllowedWidgets.TEXTFIELD);
		bExt.addWickedWidgetType(AllowedWidgets.PASSWORDFIELD);
		bExt.addWickedWidgetType(AllowedWidgets.CAPTION);
		bExt.addWickedWidgetType(AllowedWidgets.LABEL);
		bExt.addWickedWidgetType(AllowedWidgets.DATEFIELD);
		bExt.addWickedWidgetType(AllowedWidgets.FILTERSELECT);
		bExt.addWickedWidgetType(AllowedWidgets.SLIDER);
		bExt.extendComponent(rezui.getUI());

		bExt.addBallerRecordListener(new BallerRecordListener() {
			@Override
			public void onNewRecord(BallerRecordEvent event) {
				Notification.show(
						"UUGGGHH!!  REKOOORDDDD: " + event.getRecord() + " ???",
						Type.ERROR_MESSAGE);
			}
		});

		bExt.addBallerTouchListener(new BallerTouchListener() {

			@Override
			public void objectTouched(BallerTouchEvent event) {
				double t = Math.floor(Math.random() * 5);
				Notification n = null;

				if (t >= 5) {
					n = new Notification("BAM!! ", Type.HUMANIZED_MESSAGE);
					n.setPosition(Position.MIDDLE_CENTER);
				} else if (t > 4) {
					n = new Notification("JUNGE!!!! ", Type.WARNING_MESSAGE);
					n.setPosition(Position.TOP_CENTER);
				} else if (t > 3) {
					n = new Notification("KILLER!! ", Type.WARNING_MESSAGE);
					n.setPosition(Position.MIDDLE_LEFT);
				} else if (t > 2) {
					n = new Notification("DUNKLER RITTER!! ",
							Type.WARNING_MESSAGE);
					n.setPosition(Position.MIDDLE_RIGHT);
				} else if (t > 1) {
					n = new Notification("MAYAAAAAAA!! ", Type.WARNING_MESSAGE);
					n.setPosition(Position.TOP_RIGHT);
				} else {
					n = new Notification("BUTZ!! ", Type.WARNING_MESSAGE);
					n.setPosition(Position.BOTTOM_RIGHT);
				}
				n.setDescription("ehhh... " + event.getTouches());
				n.show(rezui.getPage());

			}
		});
		bExt.allowBallerHitEvents(true); // If false, client side won't send any
											// events to the server
		bExt.allowBallerRecordEvents(true); // If false, client side won't send
											// any events to the server
		bExt.setWicked(false);
	}

}

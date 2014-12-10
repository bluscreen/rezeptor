package de.dhbw.rezeptor.window;

import java.util.Properties;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

import de.dhbw.rezeptor.RezeptorUI;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Bewertung;
import de.dhbw.rezeptor.domain.Person;
import de.dhbw.rezeptor.domain.Rezept;
import de.dhbw.rezeptor.domain.Zutat;
import de.dhbw.rezeptor.util.Berechtigor;

public class Window_DeleteConfirm extends Window {

	private final static Properties prop = SystemProperties.getProperties();
	private RezeptorUI rezui = (RezeptorUI) UI.getCurrent();
	private Zutat z = null;
	private Person per = null;
	private Rezept r = null;
	private Bewertung b = null;
	private int typ = 0;
	private String statusMessage;

	public Window_DeleteConfirm(Object o, Person p) {
		if (!Berechtigor.darfRan(p, o)) {
			close();
			Notification.show("error.berechtigung");
		} else {

			if (o instanceof Zutat) {
				z = (Zutat) o;
				typ = 1;
			} else if (o instanceof Person) {
				per = (Person) o;
				typ = 2;
			} else if (o instanceof Rezept) {
				r = (Rezept) o;
				typ = 3;
			} else if (o instanceof Bewertung) {
				b = (Bewertung) o;
				typ = 4;
			}
			setCaption(prop.getProperty("ui.caption.deleteReally") + ": " + o);

			center();
			setCloseShortcut(KeyCode.ESCAPE, null);
			setClosable(true);
			setSizeUndefined();
			setModal(true);

			addStyleName("no-vertical-drag-hints");
			addStyleName("no-horizontal-drag-hints");

			Button delete = new Button(prop.getProperty("ui.caption.loeschen"));
			delete.addClickListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {

					switch (typ) {
					case 1:
						rezui.dataProvider.deleteZutat(z);
						close();
						Notification.show(prop.getProperty("status.geschafft"));
						break;

					case 2:
						rezui.dataProvider.deletePerson(per);
						close();
						Notification.show(prop.getProperty("status.geschafft"));
						break;

					case 3:
						rezui.dataProvider.deleteRezept(r);
						close();
						Notification.show(prop.getProperty("status.geschafft"));
						break;

					case 4:
						rezui.dataProvider.deleteBewertung(b);
						close();
						Notification.show(prop.getProperty("status.geschafft"));
						break;

					default:
						Notification.show(prop.getProperty("error"));
						break;
					}
				}
			});
			Button no = new Button(prop.getProperty("ui.caption.nein"));
			no.addClickListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					close();
					Notification.show(prop.getProperty("status.nagut"));
				}
			});

			HorizontalLayout hl = new HorizontalLayout();
			hl.addComponents(no, delete);
			hl.setMargin(true);
			hl.setSpacing(true);
			setContent(hl);
		}
	}
}

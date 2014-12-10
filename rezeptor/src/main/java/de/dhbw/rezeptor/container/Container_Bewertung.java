package de.dhbw.rezeptor.container;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.vaadin.teemu.ratingstars.RatingStars;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.dhbw.rezeptor.RezeptorUI;
import de.dhbw.rezeptor.data.SystemProperties;
import de.dhbw.rezeptor.domain.Bewertung;
import de.dhbw.rezeptor.domain.Person;
import de.dhbw.rezeptor.util.Hr;

/**
 * Container zur anzeige der bewertungen eines rezeptes/zutat...
 * rendert ebenfalls das formular
 * @author dhammacher
 *
 */

public class Container_Bewertung extends VerticalLayout {

	private RezeptorUI rezui = (RezeptorUI) UI.getCurrent();
	private final static Properties prop = SystemProperties.getProperties();
	private BeanItemContainer<Bewertung> bewertungContainer = new BeanItemContainer<Bewertung>(
			Bewertung.class);

	private Person person = (Person) VaadinSession.getCurrent().getAttribute(
			prop.getProperty("session.user"));

	public Container_Bewertung(List<Bewertung> bewertungen, Bewertung initialBewertung, String caption) {


		initialBewertung.setVerfasser(person);
		initialBewertung.setAnlageDatum(new Date());

		final FormLayout bewertungAddLayout = new FormLayout();
		bewertungAddLayout.setCaption(caption);
		int i = 1;
		int ownPostIndex = -1;
		final VerticalLayout bewertungenContent = new VerticalLayout();
		for (Bewertung b : bewertungen) {
			FormLayout f = renderBewertungShow(b);
			if (b.getVerfasser().equals(person)) {
				ownPostIndex = i;
			}
			i += 2;
			bewertungenContent.addComponent(new Hr());
			bewertungenContent.addComponent(f);
		}
		final RatingStars stars = new RatingStars();
		stars.setCaption(prop.getProperty("ui.caption.bewertung"));
		stars.setRequired(true);
		stars.setRequiredError(prop.getProperty("error.bewertungReq"));

		TextArea beschreibung = new TextArea();
		beschreibung.setCaption(prop.getProperty("ui.caption.beschreibung"));

		TextField titel = new TextField(prop.getProperty("ui.caption.titel"));

		Button commitButton = new Button(
				prop.getProperty("ui.caption.speichern"));
		bewertungAddLayout.addComponents(stars, titel, beschreibung,
				commitButton);

		PropertysetItem bewertungPSI = new PropertysetItem();
		
		
		Bewertung alteEigeneBewertung = rezui.dataProvider
				.getBewertungByBewertung(initialBewertung);
		if (alteEigeneBewertung != null) {
			initialBewertung = alteEigeneBewertung;
		} else {
			initialBewertung.setRating(Double.parseDouble(prop
					.getProperty("ui.def.bewertung.rating")));
			initialBewertung.setBezeichnung(prop
					.getProperty("ui.def.bewertung.titel"));
			initialBewertung.setBeschreibung(prop
					.getProperty("ui.def.bewertung.beschreibung"));
		}
		stars.setValue(initialBewertung.getRating());

		final Bewertung neuBewertung = initialBewertung;
		bewertungPSI.addItemProperty("rating", new ObjectProperty<Double>(
				neuBewertung.getRating()));
		bewertungPSI.addItemProperty("bezeichnung", new ObjectProperty<String>(
				neuBewertung.getBezeichnung()));
		bewertungPSI.addItemProperty("beschreibung",
				new ObjectProperty<String>(neuBewertung.getBeschreibung()));
		final FieldGroup bewertungFG = new FieldGroup();
		bewertungFG.setItemDataSource(bewertungPSI);
		bewertungFG.bind(stars, "rating");
		bewertungFG.bind(titel, "bezeichnung");
		bewertungFG.bind(beschreibung, "beschreibung");

		final int oldPostIndex = ownPostIndex;
		commitButton.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				String statusMessage = prop.getProperty("status.oops");
				try {
					bewertungFG.commit();
					neuBewertung.setRating(stars.getValue());
					neuBewertung.setBeschreibung((String) bewertungFG.getField(
							"beschreibung").getValue());
					neuBewertung.setBezeichnung((String) bewertungFG.getField(
							"bezeichnung").getValue());
					statusMessage = rezui.dataProvider
							.persistBewertung(neuBewertung);
				} catch (CommitException e) {
					statusMessage = prop.getProperty("error.commitFG")
							+ e.getMessage();
				}

				Notification n = new Notification(prop
						.getProperty("ui.caption.geschafft"));
				n.setDescription(statusMessage);
				n.setDelayMsec(1500);
				n.show(rezui.getPage());
				// wir entfernen das formular und den alten eintrag des users,
				// falls gefunden
				removeComponent(bewertungAddLayout);
				if (oldPostIndex != -1) {
					bewertungenContent.removeComponent(bewertungenContent
							.getComponent(oldPostIndex));
					bewertungenContent.removeComponent(bewertungenContent
							.getComponent(oldPostIndex - 1));
				}
				FormLayout f = renderBewertungShow(neuBewertung);
				bewertungenContent.addComponent(new Hr());
				bewertungenContent.addComponent(f);
			}
		});
		addComponents(bewertungAddLayout, bewertungenContent);
	}

	private FormLayout renderBewertungShow(Bewertung b) {
		FormLayout f = new FormLayout();
		Label titel = new Label("'" + b.getBezeichnung() + "' "
				+ prop.getProperty("ui.caption.von") + " " + b.getVerfasser()
				+ ", " + b.getAnlageDatum());
		titel.setCaption(prop.getProperty("ui.caption.titel"));
		RatingStars rating = new RatingStars();
		rating.setValue(b.getRating());
		rating.setReadOnly(true);
		Label beschreibung = new Label(b.getBeschreibung());
		f.addComponents(titel, rating, beschreibung);
		return f;
	}
}

package de.dhbw.rezeptor.util;

import de.dhbw.rezeptor.domain.Bewertung;
import de.dhbw.rezeptor.domain.Favorit;
import de.dhbw.rezeptor.domain.Kategorie;
import de.dhbw.rezeptor.domain.Mengeneinheit;
import de.dhbw.rezeptor.domain.Person;
import de.dhbw.rezeptor.domain.Rezept;
import de.dhbw.rezeptor.domain.Zutat;

public class Berechtigor {
	public static boolean darfRan(Person p, Object o) {
		// UserLevel...
		// 100: superadmin (kann nicht gelöscht werden)
		// 90: -(andere) PersonEdit
		// 80: - Kategorie, Mengeneinheit Edit
		// 70: - ZutatEdit
		// 60: - RezeptEdit
		// 50: - BewertungEdit
		boolean isOwner = false;
		boolean darfRan = false;

		if (p.getBerechtigung() > 99) {
			darfRan = true;
		} else if (o instanceof Zutat) {
			if (p.getBerechtigung() > 69)
				darfRan = true;
			else {
				Zutat z = (Zutat) o;
				isOwner = z.getVerfasser().equals(p) ? true : false;
			}
		} else if (o instanceof Person) {
			if (p.getBerechtigung() > 89)
				if (p.getBerechtigung() < 100) {
					darfRan = true;
				} else {
					Person per = (Person) o;
					isOwner = per.equals(p) ? true : false;
				}
		} else if (o instanceof Rezept) {
			if (p.getBerechtigung() > 59)
				darfRan = true;
			else {
				Rezept r = (Rezept) o;
				isOwner = r.getVerfasser().equals(p) ? true : false;
			}
		} else if (o instanceof Bewertung) {
			if (p.getBerechtigung() > 49)
				darfRan = true;
			else {
				Bewertung b = (Bewertung) o;
				isOwner = b.getVerfasser().equals(p) ? true : false;
			}
		} else if (o instanceof Kategorie) {
			if (p.getBerechtigung() > 79)
				darfRan = true;
			else {
				Kategorie b = (Kategorie) o;
				isOwner = b.getVerfasser().equals(p) ? true : false;
			}
		} else if (o instanceof Mengeneinheit) {
			if (p.getBerechtigung() > 79)
				darfRan = true;
			else {
				Mengeneinheit b = (Mengeneinheit) o;
				isOwner = b.getVerfasser().equals(p) ? true : false;
			}
		}
		if (!darfRan)
			darfRan = isOwner;

		return darfRan;
	}
}

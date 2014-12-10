package de.dhbw.rezeptor.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.mindrot.jbcrypt.BCrypt;

import de.dhbw.rezeptor.domain.Kategorie;
import de.dhbw.rezeptor.domain.Mengeneinheit;
import de.dhbw.rezeptor.domain.Person;
import de.dhbw.rezeptor.domain.Zutat;

/**
 * Der Generator erstellt einige daten zu testzwecken und persistiert sie in der
 * db..
 * 
 * @author dhammacher
 * 
 */

public class Generator {

	public static void generateTestUser(EntityManager em) {
		em.getTransaction().begin();
		Query q = em.createNamedQuery("Person_Count");
		long size = (Long) q.getSingleResult();
		if (size == 0) {
			Person p = new Person();
			// TO DA MAX!
			p.setBerechtigung(100);
			p.setEmail("testuser@dhbw.de");
			p.setVorname("Daniel");
			p.setNachname("Hammacher");
			p.setPasswort(BCrypt.hashpw("12345678", BCrypt.gensalt(12)));

			// teste das mal...
			System.out
					.println(BCrypt.checkpw("12345678", p.getPasswort()) ? "PASS"
							: "FAIL");

			em.persist(p);
		}
		em.getTransaction().commit();
		em.close();
	}

	public static void generateMengenEinheit(EntityManager em) {
		Query q = em.createNamedQuery("Mengeneinheit_Count");
		em.getTransaction().begin();
		long size = (Long) q.getSingleResult();
		if (size == 0) {
			HashMap<String, String> mengeneinheiten = new HashMap<String, String>();
			mengeneinheiten.put("st", "Stück");
			mengeneinheiten.put("g", "Gramm");
			mengeneinheiten.put("kg", "Kilogramm");
			mengeneinheiten.put("dl", "Deciliter");
			mengeneinheiten.put("l", "Liter");
			mengeneinheiten.put("el", "Esslöffel");
			mengeneinheiten.put("tl", "Tischlöffel");
			mengeneinheiten.put("kiste", "Kiste");
			mengeneinheiten.put("kiepe", "Kiepe");
			mengeneinheiten.put("sack", "Sack");
			mengeneinheiten.put("prise", "Prise");
			mengeneinheiten.put("pkg", "Packung");
			mengeneinheiten.put("tuetc", "Tütchen");
			mengeneinheiten.put("tasse", "Tasse");
			mengeneinheiten.put("taf", "Tafel");
			mengeneinheiten.put("glas", "Glas");
			mengeneinheiten.put("spiess", "Spieß");
			mengeneinheiten.put("bec", "Becher");
			mengeneinheiten.put("fass", "Fass");
			mengeneinheiten.put("ms", "Messerspitze");

			// packung löffelbisquits, 1 tasse espresso, 250g quark, 1 pkg
			// mascarpone, 1/2 becher sahne, 1 tütchen vanillezucker, ca 2 el
			// zucker, 1 becher sahnejoghurt, 1 pkg rote grütze, 1/2 tafel weiße
			// schoki

			Iterator<String> keySetIterator = mengeneinheiten.keySet()
					.iterator();
			long i = 1;
			while (keySetIterator.hasNext()) {
				i++;
				String key = keySetIterator.next();
				Mengeneinheit m = new Mengeneinheit();
				m.setId(i);
				m.setBezeichnung(mengeneinheiten.get(key));
				m.setKuerzel(key);
				em.persist(m);
			}
		}
		em.getTransaction().commit();
		em.close();
	}

	public static void generateKategorie(EntityManager em) {
		Query q = em.createNamedQuery("Kategorie_Count");
		em.getTransaction().begin();
		long size = (Long) q.getSingleResult();
		if (size == 0) {

			String[] k1 = { "Vor- & Nachspeisen", "Hauptspeisen", "Getraenke" };
			String[][] k2 = { { "Pudding", "Joghurt", "Obstsalat", "Kuchen" },
					{ "Herzhaft", "Gesund", "Burger", "Pasta", "Pizza" },
					{ "Wein", "Cocktails", "Bier" } };

			for (long i = 0; i < k1.length; i++) {
				Kategorie k = new Kategorie();
				k.setId(i + 1);
				k.setBezeichnung(k1[(int) i]);
				em.persist(k);
				for (long j = 0; j < k2[(int) i].length; j++) {
					Kategorie ka = new Kategorie();

					// dont try this at home...
					ka.setId(10 + (i * i * j + 1) * (i + j) * (j + 7) + 3);
					ka.setElternKategorie(k);
					ka.setBezeichnung(k2[(int) i][(int) j]);
					em.persist(ka);
				}
			}
		}
		em.getTransaction().commit();
		em.close();
	}

	public static void generateZutat(EntityManager em, Mengeneinheit men,
			Person p) {
		Query q = em.createNamedQuery("Zutat_Count");
		em.getTransaction().begin();
		long size = (Long) q.getSingleResult();
		if (size == 0) {

			String[][] dieZutaten = { { "Fisch", "Fischig" },
					{ "Gurken", "Gurkig" }, { "Eier", "Eirig" },
					{ "Bier", "Bierig" }, { "Wein", "Weinig" },
					{ "Ketchup", "TOMATO" }, { "Kornflakes", "mit Ketchup" } };
			boolean[] v = { false, true, true, false, true, true, true, true };

			for (long i = 0; i < dieZutaten.length; i++) {
				Zutat z = new Zutat();
				z.setMengeneinheit(men);
				z.setId(i + 1);
				z.setBezeichnung(dieZutaten[(int) i][0]);
				z.setBeschreibung(dieZutaten[(int) i][1]);
				z.setVerfasser(p);
				z.setAnlageDatum(new Date());
				z.setVeggie(v[(int) i]);
				em.persist(z);
			}
		}
		em.getTransaction().commit();
		em.close();
	}

	public static String randomWord(int len, boolean capitalized) {
		String[] part = { "ger", "ma", "isa", "app", "le", "ni", "ke", "mic",
				"ro", "soft", "wa", "re", "lo", "gi", "is", "acc", "el", "tes",
				"la", "ko", "ni", "ka", "so", "ny", "mi", "nol", "ta", "pa",
				"na", "so", "nic", "sa", "les", "for", "ce" };
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++) {
			String p = part[(int) (Math.random() * part.length)];
			if (i == 0 && capitalized)
				p = Character.toUpperCase(p.charAt(0)) + p.substring(1);
			sb.append(p);
		}
		return sb.toString();

	}

	public static String randomText(int words) {
		StringBuffer sb = new StringBuffer();
		int sentenceWordsLeft = 0;
		while (words-- > 0) {
			if (sb.length() > 0)
				sb.append(' ');
			if (sentenceWordsLeft == 0 && words > 0) {
				sentenceWordsLeft = (int) (Math.random() * 15);
				sb.append(randomWord(1 + (int) (Math.random() * 3), true));
			} else {
				sentenceWordsLeft--;
				sb.append(randomWord(1 + (int) (Math.random() * 3), false));
				if (words > 0 && sentenceWordsLeft > 2 && Math.random() < 0.2)
					sb.append(',');
				else if (sentenceWordsLeft == 0 || words == 0)
					sb.append('.');
			}
		}
		return sb.toString();
	}
}
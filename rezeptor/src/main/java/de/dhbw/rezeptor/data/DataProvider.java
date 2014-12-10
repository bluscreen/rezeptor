package de.dhbw.rezeptor.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.mindrot.jbcrypt.BCrypt;

import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.server.VaadinSession;

import de.dhbw.rezeptor.domain.Bewertung;
import de.dhbw.rezeptor.domain.Favorit;
import de.dhbw.rezeptor.domain.Kategorie;
import de.dhbw.rezeptor.domain.Mengeneinheit;
import de.dhbw.rezeptor.domain.Person;
import de.dhbw.rezeptor.domain.Rezept;
import de.dhbw.rezeptor.domain.RezeptZutat;
import de.dhbw.rezeptor.domain.Zutat;

/**
 * Ein historisch gewachsenes Riesen DAO... besser wären eigene (abstrakte und
 * impl) DAO Klassen zur besseren Skalierung. Hier erstmal aus Zeitgründen
 * vernachlässigt
 * 
 * @author dhammacher
 * 
 */
public class DataProvider {
	private static final Properties prop = SystemProperties.getProperties();
	private EntityManagerFactory entityManagerFactory;

	public DataProvider() {
		this.entityManagerFactory = JPAContainerFactory
				.createEntityManagerForPersistenceUnit("de.dhbw")
				.getEntityManagerFactory();

		Generator.generateTestUser(this.entityManagerFactory
				.createEntityManager());
		Generator.generateMengenEinheit(this.entityManagerFactory
				.createEntityManager());

		Generator.generateKategorie(this.entityManagerFactory
				.createEntityManager());
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return this.entityManagerFactory;
	}

	public Object loginPerson(String email, String passwort) {
		if ((email == null) || (email.equals("")) || (passwort == null)
				|| (passwort.equals(""))) {
			return prop.getProperty("error.emailAndPWReq");
		}
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query query = em.createNamedQuery("Person_loginPerson");
		query.setParameter("email", email);
		em.getTransaction().commit();
		try {
			Person person = (Person) query.getSingleResult();
			if (BCrypt.checkpw(passwort, person.getPasswort())) {
				EntityManager em2 = this.entityManagerFactory
						.createEntityManager();
				em2.getTransaction().begin();
				Query loginUpdateQuery = em2
						.createNamedQuery("Person_UpdateLogin");
				loginUpdateQuery.setParameter("lastlogindate", new Date());
				loginUpdateQuery.setParameter("personid", person.getId());
				if (loginUpdateQuery.executeUpdate() == 1) {
					em2.getTransaction().commit();
					return person;
				} else {
					em2.getTransaction().rollback();
				}
			}
			return "Toerichter Narrrrr...";
		} catch (NoResultException e) {
			Object localObject1;
			return "E-Mail existiert nicht.";
		} finally {
			em.close();
		}
	}

	// ich weiß... is wirklich sau hässlich... aber tut!
	public String persistPerson(Person person, boolean selbst, boolean isNeu)
			throws PersistenceException, NullPointerException {
		String statusMessage = "";
		// 0= error,1=status, 2=pwok
		if (person == null) {
			throw new NullPointerException(
					prop.getProperty("error.nullPointer"));
		} else {
			boolean neuePWIdentisch = person.getPasswort_neu1().equals(
					person.getPasswort_neu2()) ? true : false;
			boolean pwLaengeHinreichend = person.getPasswort_neu1().length() >= 8 ? true
					: false;
			boolean neuePWFelderGefuellt = ((person.getPasswort_neu1() != null)
					&& (person.getPasswort_neu2() != null)
					&& (!person.getPasswort_neu1().isEmpty())
					&& (!person.getPasswort_neu2().isEmpty())
					&& (person.getPasswort_neu1().trim() != "") && (person
					.getPasswort_neu2().trim() != "")) ? true : false;
			boolean altesPWFeldGefuellt = ((person.getPasswort_alt() != null)
					&& (!person.getPasswort_alt().isEmpty()) && person
					.getPasswort_alt().trim() != "") ? true : false;

			boolean pwGesetzt = false;

			EntityManager em = this.entityManagerFactory.createEntityManager();
			em.getTransaction().begin();

			if (isNeu) {
				if (neuePWIdentisch) {
					if (pwLaengeHinreichend) {
						person.setPasswort(BCrypt.hashpw(
								person.getPasswort_neu1(), BCrypt.gensalt(12)));
						pwGesetzt = true;
					} else {
						em.getTransaction().rollback();
						em.close();
						throw new PersistenceException(
								prop.getProperty("error.pwZuKurz"));
					}
				} else {
					em.getTransaction().rollback();
					em.close();
					throw new PersistenceException(
							prop.getProperty("error.pwNichtIdentisch"));
				}
			} else {
				if (selbst) {
					if (altesPWFeldGefuellt) {
						if (neuePWFelderGefuellt) {
							if (neuePWIdentisch) {
								if (pwLaengeHinreichend) {
									// Gucken ob altes pw aus db mit
									// eingegebenem
									// uebereinstimmt
									person.setPasswort(getPersonById(
											person.getId()).getPasswort());
									if (BCrypt.checkpw(
											person.getPasswort_alt(),
											person.getPasswort())) {
										person.setPasswort(BCrypt.hashpw(
												person.getPasswort_neu1(),
												BCrypt.gensalt(12)));
										pwGesetzt = true;
									} else {
										em.getTransaction().rollback();
										em.close();
										throw new PersistenceException(
												prop.getProperty("error.pwAltNichtIdentisch"));
									}
								} else {
									em.getTransaction().rollback();
									em.close();
									throw new PersistenceException(
											prop.getProperty("error.pwZuKurz"));
								}
							} else {
								em.getTransaction().rollback();
								em.close();
								throw new PersistenceException(
										prop.getProperty("error.pwNichtIdentisch"));
							}
						} else {
							em.getTransaction().rollback();
							em.close();
							throw new PersistenceException(
									prop.getProperty("error.altesPwEmpty"));
						}
					}

				} else {
					// es wird jemand anderes editiert
					if (neuePWFelderGefuellt) {
						// wenn die felder ueberhaupt gefuellt sind, aendern wir
						// vielleicht was
						if (neuePWIdentisch) {
							if (pwLaengeHinreichend) {
								person.setPasswort(BCrypt.hashpw(
										person.getPasswort_neu1(),
										BCrypt.gensalt(12)));
								pwGesetzt = true;
							} else {
								em.getTransaction().rollback();
								em.close();
								throw new PersistenceException(
										prop.getProperty("error.pwZuKurz"));
							}
						} else {
							em.getTransaction().rollback();
							em.close();
							throw new PersistenceException(
									prop.getProperty("error.pwNichtIdentisch"));
						}
					}
				}
			}
			if (isNeu) {
				em.persist(person);
				statusMessage = prop.getProperty("status.userHinzugefuegt");
			} else {
				em.merge(person);
				statusMessage = prop.getProperty("status.userGeaendert");
				if (pwGesetzt) {
					Query nq = em.createNamedQuery("Person_UpdatePasswort");
					nq.setParameter("passwort", person.getPasswort());
					nq.setParameter("personid", person.getId());
					if (nq.executeUpdate() != 1) {
						throw new PersistenceException(
								prop.getProperty("error.multiUpdate"));
					}
					statusMessage += "\n"
							+ prop.getProperty("status.pwAkzeptiert");
				}
			}
			if (selbst) {
				VaadinSession.getCurrent().setAttribute(
						prop.getProperty("session.user"), person);
			}
			em.getTransaction().commit();
			em.close();
		}
		return statusMessage;
	}

	public String persistRezept(Rezept rezept) {
		String result = "";
		if (rezept == null) {
			result = "WTF NULLPOINTER";
		} else {
			EntityManager em = this.entityManagerFactory.createEntityManager();
			em.getTransaction().begin();
			if (rezept.getId() == null) {
				em.persist(rezept);
				result = result + "Rezept gespeichert.";
			} else {
				em.merge(rezept);
				result = result + "Rezept geaendert.";
			}
			em.getTransaction().commit();
			VaadinSession.getCurrent().setAttribute("LastRezeptId",
					rezept.getId());

			em.close();
		}
		return result;
	}

	public String persistZutat(Zutat zutat) {
		String result = "";
		if (zutat == null) {
			result = "WTF NULLPOINTER";
		} else {
			EntityManager em = this.entityManagerFactory.createEntityManager();
			em.getTransaction().begin();
			if (zutat.getId() == null) {
				em.persist(zutat);
				result = result + "Zutat gespeichert.";
			} else {
				em.merge(zutat);
				result = result + "Zutat geaendert.";
			}
			VaadinSession.getCurrent().setAttribute("LastZutatId",
					zutat.getId());

			em.getTransaction().commit();
			em.close();
		}
		return result;
	}

	public String persistBewertung(Bewertung bewertung) {
		String result = "";
		if (bewertung == null) {
			result = "WTF NULLPOINTER";
		} else {
			EntityManager em = this.entityManagerFactory.createEntityManager();

			em.getTransaction().begin();
			Bewertung dbBew = getBewertungByBewertung(bewertung);
			if (dbBew == null) {
				em.persist(bewertung);
				result = result + "Bewertung gespeichert.";
			} else {
				bewertung.setId(dbBew.getId());
				em.merge(bewertung);
				result = result + "Bewertung geï¿½ndert.";
			}
			em.getTransaction().commit();
			em.close();
		}
		return result;
	}

	public String persistFavorit(Favorit f) {
		String result = "";
		if (f == null) {
			result = "WTF NULLPOINTER";
		} else {
			EntityManager em = this.entityManagerFactory.createEntityManager();

			em.getTransaction().begin();
			em.persist(f);
			result = result + "Favorit gespeichert.";
			em.getTransaction().commit();
			em.close();
		}
		return result;
	}

	public String deleteFavorit(Favorit f) {
		String result = "";
		if (f == null) {
			result = "WTF NULLPOINTER";
		} else {
			EntityManager em = this.entityManagerFactory.createEntityManager();
			em.getTransaction().begin();
			Query nq = null;
			if (f.getRezept() != null) {
				nq = em.createNamedQuery("Favorit_DeleteRezeptPerson");
				nq.setParameter("rezept", f.getRezept());
				nq.setParameter("verfasser", f.getVerfasser());
			} else {
				nq = em.createNamedQuery("Favorit_DeleteZutatPerson");
				nq.setParameter("zutat", f.getZutat());
				nq.setParameter("verfasser", f.getVerfasser());
			}
			int res = nq.executeUpdate();
			if (res == 1) {
				result = result + "Favorit entfernt.";
				em.getTransaction().commit();
			} else {
				result = result + "Favorit konnte nicht entfernt werden: "
						+ res + " Datensï¿½tze gefunden";

				em.getTransaction().rollback();
			}
			em.close();
		}
		return result;
	}

	public Rezept getRezeptById(Object id) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Rezept r = (Rezept) em.find(Rezept.class, id);
		em.getTransaction().commit();
		em.close();
		r = rezeptAnreichernBewertung(r);
		return r;
	}

	public List<RezeptZutat> getRezeptZutatenByRezept(Rezept rezept) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		if ((rezept.getId() != null) && (!rezept.getId().equals(""))) {
			em.getTransaction().begin();
			Rezept r = (Rezept) em.find(Rezept.class, rezept.getId());

			Query nq = em.createNamedQuery("RezeptZutat_getAllByRezept");
			nq.setParameter("rezept", r);
			List<RezeptZutat> rzl = nq.getResultList();
			em.getTransaction().commit();
			em.close();
			return rzl;
		}
		return null;
	}

	public Rezept getRandomRezept() {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();

		Rezept randomRezept = null;
		for (int i = 0; i < 5; i++) {
			Rezept rez = null;
			try {
				Query nq = em.createNamedQuery("Rezept_SelectRandom");
				rez = (Rezept) nq.getSingleResult();
			} catch (NoResultException e) {
				// TODO logging
			}
			if (rez != null) {
				randomRezept = rez;
				break;
			}
		}
		em.getTransaction().commit();
		em.close();
		randomRezept = rezeptAnreichernBewertung(randomRezept);
		return randomRezept;
	}

	public Bewertung getBewertungByBewertung(Bewertung b) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		// wenn bewertung oder rezept gefuellt und so..
		if ((b != null) && (b.getVerfasser() != null)
				&& ((b.getRezept() != null) || (b.getZutat() != null))) {
			em.getTransaction().begin();
			Query nq;
			if (b.getRezept() != null) {
				nq = em.createNamedQuery("Bewertung_SelectRezeptPerson");
				nq.setParameter("rezept", b.getRezept());
				nq.setParameter("verfasser", b.getVerfasser());
			} else {
				nq = em.createNamedQuery("Bewertung_SelectZutatPerson");
				nq.setParameter("zutat", b.getZutat());
				nq.setParameter("verfasser", b.getVerfasser());
			}
			Bewertung bew = null;
			try {
				bew = (Bewertung) nq.getSingleResult();
			} catch (NoResultException e) {
			}
			em.getTransaction().commit();
			em.close();
			return bew;
		}
		return null;
	}

	public Person getPersonById(Object id) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Person p = (Person) em.find(Person.class, id);
		em.getTransaction().commit();
		em.close();
		return p;
	}

	public Mengeneinheit getMengeneinheitById(Object id) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Mengeneinheit p = (Mengeneinheit) em.find(Mengeneinheit.class, id);
		em.getTransaction().commit();
		em.close();
		return p;
	}

	public Zutat getZutatById(Object id) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Zutat p = (Zutat) em.find(Zutat.class, id);
		em.getTransaction().commit();
		em.close();
		p = zutatAnreichernBewertung(p);
		return p;
	}

	public Zutat getZutatByBezeichnung(String bez) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query nq = em.createNamedQuery("Zutat_SelectBezeichnung");
		nq.setParameter("bezeichnung", bez);
		Zutat p = null;
		try {
			p = (Zutat) nq.getSingleResult();
		} catch (NoResultException e) {
			// NO RESULT
		}
		em.getTransaction().commit();
		em.close();
		p = zutatAnreichernBewertung(p);
		return p;
	}

	public Rezept getRezeptByBezeichnung(String bez) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query nq = em.createNamedQuery("Rezept_SelectBezeichnung");
		nq.setParameter("bezeichnung", bez);
		Rezept p = null;
		try {
			p = (Rezept) nq.getSingleResult();
		} catch (NoResultException e) {
			// NO RESULT
		}
		em.getTransaction().commit();
		em.close();
		p = rezeptAnreichernBewertung(p);
		return p;
	}

	public List<Rezept> getAlleRezepte() {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		List<Rezept> lr = em.createNamedQuery("Rezept_SelectAll")
				.getResultList();

		em.getTransaction().commit();
		em.close();
		lr = rezepteAnreichernBewertung(lr);
		return lr;
	}

	// TODO evtl mal em übergeben falls performance...
	public Rezept rezeptAnreichernBewertung(Rezept r) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query nq = em.createNamedQuery("Bewertung_SelectRezeptAvg");
		nq.setParameter("rezept", r);
		Double ratres = (Double) nq.getSingleResult();
		if (ratres != null) {
			r.setRating(ratres.doubleValue());
		}
		em.getTransaction().commit();
		em.close();
		return r;
	}

	public Zutat zutatAnreichernBewertung(Zutat r) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query nq = em.createNamedQuery("Bewertung_SelectZutatAvg");
		nq.setParameter("zutat", r);
		Double ratres = (Double) nq.getSingleResult();
		if (ratres != null) {
			r.setRating(ratres.doubleValue());
		}
		em.getTransaction().commit();
		em.close();
		return r;
	}

	public Person personAnreichernAnzahlEintraege(Person r) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query nq = em.createNamedQuery("Bewertung_CountPerson");
		nq.setParameter("verfasser", r);
		Long bewAnz = (Long) nq.getSingleResult();
		if (bewAnz != null) {
			r.setAnzBewertungen(bewAnz);
		}
		Query nqr = em.createNamedQuery("Rezept_CountPerson");
		nqr.setParameter("verfasser", r);
		Long rezAnz = (Long) nqr.getSingleResult();
		if (rezAnz != null) {
			r.setAnzRezepte(rezAnz);
		}
		Query nqz = em.createNamedQuery("Zutat_CountPerson");
		nqz.setParameter("verfasser", r);
		Long zutAnz = (Long) nqz.getSingleResult();
		if (zutAnz != null) {
			r.setAnzZutaten(zutAnz);
		}
		em.getTransaction().commit();
		em.close();
		return r;
	}

	// wurde seines em beraubt...
	public List<Rezept> rezepteAnreichernBewertung(List<Rezept> lr) {
		for (Rezept r : lr) {
			r = rezeptAnreichernBewertung(r);
		}
		return lr;
	}

	public List<Zutat> zutatenAnreichernBewertung(List<Zutat> lr) {
		for (Zutat r : lr) {
			r = zutatAnreichernBewertung(r);
		}
		return lr;
	}

	public List<Person> personenAnreichernAnzahlEintraege(List<Person> lr) {
		for (Person r : lr) {
			r = personAnreichernAnzahlEintraege(r);
		}
		return lr;
	}

	public List<Rezept> getFavoritRezepteByPerson(Person p) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();

		Query nq = em.createNamedQuery("Favorit_SelectPersonRezept");
		nq.setParameter("verfasser", p);
		List<Favorit> lf = nq.getResultList();

		List<Rezept> rezepte = null;

		Collection<Long> favorites = new ArrayList();
		if (lf.size() > 0) {
			for (Favorit f : lf) {
				favorites.add(f.getRezept().getId());
			}
			Query nq2 = em.createNamedQuery("Rezept_SelectFavoriten");
			nq2.setParameter("rezepte", favorites);
			rezepte = nq2.getResultList();
		} else {
			rezepte = new ArrayList();
		}
		em.getTransaction().commit();
		em.close();
		rezepte = rezepteAnreichernBewertung(rezepte);
		return rezepte;
	}

	public List<Zutat> getFavoritZutatenByPerson(Person p) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();

		Query nq = em.createNamedQuery("Favorit_SelectPersonZutat");
		nq.setParameter("verfasser", p);
		List<Favorit> lf = nq.getResultList();

		List<Zutat> zutaten = null;

		Collection<Long> favorites = new ArrayList();
		if (lf.size() > 0) {
			for (Favorit f : lf) {
				favorites.add(f.getZutat().getId());
			}
			Query nq2 = em.createNamedQuery("Zutat_SelectFavoriten");
			nq2.setParameter("zutaten", favorites);
			zutaten = nq2.getResultList();
		} else {
			zutaten = new ArrayList();
		}
		em.getTransaction().commit();
		em.close();
		zutaten = zutatenAnreichernBewertung(zutaten);
		return zutaten;
	}

	public List<Favorit> getFavoritenByRezeptOrZutat(Rezept r, Zutat z) {
		List<Favorit> lf = null;

		if (r != null) {
			// finde favoriten zu rezept r
		}

		if (z != null) {
			// finde favoriten zu zutat z

		}
		return lf;
	}

	public List<Rezept> getFilteredRezepte(String zutaten, int anzahl, Person p) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		String natQString = "";
		if (p != null) {

			// TODO ... vielleicht geht das doch mit namedquery...
			// http://www.objectdb.com/java/jpa/query/jpql/where#WHERE_Filter_in_Multi_Variable_Queries_
			natQString = "select re.* from rezept re join favorit f on re.id = f.rezept_id join rezept_rezeptzutat as rezu on re.id = rezu.rezept_id join rezeptzutat as zu on rezu.rezeptzutaten_id = zu.id where zutat_id in "
					+ zutaten
					+ " and f.verfasser_id = "
					+ p.getId()
					+ " group by re.id having count(re.id) >= " + anzahl + ";";
		} else {
			natQString = "select re.* from rezept re join rezept_rezeptzutat as rezu on re.id = rezu.rezept_id join rezeptzutat as zu on rezu.rezeptzutaten_id = zu.id where zutat_id in "
					+ zutaten
					+ " group by re.id having count(re.id) >= "
					+ anzahl + ";";
		}
		Query natQ = em.createNativeQuery(natQString, Rezept.class);
		List<Rezept> lr = natQ.getResultList();
		em.getTransaction().commit();
		em.close();
		lr = rezepteAnreichernBewertung(lr);
		return lr;
	}

	public List<Rezept> getPersonRezepte(Person p) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query nq = em.createNamedQuery("Rezept_SelectPerson");
		nq.setParameter("verfasser", p);
		List<Rezept> lr = nq.getResultList();
		em.getTransaction().commit();
		em.close();
		lr = rezepteAnreichernBewertung(lr);
		return lr;
	}

	public List<Bewertung> getPersonBewertungen(Person p) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query nq = em.createNamedQuery("Bewertung_SelectPerson");
		nq.setParameter("verfasser", p);
		List<Bewertung> lr = nq.getResultList();
		em.getTransaction().commit();
		em.close();
		return lr;
	}

	public List<Rezept> getLatestRezepte() {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query nq = em.createNamedQuery("Rezept_SelectLatest");
		List<Rezept> lr = nq.getResultList();
		em.getTransaction().commit();
		em.close();
		lr = rezepteAnreichernBewertung(lr);
		return lr;
	}

	public List<Rezept> getRezepteByAvgRating() {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query nq = em.createNamedQuery("Rezept_SelectLatest");
		List<Rezept> lr = nq.getResultList();
		em.getTransaction().commit();
		em.close();
		// TODO blabla
		lr = rezepteAnreichernBewertung(lr);
		return lr;
	}
	
	public Favorit getFavoritRezeptPerson(Rezept r, Person p) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query nq = em.createNamedQuery("Favorit_SelectRezeptPerson");
		nq.setParameter("verfasser", p);
		nq.setParameter("rezept", r);
		Favorit fav = null;
		try {
			fav = (Favorit) nq.getSingleResult();
		} catch (NoResultException e) {
			// TODO Logging (Kein favorit gefudnen)
		}
		em.getTransaction().commit();
		em.close();
		return fav;
	}

	public Favorit getFavoritZutatPerson(Zutat r, Person p) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query nq = em.createNamedQuery("Favorit_SelectZutatPerson");
		nq.setParameter("verfasser", p);
		nq.setParameter("zutat", r);
		Favorit fav = null;
		try {
			fav = (Favorit) nq.getSingleResult();
		} catch (NoResultException e) {
			// TODO Logging (Kein favorit gefudnen)
		}
		em.getTransaction().commit();
		em.close();
		return fav;
	}

	public void clickRezept(Rezept r) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query nq = em.createNamedQuery("Rezept_UpdateClick");
		nq.setParameter("rezeptid", r.getId());
		nq.executeUpdate();
		em.getTransaction().commit();
		em.close();
	}

	public List<Bewertung> getRezeptBewertungen(Rezept r) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query nq = em.createNamedQuery("Bewertung_SelectRezept");
		nq.setParameter("rezept", r);
		List<Bewertung> lr = nq.getResultList();
		em.getTransaction().commit();
		em.close();
		return lr;
	}

	public List<Bewertung> getZutatBewertungen(Zutat r) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query nq = em.createNamedQuery("Bewertung_SelectZutat");
		nq.setParameter("zutat", r);
		List<Bewertung> lr = nq.getResultList();
		em.getTransaction().commit();
		em.close();
		return lr;
	}

	public List<Mengeneinheit> getAlleMengeneinheiten() {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		List<Mengeneinheit> lm = em.createNamedQuery("Mengeneinheit_SelectAll")
				.getResultList();

		em.getTransaction().commit();
		em.close();
		return lm;
	}

	public List<Zutat> getAlleZutaten() {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		List<Zutat> lm = em.createNamedQuery("Zutat_SelectAll").getResultList();
		em.getTransaction().commit();
		em.close();
		lm = zutatenAnreichernBewertung(lm);
		return lm;
	}

	public List<Zutat> getPersonZutaten(Person p) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		Query nq = em.createNamedQuery("Zutat_SelectPerson");
		nq.setParameter("verfasser", p);
		List<Zutat> lr = nq.getResultList();
		em.getTransaction().commit();
		em.close();
		lr = zutatenAnreichernBewertung(lr);
		return lr;
	}

	public List<Person> getAllePersonen() {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		List<Person> lm = em.createNamedQuery("Person_SelectAll")
				.getResultList();

		em.getTransaction().commit();
		em.close();
		lm = personenAnreichernAnzahlEintraege(lm);
		return lm;
	}

	public List<Kategorie> getAlleKategorien() {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();

		List<Kategorie> lm = em.createNamedQuery("Kategorie_SelectAll")
				.getResultList();

		em.getTransaction().commit();
		em.close();
		return lm;
	}

	public void deleteRezept(Rezept r) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		r = em.find(Rezept.class, r.getId());
		em.remove(r);
		em.flush();

		// List<Favorit> lf =
		em.getTransaction().commit();
		em.close();
	}

	public void deleteZutat(Zutat r) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		r = em.find(Zutat.class, r.getId());
		em.remove(r);
		em.flush();
		em.getTransaction().commit();
		em.close();
	}

	public void deletePerson(Person r) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		r = em.find(Person.class, r.getId());
		em.remove(r);
		em.flush();
		em.getTransaction().commit();
		em.close();
	}

	public void deleteBewertung(Bewertung r) {
		EntityManager em = this.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		r = em.find(Bewertung.class, r.getId());
		em.remove(r);
		em.flush();
		em.getTransaction().commit();
		em.close();
	}
}

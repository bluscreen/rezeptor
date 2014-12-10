package de.dhbw.rezeptor.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@NamedQueries({
		@NamedQuery(name = "Rezept_CountPerson", query = "SELECT COUNT(z) FROM Rezept z where z.verfasser = :verfasser"),
		@NamedQuery(name = "Rezept_SelectAll", query = "SELECT r FROM Rezept r ORDER BY r.kategorie.bezeichnung, r.bezeichnung"),
		@NamedQuery(name = "Rezept_SelectLatest", query = "SELECT r FROM Rezept r ORDER BY r.anlageDatum DESC, r.bezeichnung ASC"),
		@NamedQuery(name = "Rezept_SelectFavoriten", query = "SELECT r FROM Rezept r WHERE r.id IN :rezepte ORDER BY r.kategorie.bezeichnung, r.bezeichnung"),
		@NamedQuery(name = "Rezept_UpdateClick", query = "UPDATE Rezept r SET r.clicks = r.clicks + 1 WHERE r.id = :rezeptid"),
		@NamedQuery(name = "Rezept_SelectPerson", query = "SELECT r FROM Rezept r WHERE r.verfasser = :verfasser ORDER BY r.kategorie.bezeichnung, r.bezeichnung"),
		@NamedQuery(name = "Rezept_SelectBezeichnung", query = "SELECT z FROM Rezept z WHERE z.bezeichnung = :bezeichnung ORDER BY z.bezeichnung") })
@NamedNativeQuery(name = "Rezept_SelectRandom", query = "SELECT r.* FROM rezeptor.rezept r where id = ceil(rand()*(select max(rezz.id) as anz from rezept rezz)) limit 1", resultClass = Rezept.class)
public class Rezept {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(unique = true)
	private String bezeichnung;

	@OneToMany(cascade = CascadeType.ALL)
	@OrderBy("id")
	private List<RezeptZutat> rezeptZutaten;

	@ManyToOne
	private Kategorie kategorie;

	@ManyToOne
	private Person verfasser;

	@Temporal(TemporalType.DATE)
	private Date anlageDatum;
	private double dauer;
	private double anzahlPersonen;
	private String thumburl;

	@Lob
	@Column(length = 10000)
	private String beschreibung;

	private Long clicks = 0L;

	@Transient
	private double rating;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

	public List<RezeptZutat> getRezeptZutaten() {
		return rezeptZutaten;
	}

	public void setRezeptZutaten(List<RezeptZutat> rezeptZutaten) {
		this.rezeptZutaten = rezeptZutaten;
	}

	public Person getVerfasser() {
		return verfasser;
	}

	public void setVerfasser(Person verfasser) {
		this.verfasser = verfasser;
	}

	public Date getAnlageDatum() {
		return anlageDatum;
	}

	public void setAnlageDatum(Date anlageDatum) {
		this.anlageDatum = anlageDatum;
	}

	public double getDauer() {
		return dauer;
	}

	public void setDauer(double dauer) {
		this.dauer = dauer;
	}

	public double getAnzahlPersonen() {
		return anzahlPersonen;
	}

	public void setAnzahlPersonen(double anzahlPersonen) {
		this.anzahlPersonen = anzahlPersonen;
	}

	public String getThumburl() {
		return thumburl;
	}

	public void setThumburl(String thumburl) {
		this.thumburl = thumburl;
	}

	public Kategorie getKategorie() {
		return kategorie;
	}

	public void setKategorie(Kategorie kategorie) {
		this.kategorie = kategorie;
	}

	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}

	public Long getClicks() {
		return clicks;
	}

	public void setClicks(Long clicks) {
		this.clicks = clicks;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	@Override
	public String toString() {
		return bezeichnung;
	}

	public String printAll() {
		String zutaten = "zutaten: ";
		for (RezeptZutat rz : rezeptZutaten) {
			zutaten += rz.printAll();
		}
		return "kategorie: " + kategorie.printAll() + " id: " + id
				+ " bezeichnung: " + bezeichnung + " " + zutaten
				+ " anlagedatum: " + anlageDatum + " verfasser: "
				+ verfasser.printAll() + " thumburl: " + thumburl;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Person) {
			Rezept x = (Rezept) obj;
			if (this == x) {
				return true;
			}
			if (this.id == null || x.id == null) {
				return false;
			}
			return this.id.equals(x.id);
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return id == null ? 0 : id.hashCode();
	}

}

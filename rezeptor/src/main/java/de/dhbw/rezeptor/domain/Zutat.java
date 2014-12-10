package de.dhbw.rezeptor.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@NamedQueries({
		@NamedQuery(name = "Zutat_SelectAll", query = "SELECT z FROM Zutat z ORDER BY z.bezeichnung"),
		@NamedQuery(name = "Zutat_Count", query = "SELECT COUNT(z) FROM Zutat z"),
		@NamedQuery(name = "Zutat_CountPerson", query = "SELECT COUNT(z) FROM Zutat z where z.verfasser = :verfasser"),
		@NamedQuery(name = "Zutat_SelectFavoriten", query = "SELECT z FROM Zutat z WHERE z.id IN :zutaten ORDER BY z.bezeichnung"),
		@NamedQuery(name = "Zutat_SelectPerson", query = "SELECT z FROM Zutat z WHERE z.verfasser = :verfasser ORDER BY z.bezeichnung"),
		@NamedQuery(name = "Zutat_SelectBezeichnung", query = "SELECT z FROM Zutat z WHERE z.bezeichnung = :bezeichnung ORDER BY z.bezeichnung")})
public class Zutat {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(unique = true)
	private String bezeichnung;

	@ManyToOne
	private Mengeneinheit mengeneinheit;

	private boolean veggie;

	@Temporal(TemporalType.DATE)
	private Date anlageDatum;

	private String thumburl;

	@ManyToOne
	private Person verfasser;

	@Lob
	@Column(length = 10000)
	private String beschreibung;
	
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

	public Mengeneinheit getMengeneinheit() {
		return mengeneinheit;
	}

	public void setMengeneinheit(Mengeneinheit mengeneinheit) {
		this.mengeneinheit = mengeneinheit;
	}

	public boolean isVeggie() {
		return veggie;
	}

	public void setVeggie(boolean veggie) {
		this.veggie = veggie;
	}

	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}

	public Date getAnlageDatum() {
		return anlageDatum;
	}

	public void setAnlageDatum(Date anlageDatum) {
		this.anlageDatum = anlageDatum;
	}

	public Person getVerfasser() {
		return verfasser;
	}

	public void setVerfasser(Person verfasser) {
		this.verfasser = verfasser;
	}

	public String getThumburl() {
		return thumburl;
	}

	public void setThumburl(String thumburl) {
		this.thumburl = thumburl;
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
		String vegstr = veggie ? "ja" : "nein";
		return "id: " + id + " bezeichnung: " + bezeichnung
				+ " mengeneinheit: " + mengeneinheit.printAll()
				+ " vegetarisch: " + vegstr + " verfasser: "
				+ verfasser.printAll() + " anlagedatum " + anlageDatum
				+ " beschreibung: " + beschreibung + " thumbnail: " + thumburl
				+ "\n";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Person) {
			Zutat z = (Zutat) obj;
			if (this == z) {
				return true;
			}
			if (this.id == null || z.id == null) {
				return false;
			}
			return this.id.equals(z.id);
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return id == null ? 0 : id.hashCode();
	}

}

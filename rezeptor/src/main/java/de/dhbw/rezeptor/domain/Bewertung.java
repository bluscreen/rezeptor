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

@Entity
@NamedQueries({
		@NamedQuery(name = "Bewertung_SelectAll", query = "SELECT z FROM Bewertung z ORDER BY z.anlageDatum"),
		@NamedQuery(name = "Bewertung_Count", query = "SELECT COUNT(z) FROM Bewertung z"),
		@NamedQuery(name = "Bewertung_CountPerson", query = "SELECT COUNT(z) FROM Bewertung z where z.verfasser = :verfasser"),
		@NamedQuery(name = "Bewertung_SelectRezeptAvg", query = "SELECT AVG(z.rating) FROM Bewertung z WHERE z.rezept = :rezept"),
		@NamedQuery(name = "Bewertung_SelectZutatAvg", query = "SELECT AVG(z.rating) FROM Bewertung z WHERE z.zutat = :zutat"),
		@NamedQuery(name = "Bewertung_SelectRezept", query = "SELECT z FROM Bewertung z WHERE z.rezept = :rezept ORDER BY z.anlageDatum ASC"),
		@NamedQuery(name = "Bewertung_SelectZutat", query = "SELECT z FROM Bewertung z WHERE z.zutat = :zutat ORDER BY z.anlageDatum ASC"),
		@NamedQuery(name = "Bewertung_SelectRezeptPerson", query = "SELECT z FROM Bewertung z WHERE z.rezept = :rezept AND z.verfasser = :verfasser ORDER BY z.bezeichnung"),
		@NamedQuery(name = "Bewertung_SelectZutatPerson", query = "SELECT z FROM Bewertung z WHERE z.zutat = :zutat AND z.verfasser = :verfasser ORDER BY z.bezeichnung"),
		@NamedQuery(name = "Bewertung_SelectPersonRezept", query = "SELECT z FROM Bewertung z WHERE z.verfasser = :verfasser AND z.rezept is not null ORDER BY z.bezeichnung"),
		@NamedQuery(name = "Bewertung_SelectPersonZutat", query = "SELECT z FROM Bewertung z WHERE z.verfasser = :verfasser AND z.zutat is not null ORDER BY z.bezeichnung"),
		@NamedQuery(name = "Bewertung_SelectPerson", query = "SELECT z FROM Bewertung z WHERE z.verfasser = :verfasser ORDER BY z.bezeichnung")})
public class Bewertung {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String bezeichnung;
	
	@ManyToOne
	private Rezept rezept;
	
	@ManyToOne
	private Zutat zutat;

	@Temporal(TemporalType.DATE)
	private Date anlageDatum;

	private String thumburl;

	@ManyToOne
	private Person verfasser;
	
	private double rating;

	@Lob
	@Column(length = 10000)
	private String beschreibung;

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

	public Rezept getRezept() {
		return rezept;
	}

	public void setRezept(Rezept rezept) {
		this.rezept = rezept;
	}
	
	public Zutat getZutat() {
		return zutat;
	}

	public void setZutat(Zutat zutat) {
		this.zutat = zutat;
	}

	@Override
	public String toString() {
		return bezeichnung;
	}
	
	

	public String printAll() {
		return "id: " + id + " bezeichnung: " + bezeichnung
				+ " rezept: " + rezept.printAll()
				+ " rating: " + rating + " verfasser: "
				+ verfasser.printAll() + " anlagedatum " + anlageDatum
				+ " beschreibung: " + beschreibung + " thumbnail: " + thumburl
				+ "\n";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Person) {
			Bewertung z = (Bewertung) obj;
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

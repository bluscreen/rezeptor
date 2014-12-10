package de.dhbw.rezeptor.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@NamedQueries({
		@NamedQuery(name = "Person_loginPerson", query = "SELECT DISTINCT p FROM Person p WHERE p.email = :email"),
		@NamedQuery(name = "Person_SelectAll", query = "SELECT p FROM Person p"),
		@NamedQuery(name = "Person_UpdatePasswort", query = "UPDATE Person p SET p.passwort = :passwort WHERE p.id = :personid"),
		@NamedQuery(name = "Person_UpdateLogin", query = "UPDATE Person p SET p.lastLoginDate = :lastlogindate WHERE p.id = :personid"),
		@NamedQuery(name = "Person_Count", query = "SELECT COUNT(p) FROM Person p") })
public class Person {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String vorname;
	private String nachname;
	
//	das l‰uft hier nicht. und warum? weiﬂ keiner...
//	@Pattern(regexp = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\."
//			+ "[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
//			+ "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message = "bla")
	@Column(nullable = false, unique = true)
	private String email;
	
	@Column(nullable = false, updatable = false)
	private String passwort;
	@Temporal(TemporalType.DATE)
	private Date geburtsdatum;

	@Transient
	private String passwort_alt;
	@Transient
	private String passwort_neu1;
	@Transient
	private String passwort_neu2;
	@Transient
	private Long anzRezepte;
	@Transient
	private Long anzZutaten;
	@Transient
	private Long anzBewertungen;

	private int berechtigung = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(insertable = false, updatable = false)
	private Date lastLoginDate;

	private String thumburl;

	@Lob
	@Column(length = 10000)
	private String notizen;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getVorname() {
		return vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public String getNachname() {
		return nachname;
	}

	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswort() {
		return passwort;
	}

	public void setPasswort(String passwort) {
		this.passwort = passwort;
	}

	public Date getGeburtsdatum() {
		return geburtsdatum;
	}

	public void setGeburtsdatum(Date geburtsdatum) {
		this.geburtsdatum = geburtsdatum;
	}

	public String getPasswort_alt() {
		return passwort_alt;
	}

	public void setPasswort_alt(String passwort_alt) {
		this.passwort_alt = passwort_alt;
	}

	public String getPasswort_neu1() {
		return passwort_neu1;
	}

	public void setPasswort_neu1(String passwort_neu1) {
		this.passwort_neu1 = passwort_neu1;
	}

	public String getPasswort_neu2() {
		return passwort_neu2;
	}

	public void setPasswort_neu2(String passwort_neu2) {
		this.passwort_neu2 = passwort_neu2;
	}

	public String getNotizen() {
		return notizen;
	}

	public void setNotizen(String notizen) {
		this.notizen = notizen;
	}

	@Override
	public String toString() {
		return vorname + " " + nachname;
	}

	public String printAll() {
		return "id: " + id + " vorname: " + vorname + " nachname: " + nachname
				+ " email: " + email + " geburtsdatum: " + geburtsdatum
				+ " notizen: " + notizen + "\n";
	}

	public int getBerechtigung() {
		return berechtigung;
	}

	public void setBerechtigung(int berechtigung) {
		this.berechtigung = berechtigung;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public String getThumburl() {
		return thumburl;
	}

	public void setThumburl(String thumburl) {
		this.thumburl = thumburl;
	}

	public Long getAnzRezepte() {
		return anzRezepte;
	}

	public void setAnzRezepte(Long anzRezepte) {
		this.anzRezepte = anzRezepte;
	}

	public Long getAnzZutaten() {
		return anzZutaten;
	}

	public void setAnzZutaten(Long anzZutaten) {
		this.anzZutaten = anzZutaten;
	}

	public Long getAnzBewertungen() {
		return anzBewertungen;
	}

	public void setAnzBewertungen(Long anzBewertungen) {
		this.anzBewertungen = anzBewertungen;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Person) {
			Person p = (Person) obj;
			if (this == p) {
				return true;
			}
			if (this.id == null || p.id == null) {
				return false;
			}
			return this.id.equals(p.id);
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return id == null ? 0 : id.hashCode();
	}

}

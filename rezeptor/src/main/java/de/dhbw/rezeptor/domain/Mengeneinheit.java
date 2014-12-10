package de.dhbw.rezeptor.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({
		@NamedQuery(name = "Mengeneinheit_SelectAll", query = "SELECT m FROM Mengeneinheit m ORDER BY m.kuerzel"),
		@NamedQuery(name = "Mengeneinheit_Count", query = "SELECT COUNT(m) FROM Mengeneinheit m") })
public class Mengeneinheit {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(unique = true)
	private String bezeichnung;
	@Column(unique = true)
	private String kuerzel;
	
	@ManyToOne
	private Person verfasser;

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

	public String getKuerzel() {
		return kuerzel;
	}

	public void setKuerzel(String kuerzel) {
		this.kuerzel = kuerzel;
	}
	
	public Person getVerfasser() {
		return verfasser;
	}

	public void setVerfasser(Person verfasser) {
		this.verfasser = verfasser;
	}

	@Override
	public String toString() {
		return bezeichnung;
	}
	
	public String printAll() {
		return "id: " + id + " kuerzel: " + kuerzel + " bezeichnung: "
				+ bezeichnung + "\n";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Person) {
			Mengeneinheit m = (Mengeneinheit) obj;
			if (this == m) {
				return true;
			}
			if (this.id == null || m.id == null) {
				return false;
			}
			return this.id.equals(m.id);
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return id == null ? 0 : id.hashCode();
	}

}

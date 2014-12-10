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
		@NamedQuery(name = "Kategorie_SelectAll", query = "SELECT k FROM Kategorie k ORDER BY k.elternKategorie.id, k.bezeichnung"),
		@NamedQuery(name = "Kategorie_Count", query = "SELECT COUNT(k) FROM Kategorie k") })
public class Kategorie {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(unique = true)
	private String bezeichnung;

	@ManyToOne
	private Kategorie elternKategorie;
	
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

	public Kategorie getElternKategorie() {
		return elternKategorie;
	}

	public void setElternKategorie(Kategorie elternKategorie) {
		this.elternKategorie = elternKategorie;
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
		String ret = "id: " + id + " bezeichnung: " + bezeichnung
				+ " ElternKategorie(n): ";
		Kategorie elternElement = this.elternKategorie;
		while (elternElement != null) {
			ret += "> " + elternElement.getId() + "."
					+ elternElement.getBezeichnung();
			elternElement = elternElement.getElternKategorie();
		}
		return ret + "\n";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Person) {
			Kategorie z = (Kategorie) obj;
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

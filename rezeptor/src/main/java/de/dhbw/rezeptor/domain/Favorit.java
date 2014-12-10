package de.dhbw.rezeptor.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@NamedQueries({
		@NamedQuery(name = "Favorit_SelectAll", query = "SELECT z FROM Favorit z ORDER BY z.anlageDatum"),
		@NamedQuery(name = "Favorit_Count", query = "SELECT COUNT(z) FROM Favorit z"),
		@NamedQuery(name = "Favorit_DeleteRezeptPerson", query = "DELETE FROM Favorit f WHERE f.rezept = :rezept AND f.verfasser = :verfasser"),
		@NamedQuery(name = "Favorit_DeleteZutatPerson", query = "DELETE FROM Favorit f WHERE f.zutat = :zutat AND f.verfasser = :verfasser"),
		@NamedQuery(name = "Favorit_SelectRezept", query = "SELECT z FROM Favorit z WHERE z.rezept = :rezept ORDER BY z.anlageDatum ASC"),
		@NamedQuery(name = "Favorit_SelectZutat", query = "SELECT z FROM Favorit z WHERE z.zutat = :zutat ORDER BY z.anlageDatum ASC"),
		@NamedQuery(name = "Favorit_SelectRezeptPerson", query = "SELECT z FROM Favorit z WHERE z.rezept = :rezept AND z.verfasser = :verfasser"),
		@NamedQuery(name = "Favorit_SelectZutatPerson", query = "SELECT z FROM Favorit z WHERE z.zutat = :zutat AND z.verfasser = :verfasser"),
		@NamedQuery(name = "Favorit_SelectPersonRezept", query = "SELECT z FROM Favorit z WHERE z.verfasser = :verfasser AND z.rezept is not null"),
		@NamedQuery(name = "Favorit_SelectPersonZutat", query = "SELECT z FROM Favorit z WHERE z.verfasser = :verfasser AND z.zutat is not null")})
public class Favorit {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	private Rezept rezept;
	
	@ManyToOne
	private Zutat zutat;

	@Temporal(TemporalType.DATE)
	private Date anlageDatum;

	@ManyToOne
	private Person verfasser;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String printAll() {
		return "id: " + id + " rezept: " + rezept.printAll() + " verfasser: "
				+ verfasser.printAll() + " anlagedatum " + anlageDatum + "\n";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Person) {
			Favorit z = (Favorit) obj;
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

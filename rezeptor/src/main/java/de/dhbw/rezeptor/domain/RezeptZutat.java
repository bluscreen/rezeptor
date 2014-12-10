package de.dhbw.rezeptor.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@NamedQueries({ @NamedQuery(name = "RezeptZutat_getAllByRezept", query = "SELECT z FROM Rezept r, RezeptZutat z WHERE r=:rezept ") })
public class RezeptZutat {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	
	@OneToOne
	@NotNull
	private Zutat zutat;

	@OneToOne
	@NotNull
	private Mengeneinheit mengeneinheit;

	@Max(30000)
	@Min(1)
	private double menge = 1;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Zutat getZutat() {
		return zutat;
	}

	public void setZutat(Zutat zutat) {
		this.zutat = zutat;
	}

	public Mengeneinheit getMengeneinheit() {
		return mengeneinheit;
	}

	public void setMengeneinheit(Mengeneinheit mengeneinheit) {
		this.mengeneinheit = mengeneinheit;
	}

	public double getMenge() {
		return menge;
	}

	public void setMenge(double menge) {
		this.menge = menge;
	}

	@Override
	public String toString() {
		return " id: " + id + " zutat: " + zutat.toString();
	}

	public String printAll() {
		return " id: " + id + " zutat: " + zutat.printAll()
				+ " mengeneinheit: " + mengeneinheit.printAll() + " menge: "
				+ menge + "\n";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Person) {
			RezeptZutat x = (RezeptZutat) obj;
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

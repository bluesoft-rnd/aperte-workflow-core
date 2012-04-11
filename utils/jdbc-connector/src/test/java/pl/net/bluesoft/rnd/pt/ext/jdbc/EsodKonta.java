package pl.net.bluesoft.rnd.pt.ext.jdbc;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ESOD_Konta")
public class EsodKonta {

    private String numerKonta;
    private String nazwaKonta;
    private Character typKonta;
    private Integer statusKonta;

    public EsodKonta() {
    }

    public EsodKonta(String numerKonta, String nazwaKonta,
                       Character typKonta, Integer statusKonta) {
        this.numerKonta = numerKonta;
        this.nazwaKonta = nazwaKonta;
        this.typKonta = typKonta;
        this.statusKonta = statusKonta;
    }

    public String getNumerKonta() {
        return this.numerKonta;
    }

    public void setNumerKonta(String numerKonta) {
        this.numerKonta = numerKonta;
    }

    public String getNazwaKonta() {
        return this.nazwaKonta;
    }

    public void setNazwaKonta(String nazwaKonta) {
        this.nazwaKonta = nazwaKonta;
    }

    public Character getTypKonta() {
        return this.typKonta;
    }

    public void setTypKonta(Character typKonta) {
        this.typKonta = typKonta;
    }

    public Integer getStatusKonta() {
        return this.statusKonta;
    }

    public void setStatusKonta(Integer statusKonta) {
        this.statusKonta = statusKonta;
    }

    public boolean equals(Object other) {
        if ((this == other))
            return true;
        if ((other == null))
            return false;
        if (!(other instanceof EsodKonta))
            return false;
        EsodKonta castOther = (EsodKonta) other;

        return ((this.getNumerKonta() == castOther.getNumerKonta()) || (this
                .getNumerKonta() != null && castOther.getNumerKonta() != null && this
                .getNumerKonta().equals(castOther.getNumerKonta())))
                && ((this.getNazwaKonta() == castOther.getNazwaKonta()) || (this
                .getNazwaKonta() != null
                && castOther.getNazwaKonta() != null && this
                .getNazwaKonta().equals(castOther.getNazwaKonta())))
                && ((this.getTypKonta() == castOther.getTypKonta()) || (this
                .getTypKonta() != null
                && castOther.getTypKonta() != null && this
                .getTypKonta().equals(castOther.getTypKonta())))
                && ((this.getStatusKonta() == castOther.getStatusKonta()) || (this
                .getStatusKonta() != null
                && castOther.getStatusKonta() != null && this
                .getStatusKonta().equals(castOther.getStatusKonta())));
    }

    public int hashCode() {
        int result = 17;

        result = 37
                * result
                + (getNumerKonta() == null ? 0 : this.getNumerKonta()
                .hashCode());
        result = 37
                * result
                + (getNazwaKonta() == null ? 0 : this.getNazwaKonta()
                .hashCode());
        result = 37 * result
                + (getTypKonta() == null ? 0 : this.getTypKonta().hashCode());
        result = 37
                * result
                + (getStatusKonta() == null ? 0 : this.getStatusKonta()
                .hashCode());
        return result;
    }

}

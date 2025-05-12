import java.util.List;

public class Disciplina {
    String nomeD;
    String codigoD;
    int cargaH;
    List<Disciplina> requisitos;

    public void disciplina(String nomeD, String codigoD, int cargaH, List<Disciplina> requisitos){
        this.nomeD = nomeD;
        this.codigoD = codigoD;
        this.cargaH = cargaH;
        this.requisitos = requisitos;
    }

    public void setNomeD(String nomeD){
        this.nomeD = nomeD;
    }

    public void setCodigoD(String codigoD){
        this.codigoD = codigoD;
    }

    public void setCargaH(Integer cargaH){
        this.cargaH = cargaH;
    }

    public void setRequisitos(List<Disciplina> requisitos) {
        this.requisitos = requisitos;
    }

    public String getNomeD(){
        return nomeD;
    }

    public String getCodigoD(){
        return codigoD;
    }
    public Integer getCargaH(){
        return cargaH;
    }

    public List<Disciplina> getRequisitos() {
        return requisitos;
    }
}

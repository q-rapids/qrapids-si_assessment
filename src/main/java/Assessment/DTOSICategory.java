package Assessment;

public class DTOSICategory {
    private String idSICategory;
    private float probSICategory;

    public DTOSICategory(String idSICategory, float probSICategory) {
        this.idSICategory = idSICategory;
        this.probSICategory = probSICategory;
    }

    public String getIdSICategory() {
        return idSICategory;
    }

    public void setIdSICategory(String idSICategory) {
        this.idSICategory = idSICategory;
    }

    public float getProbSICategory() {
        return probSICategory;
    }

    public void setProbSICategory(float probSICategory) {
        this.probSICategory = probSICategory;
    }

    @Override
    public String toString() {
        return "idSICategory='" + idSICategory + '\'' +
                ", probSICategory=" + probSICategory +
                '\n';
    }
}

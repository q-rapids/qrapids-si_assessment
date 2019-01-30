package Assessment;

import java.util.ArrayList;

public class DTOSIAssessment {
    private ArrayList<DTOSICategory> probsSICategories;

    DTOSIAssessment(ArrayList<DTOSICategory> probsSICategories) {
        this.probsSICategories = probsSICategories;
    }

    public DTOSIAssessment() {
        this.probsSICategories = new ArrayList<>();
    }

    public ArrayList<DTOSICategory> getProbsSICategories() {
        return probsSICategories;
    }

    @Override
    public String toString() {
        return probsSICategories.toString();
    }
}

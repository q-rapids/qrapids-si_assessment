import Assessment.SIAssessment;
import Assessment.DTOSIAssessment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TestUtil {

    public static void main(String[] args) {
        try {
            String dneFiles = "src/test/java/";

            // TEST 1
            Map<String, String> factorStates = new HashMap<>();
            factorStates.put("blockingcode", "Low");
            factorStates.put("softwarestability", "High");
            factorStates.put("codequality", "High");

            assessmentTest(dneFiles + "WSA_Blocking.dne", "blocking", factorStates);

            // TEST 2
            factorStates = new HashMap<>();
            factorStates.put("CodeQuality", "VeryHigh");
            factorStates.put("TestingStatus", "High");

            assessmentTest(dneFiles + "WSA_ProductQuality2factors.dne", "ProductQuality", factorStates);

            // TEST 3
            factorStates = new HashMap<>();
            factorStates.put("codequality", "Low");
            factorStates.put("softwarestability", "Medium");
            factorStates.put("testingstatus", "High");

            assessmentTest(dneFiles + "WSA_ProductQuality.dne", "productquality", factorStates);

            // TEST 4
//            factorStates = new HashMap<>();
//            factorStates.put("codequality", "Medium");
//            factorStates.put("softwarestability", "Very_Low");
//
//            assessmentTest(dneFiles + "WSA_productquality_nokia.dne", "productquality", factorStates);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void assessmentTest(String pathName, String SInodeName, Map<String, String> factorStates) {
        try {
            File file = new File(pathName);

            DTOSIAssessment assessmentTest = SIAssessment.AssessSI(SInodeName, factorStates, file);

            System.out.print("Probabilites after entering beliefs for node " + SInodeName + ": ");
            System.out.println(factorStates.toString());
            System.out.println(assessmentTest.toString());
    } catch (Exception e) {
        e.printStackTrace();
    }

}


}

import Util_Assessment_SI.BayesUtils;
import Util_Assessment_SI.Connection;
import Util_Assessment_SI.Constants;

import java.io.FileWriter;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BayesUtilsTests {

    public static void main(String[] args) throws Exception {
        LocalDate dateFrom = LocalDate.of(2019, 05, 01);//LocalDate.of(2018,12, 01);
        LocalDate dateTo = LocalDate.of(2019, 05, 25);//LocalDate.of(2019, 05, 01);
        String projectId="";

        String ip = ""; //Set value before test
        int port = 9200; //Set value before test
        String path= "";
        String prefix = "";
        String username = "";
        String password = "";

        FileWriter csvWriter = null;
        System.err.println("////QUERY TEST UTIL////");
        try {
            //OPEN CONNECTION
            Connection.initConnection(ip, port, path, prefix, username, password);

            //SI UTILS
            String[] factors = {"activitycompletion", "knownremainingdefects", "productstability"};
            String[] elements = {"developmenttaskcompletion", "specificationtaskcompletion", "posponedissuesratio",
                    "buildstability", "criticalissuesratio", "testsuccess"};
            String si = "productreadiness";
            Constants.QMLevel level = Constants.QMLevel.metrics;
            String[] five_categories = {"VeryLow", "Low", "Medium", "High", "VeryHigh"};
            String[] si_five_categories = {"VeryBad", "Bad", "Neutral", "Good", "VeryGood"};
            String[] three_categories = {"Low", "Medium", "High"};
            int desiredIntervals = 5;
            String[][] categories_per_element = {five_categories,
                                                five_categories,
                                                three_categories,
                                                five_categories,
                                                five_categories,
                                                five_categories};

            double[][] intervals_per_element = {{0.45f, 0.7f, 0.9f, 0.95f},
                                            {0.2f, 0.7f, 0.9f, 0.99f},
                                            {0.45f, 0.8f},
                                            {0.4f, 0.7f, 0.8f, 0.95f},
                                            {0.4f, 0.7f, 0.8f, 0.98f},
                                            {0.4f, 0.7f, 0.8f, 0.98f}};

            String BNpath = "";
            csvWriter = new FileWriter("");

            //DISCRETIZATION
            double[] equalWidthIntervals = BayesUtils.makeEqualWidthIntervals(desiredIntervals);
            System.out.println("EQUAL WIDTH BINNING: " + Arrays.toString(equalWidthIntervals));
            for (String element: elements) {
                String[] arrayOfSingleElement = {element};
                double[] equalFrequencyIntervals = BayesUtils.makeEqualFrequencyIntervals(desiredIntervals, projectId,
                        level, arrayOfSingleElement, dateFrom, dateTo);
                System.out.println("EQUAL FREQUENCY BINNING FOR " + element + " : " + Arrays.toString(equalFrequencyIntervals));
            }

            //FREQUENCY QUANTIFICATION
            Map<String, Map<String, Float>> elmenentFrequencies = BayesUtils.getFrequencyQuantification(projectId, level,
                    elements, intervals_per_element, dateFrom, dateTo);
            elmenentFrequencies.forEach((element, mapElementFreq) -> {
                System.out.println("ELEMENT " + element + " freqüency quantification:");
                mapElementFreq.forEach((interval, percentage) -> System.out.println(interval + " : " + percentage));
            });

           // OBSERVED SCENARIOS FOR PARENT NODES
            System.out.println("Observed combinations for elements: " + Arrays.toString(elements));
            Map<List<String>, Integer> observedCombinations = BayesUtils.getCommonConfigurations(projectId, level,
                    elements, categories_per_element, intervals_per_element, dateFrom, dateTo);
            System.out.println(Arrays.toString(elements) + " : findings");
            observedCombinations.forEach((key, value) -> System.out.println(key + " : " + value));
//
            //OBSERVED SCENARIOS FOR CHILD NODES (PROPAGATING PROBABILITIES)
            System.out.println("OBSERVED SCENARIOS FOR CHILD NODES (PROPAGATING PROBABILITIES)");
            System.out.println("Observed combinations for elements: " + Arrays.toString(factors));
            Map<List<String>, Integer> childsObservedCombinations = BayesUtils.getChildCommonConfigurations(projectId, level,
                    elements, categories_per_element, intervals_per_element, factors, BNpath, dateFrom, dateTo);
            System.out.println(Arrays.toString(factors) + " : findings");
            childsObservedCombinations.forEach((key, value) -> System.out.println(key + " : " + value));

            //SI ASSESSED STATES
            System.out.println("Building assessments' CSV");
            BayesUtils.buildChildPastStates(projectId, level, elements, categories_per_element, intervals_per_element,
                    si, si_five_categories, BNpath, dateFrom, dateTo, csvWriter);

        } catch (Exception e) {
            throw e;
        }
        finally {
            Connection.closeConnection();
            csvWriter.flush();
            csvWriter.close();
        }
        }}

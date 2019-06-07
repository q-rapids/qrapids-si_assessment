package Util_Assessment_SI;

import Assessment.DTOSIAssessment;
import Assessment.DTOSICategory;
import Assessment.SIAssessment;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.range.Range;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class BayesUtils {
    public static Map<List<String>, Integer> getCommonConfigurations(String projectId, Constants.QMLevel QMType,
                                                                     String[] elements, String[][] categories,
                                                                     double[][] intervals, LocalDate from, LocalDate to)
            throws IOException {
        //construir diccionari
        List<String> elementsArray = Arrays.asList(elements);
        Map<List<String>, Integer> observedCombinations = new HashMap<>();

        for (LocalDate currentDay = from; !currentDay.isAfter(to); currentDay = currentDay.plusDays(1)) {
            SearchResponse sr = Queries.getFilteredDay(projectId, QMType, currentDay, elements);
            SearchHits hits = sr.getHits();
            String[] combination = new String[elements.length];
            for (SearchHit hit : hits) {
                //processar factor i correspondencia value - categoria
                Map<String, Object> hitSource = hit.getSource();
                String element = Queries.getStringFromMap(hitSource, QMtoID(QMType));
                float value = Float.parseFloat(Queries.getStringFromMap(hitSource, Constants.VALUE));
                // construir combinatoria
                int elementIndex = elementsArray.indexOf(element);
                combination[elementIndex] = discretize(value, intervals[elementIndex], categories[elementIndex]);// combinatoria construida
            }
            observedCombinations.merge(Arrays.asList(combination), 1, Integer::sum);
        }
        return observedCombinations;
    }

    public static Map<List<String>, Integer> getChildCommonConfigurations(String projectId, Constants.QMLevel QMType,
                                                                     String[] parentElements, String[][] parentCategories,
                                                                     double[][] parentIntervals, String[] childElements,
                                                                          String BNPath, LocalDate from, LocalDate to)
            throws Exception {
        //construir diccionari
        List<String> elementsArray = Arrays.asList(parentElements);
        Map<List<String>, Integer> observedCombinations = new HashMap<>();
        File file = new File(BNPath);
        for (LocalDate currentDay = from; !currentDay.isAfter(to); currentDay = currentDay.plusDays(1)) {
            SearchResponse sr = Queries.getFilteredDay(projectId, QMType, currentDay, parentElements);
            SearchHits hits = sr.getHits();
            Map<String, String> parentStates = new HashMap<>();
            for (SearchHit hit : hits) {
                //processar factor i correspondencia value - categoria
                Map<String, Object> hitSource = hit.getSource();
                String element = Queries.getStringFromMap(hitSource, QMtoID(QMType));
                float value = Float.parseFloat(Queries.getStringFromMap(hitSource, Constants.VALUE));
                int elementIndex = elementsArray.indexOf(element);
                parentStates.put(element, discretize(value, parentIntervals[elementIndex], parentCategories[elementIndex]));
            }
            if (parentStates.size() > 0) {
                String[] childCombination = new String[childElements.length];
                int childElementIndex = 0;
                for (String childElement : childElements) {
                    DTOSIAssessment childAssessment = SIAssessment.AssessSI(childElement, parentStates, file);
                    childCombination[childElementIndex] = mostProbableCategory(childAssessment.getProbsSICategories());

                    childElementIndex++;
                }
                observedCombinations.merge(Arrays.asList(childCombination), 1, Integer::sum);
            }
        }
        return observedCombinations;
    }

    public static void buildChildPastStates(String projectId, Constants.QMLevel QMType,
                                            String[] parentElements, String[][] parentCategories,
                                            double[][] parentIntervals, String childElement,String[] childCategories,
                                            String BNPath, LocalDate from, LocalDate to, FileWriter csvWriter)
            throws Exception {
        //construir diccionari
        List<String> elementsArray = Arrays.asList(parentElements);
        File file = new File(BNPath);
        csvWriter.append("Day,");
        csvWriter.append(String.join(",", childCategories));
        csvWriter.append(",High.Prob.\n");
        for (LocalDate currentDay = from; !currentDay.isAfter(to); currentDay = currentDay.plusDays(1)) {
            SearchResponse sr = Queries.getFilteredDay(projectId, QMType, currentDay, parentElements);
            SearchHits hits = sr.getHits();
            Map<String, String> parentStates = new HashMap<>();
            for (SearchHit hit : hits) {
                //processar factor i correspondencia value - categoria
                Map<String, Object> hitSource = hit.getSource();
                String element = Queries.getStringFromMap(hitSource, QMtoID(QMType));
                float value = Float.parseFloat(Queries.getStringFromMap(hitSource, Constants.VALUE));
                int elementIndex = elementsArray.indexOf(element);
                parentStates.put(element, discretize(value, parentIntervals[elementIndex], parentCategories[elementIndex]));
            }
            if (parentStates.size() > 0) {
                DTOSIAssessment childAssessment = SIAssessment.AssessSI(childElement, parentStates, file);
                String[] dayProbabilities = buildDayProbabilities(childAssessment.getProbsSICategories(), currentDay);
                csvWriter.append(String.join(",", dayProbabilities));
                csvWriter.append("\n");
            }
        }
    }

    private static String[] buildDayProbabilities(ArrayList<DTOSICategory> categoriesAndProbabilities, LocalDate day) {
        String[] ret = new String[categoriesAndProbabilities.size() + 2];
        ret[0] = day.toString();
        for (int i = 0; i < categoriesAndProbabilities.size(); i++) {
            ret[i+1] = String.valueOf(categoriesAndProbabilities.get(i).getProbSICategory());
        }
        ret[ret.length - 1] = mostProbableCategory(categoriesAndProbabilities);
        return ret;
    }

    public static String mostProbableCategory(ArrayList<DTOSICategory> categories) {
        float highestProbability = 0;
        String highestProbabilityCategory = "";

        for (DTOSICategory categoryAndProbablity : categories) {
            if (categoryAndProbablity.getProbSICategory() > highestProbability) {
                highestProbabilityCategory = categoryAndProbablity.getIdSICategory();
                highestProbability = categoryAndProbablity.getProbSICategory();
            }
        }
        return highestProbabilityCategory;
    }

    public static Map<String, Map<String, Float>> getFrequencyQuantification(String projectId, Constants.QMLevel QMType,
                                                                             String[] elements, double[][] intervals,
                                                                             LocalDate from, LocalDate to)
            throws IOException {
        Map<String, Map<String, Float>> ret = new LinkedHashMap<>();
        int elementIndex = 0;
        for (String element : elements) {
            SearchResponse sr = Queries.getFrequencies(projectId, QMType, element, from, to, intervals[elementIndex]);
            long total = sr.getHits().totalHits;
            Range rangeAggregation = sr.getAggregations().get("categoryranges");
            Map<String, Float> dictFrequencies = new LinkedHashMap<>();
            for (Range.Bucket rangebucket : rangeAggregation.getBuckets()) {
                String key = String.format("%.2f",rangebucket.getFrom()) + "-" + String.format("%.2f",rangebucket.getTo());//rangebucket.getKeyAsString();          // bucket key
                long docCount = rangebucket.getDocCount();            // Doc count
                dictFrequencies.put(key, (float)docCount/total);
                System.err.println("Key: " + key + ", Doc Count: " + docCount);
            }
            ret.put(element, dictFrequencies);
            elementIndex++;
        }
        return ret;
    }

    private static String discretize(double value, double[] ranges, String[] categories) {
        int i = 0;
        if (value < ranges[0]) return categories[0];
        else if (value >= ranges[ranges.length-1]) return categories[categories.length-1];
        else {
            while (value >= ranges[i]) {
                //if (value < ranges[i]) break;
                i++;
            }
        }
        return categories[i];
    }

    public static double[] makeEqualWidthIntervals(int numBins) {
        double min = 0d;
        double max = 1d;

        double width = (max - min) / numBins;

        double[] intervals = new double[numBins * 2];
        intervals[0] = min;
        intervals[1] = min + width;
        for (int i = 2; i < intervals.length - 1; i += 2) {
            intervals[i] = Math.nextUp(intervals[i - 1]);
            intervals[i + 1] = intervals[i] + width;
        }
        intervals[intervals.length-1] = max;
        return intervals;
    }

    public static double[] makeEqualFrequencyIntervals(int numBins, String project, Constants.QMLevel QMType,
                                                       String[] elements, LocalDate from, LocalDate to) throws IOException {
        double min = 0d;
        double max = 1d;
        /**/
        //numBins = 3;
        /**/
        double[] intervals = new double[numBins * 2];

        SearchResponse sr = Queries.getElementsAggregations(project, QMType, elements, from, to);
        SearchHits hits = sr.getHits();
        int totalHits = (int) hits.getTotalHits();
        /**/
        //totalHits = 9;
        /**/
        Float[] sortedValues = new Float[totalHits];
        int index = 0;
        for (SearchHit hit : hits.getHits()) {
            float value = Float.parseFloat(Queries.getStringFromMap(hit.getSourceAsMap(), Constants.VALUE));
            sortedValues[index] = value;
            index++;
        }
        /**/
        //sortedValues = new Float[]{0F,4F,12F,16F,16F,18F,24F,26F,28F};
        /**/
        int groupsSize = sortedValues.length / numBins;
        intervals[0] = min;
        intervals[1] = mean(sortedValues[groupsSize-1], sortedValues[groupsSize]);
        for (int i = 2, indexBins = 2; i <= numBins+1; i += 2, indexBins++) {
            intervals[i] = intervals[i - 1];
            intervals[i + 1] = mean(sortedValues[(groupsSize * indexBins) - 1], sortedValues[groupsSize * indexBins]);
        }
        intervals[intervals.length - 2] = intervals[intervals.length - 3];
        intervals[intervals.length - 1] = max;
        return intervals;
    }

    private static double mean(double d1, double d2) {
        return (d1 + d2) / 2;
    }

    public static String QMtoID(Constants.QMLevel QMType) {
        switch (QMType) {
            case strategic_indicators:
                return Constants.STRATEGIC_INDICATOR_ID;
            case factors:
                return Constants.FACTOR_ID;
            default:
            case metrics:
                return Constants.METRIC_ID;
        }
    }
}

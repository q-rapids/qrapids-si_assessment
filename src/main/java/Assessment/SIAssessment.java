package Assessment;

import Util_Assessment_SI.Common;
import unbbayes.io.BaseIO;
import unbbayes.io.DneIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;

import java.io.File;
import java.util.Map;

public class SIAssessment {

    /**
     *
     * @param IDSI: ID of the Strategic Indicator to be assessed. Has to coincide with its corresponding node's name in
     *           the BN file to use (BNFile). Example: productquality
     * @param factorStates: Map containing the states of the SI's parent nodes. Being the keys the IDs of the parent
     *                    nodes and the values being the state itself. IDs and states need to be consistent with the
     *                    BN file in use (BNFile). Example: "CodeQuality" : "VeryLow"
     *                                                      "Stability" : "Medium"
     *                                                      "TestingStatus" : "High"
     * @param BNFile: File containing the BN to use for the assessment, in DNE format
     * @return Assessment of the SI: ArrayList of DTOSICategory, each one containing the ID of the SI category
     * (idSICategory) and its associated assessed probability (probSICategory).
     * @throws Exception
     */
    public static DTOSIAssessment AssessSI(String IDSI, Map<String, String> factorStates, File BNFile) throws Exception {
        BaseIO io = new DneIO();
        ProbabilisticNetwork net = (ProbabilisticNetwork) io.load(BNFile);
        Node SInode = net.getNode(IDSI);

        return Common.doInferenceUnbbayes(net, SInode, factorStates);
    }

}

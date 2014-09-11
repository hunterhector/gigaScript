package edu.cmu.cs.lti.gigascript.util;

import net.ricecode.similarity.DiceCoefficientStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 9/10/14
 * Time: 9:04 PM
 */
public class StringUtils {
    private static SimilarityStrategy strategy  = new DiceCoefficientStrategy();
    private static StringSimilarityService service = new StringSimilarityServiceImpl(strategy);

    public static double diceScore(String str1, String str2){
        return service.score(str1,str2);
    }

    public static double tokenDiceScore(List<String> strs1, List<String> strs2){
        double sim = 0;
        for (String str1 : strs1){
            for (String str2 : strs2){
                if (str1.equalsIgnoreCase(str2)){
                    sim ++;
                }
            }
        }
        return 2* sim / (strs1.size() + strs2.size());
    }

}

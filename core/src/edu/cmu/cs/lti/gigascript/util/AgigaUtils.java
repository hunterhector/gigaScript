package edu.cmu.cs.lti.gigascript.util;

import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 9/9/14
 * Time: 9:50 PM
 */
public class AgigaUtils {
    private final static Logger logger = Logger.getLogger(AgigaUtils.class.getName());

//    public static AgigaToken findHead(AgigaSentence sent, List<Integer> tokenIndices){
//        //use first token rule now
//        int headIndex = -1;
//        for (Integer index : tokenIndices){
//            if (headIndex > 0) {
//                if (index > headIndex) {
//                    headIndex = index;
//                }
//            }else{
//                headIndex = index;
//            }
//        }
//
//        if (headIndex == -1 ){
//            IllegalArgumentException e =  new IllegalArgumentException("Provided indices is empty or incorrect");
//            logger.log(Level.SEVERE,"",e);
//            throw e;
//        }else if ( headIndex > sent.getTokens().size() -1 ){
//            IllegalArgumentException e =  new IllegalArgumentException("Provided index is empty or larger than sentence length");
//            logger.log(Level.SEVERE,"",e);
//            throw e;
//        }else{
//            return sent.getTokens().get(headIndex);
//        }
//    }
}

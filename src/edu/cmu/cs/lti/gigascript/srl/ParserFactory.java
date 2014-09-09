package edu.cmu.cs.lti.gigascript.srl;

import tratz.parse.FullSystemWrapper;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 9/8/14
 * Time: 9:26 PM
 */
public class ParserFactory {
    static String modeBaseDir = "resources/fanse/";

    private static final String POS_MODEL = "posTaggingModel.gz", PARSE_MODEL = "parseModel.gz",
            POSSESSIVES_MODEL = "possessivesModel.gz", NOUN_COMPOUND_MODEL = "nnModel.gz",
            SRL_ARGS_MODELS = "srlArgsWrapper.gz", SRL_PREDICATE_MODELS = "srlPredWrapper.gz",
            PREPOSITION_MODELS = "psdModels.gz", WORDNET = "data/wordnet3";

    public static FullSystemWrapper getFanseParser() throws IOException, ClassNotFoundException {
        return  new FullSystemWrapper(modeBaseDir + PREPOSITION_MODELS, modeBaseDir
                + NOUN_COMPOUND_MODEL, modeBaseDir + POSSESSIVES_MODEL,
                modeBaseDir + SRL_ARGS_MODELS, modeBaseDir + SRL_PREDICATE_MODELS, modeBaseDir
                + POS_MODEL, modeBaseDir + PARSE_MODEL, modeBaseDir + WORDNET);
    }
}

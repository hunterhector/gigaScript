package edu.cmu.cs.lti.gigascript.processor;

import edu.cmu.cs.lti.gigascript.agiga.AgigaDocumentWrapper;
import edu.cmu.cs.lti.gigascript.agiga.AgigaSentenceWrapper;
import edu.cmu.cs.lti.gigascript.srl.ParserFactory;
import edu.jhu.agiga.AgigaToken;
import tratz.parse.FullSystemWrapper;
import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Sentence;
import tratz.parse.types.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 9/8/14
 * Time: 9:14 PM
 */
public class FanseBasedProcessor extends AbstractSentenceBasedGigwordProcessor {

    private FullSystemWrapper parser ;

    public FanseBasedProcessor() throws IOException, ClassNotFoundException {
        parser = ParserFactory.getFanseParser();
    }

    @Override
    protected void processSentence(AgigaSentenceWrapper sent, AgigaDocumentWrapper doc) {
        List<AgigaToken> wordList = sent.getTokens();

        Parse par = wordListToParse(wordList);

        Sentence fSent = par.getSentence();
        List<Token> tokens = fSent.getTokens();

        FullSystemWrapper.FullSystemResult result = parser.process(fSent, tokens.size() > 0
                && tokens.get(0).getPos() == null, true, true, true, true, true);

        Parse semanticParse = result.getSrlParse();

        tratz.parse.types.Sentence resultSent = semanticParse.getSentence();
        List<Token> resultTokens = resultSent.getTokens();

        for (Arc arc : semanticParse.getArcs()){
            if (arc == null || arc.getSemanticAnnotation() == null){
                continue;
            }

            AgigaToken childToken = getAgigaTokenFromFanse(arc.getChild(),wordList);
            AgigaToken headToken = getAgigaTokenFromFanse(arc.getChild(),wordList);
            String role = arc.getSemanticAnnotation();

            System.out.println(String.format("%s , %s , %s", childToken, headToken, role));
        }
    }

    private AgigaToken getAgigaTokenFromFanse(Token token, List<AgigaToken> wordList){
        int idx = token.getIndex() - 1;
        return idx < wordList.size() ? wordList.get(token.getIndex() - 1) : wordList.get(wordList.size()- 1);
    }

    private Parse wordListToParse(List<AgigaToken> words) {
        Token root = new Token("[ROOT]", 0);
        List<Token> tokens = new ArrayList<Token>();
        List<Arc> arcs = new ArrayList<Arc>();

        int tokenNum = 0;
        for (AgigaToken word : words) {
            tokenNum++;
            String wordString = word.getWord();
            Token token = new Token(wordString, tokenNum);
            tokens.add(token);
        }

        Parse result = new Parse(new tratz.parse.types.Sentence(tokens), root, arcs);

        return result;
    }
}

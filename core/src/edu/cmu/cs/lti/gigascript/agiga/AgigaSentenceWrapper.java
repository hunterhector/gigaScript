package edu.cmu.cs.lti.gigascript.agiga;

import edu.cmu.cs.lti.gigascript.model.GigaMention;
import edu.jhu.agiga.AgigaMention;
import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.AgigaToken;
import edu.jhu.agiga.AgigaTypedDependency;
import edu.stanford.nlp.trees.SemanticHeadFinder;
import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Hector, Zhengzhong Liu
 * Date: 2/28/14
 * Time: 5:46 PM
 */
public class AgigaSentenceWrapper {
    List<AgigaTypedDependency> basicDependencies;

    List<AgigaToken> tokens;
    AgigaSentence sent;
    SemanticHeadFinder headFinder = new SemanticHeadFinder();
    Tree parseTree;
    List<GigaMention> mentions;

    public AgigaSentenceWrapper(AgigaSentence sentence) {
        tokens = sentence.getTokens();
        sent = sentence;

        parseTree = sent.getStanfordContituencyTree();
        mentions = createMentions();
    }

    private List<GigaMention> createMentions() {
        List<GigaMention> mentions = new ArrayList<GigaMention>();

        List<AgigaToken> mentionTokens = new ArrayList<AgigaToken>();
        String previousTag = "O";
        for (AgigaToken token : tokens) {
            String tag = token.getNerTag();
//            System.out.println(previousTag + " " + tag);

            if (tag.equals("O") && !previousTag.equals("O")) { //E O
                GigaMention mention = new GigaMention(sent.getSentIdx(), getHeadWordIndexFromTokens(mentionTokens), mentionTokens, tag);
                mentions.add(mention);
//                System.out.println(mention.tokensToString());
                mentionTokens = new ArrayList<AgigaToken>();
            } else { // EE, OE, OO
                if (!tag.equals("O")) { //EE, OE
                    if (previousTag.equals("O")){ //OE
//                        System.out.println("Adding to tag(OE): "+tag+" " + token.getWord());
                    }else if (previousTag.equals(tag)){ //EE1
//                        System.out.println("Adding to tag(EE): "+tag+" " + token.getWord());
                    }else{//EE2
                        GigaMention mention = new GigaMention(sent.getSentIdx(), getHeadWordIndexFromTokens(mentionTokens), mentionTokens, tag);
                        mentions.add(mention);
//                        System.out.println(mention.tokensToString());
                        mentionTokens = new ArrayList<AgigaToken>();
                    }

                    mentionTokens.add(token);

                }
            }
            previousTag = tag;
        }
        if (!previousTag.equals("O")) {
            GigaMention mention = new GigaMention(sent.getSentIdx(), getHeadWordIndexFromTokens(mentionTokens), mentionTokens, previousTag);
            mentions.add(mention);
//            System.out.println(mention.tokensToString());
        }


        return mentions;
    }

    public List<GigaMention> getMentions() {
        return mentions;
    }

    public GigaMention convertMention(AgigaMention agigaMention) {
        if (agigaMention.getSentenceIdx() != sent.getSentIdx()) {
            throw new IllegalArgumentException("Mention not in this sentence");
        }

        for (GigaMention mention : mentions) {
            if (mention.getKeywordTokenIndex() == agigaMention.getHeadTokenIdx()) {
                return mention;
            }
        }

        List<AgigaToken> mentionTokens = getMentionTokens(agigaMention);

        return new GigaMention(sent.getSentIdx(), getHeadWordIndexFromTokens(mentionTokens), mentionTokens, null);
    }

    public List<AgigaToken> getTokens() {
        return tokens;
    }

    public int getHeadWordIndexFromTokens(List<AgigaToken> mentionTokens) {
        List<Integer> indices = new ArrayList<Integer>();
        for (AgigaToken token : mentionTokens) {
            indices.add(token.getTokIdx());
        }
        return getHeadWordIndex(indices);
    }

    public List<AgigaToken> getMentionTokens(AgigaMention mention) {
        List<AgigaToken> indices = new ArrayList<AgigaToken>();
        for (int i = mention.getStartTokenIdx(); i < mention.getEndTokenIdx(); i++) {
            indices.add(tokens.get(i));
        }
        return indices;
    }


    public int getHeadWordIndex(List<Integer> indices) {
        int firstIndex = indices.get(0);
//        AgigaToken firstToken = tokens.get(firstIndex);

        for (int i = 0; i < indices.size(); i++) {
            int index = indices.get(i);
            String pos = tokens.get(index).getPosTag();

            if (!(pos.equals("PP") || pos.equals("TO") || pos.equals("IN")) || pos.equals("DT")) {
                return index;
            }
        }

        return firstIndex;
    }

    public String getSentenceStr() {
        String str = "";
        for (AgigaToken token : tokens) {
            str += token.getWord();
            str += " ";
        }
        return str;
    }

    public String getSentenceLemmaStr() {
        String str = "";
        for (AgigaToken token : tokens) {
            str += token.getLemma();
            str += " ";
        }
        return str;
    }

    private String getSemanticType(AgigaToken token) {
        // trust the token's own Named Entity Tag
        if (!token.getNerTag().equals("O") && !token.getNerTag().equals("")) {
            return token.getNerTag().trim();
        }

        //null enforce people don't compare tokens without a semantic type
        return null;
    }

}

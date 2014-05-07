package edu.cmu.cs.lti.gigascript.demo;

import edu.cmu.cs.lti.gigascript.util.WordNetUtils;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 4/15/14
 * Time: 8:32 PM
 */
public class WordNetDemo {
    public static void main(String[] args) throws IOException {
        File targetSemanticTypes = new File("semantic_types");
        Set<String> semanticTypes = new HashSet<String>();
        for (String semanticType : FileUtils.readLines(targetSemanticTypes)){
            semanticTypes.add(semanticType.trim());
        }

        URL url = new URL("file", null, "/Users/zhengzhongliu/tools/wnDict/");

        //construct the Dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();

        IIndexWord idxWord = dict.getIndexWord("talk", POS.NOUN);

        for (IWordID wordId : idxWord.getWordIDs()) {
            IWord word = dict.getWord(wordId);
            System.out.println("For "+word);
            ISynset synset = word.getSynset();
            System.out.println(WordNetUtils.getMatchedSemanticTypeBfs(dict, synset, semanticTypes));
        }
    }
}
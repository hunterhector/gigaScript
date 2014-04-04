package edu.cmu.cs.lti.gigascript.index;

import edu.cmu.cs.lti.gigascript.agiga.AgigaSentenceWrapper;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import edu.jhu.agiga.AgigaDocument;
import edu.jhu.agiga.AgigaSentence;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/22/14
 * Time: 4:57 PM
 */
public class GigaWordSentenceIndexer extends GigaWordIndexer {
    public GigaWordSentenceIndexer(Configuration config) {
        super(config);
    }

    public static void main(String[] argv) throws IOException, SolrServerException {
        String propPath = "settings.properties";
        if (argv.length < 1) {
            System.err.println("Missing property file argument! Will try default property.");
        } else {
            propPath = argv[0];
        }

        Configuration config = new Configuration(new File(propPath));

        GigaWordSentenceIndexer indexer =new GigaWordSentenceIndexer(config);

        indexer.index();
    }

    @Override
    public List<Map<String, String>> getIndexContent(AgigaDocument doc) {
        List<Map<String,String>> allFields2Index = new ArrayList<Map<String, String>>();
        int sentIdx = 0;
        for (AgigaSentence sent : doc.getSents()) {
            Map<String,String> fields = new HashMap<String, String>();
            AgigaSentenceWrapper sentenceWrapper = new AgigaSentenceWrapper(sent);
            String sentStr = sentenceWrapper.getSentenceLemmaStr();

            fields.put("id",doc.getDocId() + "_" + sentIdx);
            fields.put("content", sentStr);
            fields.put("title",doc.getDocId());

            allFields2Index.add(fields);
            sentIdx += 1;
        }
        return allFields2Index;
    }
}

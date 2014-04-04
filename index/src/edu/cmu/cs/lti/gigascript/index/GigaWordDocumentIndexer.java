package edu.cmu.cs.lti.gigascript.index;

import edu.cmu.cs.lti.gigascript.agiga.AgigaDocumentWrapper;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import edu.jhu.agiga.AgigaDocument;
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
 * Date: 4/3/14
 * Time: 5:55 PM
 */
public class GigaWordDocumentIndexer extends GigaWordIndexer {
    public GigaWordDocumentIndexer(Configuration config) {
        super(config);
    }

    @Override
    public List<Map<String, String>> getIndexContent(AgigaDocument doc) {
        List<Map<String,String>> allFields2Index = new ArrayList<Map<String, String>>();

        String id =   doc.getDocId();
        String text =new AgigaDocumentWrapper(doc).getText();

        Map<String,String> field = new HashMap<String, String>();
        field.put("id",id);
        field.put("content",text);

        allFields2Index.add(field);

        return allFields2Index;
    }

    public static void main (String[] argv) throws IOException, SolrServerException {
        String propPath = "settings.properties";
        if (argv.length < 1) {
            System.err.println("Missing property file argument! Will try default property.");
        } else {
            propPath = argv[0];
        }

        Configuration config = new Configuration(new File(propPath));

        GigaWordIndexer indexer =new GigaWordDocumentIndexer(config);

        indexer.index();
    }
}

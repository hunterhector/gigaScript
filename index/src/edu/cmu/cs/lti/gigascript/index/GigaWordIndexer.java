package edu.cmu.cs.lti.gigascript.index;

import com.google.common.io.Files;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import edu.jhu.agiga.AgigaDocument;
import edu.jhu.agiga.AgigaPrefs;
import edu.jhu.agiga.StreamingDocumentReader;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 4/3/14
 * Time: 5:30 PM
 */
public abstract class GigaWordIndexer {

    Configuration config;

    public GigaWordIndexer(Configuration config)  {
        this.config = config;
    }

    public void index() throws IOException, SolrServerException {
        long startTime = System.currentTimeMillis();

        String host = config.get("edu.cmu.cs.lti.gigaScript.solr.host");

        boolean consoleMode = config.get("edu.cmu.cs.lti.gigaScript.console.mode").equals("console");

        int docNum2Flush = config.getInt("edu.cmu.cs.lti.gigaScript.flush.size");

        System.out.println("Connecting to " + host);

        SolrServer server = new HttpSolrServer(host);

        AgigaPrefs prefs = new AgigaPrefs();
        prefs.setAll(true);

        if (config.getBoolean("edu.cmu.cs.lti.gigaScript.index.reset")) {
            System.out.println("Cleaning the index!");
            server.deleteByQuery("*:*");// CAUTION: deletes everything!
        }

        System.out.println("Parsing XML...");

        System.out.println("Number of documents processed:");

        String corpusPath = config.get("edu.cmu.cs.lti.gigaScript.agiga.dir");

        File folder = new File(corpusPath);

        File[] listOfFiles = folder.listFiles();
        for (File currentFile : listOfFiles) {
            long batchStartTime = System.currentTimeMillis();

            String fileName = currentFile.getName();
            String extension = Files.getFileExtension(fileName);

            //make sure we not supply random stuff to it, like .DS_Store
            if (!extension.equals("gz")) {
                continue;
            }

            System.out.println("Processing " + fileName);

            StreamingDocumentReader reader = new StreamingDocumentReader(currentFile.getAbsolutePath(), prefs);

            Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

            for (AgigaDocument doc : reader) {

                for (Map<String,String> fields : getIndexContent(doc)){
                    SolrInputDocument solrDoc = new SolrInputDocument();
                    for (Map.Entry<String,String> field : fields.entrySet()){
                        solrDoc.addField(field.getKey(),field.getValue());
                    }
                    docs.add(solrDoc);
                }

                if (consoleMode) {
                    //nice progress view when we can view it in the console
                    System.out.print("\r" + reader.getNumDocs());
                } else {
                    //this will be more readable if we would like to direct the console output to file
                    if(reader.getNumDocs() % docNum2Flush == 0) {
                        System.out.print(reader.getNumDocs() + " ");
                    }
                }

                if (reader.getNumDocs() % docNum2Flush == 0) {
                    server.add(docs);
                    docs = new ArrayList<SolrInputDocument>();
                }
            }

            System.out.println();
            System.out.println("Batch processing time " + (System.currentTimeMillis() - batchStartTime) / 6e4 + " minutes");
        }

        System.out.println();
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("Overall processing time takes " + totalTime / 6e4 + " minutes");
    }

    public abstract List<Map<String,String>> getIndexContent(AgigaDocument doc);

}

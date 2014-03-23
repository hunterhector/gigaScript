package edu.cmu.cs.lti.gigascript.index;

import com.google.common.io.Files;
import edu.cmu.cs.lti.gigascript.agiga.AgigaSentenceWrapper;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import edu.jhu.agiga.AgigaDocument;
import edu.jhu.agiga.AgigaPrefs;
import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.StreamingDocumentReader;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/22/14
 * Time: 4:57 PM
 */
public class GigaWordIndexer {
    public static void main(String[] argv) throws IOException, SolrServerException {
        String propPath = "settings.properties";
        if (argv.length < 1) {
            System.err.println("Missing property file argument! Will try default property.");
        } else {
            propPath = argv[0];
        }

        long startTime = System.currentTimeMillis();

        Configuration config = new Configuration(new File(propPath));

        String host = config.get("edu.cmu.cs.lti.gigaScript.solor.host");

        boolean consoleMode = config.get("edu.cmu.cs.lti.gigaScript.console.mode").equals("console");

        int docNum2Flush = config.getInt("edu.cmu.cs.lti.gigaScript.flush.size");

        System.out.println("Connecting to " + host);

        SolrServer server = new HttpSolrServer(host);

        AgigaPrefs prefs = new AgigaPrefs();
        prefs.setAll(true);

        server.deleteByQuery("*:*");// CAUTION: deletes everything!

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
                int sentIdx = 0;
                for (AgigaSentence sent : doc.getSents()) {
                    SolrInputDocument solrDoc = new SolrInputDocument();
                    solrDoc.addField("id", doc.getDocId() + "_" + sentIdx);

                    AgigaSentenceWrapper sentenceWrapper = new AgigaSentenceWrapper(sent);

                    String sentStr = sentenceWrapper.getSentenceLemmaStr();
//                    System.out.println(doc.getDocId()+"_"+sentIdx);
//                    System.out.println(sentStr);

                    solrDoc.addField("content", sentStr);
                    solrDoc.addField("title",doc.getDocId());
                    docs.add(solrDoc);
                    sentIdx += 1;
                }
                if (consoleMode) {
                    //nice progress view when we can view it in the console
                    System.out.print("\r" + reader.getNumDocs());
                } else {
                    //this will be more readable if we would like to direct the console output to file
                    if(reader.getNumDocs() % 500 == 0) {
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
}

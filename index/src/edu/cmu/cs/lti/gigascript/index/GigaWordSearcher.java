package edu.cmu.cs.lti.gigascript.index;

import edu.cmu.cs.lti.gigascript.util.Configuration;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/22/14
 * Time: 7:06 PM
 */
public class GigaWordSearcher {
    public static void main(String[] argv) throws IOException, SolrServerException {
        String propPath = "settings.properties";
        if (argv.length < 1) {
            System.err.println("Missing property file argument! Will try default property.");
        } else {
            propPath = argv[0];
        }

        Configuration config = new Configuration(new File(propPath));
        SolrServer server = new HttpSolrServer(config.get("edu.cmu.cs.lti.gigaScript.solr.host"));

        SolrQuery query = new SolrQuery();
        query.setQuery("bombing, bomb, explode");
        query.setFields("id","content","title");
        query.setStart(0);
        query.setRows(10000);

        QueryResponse response = server.query(query);

//        System.out.println(response);

        SolrDocumentList results = response.getResults();
        for (int i = 0; i < results.size(); i++){
            SolrDocument result = results.get(i);
//            System.out.println(result.toString());
            System.out.println(result.getFieldValue("id"));
        }
    }
}

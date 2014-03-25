package edu.cmu.cs.lti.gigascript.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.cmu.cs.lti.gigascript.model.GoogleResults;
import edu.cmu.cs.lti.gigascript.model.GoogleResults.Result;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/19/14
 * Time: 12:34 PM
 */
public class GoogleSearcher {
    static final String address = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";

    public static List<String> search(String query) throws IOException {
        URL url = new URL(address + URLEncoder.encode(query,"UTF-8"));

        Reader reader = new InputStreamReader(url.openStream());

        GoogleResults results = new Gson().fromJson(reader, GoogleResults.class);

        int total = results.getResponseData().getResults().size();

        List<String> snippets = new ArrayList<String>();

        List<Result> responedData = results.getResponseData().getResults();
        // Show title and URL of each results
        for (int i = 0; i < total; i++) {
            snippets.add(responedData.get(i).getContent().replaceAll("\\p{Punct}","").replaceAll("\n",""));
        }

        return snippets;
    }

    public static List<String> expandBackward(String event1,String event2) throws IOException {
        String queryBackward = String.format("%s such as %s and ",event2,event1);

        List<String> backwardResults = search("\""+queryBackward+"\"");

        List<String> bckExt = new ArrayList<String>();

        if (backwardResults.size() == 0){
            return bckExt;
        }else{
            for (String result : backwardResults){
                String ext = getExtension(result,queryBackward);
                if (ext!=null){
                    bckExt.add(ext);
                }
            }
            return bckExt;
        }
    }


    public static List<String> expandForward(String event1,String event2) throws IOException {
        String queryForward = String.format("%s such as %s and ",event1,event2);

        List<String> forwardResults = search("\""+queryForward+"\"");

        List<String> forwardExt = new ArrayList<String>();

        if (forwardResults.size() == 0){
            return forwardExt;
        }else{
            for (String result : forwardResults){
                String ext = getExtension(result,queryForward);
                if (ext!=null){
                    forwardExt.add(ext);
                }
            }
            return forwardExt;
        }
    }

    private static String getExtension(String snippet, String pattern){

        String[] parts = snippet.split(pattern);

        if (parts.length >= 2){
            return parts[1].split(" ")[0].replaceAll("\\p{Punct}","");
        }else{
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        for (String result : expandBackward("attacks","bombings")){
            System.out.println(result);
        }
        for (String result : expandForward("attacks","bombings")){
            System.out.println(result);
        }
    }
}

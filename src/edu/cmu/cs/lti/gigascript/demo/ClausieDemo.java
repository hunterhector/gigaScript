package edu.cmu.cs.lti.gigascript.demo;

import de.mpii.clausie.ClausIE;
import de.mpii.clausie.Proposition;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 4/30/14
 * Time: 2:03 PM
 */
public class ClausieDemo {
    public static void main(String[] argv) throws IOException {
        ClausIE cie = new ClausIE();
        cie.parse("\"In this border town , once a gracious spa favored by Rwanda 's rich and powerful ," +
                " hundreds of weary and dirty families were camped on Saturday , waiting" +
                " until they can cross the border , hoping to flee the country before the arrival of rebel" +
                " troops , who are only 10 miles away .\n");

        cie.detectClauses();
        cie.generatePropositions();

        for (Proposition p : cie.getPropositions()) {
            System.out.println(p);
        }
    }
}

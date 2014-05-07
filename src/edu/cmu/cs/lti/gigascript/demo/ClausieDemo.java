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
        cie.parse(" U.S. , Canada and the Pacific : CONNIE WHITE in Kansas City " +
                "at 1-800-444-0267 or 816-822-8448 , or fax her at 816-822-1444 .");

        cie.detectClauses();
        cie.generatePropositions();

        for (Proposition p : cie.getPropositions()) {
            System.out.println(p);
        }
    }
}

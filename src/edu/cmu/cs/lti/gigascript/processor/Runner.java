package edu.cmu.cs.lti.gigascript.processor;

import javax.naming.ConfigurationException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 9/8/14
 * Time: 9:51 PM
 */
public class Runner {
    public static void main(String args[]) throws IOException, ClassNotFoundException, ConfigurationException {
        FanseBasedProcessor processer = new FanseBasedProcessor();
        processer.process(args);
    }
}

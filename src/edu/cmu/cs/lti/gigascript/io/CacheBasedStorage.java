package edu.cmu.cs.lti.gigascript.io;

/**
 * Created by zhengzhongliu on 3/2/14.
 */
public abstract class CacheBasedStorage extends GigaStorage {

    /**
     * Method for clean up and flush to disk
     */
    public abstract void flush();
}

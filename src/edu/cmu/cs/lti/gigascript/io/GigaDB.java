package edu.cmu.cs.lti.gigascript.io;


import edu.cmu.cs.lti.gigascript.model.AgigaArgument;
import edu.cmu.cs.lti.gigascript.util.Configuration;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 2/25/14
 * Time: 1:16 AM
 */
public class GigaDB extends CacheBasedStorage{
    public static Logger logger = Logger.getLogger(GigaDB.class.getName());

    private Connection conn = null;
    private String dbPath;

    public GigaDB(Configuration config) {
        super(config);
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Did not find appropriate JDBC class");
        }
        dbPath = config.get("edu.cmu.cs.lti.gigaScript.db.path");
        connectOrCreate();
    }

    private void connectOrCreate() {
        try {
            final File f = new File(dbPath);
            boolean exists = true;
            if (f.exists()) {
                if (f.isDirectory()) {
                    logger.log(Level.WARNING, "Provided DB path is a directory");
                } else {
                    logger.log(Level.INFO, "Connecting with an existing DB");
                }
            } else {
                exists = false;
                logger.log(Level.WARNING, "Creating new DB at provided path");
            }

            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            conn = turnonWalMode(conn);
            logger.log(Level.INFO, "Opened database successfully (with WAL model)");

            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean existTable(String tableName) throws SQLException {
        Statement stmt = conn.createStatement();
        String sql = String.format("SELECT name FROM sqlite_master WHERE type='table' AND name='%s';", tableName);
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            return rs.getString("name").equals(tableName);
        }

        return false;
    }

    private void createTables() throws SQLException {
        if (!existTable(outputTupleStoreName)) {
            logger.log(Level.INFO, String.format("Table [%s] does not exists, creating a new one", outputTupleStoreName));
            createTupleTable();
        }
        if (!existTable(outputCooccStoreName)) {
            logger.log(Level.INFO, String.format("Table [%s] does not exists, creating a new one", outputCooccStoreName));
            createBigramTable();
        }
    }

    private void createTupleTable() {
        Statement stmt = null;

        try {
            stmt = conn.createStatement();
            String sql = String.format("CREATE TABLE %s " +
                    "(Id INT PRIMARY KEY     NOT NULL," +
                    " Arg0           CHAR(50)    NOT NULL, " +
                    " Arg1          CHAR(50)     NOT NULL, " +
                    " Rel        CHAR(50)   NOT NULL, " +
                    " Count         INT" +
                    ")", outputTupleStoreName);
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        logger.log(Level.INFO, "AgigaArgument Table created successfully");
    }

    private void createBigramTable() {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            String sql = String.format("CREATE TABLE %s " +
                    "(Tuple1    INT NOT NULL," +
                    "Tuple2 INT NOT NULL," +
                    "CountOfTupleDistance1 INT DEFAULT 0 ," +
                    "CountOfTupleDistance2 INT DEFAULT 0 ," +
                    "CountOfTupleDistance3 INT DEFAULT 0 ," +
                    "CountOfTupleDistance4 INT DEFAULT 0 ," +
                    "CountOfTupleDistance5 INT DEFAULT 0 ," +
                    "CountOfTupleDistance6 INT DEFAULT 0 ," +
                    "CountOfTupleDistance7 INT DEFAULT 0 ," +
                    "CountOfTupleDistance8 INT DEFAULT 0 ," +
                    "CountOfTupleDistance9 INT DEFAULT 0 ," +
                    "CountOfTupleDistance10 INT DEFAULT 0 ," +
                    "CountOfTupleDistance15 INT DEFAULT 0 ," +
                    "CountOfTupleDistance20 INT DEFAULT 0 ," +
                    "CountOfTupleDistance30 INT DEFAULT 0 ," +
                    "CountOfTupleDistance40 INT DEFAULT 0 ," +
                    "CountOfBidirectionTupleDistance1 INT DEFAULT 0 ," +
                    "CountOfBidirectionTupleDistance2 INT DEFAULT 0 ," +
                    "CountOfBidirectionTupleDistance3 INT DEFAULT 0 ," +
                    "CountOfBidirectionTupleDistance4 INT DEFAULT 0 ," +
                    "CountOfBidirectionTupleDistance5 INT DEFAULT 0 ," +
                    "CountOfBidirectionTupleDistance6 INT DEFAULT 0 ," +
                    "CountOfBidirectionTupleDistance7 INT DEFAULT 0 ," +
                    "CountOfBidirectionTupleDistance8 INT DEFAULT 0 ," +
                    "CountOfBidirectionTupleDistance9 INT DEFAULT 0 ," +
                    "CountOfBidirectionTupleDistance10 INT DEFAULT 0 ," +
                    "CountOfBidirectionTupleDistance15 INT DEFAULT 0 ," +
                    "CountOfBidirectionTupleDistance20 INT DEFAULT 0 ," +
                    "CountOfBidirectionTupleDistance30 INT DEFAULT 0 ," +
                    "CountOfBidirectionTupleDistance40 INT DEFAULT 0 ," +
                    "CountOfSameDocument INT DEFAULT 0 ," + //this key will always be increment
                    "E11 INT NOT NULL," +
                    "E12 INT NOT NULL," +
                    "E21 INT NOT NULL," +
                    "E22 INT NOT NULL," +
                    "FOREIGN KEY(Tuple1) REFERENCES Tuples(Id)," +
                    "FOREIGN KEY(Tuple2) REFERENCES Tuples(Id)," +
                    "PRIMARY KEY (Tuple1, Tuple2))", outputCooccStoreName);
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        logger.log(Level.INFO, "Bigram Table created successfully");
    }

    public long addGigaTuple(AgigaArgument arg0, AgigaArgument arg1, String relation) {
        String arg0Hw = arg0.getHeadWordLemma().replace("'", "''");
        String arg1Hw = arg1.getHeadWordLemma().replace("'", "''");

        String insertSql = String.format("INSERT OR IGNORE INTO %s (Arg0,Arg1,Rel,Count) " +
                "  VALUES (  '%s', '%s' , '%s', 1 );",
                outputTupleStoreName, arg0Hw, arg1Hw, relation);

        String updateSql = String.format("UPDATE %s SET Count = Count + 1 WHERE " +
                "Arg0 = '%s' AND Arg1 = '%s' AND Rel = '%s';", outputTupleStoreName,arg0Hw,arg1Hw,relation);

        logger.log(Level.INFO, insertSql);

        logger.log(Level.INFO, updateSql);


        try {
            long insertReply = executeSqlUpdate(insertSql);
            long updateReply = executeSqlUpdate(updateSql);

            logger.log(Level.INFO, "Insert and update " + insertReply + " " + updateReply);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }


        return -1; //indicate failure
    }

    private long executeSqlUpdate(String sql) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        try {
            int rChanged = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            logger.log(Level.INFO, "Update by the sql " + rChanged);
            return rs.getLong(1);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            ps.close();
        }
        return -1;
    }

    public void addGigaBigram(long t1, long t2, int sentDistance, int tupleDistance, int[][] equality) {
        //1. search for the record
        try {
            Statement stmt = conn.createStatement();
            ResultSet directonalRs = stmt.executeQuery(String.format("SELECT * FROM %s WHERE Tuple1=%s AND Tuple1=%s;", outputCooccStoreName, t1, t2));

            Map<String, Integer> directionalCounts = getBigramCountsToIncrement(sentDistance, false);

            while (directonalRs.next()) {
                for (String countName : directionalCounts.keySet()) {
                    directionalCounts.put(countName, directonalRs.getInt(countName));
                }
            }

            directonalRs.close();
            stmt.close();

            if (!directionalCounts.isEmpty()) {
                StringBuffer sqlBuffer = new StringBuffer();

                int counter = 0;

                for (Entry<String, Integer> countEntry : directionalCounts.entrySet()) {
                    counter++;
                    sqlBuffer.append(countEntry.getKey() + " = ");

                    int incrementalValue = 1;
                    if (countEntry.getKey().equals("E11")) {
                        incrementalValue = equality[0][0];
                    } else if (countEntry.getKey().equals("E12")) {
                        incrementalValue = equality[0][1];
                    } else if (countEntry.getKey().equals("E21")) {
                        incrementalValue = equality[1][0];
                    } else if (countEntry.getKey().equals("E22")) {
                        incrementalValue = equality[1][1];
                    }

                    sqlBuffer.append(countEntry.getValue() + incrementalValue);

                    if (counter < directionalCounts.size()) {
                        sqlBuffer.append(",");
                    }
                }

                String sqlIncrement = String.format("UPDATE %s SET %s WHERE Tuple1=%s AND Tuple1=%s;", outputCooccStoreName, sqlBuffer.toString(), t1, t2);
            } else {
                String columnNames = "";
                String values = "";

                int counter = 0;

                for (Entry<String, Integer> countEntry : directionalCounts.entrySet()) {
                    counter++;
                    columnNames += countEntry.getKey();
                    values += "1";
                    if (counter < directionalCounts.size()) {
                        columnNames += ", ";
                        values += ", ";
                    }
                }
                String sqlNewEntry = String.format("INSERT INTO %s (%s) VALUES (%s);", outputCooccStoreName, columnNames, values);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private Map<String, Integer> getBigramCountsToIncrement(int distance, boolean reverse) {
        String base = reverse ? "CountOfBidirectionTupleDistance" : "CountOfTupleDistance";

        Map<String, Integer> counts2Update = new HashMap<String, Integer>();
        counts2Update.put("CountOfSameDocument", 0);

        if (distance <= 10) {
            counts2Update.put(base + distance, 0);
        } else if (distance <= 15) {
            counts2Update.put(base + "15", 0);
        } else if (distance <= 20) {
            counts2Update.put(base + "20", 0);
        } else if (distance <= 30) {
            counts2Update.put(base + "30", 0);
        } else if (distance <= 40) {
            counts2Update.put(base + "40", 0);
        }

        counts2Update.put("E11", 0);
        counts2Update.put("E12", 0);
        counts2Update.put("E21", 0);
        counts2Update.put("E22", 0);

        return counts2Update;
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private Connection turnonWalMode(Connection c) throws SQLException {
        final Statement statement = c.createStatement();
        statement.execute("PRAGMA journal_mode = WAL;");
        statement.close();
        return c;
    }


    @Override
    public void flush() {

    }
}

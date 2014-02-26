package edu.cmu.cs.lti.gigascript.db;


import edu.cmu.cs.lti.gigascript.util.Configuration;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhengzhongliu on 2/25/14.
 */
public class GigaDB {
    public static Logger logger = Logger.getLogger(GigaDB.class.getName());

    private Connection conn = null;
    private String dbPath;
    private String tupleTableName;
    private String bigramTableName;

    public GigaDB(Configuration config) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Did not find appropriate JDBC class");
        }
        dbPath = config.get("edu.cmu.cs.lti.gigaScript.db.path");
        tupleTableName = config.get("edu.cmu.cs.lti.gigaScript.tupleTableName");
        bigramTableName = config.get("edu.cmu.cs.lti.gigaScript.bigramTableName");
    }

    public void connectOrCreate() {
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
            logger.log(Level.INFO, "Opened database successfully (with WAL mode)");

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
        if (!existTable(tupleTableName)) {
            logger.log(Level.INFO, String.format("Table [%s] does not exists, creating a new one", tupleTableName));
            createTupleTable();
        }
        if (!existTable(bigramTableName)) {
            logger.log(Level.INFO, String.format("Table [%s] does not exists, creating a new one", bigramTableName));
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
                    ")", tupleTableName);
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        logger.log(Level.INFO, "Tuple Table created successfully");
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
                    "PRIMARY KEY (Tuple1, Tuple2))", bigramTableName);
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        logger.log(Level.INFO, "Bigram Table created successfully");
    }

    public long addGigaTuple(String arg0, String arg1, String relation) {
        arg0 = arg0.replace("'","''");
        arg1 = arg1.replace("'","''");

        String insertSql = String.format("INSERT OR REPLACE INTO %s (Arg0,Arg1,Rel,Count) \n" +
                "  VALUES (  '%s', '%s' , '%s'\n" +
                "  COALESCE((SELECT Count + 1 FROM %s WHERE Arg0 = %s AND Arg1 = %s), 1)\n" +
                "  );", tupleTableName, arg0, arg1, relation, tupleTableName, arg0, arg1);

        System.out.println(insertSql);

        try {
            PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            try {
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                return rs.getLong(1);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                ps.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; //indicate failure
    }

    public void addGigaBigram(long t1, long t2, int distance, boolean[][] equality) {
        //1. search for the record
        try {
            Statement stmt = conn.createStatement();
            ResultSet directonalRs = stmt.executeQuery(String.format("SELECT * FROM %s WHERE Tuple1=%s AND Tuple1=%s;", bigramTableName, t1, t2));

            Map<String, Integer> directionalCounts = getBigramCountsToIncrement(distance, false);

            while (directonalRs.next()) {
                for (String countName : directionalCounts.keySet()) {
                    directionalCounts.put(countName, directonalRs.getInt(countName));
                }
            }
            if (!directionalCounts.isEmpty()) {
                StringBuffer sqlBuffer = new StringBuffer();

                int counter = 0;

                for (Entry<String, Integer> countEntry : directionalCounts.entrySet()) {
                    counter++;
                    sqlBuffer.append(countEntry.getKey() + " = ");

                    int incrementalValue = 1;
                    if (countEntry.getKey().equals("E11")) {
                        incrementalValue = equality[0][0] ? 1 : 0;
                    } else if (countEntry.getKey().equals("E12")) {
                        incrementalValue = equality[0][1] ? 1 : 0;
                    } else if (countEntry.getKey().equals("E21")) {
                        incrementalValue = equality[1][0] ? 1 : 0;
                    } else if (countEntry.getKey().equals("E22")) {
                        incrementalValue = equality[1][1] ? 1 : 0;
                    }

                    sqlBuffer.append(countEntry.getValue() + incrementalValue);

                    if (counter < directionalCounts.size()) {
                        sqlBuffer.append(",");
                    }
                }

                String sqlIncrement = String.format("UPDATE %s SET %s WHERE Tuple1=%s AND Tuple1=%s;", bigramTableName, sqlBuffer.toString(),t1, t2);
                System.out.println(sqlIncrement);
                stmt.executeUpdate(sqlIncrement);
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
                        values+=", ";
                    }
                }
                String sqlNewEntry = String.format("INSERT INTO %s (%s) VALUES (%s);", bigramTableName, columnNames, values);
                stmt.executeUpdate(sqlNewEntry);
            }

            directonalRs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    private Connection turnonWalMode(Connection c) throws SQLException {
        final Statement statement = c.createStatement();
        statement.execute("PRAGMA journal_mode = WAL;");
        statement.close();
        return c;
    }


}

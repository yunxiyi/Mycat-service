package demo.test;

import java.sql.*;

/**
 * @author mycat
 */
public class TestClass1 {

    public static void main(String args[]) throws SQLException, ClassNotFoundException {
        String jdbcdriver = "com.mysql.jdbc.Driver";
        String jdbcurl = "jdbc:mysql://127.0.0.1:9066/TESTDB?useUnicode=true&characterEncoding=utf-8";
        String username = "root";
        String password = "123456";
        System.out.println("开始连接mysql:" + jdbcurl);
        Class.forName(jdbcdriver);
        Connection c = DriverManager.getConnection(jdbcurl, username, password);
        Statement st = c.createStatement();
        print("test jdbc ", st.execute("show @@help"));
        System.out.println("OK......");
        st.close();
        c.close();
    }

    static void print(String name, boolean res) throws SQLException{
        System.out.println(name + ":" + res);
    }

    static void print(String name, ResultSet res) throws SQLException {
        System.out.println(name);
        ResultSetMetaData meta = res.getMetaData();
        //System.out.println( "\t"+res.getRow()+"条记录");
        String str = "";
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            str += meta.getColumnName(i) + "   ";
            //System.out.println( meta.getColumnName(i)+"   ");
        }
        System.out.println("\t" + str);
        str = "";
        while (res.next()) {
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                str += res.getString(i) + "   ";
            }
            System.out.println("\t" + str);
            str = "";
        }
    }
}

package test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbCheck {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try(Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/greenmarket?serverTimezone=UTC&sslMode=DISABLED&allowPublicKeyRetrieval=true", "root", "T12345678");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, name, LENGTH(image) FROM shop")) {
            while(rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") + ", Image Length: " + rs.getInt(3));
            }
        }
    }
}

import java.sql.*;

/** JDBC테스트 */
public class JdbcTest {
    public static void main(String[] args){
        String url = "jdbc:mysql://15.165.144.216:3306/pt_manager";
        String userName = "lrb9105";
        String password = "!vkdnj91556";

        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url, userName, password);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM TEST");
            resultSet.next();
            
            String id = resultSet.getString("TEST_ID");
            String value = resultSet.getString("VALUE");
            System.out.println(id);
            System.out.println(value);

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}

import java.sql.*;

public class DBRequestHandler {
    private static Connection connection;
    private static Statement statement;

    public static void getConnectionWithDB(){
        try {
            connection  = DriverManager.getConnection("jdbc:sqlite:users.db");
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnectDB(){

        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkIfUserExistsForAuthorization(String login){
        String dbQuery = "SELECT login FROM users";
        try {
            ResultSet resultSet = statement.executeQuery(dbQuery);
            while (resultSet.next()){
                if (resultSet.getString("login").equals(login)){
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;

    }
    public static boolean checkIfPasswordIsRight(String login, String password){
        String dbQuery = "SELECT password FROM users WHERE login='"+login+"'";
        try {
             ResultSet resultSet = statement.executeQuery(dbQuery);
             String passwordFromDB = resultSet.getString("password");
             if (passwordFromDB.equals(password)){
                 return true;
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean registerNewUser(String login, String password){
        String dbQuery = "INSERT INTO users(login,password) VALUES ('"+login+"','"+password+"')";
        try {
            int rows = statement.executeUpdate(dbQuery);
            if (rows > 0) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}

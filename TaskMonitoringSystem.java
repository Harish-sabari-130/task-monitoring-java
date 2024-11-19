import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class TaskMonitoringSystem {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/taskmonitoring";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "";  

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            createTables(connection); // Ensure required tables are created

            while (running) {
                System.out.println("\nTask Monitoring System");
                System.out.println("1. Add Task");
                System.out.println("2. View Tasks");
                System.out.println("3. Update Task Status");
                System.out.println("4. Exit");
                System.out.print("Choose an option: ");

                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> addTask(scanner, connection);
                    case 2 -> viewTasks(connection);
                    case 3 -> updateTaskStatus(scanner, connection);
                    case 4 -> {
                        System.out.println("Exiting...");
                        running = false;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }

        scanner.close();
    }

    // Method to create required tables
    private static void createTables(Connection connection) throws SQLException {
        String createTaskTableSQL = """
                CREATE TABLE IF NOT EXISTS tasks (
                    task_id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100),
                    description TEXT,
                    due_date DATE,
                    status VARCHAR(50) DEFAULT 'Pending'
                );
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(createTaskTableSQL);
        }
    }

    // Add a new task
    private static void addTask(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter task name: ");
            String name = scanner.nextLine();

            System.out.print("Enter task description: ");
            String description = scanner.nextLine();

            System.out.print("Enter due date (YYYY-MM-DD): ");
            LocalDate dueDate = LocalDate.parse(scanner.nextLine());

            String insertSQL = "INSERT INTO tasks (name, description, due_date) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, description);
                preparedStatement.setDate(3, Date.valueOf(dueDate));
                preparedStatement.executeUpdate();

                System.out.println("Task added successfully!");
            }
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error adding task: " + e.getMessage());
        }
    }

    // View all tasks
    private static void viewTasks(Connection connection) {
        String querySQL = "SELECT * FROM tasks";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(querySQL)) {

            System.out.println("\n--- Task List ---");
            boolean hasTasks = false;

            while (resultSet.next()) {
                hasTasks = true;
                int taskId = resultSet.getInt("task_id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
                String status = resultSet.getString("status");

                System.out.printf("ID: %d | Name: %s | Due Date: %s | Status: %s%nDescription: %s%n%n",
                        taskId, name, dueDate, status, description);
            }

            if (!hasTasks) {
                System.out.println("No tasks found.");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving tasks: " + e.getMessage());
        }
    }

    // Update task status
    private static void updateTaskStatus(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter task ID to update: ");
            int taskId = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter new status (Pending/In Progress/Completed): ");
            String status = scanner.nextLine();

            String updateSQL = "UPDATE tasks SET status = ? WHERE task_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
                preparedStatement.setString(1, status);
                preparedStatement.setInt(2, taskId);

                int rowsUpdated = preparedStatement.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Task status updated successfully!");
                } else {
                    System.out.println("Task ID not found. Please check and try again.");
                }
            }
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error updating task status: " + e.getMessage());
        }
    }
}
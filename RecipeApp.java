import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class RecipeApp {
    private static final String DB_URL = "jdbc:mysql://localhost:3307/recipe";
    private static final String USER = "root"; // your DB username
    private static final String PASS = "root"; // your DB password

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public RecipeApp() {
        frame = new JFrame("Recipe Application");
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createSignupPanel(), "Signup");
        mainPanel.add(createMenuPanel(), "Menu");
        mainPanel.add(createSearchRecipePanel(), "SearchRecipe");
        mainPanel.add(createEnterRecipePanel(), "EnterRecipe");

        frame.add(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton signupButton = new JButton("Sign Up");

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(signupButton);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (login(username, password)) {
                cardLayout.show(mainPanel, "Menu");
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid credentials");
            }
        });

        signupButton.addActionListener(e -> cardLayout.show(mainPanel, "Signup"));

        return panel;
    }

    private JPanel createSignupPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton signupButton = new JButton("Sign Up");

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(signupButton);

        signupButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (signup(username, password)) {
                JOptionPane.showMessageDialog(frame, "Signup successful");
                cardLayout.show(mainPanel, "Login");
            } else {
                JOptionPane.showMessageDialog(frame, "Signup failed");
            }
        });

        return panel;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        JButton searchButton = new JButton("Search Recipe");
        JButton enterButton = new JButton("Enter Recipe");

        panel.add(searchButton);
        panel.add(enterButton);

        searchButton.addActionListener(e -> cardLayout.show(mainPanel, "SearchRecipe"));
        enterButton.addActionListener(e -> cardLayout.show(mainPanel, "EnterRecipe"));

        return panel;
    }

    private JPanel createSearchRecipePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        JTextArea resultsArea = new JTextArea();

        panel.add(searchField, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);
        panel.add(searchButton, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> {
            String searchCriteria = searchField.getText();
            ArrayList<String> results = searchRecipe(searchCriteria);
            resultsArea.setText(String.join("\n", results));
        });

        return panel;
    }

    private JPanel createEnterRecipePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2));

        JTextField nameField = new JTextField();
        JTextField timeField = new JTextField();
        JTextArea ingredientsArea = new JTextArea();
        JTextArea instructionsArea = new JTextArea();
        JButton submitButton = new JButton("Submit");

        panel.add(new JLabel("Recipe Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Time (minutes):"));
        panel.add(timeField);
        panel.add(new JLabel("Ingredients (comma separated):"));
        panel.add(new JScrollPane(ingredientsArea));
        panel.add(new JLabel("Instructions:"));
        panel.add(new JScrollPane(instructionsArea));
        panel.add(submitButton);

        submitButton.addActionListener(e -> {
            String name = nameField.getText();
            int time = Integer.parseInt(timeField.getText());
            String ingredients = ingredientsArea.getText();
            String instructions = instructionsArea.getText();
            if (enterRecipe(name, time, ingredients, instructions)) {
                JOptionPane.showMessageDialog(frame, "Recipe added successfully");
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to add recipe");
            }
        });

        return panel;
    }

    private boolean login(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement stmt = conn
                        .prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean signup(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement stmt = conn
                        .prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ArrayList<String> searchRecipe(String criteria) {
        ArrayList<String> results = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM recipes WHERE name LIKE ?")) {
            stmt.setString(1, "%" + criteria + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                String ingredients = rs.getString("ingredients");
                String instructions = rs.getString("instructions");
                int time = rs.getInt("time");
                results.add("Name: " + name + ", Ingredients: " + ingredients + ", Instructions: " + instructions
                        + ", Time: " + time + " mins");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    private boolean enterRecipe(String name, int time, String ingredients, String instructions) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO recipes (name, ingredients, instructions, time) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, name);
            stmt.setString(2, ingredients);
            stmt.setString(3, instructions);
            stmt.setInt(4, time);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RecipeApp::new);
    }
}
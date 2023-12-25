import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class IPFinder extends JFrame implements ActionListener {
    private JLabel l, createdByLabel;
    private JTextField tf;
    private JButton b, historyButton, deleteButton, editButton;
    private List<String> history;

    private Connection connection;
    private Statement statement;

    IPFinder() {
        super("IP Finder Tool");
        l = new JLabel("Enter URL:");
        l.setBounds(50, 70, 150, 20);

        createdByLabel = new JLabel("By: AKSHAT & ABHIJEET");
        createdByLabel.setHorizontalAlignment(SwingConstants.CENTER);
        createdByLabel.setForeground(new java.awt.Color(128, 128, 128));
        createdByLabel.setBounds(10, 10, 280, 20);

        tf = new JTextField();
        tf.setBounds(50, 100, 200, 20);

        b = new JButton("Find IP");
        b.setBounds(50, 150, 80, 30);
        b.addActionListener(this);

        historyButton = new JButton("View History");
        historyButton.setBounds(150, 150, 120, 30);
        historyButton.addActionListener(this);

        deleteButton = new JButton("Delete");
        deleteButton.setBounds(50, 190, 80, 30);
        deleteButton.addActionListener(this);

        editButton = new JButton("Edit");
        editButton.setBounds(150, 190, 80, 30);
        editButton.addActionListener(this);

        add(l);
        add(createdByLabel);
        add(tf);
        add(b);
        add(historyButton);
        add(deleteButton);
        add(editButton);

        history = new ArrayList<>();

        initializeDatabase();

        setSize(300, 270);
        setLayout(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initializeDatabase() {
        try {
            String jdbcUrl = "jdbc:mysql://localhost:3306/temp";
            String user = "root";
            String password = "";

            connection = DriverManager.getConnection(jdbcUrl, user, password);
            statement = connection.createStatement();

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ip_history(url VARCHAR(255), ip VARCHAR(15))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == b) {
            findAndDisplayIP();
        } else if (e.getSource() == historyButton) {
            showHistory();
        } else if (e.getSource() == deleteButton) {
            deleteSelectedHistoryEntry();
        } else if (e.getSource() == editButton) {
            editSelectedHistoryEntry();
        }
    }

    private void findAndDisplayIP() {
        String url = tf.getText();
        try {
            InetAddress ia = InetAddress.getByName(url);
            String ip = ia.getHostAddress();
            JOptionPane.showMessageDialog(this, ip);
            history.add(url + " - " + ip);
            saveToDatabase(url, ip);
        } catch (UnknownHostException e1) {
            JOptionPane.showMessageDialog(this, e1.toString());
        }
    }

    private void saveToDatabase(String url, String ip) {
        try {
            String insertQuery = "INSERT INTO ip_history (url, ip) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                preparedStatement.setString(1, url);
                preparedStatement.setString(2, ip);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showHistory() {
        String[] historyArray = history.toArray(new String[0]);
        String selectedEntry = (String) JOptionPane.showInputDialog(
                this,
                "Select entry to view, edit, or delete:",
                "View/Edit/Delete Entry",
                JOptionPane.QUESTION_MESSAGE,
                null,
                historyArray,
                historyArray[0]);

        if (selectedEntry != null) {
            StringBuilder historyMessage = new StringBuilder("IP History:\n");
            for (String entry : history) {
                historyMessage.append(entry).append("\n");
            }
            JOptionPane.showMessageDialog(this, historyMessage.toString());
        }
    }

    private void deleteSelectedHistoryEntry() {
        String selectedEntry = (String) JOptionPane.showInputDialog(
                this,
                "Select entry to delete:",
                "Delete Entry",
                JOptionPane.QUESTION_MESSAGE,
                null,
                history.toArray(),
                history.get(0));

        if (selectedEntry != null) {
            int index = history.indexOf(selectedEntry);
            history.remove(index);
            deleteFromDatabase(selectedEntry);
        }
    }

    private void deleteFromDatabase(String selectedEntry) {
        String[] parts = selectedEntry.split(" - ");
        String url = parts[0];
        String ip = parts[1];

        try {
            String deleteQuery = "DELETE FROM ip_history WHERE url = ? AND ip = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                preparedStatement.setString(1, url);
                preparedStatement.setString(2, ip);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editSelectedHistoryEntry() {
        String selectedEntry = (String) JOptionPane.showInputDialog(
                this,
                "Select entry to edit:",
                "Edit Entry",
                JOptionPane.QUESTION_MESSAGE,
                null,
                history.toArray(),
                history.get(0));

        if (selectedEntry != null) {
            String newEntry = JOptionPane.showInputDialog(this, "Edit entry:", selectedEntry);
            if (newEntry != null) {
                int index = history.indexOf(selectedEntry);
                history.set(index, newEntry);
                updateDatabaseEntry(selectedEntry, newEntry);
            }
        }
    }

    private void updateDatabaseEntry(String selectedEntry, String newEntry) {
        deleteFromDatabase(selectedEntry);
        String[] parts = newEntry.split(" - ");
        String url = parts[0];
        String ip = parts[1];
        saveToDatabase(url, ip);
    }

    public static void main(String[] args) {
        new IPFinder();
    }
}

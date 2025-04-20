import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class DigitalDiary {
    private JFrame frame;
    private JDateChooser dateChooser;
    private JTextField titleField;
    private JTextArea diaryContent;
    private JButton saveButton, editButton, deleteButton, newEntryButton, searchButton;
    private JList<String> entryList;
    private DefaultListModel<String> listModel;
    private Connection connection;
    private String currentEntryTitle = null;

    public DigitalDiary() {
        connectToDatabase();
        initializeUI();
        loadEntriesFromDatabase();
    }

    private void connectToDatabase() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("config.properties"));

            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database connection failed.");
            System.exit(1);
        }
    }

    private void initializeUI() {
        frame = new JFrame("Digital Diary");
        frame.setSize(800, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(245, 245, 245));

        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(new Color(60, 63, 65));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(dateLabel, gbc);

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new java.util.Date());
        gbc.gridx = 1;
        topPanel.add(dateChooser, gbc);

        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 2;
        topPanel.add(titleLabel, gbc);

        titleField = new JTextField(18);
        gbc.gridx = 3;
        topPanel.add(titleField, gbc);

        searchButton = createStyledButton("Search");
        gbc.gridx = 4;
        topPanel.add(searchButton, gbc);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        diaryContent = new JTextArea();
        diaryContent.setFont(new Font("Georgia", Font.PLAIN, 16));
        diaryContent.setForeground(Color.DARK_GRAY);
        diaryContent.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        diaryContent.setWrapStyleWord(true);
        diaryContent.setLineWrap(true);

        JScrollPane scrollPane = new JScrollPane(diaryContent);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Write your thoughts..."));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        listModel = new DefaultListModel<>();
        entryList = new JList<>(listModel);
        entryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        entryList.setBorder(BorderFactory.createTitledBorder("Previous Entries"));
        entryList.setBackground(new Color(230, 230, 230));

        JScrollPane sidePanel = new JScrollPane(entryList);
        sidePanel.setPreferredSize(new Dimension(200, 0));

        JPanel bottomPanel = new JPanel(new GridLayout(1, 4, 15, 10));
        bottomPanel.setBackground(new Color(60, 63, 65));

        saveButton = createStyledButton("Save");
        editButton = createStyledButton("Edit");
        deleteButton = createStyledButton("Delete");
        newEntryButton = createStyledButton("New Entry");

        bottomPanel.add(saveButton);
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(newEntryButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(sidePanel, BorderLayout.WEST);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        addEventListeners();
        frame.setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(70, 130, 180));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return button;
    }

    private void addEventListeners() {
        newEntryButton.addActionListener(e -> {
            titleField.setText("");
            diaryContent.setText("");
            dateChooser.setDate(new java.util.Date());
            currentEntryTitle = null;
        });

        saveButton.addActionListener(e -> saveEntry());

        editButton.addActionListener(e -> editEntry());

        deleteButton.addActionListener(e -> deleteEntry());

        searchButton.addActionListener(e -> loadEntryByTitle());

        entryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedTitle = entryList.getSelectedValue();
                if (selectedTitle != null) {
                    loadEntryByTitle(selectedTitle);
                }
            }
        });
    }

    private void saveEntry() {
        String title = titleField.getText().trim();
        String content = diaryContent.getText().trim();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(dateChooser.getDate());

        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Title and Content cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String query = "INSERT INTO entries (date, title, content) VALUES (?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, date);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.executeUpdate();

            listModel.addElement(title);
            currentEntryTitle = title;
            JOptionPane.showMessageDialog(frame, "Entry Saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(frame, "Title already exists! Try editing it.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void editEntry() {
        String newTitle = titleField.getText().trim();
        String content = diaryContent.getText().trim();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(dateChooser.getDate());

        if (newTitle.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Title and Content cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (currentEntryTitle == null) {
            JOptionPane.showMessageDialog(frame, "Select an entry to edit!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (!newTitle.equals(currentEntryTitle)) {
                String checkQuery = "SELECT COUNT(*) FROM entries WHERE title=?";
                PreparedStatement checkPs = connection.prepareStatement(checkQuery);
                checkPs.setString(1, newTitle);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(frame, "Another entry with this title already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            String updateQuery = "UPDATE entries SET date=?, title=?, content=? WHERE title=?";
            PreparedStatement ps = connection.prepareStatement(updateQuery);
            ps.setString(1, date);
            ps.setString(2, newTitle);
            ps.setString(3, content);
            ps.setString(4, currentEntryTitle);
            int updated = ps.executeUpdate();

            if (updated > 0) {
                if (!newTitle.equals(currentEntryTitle)) {
                    listModel.removeElement(currentEntryTitle);
                    listModel.addElement(newTitle);
                }
                currentEntryTitle = newTitle;
                JOptionPane.showMessageDialog(frame, "Entry Updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Entry not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteEntry() {
        String title = titleField.getText().trim();

        try {
            String query = "DELETE FROM entries WHERE title=?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, title);
            int deleted = ps.executeUpdate();

            if (deleted > 0) {
                listModel.removeElement(title);
                titleField.setText("");
                diaryContent.setText("");
                currentEntryTitle = null;
                JOptionPane.showMessageDialog(frame, "Entry Deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Entry not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadEntryByTitle() {
        String title = titleField.getText().trim();
        loadEntryByTitle(title);
    }

    private void loadEntryByTitle(String title) {
        try {
            String query = "SELECT * FROM entries WHERE title=?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String content = rs.getString("content");
                String date = rs.getString("date");
                titleField.setText(title);
                diaryContent.setText(content);
                dateChooser.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(date));
                currentEntryTitle = title;
            } else {
                JOptionPane.showMessageDialog(frame, "Entry not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }

            rs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadEntriesFromDatabase() {
        try {
            String query = "SELECT title FROM entries";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                listModel.addElement(rs.getString("title"));
            }
            rs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DigitalDiary::new);
    }
}
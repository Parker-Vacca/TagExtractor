import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class TagExtractor extends JFrame {

    private JTextArea tagDisplayArea;
    private JButton pickTextFileButton, pickStopWordFileButton, saveTagsButton;
    private JFileChooser fileChooser;
    private Map<String, Integer> tagMap;
    private Set<String> stopWords;
    private File textFile;

    public TagExtractor() {
        super("Tag Extractor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        initializeComponents();
        initializeFileChooser();
        layoutComponents();
        addActionListeners();
    }

    private void initializeComponents() {
        fileChooser = new JFileChooser();
        tagMap = new HashMap<>();
        stopWords = new HashSet<>();

        pickTextFileButton = new JButton("Pick Text File");
        pickStopWordFileButton = new JButton("Pick Stop Word File");
        saveTagsButton = new JButton("Save Tags");

        tagDisplayArea = new JTextArea(10, 50);
        tagDisplayArea.setEditable(false);
    }

    private void initializeFileChooser() {
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
    }

    private void layoutComponents() {
        JPanel panel = new JPanel();
        panel.add(pickTextFileButton);
        panel.add(pickStopWordFileButton);
        panel.add(saveTagsButton);

        JScrollPane scrollPane = new JScrollPane(tagDisplayArea);
        getContentPane().add(panel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private void addActionListeners() {
        pickTextFileButton.addActionListener(e -> pickTextFile());
        pickStopWordFileButton.addActionListener(e -> pickStopWordsFile());
        saveTagsButton.addActionListener(e -> {
            try {
                saveTagsToFile();
            } catch (IOException ioException) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ioException.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void pickTextFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            textFile = fileChooser.getSelectedFile();
            scanFile(textFile);
        }
    }

    private void pickStopWordsFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File stopWordsFile = fileChooser.getSelectedFile();
            loadStopWords(stopWordsFile);
        }
    }

    private void scanFile(File file) {
        tagMap.clear(); // Clear previous tags
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.toLowerCase().split("\\W+"); // Split by non-word characters
                for (String word : words) {
                    if (!word.trim().isEmpty() && !stopWords.contains(word)) {
                        tagMap.merge(word, 1, Integer::sum); // Increment frequency
                    }
                }
            }
            displayTags();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadStopWords(File file) {
        stopWords.clear(); // Clear previous stop words
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading stop words: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayTags() {
        StringBuilder sb = new StringBuilder();
        tagMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n"));
        tagDisplayArea.setText(sb.toString());
    }

    private void saveTagsToFile() throws IOException {
        if (textFile == null || tagMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tags to save!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File saveFile = new File(textFile.getParent(), "tags_output.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(saveFile))) {
            tagMap.forEach((tag, freq) -> writer.println(tag + ": " + freq));
        }
        JOptionPane.showMessageDialog(this, "Tags saved to " + saveFile.getAbsolutePath(), "Tags Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TagExtractor().setVisible(true);
        });
    }
}
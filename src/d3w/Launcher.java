package d3w;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * D3W Processor GUI Launcher
 * ファイル選択UIを提供する簡易ランチャー
 */
public class Launcher extends JFrame {
    
    private JTextField d3wFileField;
    private DefaultListModel<String> yamlListModel;
    private JList<String> yamlList;
    private JTextArea logArea;
    
    public Launcher() {
        setTitle("D3W Processor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 上部: ファイル選択エリア
        JPanel filePanel = createFileSelectionPanel();
        
        // 中央: ログ表示エリア
        JPanel logPanel = createLogPanel();
        
        // 下部: 実行ボタン
        JPanel buttonPanel = createButtonPanel();
        
        mainPanel.add(filePanel, BorderLayout.NORTH);
        mainPanel.add(logPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createFileSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("ファイル選択"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // .d3wファイル選択
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("雛型.d3w:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        d3wFileField = new JTextField();
        d3wFileField.setEditable(false);
        panel.add(d3wFileField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton d3wBrowseButton = new JButton("参照...");
        d3wBrowseButton.addActionListener(e -> selectD3wFile());
        panel.add(d3wBrowseButton, gbc);
        
        // YAMLファイルリスト
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        
        JPanel yamlPanel = new JPanel(new BorderLayout(5, 5));
        yamlPanel.add(new JLabel("YAML設定ファイル:"), BorderLayout.NORTH);
        
        yamlListModel = new DefaultListModel<>();
        yamlList = new JList<>(yamlListModel);
        JScrollPane scrollPane = new JScrollPane(yamlList);
        scrollPane.setPreferredSize(new Dimension(0, 120));
        yamlPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel yamlButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("追加...");
        addButton.addActionListener(e -> addYamlFiles());
        JButton removeButton = new JButton("削除");
        removeButton.addActionListener(e -> removeSelectedYaml());
        JButton clearButton = new JButton("クリア");
        clearButton.addActionListener(e -> yamlListModel.clear());
        
        yamlButtonPanel.add(addButton);
        yamlButtonPanel.add(removeButton);
        yamlButtonPanel.add(clearButton);
        yamlPanel.add(yamlButtonPanel, BorderLayout.SOUTH);
        
        panel.add(yamlPanel, gbc);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("ログ"));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton processButton = new JButton("処理実行");
        processButton.setPreferredSize(new Dimension(120, 30));
        processButton.addActionListener(e -> executeProcess());
        
        panel.add(processButton);
        
        return panel;
    }
    
    private void selectD3wFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "D3Wファイル (*.d3w)", "d3w"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            d3wFileField.setText(file.getAbsolutePath());
        }
    }
    
    private void addYamlFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "YAMLファイル (*.yml, *.yaml)", "yml", "yaml"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                yamlListModel.addElement(file.getAbsolutePath());
            }
        }
    }
    
    private void removeSelectedYaml() {
        int[] selectedIndices = yamlList.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            yamlListModel.remove(selectedIndices[i]);
        }
    }
    
    private void executeProcess() {
        // 入力チェック
        String d3wPath = d3wFileField.getText().trim();
        if (d3wPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "雛型.d3wファイルを選択してください。", 
                "エラー", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (yamlListModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "少なくとも1つのYAMLファイルを追加してください。", 
                "エラー", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // YAMLファイルリストを取得
        List<String> yamlPaths = new ArrayList<>();
        for (int i = 0; i < yamlListModel.size(); i++) {
            yamlPaths.add(yamlListModel.get(i));
        }
        
        // ログをクリア
        logArea.setText("");
        
        // 別スレッドで処理を実行
        new Thread(() -> {
            try {
                appendLog("=== 処理開始 ===\n");
                appendLog("雛型.d3w: " + d3wPath + "\n");
                appendLog("YAMLファイル数: " + yamlPaths.size() + "\n");
                for (int i = 0; i < yamlPaths.size(); i++) {
                    appendLog("  [" + (i + 1) + "] " + yamlPaths.get(i) + "\n");
                }
                appendLog("\n");
                
                // Mainクラスの処理を実行
                String[] args = new String[1 + yamlPaths.size()];
                args[0] = d3wPath;
                for (int i = 0; i < yamlPaths.size(); i++) {
                    args[i + 1] = yamlPaths.get(i);
                }
                
                // 標準出力をキャプチャ
                java.io.PrintStream originalOut = System.out;
                java.io.PrintStream originalErr = System.err;
                
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                java.io.PrintStream ps = new java.io.PrintStream(baos);
                System.setOut(ps);
                System.setErr(ps);
                
                try {
                    Main.main(args);
                    
                    System.out.flush();
                    System.setOut(originalOut);
                    System.setErr(originalErr);
                    
                    appendLog(baos.toString("UTF-8"));
                    
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                        "処理が正常に完了しました。",
                        "完了",
                        JOptionPane.INFORMATION_MESSAGE));
                    
                } catch (Exception e) {
                    System.setOut(originalOut);
                    System.setErr(originalErr);
                    
                    appendLog(baos.toString("UTF-8"));
                    appendLog("\nエラー: " + e.getMessage() + "\n");
                    
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                        "処理中にエラーが発生しました:\n" + e.getMessage(),
                        "エラー",
                        JOptionPane.ERROR_MESSAGE));
                }
                
            } catch (Exception e) {
                appendLog("\nエラー: " + e.getMessage() + "\n");
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                    "処理中にエラーが発生しました:\n" + e.getMessage(),
                    "エラー",
                    JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }
    
    private void appendLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public static void main(String[] args) {
        // 引数がある場合はCLIモードで実行
        if (args.length > 0) {
            Main.main(args);
            return;
        }
        
        // 引数がない場合はGUIモードで起動
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Look and Feel設定エラー: " + e.getMessage());
            }
            
            Launcher launcher = new Launcher();
            launcher.setVisible(true);
        });
    }
}

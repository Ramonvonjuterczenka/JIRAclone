package com.jiracopier.gui;

import com.jiracopier.ConfigManager;
import com.jiracopier.IssueCopier;
import com.jiracopier.JiraClientFactory;
import com.atlassian.jira.rest.client.api.JiraRestClient;

import javax.swing.*;
import java.awt.*;
import java.net.URISyntaxException;

public class MainFrame extends JFrame {

    // --- Enums and Constants ---
    public enum AuthMethod {
        TECHNICAL_USER, BEARER_TOKEN
    }

    private static final String AUTH_METHOD_TECHNICAL_USER = "TECHNICAL_USER";
    private static final String AUTH_METHOD_BEARER_TOKEN = "BEARER_TOKEN";

    // Configuration keys
    private static final String KEY_SOURCE_URL = "source.url";
    private static final String KEY_SOURCE_AUTH_METHOD = "source.authMethod";
    private static final String KEY_SOURCE_USER = "source.user";
    private static final String KEY_SOURCE_TOKEN = "source.token";
    private static final String KEY_TARGET_URL = "target.url";
    private static final String KEY_TARGET_AUTH_METHOD = "target.authMethod";
    private static final String KEY_TARGET_USER = "target.user";
    private static final String KEY_TARGET_TOKEN = "target.token";
    private static final String KEY_SOURCE_ISSUE = "issue.source";
    private static final String KEY_TARGET_PROJECT = "issue.target.project";
    private static final String KEY_TARGET_TYPE = "issue.target.type";
    private static final String KEY_PROXY_HOST = "proxy.host";
    private static final String KEY_PROXY_PORT = "proxy.port";

    private final ConfigManager configManager;

    // --- UI Components ---
    // Source JIRA
    private JTextField sourceUrlField;
    private AuthPanel sourceAuthPanel;

    // Target JIRA
    private JTextField targetUrlField;
    private AuthPanel targetAuthPanel;

    // Other fields
    private final JTextField sourceIssueKeyField = new JTextField(15);
    private final JTextField targetProjectKeyField = new JTextField(15);
    private final JTextField targetIssueTypeField = new JTextField(15);
    private final JTextField proxyHostField = new JTextField(30);
    private final JTextField proxyPortField = new JTextField(5);
    private final JButton copyButton = new JButton("Copy Issue");
    private final JTextArea statusArea = new JTextArea(10, 50);

    public MainFrame() {
        super("JIRA Issue Copier");
        this.configManager = new ConfigManager();
        initComponents();
        layoutComponents();
        loadConfiguration();
        attachListeners();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 750); // Increased height for new components
        setLocationRelativeTo(null);
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);

        sourceUrlField = new JTextField(30);
        targetUrlField = new JTextField(30);
        sourceAuthPanel = new AuthPanel();
        targetAuthPanel = new AuthPanel();
    }

    private void layoutComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Create sections ---
        JPanel sourcePanel = createJiraSectionPanel("Source JIRA", sourceUrlField, sourceAuthPanel);
        JPanel targetPanel = createJiraSectionPanel("Target JIRA", targetUrlField, targetAuthPanel);
        JPanel issuePanel = createSectionPanel("Issue Details");
        JPanel proxyPanel = createSectionPanel("Proxy Settings (Optional)");

        // --- Populate static panels ---
        addLabeledComponent(issuePanel, "Source Issue Key:", sourceIssueKeyField, 0);
        addLabeledComponent(issuePanel, "Target Project Key:", targetProjectKeyField, 1);
        addLabeledComponent(issuePanel, "Target Issue Type:", targetIssueTypeField, 2);
        addLabeledComponent(proxyPanel, "Proxy Host:", proxyHostField, 0);
        addLabeledComponent(proxyPanel, "Proxy Port:", proxyPortField, 1);

        // --- Add all panels to the main frame ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0; add(sourcePanel, gbc);
        gbc.gridy++; add(targetPanel, gbc);
        gbc.gridy++; add(issuePanel, gbc);
        gbc.gridy++; add(proxyPanel, gbc);
        gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER; gbc.weightx = 0; add(copyButton, gbc);
        gbc.gridy++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0; add(new JScrollPane(statusArea), gbc);
    }

    private JPanel createJiraSectionPanel(String title, JTextField urlField, AuthPanel authPanel) {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // URL Field
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        addLabeledComponent(mainPanel, "JIRA URL:", urlField, 0);

        // Auth Method Selection
        gbc.gridy++;
        mainPanel.add(authPanel, gbc);

        return mainPanel;
    }

    private void attachListeners() {
        copyButton.addActionListener(e -> startCopyProcess());
    }

    private void loadConfiguration() {
        configManager.loadConfig();
        sourceUrlField.setText(configManager.getProperty(KEY_SOURCE_URL, ""));
        targetUrlField.setText(configManager.getProperty(KEY_TARGET_URL, ""));

        sourceAuthPanel.loadConfig(configManager, KEY_SOURCE_AUTH_METHOD, KEY_SOURCE_USER, KEY_SOURCE_TOKEN);
        targetAuthPanel.loadConfig(configManager, KEY_TARGET_AUTH_METHOD, KEY_TARGET_USER, KEY_TARGET_TOKEN);

        sourceIssueKeyField.setText(configManager.getProperty(KEY_SOURCE_ISSUE, ""));
        targetProjectKeyField.setText(configManager.getProperty(KEY_TARGET_PROJECT, ""));
        targetIssueTypeField.setText(configManager.getProperty(KEY_TARGET_TYPE, ""));
        proxyHostField.setText(configManager.getProperty(KEY_PROXY_HOST, ""));
        proxyPortField.setText(configManager.getProperty(KEY_PROXY_PORT, ""));
    }

    private void saveConfiguration() {
        configManager.setProperty(KEY_SOURCE_URL, sourceUrlField.getText());
        configManager.setProperty(KEY_TARGET_URL, targetUrlField.getText());

        sourceAuthPanel.saveConfig(configManager, KEY_SOURCE_AUTH_METHOD, KEY_SOURCE_USER, KEY_SOURCE_TOKEN);
        targetAuthPanel.saveConfig(configManager, KEY_TARGET_AUTH_METHOD, KEY_TARGET_USER, KEY_TARGET_TOKEN);

        configManager.setProperty(KEY_SOURCE_ISSUE, sourceIssueKeyField.getText());
        configManager.setProperty(KEY_TARGET_PROJECT, targetProjectKeyField.getText());
        configManager.setProperty(KEY_TARGET_TYPE, targetIssueTypeField.getText());
        configManager.setProperty(KEY_PROXY_HOST, proxyHostField.getText());
        configManager.setProperty(KEY_PROXY_PORT, proxyPortField.getText());
        configManager.saveConfig();
    }

    private void startCopyProcess() {
        saveConfiguration();
        copyButton.setEnabled(false);
        statusArea.setText("Starting copy process...\n");

        SwingWorker<String, String> worker = new SwingWorker<String, String>() {
            @Override
            protected String doInBackground() throws Exception {
                String proxyHost = proxyHostField.getText();
                int proxyPort = 0;
                if (proxyHost != null && !proxyHost.trim().isEmpty()) {
                    try {
                        proxyPort = Integer.parseInt(proxyPortField.getText());
                    } catch (NumberFormatException ex) {
                        SwingUtilities.invokeLater(() -> statusArea.append("Warning: Invalid proxy port specified. It must be a number.\n"));
                    }
                }

                try (JiraRestClient sourceClient = sourceAuthPanel.createClient(sourceUrlField.getText(), proxyHost, proxyPort);
                     JiraRestClient targetClient = targetAuthPanel.createClient(targetUrlField.getText(), proxyHost, proxyPort)) {

                    IssueCopier copier = new IssueCopier(sourceClient, targetClient, (message) -> SwingUtilities.invokeLater(() -> statusArea.append(message + "\n")));
                    copier.copyIssue(sourceIssueKeyField.getText(), targetProjectKeyField.getText(), targetIssueTypeField.getText());

                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                }
                return "Process completed.";
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    statusArea.append(result + "\n");
                } catch (Exception e) {
                    statusArea.append("An unexpected error occurred: " + e.getMessage() + "\n");
                    e.printStackTrace();
                } finally {
                    copyButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    private static void addLabeledComponent(JPanel panel, String labelText, JComponent component, int yPos) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = yPos; panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1; gbc.gridy = yPos; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(component, gbc);
    }

    /**
     * Inner class to manage the authentication selection UI and logic for one JIRA instance.
     */
    private static class AuthPanel extends JPanel {
        private final JRadioButton techUserRadio = new JRadioButton("Technical User");
        private final JRadioButton bearerTokenRadio = new JRadioButton("Bearer Token");
        private final CardLayout cardLayout = new CardLayout();
        private final JPanel fieldsPanel = new JPanel(cardLayout);

        private final JTextField usernameField = new JTextField(30);
        private final JPasswordField apiTokenField = new JPasswordField(30);
        private final JPasswordField bearerTokenField = new JPasswordField(30);

        AuthPanel() {
            // Layout for this panel (AuthPanel)
            setLayout(new BorderLayout(0, 5));

            // Radio buttons panel
            JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            radioPanel.add(new JLabel("Auth Method:"));
            radioPanel.add(techUserRadio);
            radioPanel.add(bearerTokenRadio);
            ButtonGroup group = new ButtonGroup();
            group.add(techUserRadio);
            group.add(bearerTokenRadio);

            // Panel for Technical User fields
            JPanel techUserPanel = new JPanel(new GridBagLayout());
            addLabeledComponent(techUserPanel, "Username:", usernameField, 0);
            addLabeledComponent(techUserPanel, "API Token:", apiTokenField, 1);

            // Panel for Bearer Token field
            JPanel bearerTokenPanel = new JPanel(new GridBagLayout());
            addLabeledComponent(bearerTokenPanel, "Bearer Token:", bearerTokenField, 0);

            // Add individual field panels to the card layout
            fieldsPanel.add(techUserPanel, AUTH_METHOD_TECHNICAL_USER);
            fieldsPanel.add(bearerTokenPanel, AUTH_METHOD_BEARER_TOKEN);

            // Add radio panel and fields panel to the main AuthPanel
            add(radioPanel, BorderLayout.NORTH);
            add(fieldsPanel, BorderLayout.CENTER);

            // Add listeners to switch cards
            techUserRadio.addActionListener(e -> cardLayout.show(fieldsPanel, AUTH_METHOD_TECHNICAL_USER));
            bearerTokenRadio.addActionListener(e -> cardLayout.show(fieldsPanel, AUTH_METHOD_BEARER_TOKEN));

            techUserRadio.setSelected(true);
        }

        AuthMethod getSelectedAuthMethod() {
            return bearerTokenRadio.isSelected() ? AuthMethod.BEARER_TOKEN : AuthMethod.TECHNICAL_USER;
        }

        void loadConfig(ConfigManager cm, String authKey, String userKey, String tokenKey) {
            String authMethod = cm.getProperty(authKey, AUTH_METHOD_TECHNICAL_USER);
            usernameField.setText(cm.getProperty(userKey, ""));
            String token = cm.getProperty(tokenKey, "");

            if (AUTH_METHOD_BEARER_TOKEN.equals(authMethod)) {
                bearerTokenRadio.setSelected(true);
                cardLayout.show(fieldsPanel, AUTH_METHOD_BEARER_TOKEN);
                bearerTokenField.setText(token);
                apiTokenField.setText(""); // Clear other field
            } else {
                techUserRadio.setSelected(true);
                cardLayout.show(fieldsPanel, AUTH_METHOD_TECHNICAL_USER);
                apiTokenField.setText(token);
                bearerTokenField.setText(""); // Clear other field
            }
        }

        void saveConfig(ConfigManager cm, String authKey, String userKey, String tokenKey) {
            if (bearerTokenRadio.isSelected()) {
                cm.setProperty(authKey, AUTH_METHOD_BEARER_TOKEN);
                cm.setProperty(userKey, ""); // Clear user field
                cm.setProperty(tokenKey, new String(bearerTokenField.getPassword()));
            } else {
                cm.setProperty(authKey, AUTH_METHOD_TECHNICAL_USER);
                cm.setProperty(userKey, usernameField.getText());
                cm.setProperty(tokenKey, new String(apiTokenField.getPassword()));
            }
        }

        JiraRestClient createClient(String url, String proxyHost, int proxyPort) throws URISyntaxException {
            if (bearerTokenRadio.isSelected()) {
                return JiraClientFactory.createWithBearerTokenAuthentication(url, new String(bearerTokenField.getPassword()), proxyHost, proxyPort);
            } else {
                return JiraClientFactory.createWithBasicAuthentication(url, usernameField.getText(), new String(apiTokenField.getPassword()), proxyHost, proxyPort);
            }
        }
    }
}

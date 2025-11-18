package com.jiracopier.gui;

import com.jiracopier.ConfigManager;
import com.jiracopier.IssueCopier;
import com.jiracopier.JiraClientFactory;
import com.atlassian.jira.rest.client.api.JiraRestClient;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class MainFrame extends JFrame {

    // Configuration keys
    private static final String KEY_SOURCE_URL = "source.url";
    private static final String KEY_SOURCE_USER = "source.user";
    private static final String KEY_SOURCE_TOKEN = "source.token";
    private static final String KEY_TARGET_URL = "target.url";
    private static final String KEY_TARGET_USER = "target.user";
    private static final String KEY_TARGET_TOKEN = "target.token";
    private static final String KEY_SOURCE_ISSUE = "issue.source";
    private static final String KEY_TARGET_PROJECT = "issue.target.project";
    private static final String KEY_TARGET_TYPE = "issue.target.type";
    private static final String KEY_PROXY_HOST = "proxy.host";
    private static final String KEY_PROXY_PORT = "proxy.port";

    private final ConfigManager configManager;

    // --- UI Components ---
    private final JTextField sourceUrlField = new JTextField(30);
    private final JTextField sourceUsernameField = new JTextField(30);
    private final JPasswordField sourceApiTokenField = new JPasswordField(30);
    private final JTextField targetUrlField = new JTextField(30);
    private final JTextField targetUsernameField = new JTextField(30);
    private final JPasswordField targetApiTokenField = new JPasswordField(30);
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
        setSize(700, 600);
        setLocationRelativeTo(null);
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
    }

    private void layoutComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel sourcePanel = createSectionPanel("Source JIRA");
        addLabeledComponent(sourcePanel, "JIRA URL:", sourceUrlField, 0);
        addLabeledComponent(sourcePanel, "Username:", sourceUsernameField, 1);
        addLabeledComponent(sourcePanel, "API Token:", sourceApiTokenField, 2);

        JPanel targetPanel = createSectionPanel("Target JIRA");
        addLabeledComponent(targetPanel, "JIRA URL:", targetUrlField, 0);
        addLabeledComponent(targetPanel, "Username:", targetUsernameField, 1);
        addLabeledComponent(targetPanel, "API Token:", targetApiTokenField, 2);

        JPanel issuePanel = createSectionPanel("Issue Details");
        addLabeledComponent(issuePanel, "Source Issue Key:", sourceIssueKeyField, 0);
        addLabeledComponent(issuePanel, "Target Project Key:", targetProjectKeyField, 1);
        addLabeledComponent(issuePanel, "Target Issue Type:", targetIssueTypeField, 2);

        JPanel proxyPanel = createSectionPanel("Proxy Settings (Optional)");
        addLabeledComponent(proxyPanel, "Proxy Host:", proxyHostField, 0);
        addLabeledComponent(proxyPanel, "Proxy Port:", proxyPortField, 1);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; add(sourcePanel, gbc);
        gbc.gridy++; add(targetPanel, gbc);
        gbc.gridy++; add(issuePanel, gbc);
        gbc.gridy++; add(proxyPanel, gbc);

        gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER; add(copyButton, gbc);
        gbc.gridy++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0; add(new JScrollPane(statusArea), gbc);
    }

    private void attachListeners() {
        copyButton.addActionListener(e -> startCopyProcess());
    }

    private void loadConfiguration() {
        configManager.loadConfig();
        sourceUrlField.setText(configManager.getProperty(KEY_SOURCE_URL, ""));
        sourceUsernameField.setText(configManager.getProperty(KEY_SOURCE_USER, ""));
        sourceApiTokenField.setText(configManager.getProperty(KEY_SOURCE_TOKEN, ""));
        targetUrlField.setText(configManager.getProperty(KEY_TARGET_URL, ""));
        targetUsernameField.setText(configManager.getProperty(KEY_TARGET_USER, ""));
        targetApiTokenField.setText(configManager.getProperty(KEY_TARGET_TOKEN, ""));
        sourceIssueKeyField.setText(configManager.getProperty(KEY_SOURCE_ISSUE, ""));
        targetProjectKeyField.setText(configManager.getProperty(KEY_TARGET_PROJECT, ""));
        targetIssueTypeField.setText(configManager.getProperty(KEY_TARGET_TYPE, ""));
        proxyHostField.setText(configManager.getProperty(KEY_PROXY_HOST, ""));
        proxyPortField.setText(configManager.getProperty(KEY_PROXY_PORT, ""));
    }

    private void saveConfiguration() {
        configManager.setProperty(KEY_SOURCE_URL, sourceUrlField.getText());
        configManager.setProperty(KEY_SOURCE_USER, sourceUsernameField.getText());
        configManager.setProperty(KEY_SOURCE_TOKEN, new String(sourceApiTokenField.getPassword()));
        configManager.setProperty(KEY_TARGET_URL, targetUrlField.getText());
        configManager.setProperty(KEY_TARGET_USER, targetUsernameField.getText());
        configManager.setProperty(KEY_TARGET_TOKEN, new String(targetApiTokenField.getPassword()));
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
                String sourceJiraUrl = sourceUrlField.getText();
                String sourceJiraUsername = sourceUsernameField.getText();
                String sourceJiraApiToken = new String(sourceApiTokenField.getPassword());
                String targetJiraUrl = targetUrlField.getText();
                String targetJiraUsername = targetUsernameField.getText();
                String targetJiraApiToken = new String(targetApiTokenField.getPassword());
                String sourceIssueKey = sourceIssueKeyField.getText();
                String targetProjectKey = targetProjectKeyField.getText();
                String targetIssueTypeName = targetIssueTypeField.getText();
                String proxyHost = proxyHostField.getText();
                int proxyPort = 0;
                try {
                    proxyPort = Integer.parseInt(proxyPortField.getText());
                } catch (NumberFormatException ex) {
                    // Ignore if port is not a valid number
                }

                try (JiraRestClient sourceClient = JiraClientFactory.createJiraRestClient(sourceJiraUrl, sourceJiraUsername, sourceJiraApiToken, proxyHost, proxyPort);
                     JiraRestClient targetClient = JiraClientFactory.createJiraRestClient(targetJiraUrl, targetJiraUsername, targetJiraApiToken, proxyHost, proxyPort)) {

                    IssueCopier copier = new IssueCopier(sourceClient, targetClient, (message) -> SwingUtilities.invokeLater(() -> statusArea.append(message + "\n")));
                    copier.copyIssue(sourceIssueKey, targetProjectKey, targetIssueTypeName);

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

    private void addLabeledComponent(JPanel panel, String labelText, JComponent component, int yPos) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = yPos; panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(component, gbc);
    }
}

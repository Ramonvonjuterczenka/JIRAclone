package com.jiracopier;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import io.atlassian.util.concurrent.Promise;
import java.util.function.Consumer;

public class IssueCopier {

    private final JiraRestClient targetJiraClient;
    private final Consumer<String> logger;

    /**
     * @param targetJiraClient The client for the target JIRA instance.
     * @param logger           A consumer for log messages to be displayed in the UI.
     */
    public IssueCopier(JiraRestClient targetJiraClient, Consumer<String> logger) {
        this.targetJiraClient = targetJiraClient;
        this.logger = logger != null ? logger : (message) -> {}; // Avoid NullPointerException
    }

    /**
     * Creates an issue in the target JIRA instance based on the provided IssueInput.
     *
     * @param issueInput The fully constructed issue data to create.
     */
    public void copyIssue(IssueInput issueInput) {
        try {
            log("Creating new issue in target JIRA...");
            Promise<BasicIssue> createIssuePromise = targetJiraClient.getIssueClient().createIssue(issueInput);
            BasicIssue createdIssue = createIssuePromise.claim();

            log("-> Successfully created issue in target JIRA: " + createdIssue.getKey());

        } catch (Exception e) {
            log("An error occurred while copying the issue: " + e.getMessage());
            // Re-throw as a runtime exception to be caught by the SwingWorker
            throw new RuntimeException(e);
        }
    }

    private void log(String message) {
        this.logger.accept(message);
    }
}

package com.jiracopier;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import io.atlassian.util.concurrent.Promise;

import java.util.function.Consumer;

public class IssueCopier {

    private final JiraRestClient sourceJiraClient;
    private final JiraRestClient targetJiraClient;
    private final Consumer<String> logger;

    /**
     * @param logger A consumer for log messages to be displayed in the UI.
     */
    public IssueCopier(JiraRestClient sourceJiraClient, JiraRestClient targetJiraClient, Consumer<String> logger) {
        this.sourceJiraClient = sourceJiraClient;
        this.targetJiraClient = targetJiraClient;
        this.logger = logger != null ? logger : (message) -> {}; // Avoid NullPointerException
    }

    public void copyIssue(String sourceIssueKey, String targetProjectKey, String targetIssueTypeName) {
        try {
            // 1. Fetch the source issue
            log("Fetching issue '" + sourceIssueKey + "' from source JIRA...");
            Promise<Issue> getIssuePromise = sourceJiraClient.getIssueClient().getIssue(sourceIssueKey);
            Issue sourceIssue = getIssuePromise.claim();
            log("-> Fetched issue: " + sourceIssue.getSummary());

            // 2. Get the Issue Type ID from the target instance
            log("Fetching issue types from target JIRA...");
            Promise<Iterable<IssueType>> issueTypesPromise = targetJiraClient.getMetadataClient().getIssueTypes();
            Long issueTypeId = null;
            for (IssueType issueType : issueTypesPromise.claim()) {
                if (issueType.getName().equalsIgnoreCase(targetIssueTypeName)) {
                    issueTypeId = issueType.getId();
                    break;
                }
            }

            if (issueTypeId == null) {
                throw new RuntimeException("Error: Issue Type '" + targetIssueTypeName + "' not found in target JIRA.");
            }
            log("-> Found issue type '" + targetIssueTypeName + "' with ID: " + issueTypeId);

            // 3. Prepare the new issue for the target Jira
            String summary = sourceIssue.getSummary();
            String description = sourceIssue.getDescription();

            IssueInputBuilder issueInputBuilder = new IssueInputBuilder(targetProjectKey, issueTypeId, summary);
            issueInputBuilder.setDescription(description);

            IssueInput newIssue = issueInputBuilder.build();

            // 4. Create the issue in the target Jira
            log("Creating new issue in target JIRA...");
            Promise<BasicIssue> createIssuePromise = targetJiraClient.getIssueClient().createIssue(newIssue);
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

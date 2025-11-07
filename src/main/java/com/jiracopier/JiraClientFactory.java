package com.jiracopier;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class JiraClientFactory {

    public static JiraRestClient createJiraRestClient(String jiraUrl, String username, String apiToken) throws URISyntaxException {
        return new AsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthentication(new URI(jiraUrl), username, apiToken);
    }
}

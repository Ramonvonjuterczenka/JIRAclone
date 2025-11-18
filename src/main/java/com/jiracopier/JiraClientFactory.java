package com.jiracopier;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class JiraClientFactory {

    /**
     * Creates a JIRA REST client without a proxy.
     */
    public static JiraRestClient createJiraRestClient(String jiraUrl, String username, String apiToken) throws URISyntaxException {
        return createJiraRestClient(jiraUrl, username, apiToken, null, 0);
    }

    /**
     * Creates a JIRA REST client with optional proxy settings by setting system properties.
     */
    public static JiraRestClient createJiraRestClient(String jiraUrl, String username, String apiToken, String proxyHost, int proxyPort) throws URISyntaxException {
        // Set proxy settings via system properties, as this is the most reliable method
        // supported by the underlying HTTP client used by the JIRA REST client.
        if (proxyHost != null && !proxyHost.trim().isEmpty() && proxyPort > 0) {
            System.setProperty("https.proxyHost", proxyHost);
            System.setProperty("https.proxyPort", String.valueOf(proxyPort));
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", String.valueOf(proxyPort));
        } else {
            // Clear proxy settings if not provided
            System.clearProperty("https.proxyHost");
            System.clearProperty("https.proxyPort");
            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
        }

        final URI serverUri = new URI(jiraUrl);
        return new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(serverUri, username, apiToken);
    }
}

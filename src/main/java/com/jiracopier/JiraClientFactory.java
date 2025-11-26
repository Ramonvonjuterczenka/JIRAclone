package com.jiracopier;

import com.atlassian.httpclient.api.Request;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class JiraClientFactory {

    /**
     * Creates a JIRA REST client with Basic Auth (username/apiToken).
     */
    public static JiraRestClient createWithBasicAuthentication(String jiraUrl, String username, String apiToken, String proxyHost, int proxyPort) throws URISyntaxException {
        setProxyProperties(proxyHost, proxyPort);
        final URI serverUri = new URI(jiraUrl);
        return new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(serverUri, username, apiToken);
    }

    /**
     * Creates a JIRA REST client using Bearer Token authentication.
     */
    public static JiraRestClient createWithBearerTokenAuthentication(String jiraUrl, String bearerToken, String proxyHost, int proxyPort) throws URISyntaxException {
        setProxyProperties(proxyHost, proxyPort);
        final URI serverUri = new URI(jiraUrl);

        final AuthenticationHandler bearerAuthHandler = builder -> builder.setHeader("Authorization", "Bearer " + bearerToken);

        return new AsynchronousJiraRestClientFactory().create(serverUri, bearerAuthHandler);
    }

    /**
     * Sets or clears the system properties for HTTP/HTTPS proxy.
     */
    private static void setProxyProperties(String proxyHost, int proxyPort) {
        if (proxyHost != null && !proxyHost.trim().isEmpty() && proxyPort > 0) {
            System.setProperty("https.proxyHost", proxyHost);
            System.setProperty("https.proxyPort", String.valueOf(proxyPort));
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", String.valueOf(proxyPort));
        } else {
            System.clearProperty("https.proxyHost");
            System.clearProperty("https.proxyPort");
            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
        }
    }
}

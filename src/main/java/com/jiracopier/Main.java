package com.jiracopier;

import com.atlassian.jira.rest.client.api.JiraRestClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- Source JIRA ---");
        System.out.print("Enter Source JIRA URL: ");
        String sourceJiraUrl = scanner.nextLine();
        System.out.print("Enter Source JIRA Username: ");
        String sourceJiraUsername = scanner.nextLine();
        System.out.print("Enter Source JIRA API Token: ");
        String sourceJiraApiToken = scanner.nextLine();

        System.out.println("\n--- Target JIRA ---");
        System.out.print("Enter Target JIRA URL: ");
        String targetJiraUrl = scanner.nextLine();
        System.out.print("Enter Target JIRA Username: ");
        String targetJiraUsername = scanner.nextLine();
        System.out.print("Enter Target JIRA API Token: ");
        String targetJiraApiToken = scanner.nextLine();

        System.out.println("\n--- Issue Details ---");
        System.out.print("Enter Source Issue Key (e.g., PROJ-123): ");
        String sourceIssueKey = scanner.nextLine();
        System.out.print("Enter Target Project Key (e.g., NEWPROJ): ");
        String targetProjectKey = scanner.nextLine();
        System.out.print("Enter Target Issue Type Name (e.g., Story, Task, Bug): ");
        String targetIssueTypeName = scanner.nextLine();

        scanner.close();

        try (JiraRestClient sourceJiraClient = JiraClientFactory.createJiraRestClient(sourceJiraUrl, sourceJiraUsername, sourceJiraApiToken);
             JiraRestClient targetJiraClient = JiraClientFactory.createJiraRestClient(targetJiraUrl, targetJiraUsername, targetJiraApiToken)) {

            System.out.println("\nConnecting to JIRA instances...");
            IssueCopier issueCopier = new IssueCopier(sourceJiraClient, targetJiraClient);
            issueCopier.copyIssue(sourceIssueKey, targetProjectKey, targetIssueTypeName);

        } catch (URISyntaxException e) {
            System.err.println("Error: A JIRA URL seems to be invalid. Details: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error closing the JIRA client. Details: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during the process. Details: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

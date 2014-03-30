package hudson.plugins.jira.remote.rest;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;

import hudson.plugins.jira.remote.JiraInteractionSession;
import hudson.plugins.jira.soap.RemoteComponent;
import hudson.plugins.jira.soap.RemoteFieldValue;
import hudson.plugins.jira.soap.RemoteGroup;
import hudson.plugins.jira.soap.RemoteIssue;
import hudson.plugins.jira.soap.RemoteIssueType;
import hudson.plugins.jira.soap.RemoteProjectRole;
import hudson.plugins.jira.soap.RemoteVersion;

public class JiraRestSession implements JiraInteractionSession {

    private static final Logger LOGGER = Logger.getLogger(JiraRestSession.class.getName());

    private final JiraRestClient jiraRestClient;

    public static JiraRestSession createSession(URI jiraUri, String username, String password) {

        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final JiraRestClient jiraRestClient = factory.createWithBasicHttpAuthentication(jiraUri, username, password);

        JiraRestSession jiraRestSession = new JiraRestSession(jiraRestClient);

        try {
            ServerInfo serverInfo = jiraRestClient.getMetadataClient().getServerInfo().get();
            String jiraVersion = serverInfo.getVersion();
            LOGGER.info("Successfully connected to JIRAira instance, found version " + jiraVersion);
            return jiraRestSession;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Could not connect to JIRA instance at " + jiraUri, ex);
            return null;
        }
    }

    /* package */ JiraRestSession(JiraRestClient jiraRestClient) {
        this.jiraRestClient = jiraRestClient;
    }

    public String getEmailForUsername(String username) throws RemoteException, InterruptedException, ExecutionException {
        Promise<User> userPromise = jiraRestClient.getUserClient().getUser(username);
        User user = userPromise.get();
        return user.getEmailAddress();
    }

    public Set<String> getProjectKeys() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public void addComment(String issueId, String comment,
            String groupVisibility, String roleVisibility)
                    throws RemoteException {
        // TODO Auto-generated method stub

    }

    public RemoteIssue getIssue(String id) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteIssue[] getIssuesFromJqlSearch(String jqlSearch)
            throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteGroup getGroup(String groupId) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteProjectRole getRole(String roleId) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteVersion[] getVersions(String projectKey)
            throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteVersion getVersionByName(String projectKey, String name)
            throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteIssue[] getIssuesWithFixVersion(String projectKey,
            String version) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteIssue[] getIssuesWithFixVersion(String projectKey,
            String version, String filter) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteIssueType[] getIssueTypes() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean existsIssue(String id) throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }

    public void releaseVersion(String projectKey, RemoteVersion version)
            throws RemoteException {
        // TODO Auto-generated method stub

    }

    public void migrateIssuesToFixVersion(String projectKey, String version,
            String query) throws RemoteException {
        // TODO Auto-generated method stub

    }

    public void replaceFixVersion(String projectKey, String fromVersion,
            String toVersion, String query) throws RemoteException {
        // TODO Auto-generated method stub

    }

    public String progressWorkflowAction(String issueKey,
            String workflowActionName, RemoteFieldValue[] fields)
                    throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getActionIdForIssue(String issueKey, String workflowAction)
            throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getStatusById(String statusId) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteIssue createIssue(String projectKey, String description,
            String assignee, RemoteComponent[] components, String summary)
                    throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public void addCommentWithoutConstrains(String issueId, String comment)
            throws RemoteException {
        // TODO Auto-generated method stub

    }

    public RemoteIssue getIssueByKey(String issueId) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteComponent[] getComponents(String projectKey)
            throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteVersion addVersion(String version, String projectKey)
            throws hudson.plugins.jira.soap.RemoteException, RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

}

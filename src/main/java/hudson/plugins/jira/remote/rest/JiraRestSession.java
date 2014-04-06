package hudson.plugins.jira.remote.rest;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;

import hudson.plugins.jira.remote.JiraInteractionSession;
import hudson.plugins.jira.soap.RemoteComponent;
import hudson.plugins.jira.soap.RemoteFieldValue;
import hudson.plugins.jira.soap.RemoteGroup;
import hudson.plugins.jira.soap.RemoteIssue;
import hudson.plugins.jira.soap.RemoteIssueType;
import hudson.plugins.jira.soap.RemoteVersion;
import hudson.util.Secret;

/**
 * Allows interaction with a JIRA instance using its REST API.
 *
 * @author Stefan Thurnherr
 */
public class JiraRestSession implements JiraInteractionSession {

    private static final Logger LOGGER = Logger.getLogger(JiraRestSession.class.getName());

    private final JiraRestClient jiraRestClient;

    public static JiraRestSession createSession(URI jiraUri, UsernamePasswordCredentials credentials) {

        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final JiraRestClient jiraRestClient;

        if (credentials == null) {
            LOGGER.info("No credentials specified, trying to connect to JIRA instance at " + jiraUri + " anonymously.");
            jiraRestClient = factory.create(jiraUri, new AnonymousAuthenticationHandler());

        } else {
            LOGGER.info("Trying to connect to JIRA instance at " + jiraUri + " using specified credentials (description: " + credentials.getDescriptor() + ").");
            final String password = Secret.toString(credentials.getPassword());
            jiraRestClient = factory.createWithBasicHttpAuthentication(jiraUri, credentials.getUsername(), password);
        }

        try {
            final JiraRestSession jiraRestSession = new JiraRestSession(jiraRestClient);

            //FIXME: access to /serverInfo resource seems not allowed for anonymous users - find better solution.
            ServerInfo serverInfo = jiraRestClient.getMetadataClient().getServerInfo().get();
            String jiraVersion = serverInfo.getVersion();
            LOGGER.info("Successfully connected to JIRA instance, found version " + jiraVersion);
            return jiraRestSession;

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Could not connect to JIRA instance using credentials " + credentials, ex);
            return null;
        }
    }

    public static JiraRestSession createSession(URI jiraUri, String username, String password) {

        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final JiraRestClient jiraRestClient = factory.createWithBasicHttpAuthentication(jiraUri, username, password);
        factory.create(jiraUri, new AnonymousAuthenticationHandler());

        JiraRestSession jiraRestSession = new JiraRestSession(jiraRestClient);

        try {
            //FIXME: access to /serverInfo resource seems not allowed for anonymous users - find better solution.
            ServerInfo serverInfo = jiraRestClient.getMetadataClient().getServerInfo().get();
            String jiraVersion = serverInfo.getVersion();
            LOGGER.info("Successfully connected to JIRA instance, found version " + jiraVersion);
            return jiraRestSession;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Could not connect to JIRA instance at " + jiraUri, ex);
            return null;
        }
    }

    /* package */ JiraRestSession(JiraRestClient jiraRestClient) {
        this.jiraRestClient = jiraRestClient;
    }

    private <T> T throwNotImplementedYet() throws RuntimeException {
        throw new RuntimeException("This functionality is not implemented yet for REST-based JIRA access.");
    }

    public String getEmailForUsername(String username) throws RemoteException, InterruptedException, ExecutionException {
        Promise<User> userPromise = jiraRestClient.getUserClient().getUser(username);
        User user = userPromise.get();
        return user.getEmailAddress();
    }

    public Set<String> getProjectKeys() throws RemoteException {
        return throwNotImplementedYet();
    }

    public Promise<Iterable<BasicProject>> getProjectKeysAsync() {
        return jiraRestClient.getProjectClient().getAllProjects();
    }

    public void addComment(String issueId, String comment, String groupVisibility, String roleVisibility) throws RemoteException {
        throwNotImplementedYet();
    }

    public RemoteIssue getIssue(String id) throws RemoteException {
        return throwNotImplementedYet();
    }

    public Promise<Issue> getIssueAsync(String id) {
        return jiraRestClient.getIssueClient().getIssue(id);
    }

    public RemoteIssue[] getIssuesFromJqlSearch(String jqlSearch)
            throws RemoteException {
        return throwNotImplementedYet();
    }

    public RemoteGroup getGroup(String groupId) throws RemoteException {
        return throwNotImplementedYet();
    }

    public RemoteVersion[] getVersions(String projectKey)
            throws RemoteException {
        return throwNotImplementedYet();
    }

    public RemoteIssue[] getIssuesWithFixVersion(String projectKey,
            String version) throws RemoteException {
        return throwNotImplementedYet();
    }

    public RemoteIssue[] getIssuesWithFixVersion(String projectKey,
            String version, String filter) throws RemoteException {
        return throwNotImplementedYet();
    }

    public RemoteIssueType[] getIssueTypes() throws RemoteException {
        return throwNotImplementedYet();
    }

    public boolean existsIssue(String id) throws RemoteException {
        return (Boolean)throwNotImplementedYet();
    }

    public void releaseVersion(String projectKey, RemoteVersion version)
            throws RemoteException {
        throwNotImplementedYet();
    }

    public void migrateIssuesToFixVersion(String projectKey, String version,
            String query) throws RemoteException {
        throwNotImplementedYet();
    }

    public void replaceFixVersion(String projectKey, String fromVersion,
            String toVersion, String query) throws RemoteException {
        throwNotImplementedYet();
    }

    public String progressWorkflowAction(String issueKey,
            String workflowActionName, RemoteFieldValue[] fields)
                    throws RemoteException {
        return throwNotImplementedYet();
    }

    public String getActionIdForIssue(String issueKey, String workflowAction)
            throws RemoteException {
        return throwNotImplementedYet();
    }

    public RemoteIssue createIssue(String projectKey, String description,
            String assignee, RemoteComponent[] components, String summary)
                    throws RemoteException {
        return throwNotImplementedYet();
    }

    public void addCommentWithoutConstrains(String issueId, String comment)
            throws RemoteException {
        throwNotImplementedYet();
    }

    public RemoteIssue getIssueByKey(String issueId) throws RemoteException {
        return throwNotImplementedYet();
    }

    public RemoteComponent[] getComponents(String projectKey)
            throws RemoteException {
        return throwNotImplementedYet();
    }

    public RemoteVersion addVersion(String version, String projectKey)
            throws hudson.plugins.jira.soap.RemoteException, RemoteException {
        return throwNotImplementedYet();
    }

}

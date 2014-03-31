package hudson.plugins.jira.remote.soap;

import hudson.plugins.jira.JiraSite;
import hudson.plugins.jira.remote.JiraInteractionSession;
import hudson.plugins.jira.soap.*;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.util.concurrent.Promise;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Connection to JIRA.
 * JIRA has a built-in timeout for a session, so after some inactive period the
 * session will become invalid. The caller must make sure that this doesn't
 * happen.
 *
 * @author Kohsuke Kawaguchi
 */
public class JiraSoapSession implements JiraInteractionSession {
    private static final Logger LOGGER = Logger.getLogger(JiraSoapSession.class.getName());

    public final JiraSoapService service;

    private final String urlExternalForm;

    /**
     * This security token is used by the server to associate SOAP invocations
     * with a specific user.
     */
    public final String token;

    /**
     * Lazily computed list of project keys.
     */
    private Set<String> projectKeys;

    /**
     * This session is created for this site.
     */
    private final JiraSite site;

    //FIXME: first method argument 'site' shouldn't be necessary, check why.
    public static JiraSoapSession createSession(JiraSite site, URL url, String username, String password, boolean useHttpAuth) throws IOException, ServiceException {

        if (username == null || password == null) {
            return null;    // remote access not supported
        }

        final JiraSoapServiceService jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
        final String urlExternalForm = url.toExternalForm();

        if (useHttpAuth) {
            String httpAuthUrl = url.toExternalForm().replace(
                    url.getHost(),
                    username + ":" + password + "@" + url.getHost()) + "rpc/soap/jirasoapservice-v2";
            JiraSoapService service = jiraSoapServiceGetter.getJirasoapserviceV2(
                    new URL(httpAuthUrl));

            return new JiraSoapSession(site, urlExternalForm, service, null); //no need to login
        }

        JiraSoapService service = jiraSoapServiceGetter.getJirasoapserviceV2(
                new URL(url, "rpc/soap/jirasoapservice-v2"));

        final String jiraToken = service.login(username, password);
        return new JiraSoapSession(site, urlExternalForm, service, jiraToken);
    }

    /* package */JiraSoapSession(JiraSite site, String urlExternalForm, JiraSoapService service,
            String token) {
        this.service = service;
        this.urlExternalForm = urlExternalForm;
        this.token = token;
        this.site = site;
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#getProjectKeys()
     */
    public Set<String> getProjectKeys() throws RemoteException {
        if (projectKeys == null) {
            LOGGER.fine("Fetching remote project key list from " + urlExternalForm);
            RemoteProject[] remoteProjects = service
                    .getProjectsNoSchemes(token);
            projectKeys = new HashSet<String>(remoteProjects.length);
            for (RemoteProject p : remoteProjects) {
                projectKeys.add(p.getKey().toUpperCase());
            }
            LOGGER.fine("Project list=" + projectKeys);
        }
        return projectKeys;
    }

    public Promise<Iterable<BasicProject>> getProjectKeysAsync() {
        throw new RuntimeException("async not impl for SOAP API.");
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#addComment(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void addComment(String issueId, String comment,
            String groupVisibility, String roleVisibility) throws RemoteException {
        RemoteComment rc = new RemoteComment();
        rc.setBody(comment);

        try {
            if (isNotEmpty(roleVisibility) && getRole(roleVisibility) != null) {
                rc.setRoleLevel(roleVisibility);
            }
        } catch (RemoteValidationException rve) {
            LOGGER.throwing(this.getClass().toString(), "setRoleLevel", rve);
        }

        try {
            if (isNotEmpty(groupVisibility) && getGroup(groupVisibility) != null) {
                rc.setGroupLevel(groupVisibility);
            }
        } catch (RemoteValidationException rve) {
            LOGGER.throwing(this.getClass().toString(), "setGroupLevel", rve);
        }

        service.addComment(token, issueId, rc);
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#getIssue(java.lang.String)
     */
    public RemoteIssue getIssue(String id) throws RemoteException {
        if (existsIssue(id)) {
            return service.getIssue(token, id);
        } else {
            return null;
        }
    }

    public Promise<Issue> getIssueAsync(String id) {
        throw new RuntimeException("Async calls not supported by SOAP API.");
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#getIssuesFromJqlSearch(java.lang.String)
     */
    public RemoteIssue[] getIssuesFromJqlSearch(final String jqlSearch)
            throws RemoteException {
        return service.getIssuesFromJqlSearch(token, jqlSearch, 50);
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#getGroup(java.lang.String)
     */
    public RemoteGroup getGroup(String groupId) throws RemoteException {
        LOGGER.fine("Fetching groupInfo from " + groupId);
        return service.getGroup(token, groupId);
    }

    /**
     * Gets the details of a role, given a roleId. Used for validating role
     * visibility. TODO: Cannot validate against the real project role the user
     * have in the project, jira soap api has no such function!
     * 
     * @param roleId
     *            like "Software Development"
     * @return null if no such role exists
     */
    private RemoteProjectRole getRole(String roleId) throws RemoteException {
        LOGGER.fine("Fetching roleInfo from " + roleId);

        RemoteProjectRole[] roles = service.getProjectRoles(token);

        if (roles != null && roles.length > 0) {
            for (RemoteProjectRole role : roles) {
                if (role != null && role.getName() != null && role.getName().equals(roleId)) {
                    return role;
                }
            }
        }

        LOGGER.info("Did not find role named " + roleId + ".");

        return null;
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#getVersions(java.lang.String)
     */
    public RemoteVersion[] getVersions(String projectKey) throws RemoteException {
        LOGGER.fine("Fetching versions from project: " + projectKey);

        return service.getVersions(token, projectKey);
    }

    /**
     * Gets a version of a JIRA project by its name
     *
     * @return A RemoteVersion, or null if not found
     */
    private RemoteVersion getVersionByName(String projectKey, String name) throws RemoteException {
        LOGGER.fine("Fetching versions from project: " + projectKey);
        RemoteVersion[] versions = getVersions(projectKey);
        if (versions == null) {
            return null;
        }
        for (RemoteVersion version : versions) {
            if (version.getName().equals(name)) {
                return version;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#getIssuesWithFixVersion(java.lang.String, java.lang.String)
     */
    public RemoteIssue[] getIssuesWithFixVersion(String projectKey, String version) throws RemoteException {
        return getIssuesWithFixVersion(projectKey, version, "");
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#getIssuesWithFixVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    public RemoteIssue[] getIssuesWithFixVersion(String projectKey, String version, String filter) throws RemoteException {
        LOGGER.fine("Fetching versions from project: " + projectKey + " with fixVersion:" + version);
        if (isNotEmpty(filter)) {
            return service.getIssuesFromJqlSearch(token, String.format("project = \"%s\" and fixVersion = \"%s\" and " + filter, projectKey, version), Integer.MAX_VALUE);
        }
        return service.getIssuesFromJqlSearch(token, String.format("project = \"%s\" and fixVersion = \"%s\"", projectKey, version), Integer.MAX_VALUE);
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#getIssueTypes()
     */
    public RemoteIssueType[] getIssueTypes() throws RemoteException {
        LOGGER.fine("Fetching issue types");

        return service.getIssueTypes(token);
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#existsIssue(java.lang.String)
     */
    public boolean existsIssue(String id) throws RemoteException {
        return site.existsIssue(id);
    }


    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#releaseVersion(java.lang.String, hudson.plugins.jira.soap.RemoteVersion)
     */
    public void releaseVersion(String projectKey, RemoteVersion version) throws RemoteException {
        LOGGER.fine("Releaseing version: " + version.getName());

        service.releaseVersion(token, projectKey, version);
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#migrateIssuesToFixVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    public void migrateIssuesToFixVersion(String projectKey, String version, String query) throws RemoteException {

        RemoteVersion newVersion = getVersionByName(projectKey, version);
        if (newVersion == null) {
            return;
        }

        LOGGER.fine("Fetching versions with JQL:" + query);
        RemoteIssue[] issues = service.getIssuesFromJqlSearch(token, query, Integer.MAX_VALUE);
        if (issues == null) {
            return;
        }
        LOGGER.fine("Found issues: " + issues.length);

        RemoteFieldValue value = new RemoteFieldValue("fixVersions", new String[]{newVersion.getId()});
        for (RemoteIssue issue : issues) {
            LOGGER.fine("Migrating issue: " + issue.getKey());
            service.updateIssue(token, issue.getKey(), new RemoteFieldValue[]{value});
        }
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#replaceFixVersion(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void replaceFixVersion(String projectKey, String fromVersion, String toVersion, String query) throws RemoteException {

        RemoteVersion newVersion = getVersionByName(projectKey, toVersion);
        if (newVersion == null) {
            return;
        }

        LOGGER.fine("Fetching versions with JQL:" + query);
        RemoteIssue[] issues = service.getIssuesFromJqlSearch(token, query, Integer.MAX_VALUE);
        if (issues == null) {
            return;
        }
        LOGGER.fine("Found issues: " + issues.length);

        for (RemoteIssue issue : issues) {
            Set<String> newVersions = new HashSet<String>();
            newVersions.add(newVersion.getId());
            for (RemoteVersion currentVersion : issue.getFixVersions()) {
                if (!currentVersion.getName().equals(fromVersion)) {
                    newVersions.add(currentVersion.getId());
                }
            }

            RemoteFieldValue value = new RemoteFieldValue("fixVersions", newVersions.toArray(new String[0]));

            LOGGER.fine("Replaceing version in issue: " + issue.getKey());
            service.updateIssue(token, issue.getKey(), new RemoteFieldValue[]{value});
        }
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#progressWorkflowAction(java.lang.String, java.lang.String, hudson.plugins.jira.soap.RemoteFieldValue[])
     */
    public String progressWorkflowAction(String issueKey, String workflowActionName, RemoteFieldValue[] fields)
            throws RemoteException {
        LOGGER.fine("Progressing issue " + issueKey + " with workflow action: " + workflowActionName);
        RemoteIssue issue = service.progressWorkflowAction(token, issueKey, workflowActionName, fields);
        return getStatusById(issue.getStatus());
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#getActionIdForIssue(java.lang.String, java.lang.String)
     */
    public String getActionIdForIssue(String issueKey, String workflowAction) throws RemoteException {
        RemoteNamedObject[] actions = service.getAvailableActions(token, issueKey);

        if (actions != null) {
            for (RemoteNamedObject action : actions) {
                if (action.getName() != null && action.getName().equalsIgnoreCase(workflowAction)) {
                    return action.getId();
                }
            }
        }

        return null;
    }


    /**
     * Returns the name of a status identified by status id.
     */
    private String getStatusById(String statusId) throws RemoteException {
        String status = getKnownStatuses().get(statusId);

        if (status == null) {
            LOGGER.warning("JIRA status could not be found: " + statusId + ". Checking JIRA for new status types.");
            knownStatuses = null;
            // Try again, just in case the admin has recently added a new status. This should be a rare condition.
            status = getKnownStatuses().get(statusId);
        }

        return status;
    }

    private HashMap<String, String> knownStatuses = null;

    /**
     * Returns all known statuses.
     *
     * @return
     * @throws RemoteException
     */
    private HashMap<String, String> getKnownStatuses() throws RemoteException {
        if (knownStatuses == null) {
            RemoteStatus[] statuses = service.getStatuses(token);
            knownStatuses = new HashMap<String, String>(statuses.length);
            for (RemoteStatus status : statuses) {
                knownStatuses.put(status.getId(), status.getName());
            }
        }
        return knownStatuses;
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#createIssue(java.lang.String, java.lang.String, java.lang.String, hudson.plugins.jira.soap.RemoteComponent[], java.lang.String)
     */
    public RemoteIssue createIssue(String projectKey, String description, String assignee, RemoteComponent[] components, String summary) throws RemoteException {
        RemoteIssue issue = new RemoteIssue();
        issue.setProject(projectKey.toUpperCase());
        issue.setDescription(description);
        issue.setSummary(summary);
        issue.setAssignee(assignee);
        issue.setType("1");
        issue.setComponents(components);
        RemoteIssue createdIssue;
        createdIssue = service.createIssue(token, issue);
        return createdIssue;
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#addCommentWithoutConstrains(java.lang.String, java.lang.String)
     */
    public void addCommentWithoutConstrains(String issueId, String comment) throws RemoteException {
        RemoteComment rc = new RemoteComment();
        rc.setBody(comment);
        service.addComment(token, issueId, rc);
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#getIssueByKey(java.lang.String)
     */
    public RemoteIssue getIssueByKey(String issueId) throws RemoteException {
        return service.getIssue(token, issueId);
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#getComponents(java.lang.String)
     */
    public RemoteComponent[] getComponents(String projectKey) throws RemoteException {
        return service.getComponents(token, projectKey);
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#addVersion(java.lang.String, java.lang.String)
     */
    public RemoteVersion addVersion(String version, String projectKey) throws hudson.plugins.jira.soap.RemoteException, RemoteException {
        RemoteVersion newVersion = new RemoteVersion();
        newVersion.setName(version);
        return service.addVersion(token, projectKey, newVersion);
    }

    /* (non-Javadoc)
     * @see hudson.plugins.jira.remote.JiraInteractionSession#getEmailForUsername(java.lang.String)
     */
    public String getEmailForUsername(String username) throws RemoteException {
        RemoteUser user = service.getUser(token, username);
        if (user != null) {
            String email = user.getEmail();
            if (email != null) {
                return email;
            }
        }
        return null;
    }
}

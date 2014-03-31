package hudson.plugins.jira.remote;

import hudson.plugins.jira.soap.RemoteComponent;
import hudson.plugins.jira.soap.RemoteFieldValue;
import hudson.plugins.jira.soap.RemoteGroup;
import hudson.plugins.jira.soap.RemoteIssue;
import hudson.plugins.jira.soap.RemoteIssueType;
import hudson.plugins.jira.soap.RemoteProjectRole;
import hudson.plugins.jira.soap.RemoteVersion;

import java.rmi.RemoteException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public interface JiraInteractionSession {

    /**
     * @return the email address registered in this JIRA instance for the
     *         supplied username
     */
    String getEmailForUsername(String username) throws RemoteException, InterruptedException, ExecutionException;

    /**
     * Returns the set of project keys (like MNG, JENKINS, etc) that are
     * available in this JIRA. Guarantees to return all project keys in upper
     * case.
     */
    Set<String> getProjectKeys() throws RemoteException;

    /**
     * Adds a comment to the existing issue. Constrains the visibility of the
     * comment the the supplied groupVisibility.
     */
    void addComment(String issueId, String comment, String groupVisibility,
            String roleVisibility) throws RemoteException;

    /**
     * Gets the details of one issue.
     * 
     * @param id
     *            Issue ID like "MNG-1235"
     * @return null if no such issue exists
     */
    RemoteIssue getIssue(String id) throws RemoteException;

    /**
     * Gets all issues that match the given JQL filter.
     */
    RemoteIssue[] getIssuesFromJqlSearch(String jqlSearch)
            throws RemoteException;

    /**
     * Gets the details of a group, given a groupId. Used for validating group
     * visibility.
     * 
     * @param groupId
     *            like "Software Development"
     * @return null if no such group exists
     */
    //FIXME: only used internally in SOAP impl
    RemoteGroup getGroup(String groupId) throws RemoteException;

    /**
     * Gets the details of a role, given a roleId. Used for validating role
     * visibility. TODO: Cannot validate against the real project role the user
     * have in the project, jira soap api has no such function!
     * 
     * @param roleId
     *            like "Software Development"
     * @return null if no such role exists
     */
    //FIXME: only used in internally in SOAP impl
    RemoteProjectRole getRole(String roleId) throws RemoteException;

    /**
     * @return all versions of the specified JIRA project
     */
    RemoteVersion[] getVersions(String projectKey) throws RemoteException;

    /**
     * Gets a version of a JIRA project by its name
     *
     * @return A RemoteVersion, or null if not found
     */
    //FIXME: only used internally in SOAP impl
    RemoteVersion getVersionByName(String projectKey, String name)
            throws RemoteException;

    /**
     * Gets alls issues having the specified project and fixVersion..
     */
    RemoteIssue[] getIssuesWithFixVersion(String projectKey, String version)
            throws RemoteException;

    /**
     * Gets alls issues having the specified project and fixVersion, and
     * matching the specified JQL query.
     */
    RemoteIssue[] getIssuesWithFixVersion(String projectKey, String version,
            String filter) throws RemoteException;

    /**
     * @return all available issue types
     */
    RemoteIssueType[] getIssueTypes() throws RemoteException;

    /**
     * @return <code>true</code> if an issue with the specified key exists,
     *         <code>false</code> otherwise
     */
    boolean existsIssue(String id) throws RemoteException;

    /**
     * Marks the specified version as released.
     */
    void releaseVersion(String projectKey, RemoteVersion version)
            throws RemoteException;

    /**
     * Sets the fixVersion to the specified version for all issues of the
     * specified project and matching the specified JQL query.
     */
    void migrateIssuesToFixVersion(String projectKey, String version,
            String query) throws RemoteException;

    /**
     * Sets the fixVersion to the specified toVersion for all issues of the
     * specified project, with the specified fromVersion and matching the
     * specified JQL query.
     */
    void replaceFixVersion(String projectKey, String fromVersion,
            String toVersion, String query) throws RemoteException;

    /**
     * Progresses the issue's workflow by performing the specified action.
     *
     * @return The new status of the issue
     */
    String progressWorkflowAction(String issueKey, String workflowActionName,
            RemoteFieldValue[] fields) throws RemoteException;

    /**
     * Returns the matching action id for a given action name.
     *
     * @return The action id, or null if the action cannot be found.
     */
    String getActionIdForIssue(String issueKey, String workflowAction)
            throws RemoteException;

    /**
     * Returns the name of a status identified by status id.
     */
    //FIXME: only used internally in SOAP impl
    String getStatusById(String statusId) throws RemoteException;

    /**
     * Creates a new issue with the specified fields and returns it.
     */
    RemoteIssue createIssue(String projectKey, String description,
            String assignee, RemoteComponent[] components, String summary)
                    throws RemoteException;

    /**
     * Adds a comment to the existing issue. There are no constrains to the visibility of the comment.
     */
    void addCommentWithoutConstrains(String issueId, String comment)
            throws RemoteException;

    /**
     * Returns information about the specific issue as identified by the issue id
     */
    RemoteIssue getIssueByKey(String issueId) throws RemoteException;

    /**
     * Returns all the components for the particular project
     */
    RemoteComponent[] getComponents(String projectKey) throws RemoteException;

    /**
     * Creates a new version and returns it
     */
    RemoteVersion addVersion(String version, String projectKey)
            throws hudson.plugins.jira.soap.RemoteException, RemoteException;

}
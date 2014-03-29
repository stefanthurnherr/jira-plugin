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

public interface JiraInteractionSession {

	/**
	 * @param username
	 * @return the email address registered in this JIRA for the supplier
	 *         username
	 */
	String getEmailForUsername(String username) throws RemoteException;
	
	/**
	 * Returns the set of project keys (like MNG, JENKINS, etc) that are
	 * available in this JIRA.
	 * Guarantees to return all project keys in upper case.
	 */
	Set<String> getProjectKeys() throws RemoteException;

	/**
	 * Adds a comment to the existing issue. Constrains the visibility of the
	 * comment the the supplied groupVisibility.
	 *
	 * @param groupVisibility
	 */
	void addComment(String issueId, String comment, String groupVisibility,
			String roleVisibility) throws RemoteException;

	/**
	 * Gets the details of one issue.
	 *
	 * @param id Issue ID like "MNG-1235".
	 * @return null if no such issue exists.
	 */
	RemoteIssue getIssue(String id) throws RemoteException;

	/**
	 * Gets all issues that match the given JQL filter
	 *
	 * @param jqlSearch JQL query string to execute
	 * @return issues matching the JQL query
	 * @throws RemoteException
	 */
	RemoteIssue[] getIssuesFromJqlSearch(String jqlSearch)
			throws RemoteException;

	/**
	 * Gets the details of a group, given a groupId. Used for validating group
	 * visibility.
	 *
	 * @param groupId like "Software Development"
	 * @return null if no such group exists
	 */
	RemoteGroup getGroup(String groupId) throws RemoteException;

	/**
	 * Gets the details of a role, given a roleId. Used for validating role
	 * visibility.
	 * TODO: Cannot validate against the real project role the user have in the project,
	 * jira soap api has no such function!
	 *
	 * @param roleId like "Software Development"
	 * @return null if no such role exists
	 */
	RemoteProjectRole getRole(String roleId) throws RemoteException;

	/**
	 * Get all versions from the given project
	 *
	 * @param projectKey The key for the project
	 * @return An array of versions
	 * @throws RemoteException
	 */
	RemoteVersion[] getVersions(String projectKey) throws RemoteException;

	/**
	 * Get a version by its name
	 *
	 * @param projectKey The key for the project
	 * @param name       The version name
	 * @return A RemoteVersion, or null if not found
	 * @throws RemoteException
	 */
	RemoteVersion getVersionByName(String projectKey, String name)
			throws RemoteException;

	RemoteIssue[] getIssuesWithFixVersion(String projectKey, String version)
			throws RemoteException;

	RemoteIssue[] getIssuesWithFixVersion(String projectKey, String version,
			String filter) throws RemoteException;

	/**
	 * Get all issue types
	 *
	 * @return An array of issue types
	 * @throws RemoteException
	 */
	RemoteIssueType[] getIssueTypes() throws RemoteException;

	boolean existsIssue(String id) throws RemoteException;

	void releaseVersion(String projectKey, RemoteVersion version)
			throws RemoteException;

	/**
	 * Replaces the fix version list of all issues matching the JQL Query with the version specified.
	 *
	 * @param projectKey The JIRA Project key
	 * @param version    The replacement version
	 * @param query      The JQL Query
	 * @throws RemoteException
	 */
	void migrateIssuesToFixVersion(String projectKey, String version,
			String query) throws RemoteException;

	/**
	 * Replaces the given fromVersion with toVersion in all issues matching the JQL query.
	 *
	 * @param projectKey  The JIRA Project
	 * @param fromVersion The name of the version to replace
	 * @param toVersion   The name of the replacement version
	 * @param query       The JQL Query
	 * @throws RemoteException
	 */
	void replaceFixVersion(String projectKey, String fromVersion,
			String toVersion, String query) throws RemoteException;

	/**
	 * Progresses the issue's workflow by performing the specified action. The issue's new status is returned.
	 *
	 * @param issueKey
	 * @param workflowActionName
	 * @param fields
	 * @return The new status
	 * @throws RemoteException
	 */
	String progressWorkflowAction(String issueKey, String workflowActionName,
			RemoteFieldValue[] fields) throws RemoteException;

	/**
	 * Returns the matching action id for a given action name.
	 *
	 * @param issueKey
	 * @param workflowAction
	 * @return The action id, or null if the action cannot be found.
	 * @throws RemoteException
	 */
	String getActionIdForIssue(String issueKey, String workflowAction)
			throws RemoteException;

	/**
	 * Returns the status name by status id.
	 *
	 * @param statusId
	 * @return
	 * @throws RemoteException
	 */
	String getStatusById(String statusId) throws RemoteException;

	/**
	 * Returns issue-id of the created issue
	 *
	 * @param projectKey
	 * @param description
	 * @param assignee
	 * @param components
	 * @param summary
	 * @return The issue id
	 * @throws RemoteException
	 */
	RemoteIssue createIssue(String projectKey, String description,
			String assignee, RemoteComponent[] components, String summary)
			throws RemoteException;

	/**
	 * Adds a comment to the existing issue.There is no constrains to the visibility of the comment.
	 *
	 * @param issueId
	 * @param comment
	 * @throws RemoteException
	 */
	void addCommentWithoutConstrains(String issueId, String comment)
			throws RemoteException;

	/**
	 * Returns information about the specific issue as identified by the issue id
	 *
	 * @param issueId
	 * @return issue object
	 * @throws RemoteException
	 */
	RemoteIssue getIssueByKey(String issueId) throws RemoteException;

	/**
	 * Returns all the components for the particular project
	 *
	 * @param projectKey
	 * @return An array of componets
	 * @throws RemoteException
	 */
	RemoteComponent[] getComponents(String projectKey) throws RemoteException;

	/**
	 * Creates a new version and returns it
	 *
	 * @param version    version id to create
	 * @param projectKey
	 * @return
	 * @throws hudson.plugins.jira.soap.RemoteException
	 *
	 * @throws RemoteException
	 */
	RemoteVersion addVersion(String version, String projectKey)
			throws hudson.plugins.jira.soap.RemoteException, RemoteException;

}
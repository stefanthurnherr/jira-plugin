package hudson.plugins.jira.remote;

import hudson.model.Item;
import hudson.plugins.jira.JiraSite;
import hudson.plugins.jira.remote.soap.JiraSoapSession;
import hudson.util.Secret;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.acegisecurity.Authentication;
import org.apache.commons.lang.StringUtils;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.HostnameRequirement;

public class JiraSessionManager {

    private static final Logger LOGGER = Logger.getLogger(JiraSessionManager.class.getName());

    /**
     * Creates a remote access session to this JIRA.
     *
     * @return null if remote access is not supported.
     * @deprecated please use {@link JiraSite#getSession()} unless you really want a NEW session
     */
    @Deprecated
    public static JiraInteractionSession createSession(JiraSite site, URL url, String username, String password, boolean useHttpAuth) throws IOException, ServiceException {

        UsernamePasswordCredentials credentials = lookupCredentials(url);
        if (credentials != null) {
            LOGGER.info("Trying to create JIRA session using domain-based credentials.");
            return JiraSoapSession.createSession(site, url, credentials.getUsername(), Secret.toString(credentials.getPassword()), useHttpAuth);
        }

        LOGGER.info("No matching credentials found - trying to create JIRA session using good old user/pass.");
        if (username == null || password == null) {
            return null;    // remote access not supported
        }
        return JiraSoapSession.createSession(site, url, username, password, useHttpAuth);
    }

    private static UsernamePasswordCredentials lookupCredentials(URL url) {
        final String jiraDomain = url.getHost();
        final DomainRequirement hostnameRequirement = new HostnameRequirement(jiraDomain);

        final List<UsernamePasswordCredentials> credentials = CredentialsProvider
                .lookupCredentials(UsernamePasswordCredentials.class, (Item)null, (Authentication)null, hostnameRequirement);

        if (!credentials.isEmpty()) {
            if (credentials.size() > 1) {
                List<String> usernames = new LinkedList<String>();
                for (UsernamePasswordCredentials someCredentials : credentials) {
                    usernames.add(someCredentials.getUsername());
                }
                LOGGER.log(Level.WARNING, "Found " + credentials.size() + " credentials matching JIRA url " + url.toExternalForm() + ", using the first one. Found usernames are: " + StringUtils.join(usernames, ", "));
            }
            return credentials.get(0);
        } else {
            LOGGER.log(Level.WARNING, "Found no credentials matching JIRA url " + url.toExternalForm());
            return null;
        }

    }

}

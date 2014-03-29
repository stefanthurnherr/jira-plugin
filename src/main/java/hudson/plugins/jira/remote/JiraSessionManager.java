package hudson.plugins.jira.remote;

import hudson.plugins.jira.remote.soap.JiraSoapSession;

import java.io.IOException;
import java.net.URL;

import javax.xml.rpc.ServiceException;

public class JiraSessionManager {

    /**
     * Creates a remote access session to this JIRA.
     *
     * @return null if remote access is not supported.
     * @deprecated please use {@link #getSession()} unless you really want a NEW session
     */
    @Deprecated
    public static JiraInteractionSession createSession(JiraSite site, URL url, String username, String password, boolean useHttpAuth) throws IOException, ServiceException {
        if (username == null || password == null) {
            return null;    // remote access not supported
        }
        
        return JiraSoapSession.createSession(site, url, username, password, useHttpAuth);
    }
	
}

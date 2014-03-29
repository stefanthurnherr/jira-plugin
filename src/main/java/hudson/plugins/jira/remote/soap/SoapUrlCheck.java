package hudson.plugins.jira.remote.soap;

import hudson.Util;
import hudson.plugins.jira.JiraSite;
import hudson.plugins.jira.Messages;
import hudson.util.FormValidation;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletException;


public class SoapUrlCheck extends hudson.util.FormValidation.URLCheck {

    private static final Logger LOGGER = Logger.getLogger(SoapUrlCheck.class.getName());
	
	private final String urlValue;

	public SoapUrlCheck(@Nullable String urlValue) {
		this.urlValue = urlValue;
	}

	public FormValidation doCheck() throws IOException, ServletException {
		return check();
	}
	
	@Override
	protected FormValidation check() throws IOException, ServletException {
		
		String url = Util.fixEmpty(urlValue);
        if (url == null) {
            return FormValidation.error(Messages
                    .JiraProjectProperty_JiraUrlMandatory());
        }

        // call the wsdl uri to check if the jira soap service can be reached
        try {
            if (!findText(open(new URL(url)), "Atlassian JIRA")) {
                return FormValidation.error(Messages
                        .JiraProjectProperty_NotAJiraUrl());
            }

            URL soapUrl = new URL(new URL(url), "rpc/soap/jirasoapservice-v2?wsdl");
            if (!findText(open(soapUrl), "wsdl:definitions")) {
                return FormValidation.error(Messages
                        .JiraProjectProperty_NoWsdlAvailable());
            }

            return FormValidation.ok();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to connect to " + url, e);
            return handleIOException(url, e);
        }

	}

}

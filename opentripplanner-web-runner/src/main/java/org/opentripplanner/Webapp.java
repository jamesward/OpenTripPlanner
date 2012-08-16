package org.opentripplanner;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.catalina.Context;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.realm.UserDatabaseRealm;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FilenameUtils;

// Based on John Simones' webapp-runner
public class Webapp {

    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();

        String webPort = System.getenv("PORT");
        if(webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        Connector nioConnector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        nioConnector.setPort(Integer.valueOf(webPort));

        tomcat.setConnector(nioConnector);
        tomcat.getService().addConnector(tomcat.getConnector());

        tomcat.setPort(Integer.valueOf(webPort));

        tomcat.setBaseDir(resolveTomcatBaseDir(webPort));

        for (String arg : args) {
            File war = new File(arg);
            String ctxName = "/" + FilenameUtils.removeExtension(war.getName());
            System.out.println("Adding Context " + ctxName + " for " + war.getPath());
            Context ctx = tomcat.addWebapp(ctxName, war.getAbsolutePath());
        }

        tomcat.start();
        tomcat.getServer().await();
    }

    /**
     * Gets or creates temporary Tomcat base directory within target dir
     *
     * @param port port of web process
     * @return absolute dir path
     * @throws IOException if dir fails to be created
     */
    static String resolveTomcatBaseDir(String port) throws IOException {
        final File baseDir = new File(System.getProperty("user.dir") + "/target/tomcat." + port);

        if (!baseDir.isDirectory() && !baseDir.mkdirs()) {
            throw new IOException("Could not create temp dir: " + baseDir);
        }

        try {
            return baseDir.getCanonicalPath();
        } catch (IOException e) {
            return baseDir.getAbsolutePath();
        }
    }
}
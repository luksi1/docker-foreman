#!/usr/bin/env groovy

import groovyx.net.http.*
import org.apache.http.auth.*
import org.apache.http.impl.client.*
import org.apache.http.client.params.*
import org.apache.http.auth.params.*
import groovy.xml.*
import junit.framework.Test
import junit.textui.TestRunner
import org.apache.commons.text.StringEscapeUtils
import groovy.json.JsonSlurper
import static org.junit.matchers.JUnitMatchers.*

/**
Run foreman-rake against the image to reset and grab Foreman's admin password.
We should be able to run commands from the Docker Engine API, but for simplicity, just grab it from the
command line.
*/
public class Foreman {

  private static Foreman instance;

  public String adminPassword;

  private Foreman(){
    (["/usr/bin/docker","ps"].execute().text =~ /(\S+).*foreman:latest.*/).each { 
      fullContainerId, containerId -> 
        (["/usr/bin/docker","exec","-t",containerId,"foreman-rake","permissions:reset"].execute().text =~ /Reset to user: admin, password: (\S+)\s+/).each { 
          full, match -> 
            adminPassword = match
        }
    }
  }
    
  public static Foreman getInstance(){
    if(instance == null){
      instance = new Foreman();
    }
    return instance;
  }
}

class ForemanIT extends GroovyTestCase {

  void testConnectity() {

    def foremanPort = System.getProperty("foremanPort");

    String url = "https://localhost:" + foremanPort
    Foreman f = Foreman.getInstance()
    String user = 'admin'
    String password = f.adminPassword
    String usernamePassword = user + ":" + password
    String base64UsernamePassword = usernamePassword.bytes.encodeBase64().toString()

    HTTPBuilder remote = new HTTPBuilder(url)
    remote.ignoreSSLIssues()
    remote.setHeaders([Authorization: "Basic ${base64UsernamePassword}"])

    remote.request(Method.GET) { req ->
      uri.path = "/api/v2/dashboard"
      response.success = { resp, json ->
        assertEquals((int)resp.status, 200)
        assertEquals((int)json.total_hosts, 0)
      }
    }
  }
}

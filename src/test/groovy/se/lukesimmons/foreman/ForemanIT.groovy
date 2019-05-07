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
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

/**
Run foreman-rake against the image to reset and grab Foreman's admin password.
We should be able to run commands from the Docker Engine API, but for simplicity, just grab it from the
command line.
*/
public class Foreman {

  private static Foreman instance;

  public String adminPassword;

  private Foreman(){
    String processes = ["/usr/bin/sudo","/usr/bin/docker","ps"].execute().text
    println(processes)
    String containerId = ["/usr/bin/sudo","/usr/bin/docker","ps","-aqf","label=org.label-schema.name=foreman"].execute().text
    println(containerId)
    (["/usr/bin/sudo","/usr/bin/docker","exec","-t",containerId,"foreman-rake","permissions:reset"].execute().text =~ /Reset to user: admin, password: (\S+)\s+/).each {
      full, match ->
        adminPassword = match
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

  String getPort() {
    String port = "443";

    // Set to static port if we're testing with docker-compose
    // We are unable to set the port number via the external configuration, and thus
    // unable to dynamically allocate a port and bind it to our system variable
    if(System.getProperty("foremanPort") != null && !System.getProperty("foremanPort").isEmpty()) {
      port = System.getProperty("foremanPort");
    }
    return port;
  }

  void testConnectity() {

    String url = "https://localhost:" + getPort();
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

  void testAddingPuppetSmartProxy() {


    String url = "https://localhost:" + getPort();
    println(url)
    Foreman f = Foreman.getInstance()
    String user = 'admin'
    String password = f.adminPassword
    String usernamePassword = user + ":" + password
    println usernamePassword
    String base64UsernamePassword = usernamePassword.bytes.encodeBase64().toString()

    HTTPBuilder remote = new HTTPBuilder(url)
    remote.ignoreSSLIssues()
    remote.setHeaders([Authorization: "Basic ${base64UsernamePassword}"])

    remote.request(POST) {
      uri.path = "/api/v2/smart_proxies"
      headers.'Accept' = 'application/json'
      requestContentType = ContentType.JSON
      body = ["smart_proxy": ["name": "puppet", "url": "https://puppet-smart-proxy.dummy.test:8443"]]
      response.success = { resp, json ->
        def puppetSmartProxyId  = json.id
        println "id: ${puppetSmartProxyId}"
        assertEquals((int)resp.status, 201)
      }
      response.failure = { resp, json ->
        throw new Exception("Stopping at item POST: uri: " + uri + "\n" +
            "   Unknown error trying to create item: ${resp.status}, not creating Item.")
      }

    }
  }

  /**
  void testDeletePuppetSmartProxy() {

    def foremanPort = System.getProperty("foremanPort");

    String url = "https://localhost:" + foremanPort
    println(url)
    Foreman f = Foreman.getInstance()
    String user = 'admin'
    String password = f.adminPassword
    String usernamePassword = user + ":" + password
    String base64UsernamePassword = usernamePassword.bytes.encodeBase64().toString()

    HTTPBuilder remote = new HTTPBuilder(url)
    remote.ignoreSSLIssues()
    remote.setHeaders([Authorization: "Basic ${base64UsernamePassword}"])

    remote.request(POST) {
      uri.path = "/api/v2/smart_proxies"
      headers.'Accept' = 'application/json'
      requestContentType = ContentType.JSON
      body = ["smart_proxy": ["name": "puppet", "url": "https://puppet-smart-proxy.dummy.test:8443"]]
      response.success = { resp ->
        println "POST response status: ${resp.statusLine}"
        assertEquals((int)resp.status, 201)
      }
      response.failure = { resp, json ->
        throw new Exception("Stopping at item POST: uri: " + uri + "\n" +
            "   Unknown error trying to create item: ${resp.status}, not creating Item.")
      }

    }
  }*/
}

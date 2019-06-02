#!/usr/bin/env groovy

import groovyx.net.http.*;
import org.apache.http.auth.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.params.*;
import org.apache.http.auth.params.*;
import groovy.xml.*;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.apache.commons.text.StringEscapeUtils;
import groovy.json.JsonSlurper;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.*;
import com.spotify.docker.client.LogStream;
import static org.junit.matchers.JUnitMatchers.*;
import static groovyx.net.http.ContentType.*;
import static groovyx.net.http.Method.*;

/**
Run foreman-rake against the image to reset and grab Foreman's admin password.
We should use a Java Docker client for this, but for simplicity, just grab it from the
command line.
*/
public class Foreman {

  private static Foreman instance;

  public String adminPassword;

  private Foreman(){
    String containerId = ["/usr/bin/sudo","/usr/bin/docker","ps","-aqf","label=org.label-schema.name=foreman"].execute().text.trim()
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

  public int getTrustedHostsId(String port) {

    String url = "https://localhost:" + port;
    String user = 'admin'
    String password = adminPassword
    String usernamePassword = user + ":" + password
    String base64UsernamePassword = usernamePassword.bytes.encodeBase64().toString()
    HTTPBuilder remote = new HTTPBuilder(url)
    remote.ignoreSSLIssues()
    remote.setHeaders([Authorization: "Basic ${base64UsernamePassword}"])

    remote.request(Method.GET) { req ->
      uri.path = "/api/v2/settings/?search=trusted_hosts"
      response.success = { resp, json ->
        return json.id
      }
    }
  }
}

public class MyDockerClient {

  DockerClient docker;

  public MyDockerClient() {
    docker = DefaultDockerClient.fromEnv().build();
  }

  public String getNetworkId(String name) {
    def list = docker.listNetworks();
    for (def item: list) {
      if (name.equals(item.name)) {
        return item.id
      }
    }
    throw new RuntimeException(name + "was not a docker network name");
  }

  public String getContainerIdByLabel(String label, String value) {
    def list = docker.listContainers(DockerClient.ListContainersParam.withLabel(label, value));
    if (list.length > 1) {
      throw new RuntimeException("This should have only netted one result, but netted: " + list.length);
    }
    return list[0].id
  }

  public String executeCommand(String containerId, String command) {
    String[] cmd = ["sh","-c",command];
    def exec = docker.execCreate(containerId, 
      cmd,
      DockerClient.ExecCreateParam.attachStdout(),
      DockerClient.ExecCreateParam.attachStderr());

    def execId = exec.id();
    LogStream stream = docker.execStart(execId);
    final String execOutput = stream.readFully();
    return execOutput;
  }
}

public class PuppetAgent {

  public run(String hostname) {

    String networkName = "puppet-foreman-network";
    String image = "puppet/puppet-agent-ubuntu";
    final DockerClient docker = DefaultDockerClient.fromEnv().build();
    MyDockerClient myDockerClient = new MyDockerClient();
    String networkId = myDockerClient.getNetworkId(networkName);

    // get image
    docker.pull(image)

    final ContainerConfig containerConfig = ContainerConfig.builder()
      .hostname(hostname)
      .image(image)
      .build();

    final ContainerCreation creation = docker.createContainer(containerConfig);
    final String id = creation.id();
    docker.connectToNetwork(networkId);
    docker.startContainer(id);
  }
}

class ForemanIT extends GroovyTestCase {

  String puppetSmartProxyId = "1";
  Foreman f = Foreman.getInstance();
  String foremanPassword = f.adminPassword;

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
        puppetSmartProxyId = json.id
        assertEquals((int)resp.status, 201)
        println(puppetSmartProxyId)
      }
      response.failure = { resp, json ->
        println(json)
        throw new Exception("Stopping at item POST: uri: " + uri + "\n" +
          "   Unknown error trying to create item: ${resp.status}, not creating Item.")
      }
    }
  }

  void testAgentRun() {

    PuppetAgent agent = new PuppetAgent();
    def a = agent.run("foo.dummy.test");
    println(a)

  }

  void testDeletePuppetSmartProxy() {

    println(puppetSmartProxyId)
    String url = "https://localhost:" + getPort();
    Foreman f = Foreman.getInstance()
    String user = 'admin'
    String password = f.adminPassword
    String usernamePassword = user + ":" + password
    String base64UsernamePassword = usernamePassword.bytes.encodeBase64().toString()

    HTTPBuilder remote = new HTTPBuilder(url)
    remote.ignoreSSLIssues()
    remote.setHeaders([Authorization: "Basic ${base64UsernamePassword}"])

    remote.request(Method.DELETE) { req ->
      uri.path = "/api/v2/smart_proxies/" + puppetSmartProxyId
      headers.'Accept' = 'application/json'
      requestContentType = ContentType.JSON
      response.success = { resp ->
        println("successfully deleted puppet smartproxy with id:" + puppetSmartProxyId)
        assertEquals((int)resp.status, 200)
      }
      response.failure = { resp, json ->
        println(json)
        throw new Exception("Stopping at item POST: uri: " + uri + "\n" +
            "   Unknown error trying to create item: ${resp.status}, not creating Item.")
      }
    }
  }
}

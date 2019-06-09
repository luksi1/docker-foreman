#!/usr/bin/env groovy

import org.apache.groovy.json.internal.LazyMap;
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
import static org.hamcrest.MatcherAssert.assertThat;

/**
Run foreman-rake against the image to reset and grab Foreman's admin password.
We should use a Java Docker client for this, but for simplicity, just grab it from the
command line.
*/
public class Foreman {

  private static Foreman instance;

  public String url;
  public String user = 'admin';
  public base64UsernamePassword;

  private Foreman(){
    private String adminPassword;
    private String containerId = ["/usr/bin/sudo","/usr/bin/docker","ps","-aqf","label=org.label-schema.name=foreman"].execute().text.trim()
    (["/usr/bin/sudo","/usr/bin/docker","exec","-t",containerId,"foreman-rake","permissions:reset"].execute().text =~ /Reset to user: admin, password: (\S+)\s+/).each {
      full, match ->
        adminPassword = match
    }
    url = "https://localhost:" + Integer.toString(getPort());
    private String usernamePassword = user + ":" + adminPassword
    base64UsernamePassword = usernamePassword.bytes.encodeBase64().toString()
  }
    
  public static Foreman getInstance(){
    if(instance == null){
      instance = new Foreman();
    }
    return instance;
  }

  private int getPort() {
    int port = 443;
    // Set to static port if we're testing with docker-compose
    // We are unable to set the port number via the external configuration, and thus
    // unable to dynamically allocate a port and bind it to our system variable
    if(System.getProperty("foremanPort") != null && !System.getProperty("foremanPort").isEmpty()) {
      port = Integer.parseInt(System.getProperty("foremanPort"));
    }
    return port;
  }

  public int getSettingId(String name) {

    HTTPBuilder remote = new HTTPBuilder(url);
    remote.ignoreSSLIssues();
    remote.setHeaders([Authorization: "Basic ${base64UsernamePassword}"]);

    remote.request(Method.GET) { req ->
      uri.path = "/api/v2/settings"
      uri.query = [search: "${name}"]
      response.success = { resp, json ->
        return json.results.id[0]
      }
      response.failure = { resp, json ->
        println(json)
        throw new Exception("Stopping at item GET: uri: " + uri + "\n" +
          "   Unknown error trying to create item: ${resp.status}, not creating Item.")
      }
    }
  }

  public updateSetting(int id) {

    HTTPBuilder remote = new HTTPBuilder(url);
    remote.ignoreSSLIssues();
    remote.setHeaders([Authorization: "Basic ${base64UsernamePassword}"]);

    remote.request(PUT) {
      uri.path = "/api/v2/settings/" + Integer.toString(id)
      headers.'Accept' = 'application/json'
      requestContentType = ContentType.JSON
      body = ["setting": ["value": "[ puppet ]"]]
      response.success = { resp, json ->
        return resp
      }
      response.failure = { resp, json ->
        println(json)
        throw new Exception("Stopping at item PUT: uri: " + uri + "\n" +
          "   Unknown error trying to create item: ${resp.status}, not creating Item.")
      }
    }
  }

  public LazyMap getSmartProxyByName(String name) {

    HTTPBuilder remote = new HTTPBuilder(url);
    remote.ignoreSSLIssues();
    remote.setHeaders([Authorization: "Basic ${base64UsernamePassword}"]);
    // Wrap name in a regex for searching in the API
    String regExName = "name ~ \"${name}\""

    remote.request(Method.GET) { req ->
      uri.path = "/api/v2/smart_proxies"
      uri.query = [search: regExName]
      response.success = { resp, json ->
        return json
      }
      response.failure = { resp, json ->
        println(json)
        throw new Exception("Stopping at item GET: uri: " + uri + "\n" +
          "   Unknown error trying to create item: ${resp.status}, not creating Item.")
      }
    }
  }

  public LazyMap addSmartProxy(String name, String smartProxyUrl) {

    HTTPBuilder remote = new HTTPBuilder(url);
    remote.ignoreSSLIssues();
    remote.setHeaders([Authorization: "Basic ${base64UsernamePassword}"]);

    remote.request(POST) {
      uri.path = "/api/v2/smart_proxies"
      headers.'Accept' = 'application/json'
      requestContentType = ContentType.JSON
      body = ["smart_proxy": ["name": name, "url": smartProxyUrl]]
      response.success = { resp, json ->
        return json
      }
      response.failure = { resp, json ->
        return json
      }
    }
  }

  public LazyMap deleteSmartProxy(int smartProxyId) {

    HTTPBuilder remote = new HTTPBuilder(url);
    remote.ignoreSSLIssues();
    remote.setHeaders([Authorization: "Basic ${base64UsernamePassword}"]);

    remote.request(Method.DELETE) { req ->
      uri.path = "/api/v2/smart_proxies/" + Integer.toString(smartProxyId)
      headers.'Accept' = 'application/json'
      requestContentType = ContentType.JSON
      response.success = { resp, json ->
        return json
      }
      response.failure = { resp, json ->
        println(json)
        throw new Exception("Stopping at item POST: uri: " + uri + "\n" +
            "   Unknown error trying to create item: ${resp.status}, not creating Item.")
      }
    }
  }

  public LazyMap getDashboardInformation() {

    HTTPBuilder remote = new HTTPBuilder(url)
    remote.ignoreSSLIssues()
    remote.setHeaders([Authorization: "Basic ${base64UsernamePassword}"])

    remote.request(Method.GET) { req ->
      uri.path = "/api/v2/dashboard"
      response.success = { resp, json ->
        return json;
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
    final String containerId = creation.id();
    docker.connectToNetwork(containerId, networkId);
    docker.startContainer(containerId);
    docker.close();
  }
}

class ForemanIT extends GroovyTestCase {

  Foreman f = Foreman.getInstance();

  public void testConnectity() {
    def json = f.getDashboardInformation();
    assertEquals((int)json.reports_missing, 0);
  }

  // This needs to be done or else not trust will be created for puppetserver
  // This should probably be moved to a setup phase
  public void testAddingPuppetServerToTrustedHosts() {
    private int trustedHostsId = f.getSettingId("trusted_hosts");
    def response = f.updateSetting(trustedHostsId);
    assertEquals((int)response.status, 200);
  }

  public void testAddingPuppetSmartProxy() {
    def json = f.addSmartProxy("puppet", "https://puppet-smart-proxy.dummy.test:8443");
    assertEquals("puppet", json.name);
  }

  public void testThatFooDummyTestWasAdded() {
    // before puppet run there should not be any hosts
    def noHosts = f.getDashboardInformation();
    assertEquals((int)noHosts.total_hosts, 0);

    // run puppet from host foo.dummy.test
    PuppetAgent agent = new PuppetAgent();
    agent.run("foo.dummy.test");

    // after puppet run there should one host
    def oneHost = f.getDashboardInformation();
    println(oneHost);
    assertEquals((int)oneHost.total_hosts, 1);
  }
  // No reason to test that we can delete the proxy
  // This was actually added to test locally
  void testDeletePuppetSmartProxy() {
    def proxy = f.getSmartProxyByName("puppet");
    def json = f.deleteSmartProxy(proxy.results[0].id);
    println("ID: " + proxy.results[0].id);
    // assertEquals("puppet", json.results.name);
  }
}

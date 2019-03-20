#!/usr/bin/env groovy

import junit.framework.Test
import junit.textui.TestRunner
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.equalTo
import static org.junit.matchers.JUnitMatchers.*
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET

// Test that PuppetDB is alive and well
// Test that PuppetDB returns some relevent facter information
// Test that master is sending data to PuppetDB
// Test that our puppet agent is sending facter information to the master
class PuppetDbIT extends GroovyTestCase {

  String port = System.getProperty("puppetdbPort");
  String url = 'http://localhost:' + port
  String node = System.getProperty("puppetAgentHostname")
  String allNodes = '/pdb/query/v4/nodes'
  String nodeFacts = allNodes + "/" + node + "/facts/osfamily"

  void testPuppetDbConnectivity() {
    new HTTPBuilder(url + allNodes).request(GET) { req ->
      response.success = { resp ->
        assertEquals((int)resp.status, 200)
      }
    }
  }

  void testPuppetDbCallReturnsCorrectFacterInformation() {
    def http = new HTTPBuilder(url)
    def request = (String)http.get(path : nodeFacts)
    assertThat(request, containsString("certname=" + node))
    assertThat(request, containsString("name=osfamily"))
    assertThat(request, containsString("value=Debian"))
  }
}

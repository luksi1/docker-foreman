#!/usr/bin/env groovy

import junit.framework.Test
import junit.textui.TestRunner
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.junit.matchers.JUnitMatchers.*

// Test to see if our agent's Puppet certificates are created
// This will test connectivity between our agent and the master
class AgentCertificatesIT extends GroovyTestCase {
  
  void testIfCertificatesAreCreated() {
    def filePath = System.getProperty("volumesDir") + "/puppet/ssl/certs/" + System.getProperty("puppetAgentHostname") + ".pem"
    println(filePath)
    def file = new File(filePath)
    assert file.exists() : "file not found"
  }

}

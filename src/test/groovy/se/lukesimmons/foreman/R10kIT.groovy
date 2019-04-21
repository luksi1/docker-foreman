#!/usr/bin/env groovy

import junit.framework.Test
import junit.textui.TestRunner
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.junit.matchers.JUnitMatchers.*

// Test to see if our agent's Puppet certificates are created
// This will test connectivity between our agent and the master
class R10kIT extends GroovyTestCase {
  
  void testIfR10kModulesAreImported() {
    def filePath = System.getProperty("volumesDir") + "/code/environments/production/Puppetfile"
    def file = new File(filePath)
    assert file.exists() : "file not found"
  }

}

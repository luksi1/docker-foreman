#!/usr/bin/env groovy

//@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
import groovyx.net.http.*
import org.apache.http.auth.*
import org.apache.http.impl.client.*
import org.apache.http.client.params.*
import org.apache.http.auth.params.*
import groovy.xml.*
import junit.framework.Test
import junit.textui.TestRunner
import org.apache.commons.text.StringEscapeUtils
import static org.junit.matchers.JUnitMatchers.*

public class Foreman {

  private static Foreman instance;

  public String adminPassword;
    
  private Foreman(){
    def command = '/usr/bin/docker exec -t $(/usr/bin/docker ps | /bin/grep "->443" | /usr/bin/awk \'{print $1)\') foreman-rake permissions:reset | /usr/bin/tail -1 | /usr/bin/awk \'{print $NF}\''
    def proc = commmand.execute()
    proc.waitFor()
    adminPassword = proc.in.text
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

    System.getProperty("foreman.port");

    HTTPBuilder remote = new HTTPBuilder("https://foreman.foobar.test")
    Foreman f = Foreman.getInstance()
    String user = 'admin'
    String password = f.adminPassword

    remote.ignoreSSLIssues()
    remote.setHeaders([Authorization: "Basic ${"${user}:${password}".bytes.encodeBase64().toString()}"])

    remote.request(Method.GET) { req ->
      uri.path = "/api/v2/dashboard"
      response.success = { resp, json ->
        assertEquals((int)resp.status, 200)
        assertEquals((int)json.total_hosts, 0)
      }
    }
  }
}

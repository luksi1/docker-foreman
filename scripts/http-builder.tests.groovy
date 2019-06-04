#!/usr/bin/env groovy

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
@Grab(group='org.hamcrest', module='hamcrest-all', version='1.3')
@Grab(group='com.spotify', module='docker-client', version='8.14.0')
@Grab(group='org.slf4j', module='slf4j-api', version='1.7.26')
@Grab(group='org.slf4j', module='slf4j-log4j12', version='1.7.26')
@Grab(group='org.apache.commons', module='commons-text', version='1.3')
@Grab(group='org.codehaus.groovy', module='groovy-json', version='2.5.6')

import groovyx.net.http.*;
import org.apache.http.auth.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.params.*;
import org.apache.http.auth.params.*;

String url = "https://localhost:443"

String password = 'yMczs2ktGpYa5CcB';
String username = 'admin'
String usernamePassword = username + ":" + password
String base64UsernamePassword = usernamePassword.bytes.encodeBase64().toString()

HTTPBuilder remote = new HTTPBuilder(url)
remote.ignoreSSLIssues()
remote.setHeaders([Authorization: "Basic ${base64UsernamePassword}"])
remote.request(Method.GET) { req ->
  uri.path = '/api/v2/settings'
  uri.
  response.success = { resp, json ->
    println(json.id)
  }
  response.failure = { resp, json ->
    println(json)
    throw new Exception("Stopping at item GET: uri: " + uri + "\n" +
      "   Unknown error trying to create item: ${resp.status}, not creating Item.")
  }
}

#!/usr/bin/groovy

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' )

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import static org.junit.Assert.*
import org.junit.*

String url = "https://localhost:443"
String user = 'admin'
String password = 'vYxuHwS9UhWXxLpE'
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
  response.success = { resp, json ->
    println(json)
    def puppetSmartProxyId  = json.id
    println "id: ${puppetSmartProxyId}"
    println "status: ${resp.status}"
    assertEquals((int)resp.status, 201)
  }
  response.failure = { resp, json ->
    throw new Exception("Stopping at item POST: uri: " + uri + "\n" +
  "   Unknown error trying to create item: ${resp.status}, not creating Item.")
  }
}

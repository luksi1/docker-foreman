
@Grab(group='org.hamcrest',module='hamcrest-all',version='1.3')
//@Grab(group='org.apache.commons',module='commons-text',version='1.3')
//@Grab(group='org.codehaus.groovy.modules',module='groovyws',version='0.5.2')
@Grab(group='org.codehaus.groovy.modules.http-builder',module='http-builder',version='0.7.1')
//@Grab(group='org.codehaus.groovy',module='groovy-json',version='2.5.6')


def password
def foremanPort = "329832986"

(["/usr/bin/docker","ps"].execute().text =~ /(\S+).*foreman:latest.*/).each { fullContainerId, containerId -> (["/usr/bin/docker","exec","-t",containerId,"foreman-rake","permissions:reset"].execute().text =~ /Reset to user: admin, password: (\S+)\s+/).each { full, match -> password = match }
}
println(password)

    String url = "https://localhost:" + foremanPort
    Foreman f = Foreman.getInstance()
    String user = 'admin'
    String password = f.adminPassword
    String base64UsernamePassword = user + ":" + password.bytes.encodeBase64().toString()

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


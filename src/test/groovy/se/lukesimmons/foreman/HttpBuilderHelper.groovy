import groovyx.net.http.*;

public class HttpBuilderHelper {

  public String url;
  public String base64UsernamePassword;

  public HttpBuilderHelper (String url, String base64UsernamePassword) {
    this.url = url;
    this.base64UsernamePassword = base64UsernamePassword;
  }

  public post(String path, requestBody) {
    HTTPBuilder remote = new HTTPBuilder(url)
    remote.ignoreSSLIssues()
    remote.setHeaders([Authorization: "Basic ${base64UsernamePassword}"])

    remote.request(POST) {
      uri.path = path;
      headers.'Accept' = 'application/json'
      requestContentType = ContentType.JSON
      body = requestBody;
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
}

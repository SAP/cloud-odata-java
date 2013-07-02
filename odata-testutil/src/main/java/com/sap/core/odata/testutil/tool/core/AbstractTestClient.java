package com.sap.core.odata.testutil.tool.core;

import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.odata.testutil.TestUtilRuntimeException;
import com.sap.core.odata.testutil.tool.core.TestRequest.RequestHttpMethod;

public abstract class AbstractTestClient {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractTestClient.class);

  protected final DefaultHttpClient httpClient;
  protected final CallerConfig config;
  protected final TestResponseHandler responseHandler;

  AbstractTestClient(final CallerConfig config) {
    this.config = config;
    this.responseHandler = config.getResponseHandler();
    this.httpClient = createHttpClient(config);
  }

  private DefaultHttpClient createHttpClient(CallerConfig config) {
    DefaultHttpClient httpClient = new DefaultHttpClient();
    if (config.isProxySet()) {
      initProxy(httpClient, config);
    }

    if (config.isBasicAuthCredentialsSet()) {
      final URI baseUri = config.getBaseUri();
      httpClient.getCredentialsProvider().setCredentials(
          new AuthScope(baseUri.getHost(), baseUri.getPort()),
          new UsernamePasswordCredentials(config.getBasicAuthCredentials()));
    }
    return httpClient;
  }

  private void initProxy(DefaultHttpClient httpClient, CallerConfig config) throws IllegalArgumentException {
    final String proxyUrl = config.getProxy();
    final String[] hostAndPort = proxyUrl.split(":");
    if (hostAndPort.length != 2) {
      throw new IllegalArgumentException("Unable to parse proxy url. Supported format is 'hostname:port''");
    }
    final String host = hostAndPort[0];
    int port;
    try {
      port = Integer.parseInt(hostAndPort[1]);
    } catch (final NumberFormatException e) {
      throw new IllegalArgumentException("Unable to parse proxy url because port is Not a Number. Supported format is 'hostname:port''", e);
    }
    final HttpHost proxy = new HttpHost(host, port);
    LOG.info("Use proxy '{}:{}'", proxy.getHostName(), proxy.getPort());
    httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
  }

  public void call() {
    call(-1, -1);
  }

  public void callOnce() {
    call(1, 1);
  }

  public void call(int minCallCount) {
    call(minCallCount, -1);
  }

  public void call(int minCallCount, int maxCallCount) {
    if(maxCallCount > 0 && maxCallCount < minCallCount) {
      throw new IllegalArgumentException("MaxCallCount must be bigger then MinCallCount (or negative to be ignored).");
    }

    for (final TestRequest testRequest: config.getTestRequests()) {
      int calls = (minCallCount > testRequest.getCallCount() ? minCallCount : testRequest.getCallCount());
      calls = (maxCallCount < testRequest.getCallCount() ? maxCallCount : testRequest.getCallCount());

      call(testRequest, calls);
    }
  }

  protected void call(TestRequest testRequest, int exactCallCount) {

    for (int i = 0; i < exactCallCount; i++) {
      call(config.getBaseUri(), testRequest);
    }
  }

  private void call(final URI baseUri, final TestRequest testPath) {
    HttpRequestBase request = null;
    try {
      request = createRequest(baseUri, testPath);
      testPath.applyHeaders(request);

      LOG.debug("Call for request line '{}'", request.getRequestLine());

      final HttpHost targetHost = new HttpHost(baseUri.getHost(), baseUri.getPort());
      final HttpResponse response = httpClient.execute(targetHost, request);

      //
      handleSuccessCall(baseUri, testPath, request, response);
    } catch (Exception e) {
      handleFailureCall(testPath, request, e);
    } finally {
      if (request != null) {
        request.releaseConnection();
      }
    }
  }

  private void handleFailureCall(final TestRequest testPath, HttpRequestBase request, Exception e) {
    if(responseHandler != null) {
      responseHandler.handle(testPath, request, e);
    }
    LOG.debug("Got exception for calling TestPath '" + testPath + "'.");
  }

  private void handleSuccessCall(final URI baseUri, final TestRequest testRequest, HttpRequestBase request, final HttpResponse response) {
    if(responseHandler != null) {
      responseHandler.handle(baseUri, testRequest, request, response);
    } else {
      LOG.trace("Successfull call for calling TestRequest '{}'.", testRequest);      
    }
  }

  private HttpRequestBase createRequest(final URI baseUri, final TestRequest testRequest) {
    RequestHttpMethod method = testRequest.getHttpMethod();
    if(method == null) {
      throw new TestUtilRuntimeException("No HttpMethod set.");
    }
    
    switch (method) {
    case GET: return new HttpGet(baseUri.getPath() + testRequest.getPath());
    case POST: 
      TestPostRequest postRequest = (TestPostRequest) testRequest;
      HttpPost post = new HttpPost(baseUri.getPath() + testRequest.getPath());
      post.setEntity(new InputStreamEntity(postRequest.getContentAsStream(), -1, ContentType.create(postRequest.getContentType())));
      return post;
    default:
      throw new TestUtilRuntimeException("Unknown HttpMethod '" + method + "' set.");
    }
  }
}

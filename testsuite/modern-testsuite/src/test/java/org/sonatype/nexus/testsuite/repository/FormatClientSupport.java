package org.sonatype.nexus.testsuite.repository;

import java.io.IOException;
import java.net.URI;

import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
public class FormatClientSupport
    extends ComponentSupport
{
  protected final HttpClient httpClient;

  protected final HttpClientContext httpClientContext;

  protected final URI repositoryBaseUri;

  public FormatClientSupport(final HttpClient httpClient, final HttpClientContext httpClientContext,
                             final URI repositoryBaseUri)
  {
    this.httpClient = checkNotNull(httpClient);
    this.httpClientContext = checkNotNull(httpClientContext);
    this.repositoryBaseUri = checkNotNull(repositoryBaseUri);
  }

  public static String asString(final HttpResponse response) throws IOException {
    final String asString = EntityUtils.toString(response.getEntity());
    return asString;
  }

  /**
   * GET a response from the repository.
   */
  public HttpResponse get(final String path) throws IOException {
    final URI uri = resolve(path);
    final HttpGet get = new HttpGet(uri);
    return execute(get);
  }


  protected HttpResponse execute(final HttpUriRequest request) throws IOException {
    log.info("Requesting {}", request);
    final HttpResponse response = httpClient.execute(request, httpClientContext);
    log.info("Received {}", response);
    return response;
  }

  @NotNull
  protected URI resolve(final String path) {
    return repositoryBaseUri.resolve(path);
  }

  public static int status(HttpResponse response) {
    checkNotNull(response);
    return response.getStatusLine().getStatusCode();
  }
}

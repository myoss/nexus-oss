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

  protected String asString(final HttpResponse response) throws IOException {
    assert response.getStatusLine().getStatusCode() == HttpStatus.OK;
    final String asString = EntityUtils.toString(response.getEntity());

    String synopsis = asString.substring(0, Math.min(asString.length(), 60));
    synopsis = synopsis.replaceAll("\\n", "");
    log.info("Received {}", synopsis);

    return asString;
  }

  /**
   * GET a response from the repository.
   */
  public HttpResponse get(final String path) throws IOException {
    final HttpGet get = new HttpGet(repositoryBaseUri.resolve(path));
    return execute(get);
  }

  protected HttpResponse execute(final HttpUriRequest request) throws IOException {
    log.info("Nuget client requesting {}", request);
    final HttpResponse response = httpClient.execute(request, httpClientContext);
    log.info("Nuget client received {}", response);
    return response;
  }
}

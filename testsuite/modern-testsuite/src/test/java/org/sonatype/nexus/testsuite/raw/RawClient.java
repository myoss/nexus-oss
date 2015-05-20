
package org.sonatype.nexus.testsuite.raw;

import java.io.File;
import java.net.URI;

import org.sonatype.nexus.testsuite.repository.FormatClientSupport;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.util.EntityUtils;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;


/**
 * A simple test client for Raw repositories.
 */
public class RawClient
    extends FormatClientSupport
{
  public RawClient(final HttpClient httpClient,
                   final HttpClientContext httpClientContext,
                   final URI repositoryBaseUri)
  {
    super(httpClient, httpClientContext, repositoryBaseUri);
  }

  public int put(final String path, final File file) throws Exception {
    checkNotNull(path);
    checkNotNull(file);

    HttpPut put = new HttpPut(repositoryBaseUri.resolve(path));
    put.setEntity(EntityBuilder.create().setFile(file).build());

    final HttpResponse response = this.execute(put);
    return response.getStatusLine().getStatusCode();
  }

  public byte[] getBytes(final String path) throws Exception {
    final HttpResponse httpResponse = get(path);
    checkState(httpResponse.getEntity() != null);

    final byte[] bytes = EntityUtils.toByteArray(httpResponse.getEntity());
    return bytes;
  }

  public int delete(final String path) throws Exception {
    final HttpResponse response = execute(new HttpDelete(resolve(path)));
    return response.getStatusLine().getStatusCode();
  }
}

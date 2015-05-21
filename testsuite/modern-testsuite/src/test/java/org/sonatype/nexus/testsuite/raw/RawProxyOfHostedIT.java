package org.sonatype.nexus.testsuite.raw;

import java.io.File;
import java.net.URL;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.http.HttpStatus;

import com.google.common.io.Files;
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * IT for proxy raw repositories
 */
@ExamReactorStrategy(PerClass.class)
public class RawProxyOfHostedIT
    extends RawITSupport
{
  public static final String TEST_PATH = "alphabet.txt";

  public static final String TEST_CONTENT = "alphabet.txt";

  private RawClient hostedClient;

  private RawClient proxyClient;

  private Repository hostedRepo;

  @Before
  public void setUpRepositories() throws Exception {
    hostedRepo = createRepository(hostedConfig("raw-test-hosted"));
    hostedClient = client(hostedRepo);

    URL hostedRepoUrl = repositoryBaseUrl(hostedRepo);
    final Configuration proxyConfig = proxyConfig("raw-test-proxy", hostedRepoUrl.toExternalForm());
    proxyClient = client(createRepository(proxyConfig));
  }

  @Test
  public void unresponsiveRemoteProduces404() throws Exception {
    deleteRepository(hostedRepo);

    final HttpResponse httpResponse = proxyClient.get(TEST_PATH);
    assertThat(httpResponse.getStatusLine().getStatusCode(), is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void responsiveRemoteProduces404() throws Exception {
    final HttpResponse httpResponse = proxyClient.get(TEST_PATH);
    assertThat(httpResponse.getStatusLine().getStatusCode(), is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void fetchFromRemote() throws Exception {
    final File testFile = resolveTestFile(TEST_CONTENT);
    hostedClient.put(TEST_PATH, testFile);

    final byte[] bytes = proxyClient.getBytes(TEST_PATH);
    assertThat(bytes, is(Files.toByteArray(testFile)));
  }

  @Test
  public void notFoundCaches404() throws Exception {
    // Ask for a nonexistent file
    proxyClient.get(TEST_PATH);

    // Put the file in the hosted repo
    hostedClient.put(TEST_PATH, resolveTestFile(TEST_CONTENT));

    // The NFC should ensure we still see the 404
    final HttpResponse httpResponse = proxyClient.get(TEST_PATH);
    assertThat(httpResponse.getStatusLine().getStatusCode(), is(HttpStatus.NOT_FOUND));
  }
}

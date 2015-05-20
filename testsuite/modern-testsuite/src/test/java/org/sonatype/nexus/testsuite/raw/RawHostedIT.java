package org.sonatype.nexus.testsuite.raw;

import java.io.File;
import java.net.URL;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.raw.internal.RawHostedRecipe;
import org.sonatype.nexus.repository.storage.WritePolicy;
import org.sonatype.nexus.testsuite.repository.RepositoryTestSupport;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.http.HttpResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * IT for hosted raw repositories
 */
public class RawHostedIT
    extends RepositoryTestSupport
{
  public static final String HOSTED_REPO = "raw-test-hosted";

  private RawClient rawClient;

  @Before
  public void createHostedRepository() throws Exception {
    final Configuration config = hostedConfig(HOSTED_REPO);
    final Repository repository = createRepository(config);
    URL hostedRepoUrl = this.repositoryBaseUrl(repository);
    rawClient = new RawClient(clientBuilder().build(), clientContext(), hostedRepoUrl.toURI());
  }

  @Test
  public void uploadAndDownload() throws Exception {
    final String path = "alphabet.txt";

    final File testFile = resolveTestFile(path);
    final int response = rawClient.put(path, testFile);
    assertThat(response, is(HttpStatus.CREATED));

    final byte[] bytes = rawClient.getBytes(path);

    assertThat(bytes, is(Files.toByteArray(testFile)));

    int deleteStatus = rawClient.delete(path);
    assertThat(deleteStatus, is(HttpStatus.NO_CONTENT));

    final HttpResponse httpResponse = rawClient.get(path);
    assertThat("content should be deleted", httpResponse.getStatusLine().getStatusCode(), is(HttpStatus.NOT_FOUND));
  }

  @NotNull
  protected Configuration hostedConfig(final String name) {
    final Configuration config = new Configuration();
    config.setRepositoryName(name);
    config.setRecipeName(RawHostedRecipe.NAME);
    config.setOnline(true);
    config.attributes("storage").set("writePolicy", WritePolicy.ALLOW.toString());
    return config;
  }
}

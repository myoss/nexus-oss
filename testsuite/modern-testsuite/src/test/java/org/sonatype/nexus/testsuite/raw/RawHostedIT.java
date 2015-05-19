package org.sonatype.nexus.testsuite.raw;

import java.io.File;
import java.net.URL;

import com.sonatype.nexus.repository.nuget.internal.NugetHostedRecipe;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.raw.internal.RawHostedRecipe;
import org.sonatype.nexus.repository.storage.WritePolicy;
import org.sonatype.nexus.testsuite.repository.RepositoryTestSupport;

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

  private URL hostedRepoUrl;

  private RawClient rawClient;

  @Before
  public void createHostedRepository() throws Exception {
    final Configuration config = hostedConfig(HOSTED_REPO);
    final Repository repository = createRepository(config);
    hostedRepoUrl = this.repositoryBaseUrl(repository);
    rawClient = new RawClient(clientBuilder().build(), clientContext(), hostedRepoUrl.toURI());
  }

  @Test
  public void uploadAndDownload() throws Exception {
    final File testFile = resolveTestFile("alphabet.txt");
    final int response = rawClient.put("alphabet.txt", testFile);
    assertThat(response, is(HttpStatus.CREATED));

    final byte[] bytes = rawClient.getBytes("upload.txt");

    assertThat(bytes, is(Files.toByteArray(testFile)));

    int deleteStatus = rawClient.delete("alphabet.txt");
    assertThat(deleteStatus, is(HttpStatus.NO_CONTENT));

    final HttpResponse httpResponse = rawClient.get("upload.txt");
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

package org.sonatype.nexus.testsuite.raw;

import java.io.File;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.raw.internal.RawHostedRecipe;
import org.sonatype.nexus.repository.storage.WritePolicy;

import com.google.common.io.Files;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.status;

/**
 * IT for hosted raw repositories
 */
public class RawHostedIT
    extends RawITSupport
{
  public static final String HOSTED_REPO = "raw-test-hosted";

  private RawClient rawClient;

  @Before
  public void createHostedRepository() throws Exception {
    final Configuration config = hostedConfig(HOSTED_REPO);
    final Repository repository = createRepository(config);
    rawClient = client(repository);
  }

  @Test
  public void uploadAndDownload() throws Exception {
    final String path = "alphabet.txt";

    final File testFile = resolveTestFile(path);
    final int response = rawClient.put(path, testFile);
    assertThat(response, is(HttpStatus.CREATED));

    final byte[] bytes = rawClient.getBytes(path);

    assertThat(bytes, is(Files.toByteArray(testFile)));

    assertThat(status(rawClient.delete(path)), is(HttpStatus.NO_CONTENT));

    assertThat("content should be deleted", status(rawClient.get(path)), is(HttpStatus.NOT_FOUND));
  }
}

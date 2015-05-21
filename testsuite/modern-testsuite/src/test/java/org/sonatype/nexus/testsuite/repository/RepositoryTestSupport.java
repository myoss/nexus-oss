package org.sonatype.nexus.testsuite.repository;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.testsuite.NexusHttpsITSupport;

import org.jetbrains.annotations.NotNull;
import org.junit.After;

/**
 * Support class for repository format ITs.
 */
public abstract class RepositoryTestSupport
    extends NexusHttpsITSupport
{
  private List<Repository> repositories = new ArrayList<>();

  @Inject
  private RepositoryManager repositoryManager;

  /**
   * Creates a repository, first removing an existing one if necessary.
   */
  protected Repository createRepository(final Configuration config) throws Exception {
    waitFor(responseFrom(nexusUrl));
    final Repository repository = repositoryManager.create(config);
    repositories.add(repository);
    calmPeriod();
    return repository;
  }

  public void deleteRepository(Repository repository) throws Exception {
    repositories.remove(repository);
    repositoryManager.delete(repository.getName());
  }

  @After
  public void deleteRepositories() throws Exception {
    for (Repository repository : repositories) {
      repositoryManager.delete(repository.getName());
    }
  }

  @NotNull
  protected URL repositoryBaseUrl(final Repository repository) {
    return resolveUrl(nexusUrl, "/repository/" + repository.getName() + "/");
  }
}

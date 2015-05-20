package org.sonatype.nexus.testsuite.raw;

import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.raw.internal.RawHostedRecipe;
import org.sonatype.nexus.repository.raw.internal.RawProxyRecipe;
import org.sonatype.nexus.repository.storage.WritePolicy;
import org.sonatype.nexus.testsuite.repository.RepositoryTestSupport;

import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support class for raw ITs.
 */
public class RawITSupport
    extends RepositoryTestSupport
{
  @NotNull
  protected Configuration hostedConfig(final String name) {
    final Configuration config = new Configuration();
    config.setRepositoryName(name);
    config.setRecipeName(RawHostedRecipe.NAME);
    config.setOnline(true);
    config.attributes("storage").set("writePolicy", WritePolicy.ALLOW.toString());
    return config;
  }

  @NotNull
  protected Configuration proxyConfig(final String name, final String remoteUrl) {
    final Configuration config = new Configuration();
    config.setRepositoryName(name);
    config.setRecipeName(RawProxyRecipe.NAME);
    config.setOnline(true);

    final NestedAttributesMap proxy = config.attributes("proxy");
    proxy.set("remoteUrl", remoteUrl);
    proxy.set("artifactMaxAge", 5);

    final NestedAttributesMap negativeCache = config.attributes("negativeCache");
    negativeCache.set("enabled", true);
    negativeCache.set("timeToLive", new Integer(100000));

    return config;
  }

  @NotNull
  protected RawClient client(final Repository repository) throws Exception {
    checkNotNull(repository);
    return new RawClient(clientBuilder().build(),
        clientContext(),
        repositoryBaseUrl(repository).toURI());
  }
}

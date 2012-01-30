/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.internal.condition;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.nexus.plugins.capabilities.support.condition.ConditionSupport;
import org.sonatype.nexus.plugins.capabilities.support.condition.RepositoryConditions;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Support class for repository conditions.
 *
 * @since 2.0
 */
public abstract class RepositoryConditionSupport
    extends ConditionSupport
{

    private final RepositoryRegistry repositoryRegistry;

    private final RepositoryConditions.RepositoryId repositoryId;

    private final ReentrantReadWriteLock bindLock;

    public RepositoryConditionSupport( final EventBus eventBus,
                                       final RepositoryRegistry repositoryRegistry,
                                       final RepositoryConditions.RepositoryId repositoryId )
    {
        super( eventBus, false );
        this.repositoryRegistry = checkNotNull( repositoryRegistry );
        this.repositoryId = checkNotNull( repositoryId );
        bindLock = new ReentrantReadWriteLock();
    }

    @Override
    protected void doBind()
    {
        try
        {
            bindLock.writeLock().lock();
            for ( final Repository repository : repositoryRegistry.getRepositories() )
            {
                handle( new RepositoryRegistryEventAdd( repositoryRegistry, repository ) );
            }
        }
        finally
        {
            bindLock.writeLock().unlock();
        }
        getEventBus().register( this );
    }

    @Override
    public void doRelease()
    {
        getEventBus().unregister( this );
    }

    public abstract void handle( final RepositoryRegistryEventAdd event );

    @Override
    protected void setSatisfied( final boolean satisfied )
    {
        try
        {
            bindLock.readLock().lock();
            super.setSatisfied( satisfied );
        }
        finally
        {
            bindLock.readLock().unlock();
        }
    }

    /**
     * Checks that condition is about the passed in repository id.
     *
     * @param repositoryId to check
     * @return true, if condition repository matches the specified repository id
     */
    protected boolean sameRepositoryAs( final String repositoryId )
    {
        return repositoryId != null && repositoryId.equals( getRepositoryId() );
    }

    protected String getRepositoryId()
    {
        return repositoryId.get();
    }

}

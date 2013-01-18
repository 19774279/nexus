/*
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
package org.sonatype.nexus.proxy.maven.wl.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;
import org.sonatype.nexus.proxy.maven.wl.discovery.DiscoveryResult;
import org.sonatype.nexus.proxy.maven.wl.discovery.RemoteContentDiscoverer;
import org.sonatype.nexus.proxy.maven.wl.discovery.RemoteStrategy;
import org.sonatype.nexus.proxy.maven.wl.discovery.Strategy.StrategyPriorityOrderingComparator;
import org.sonatype.nexus.proxy.maven.wl.discovery.StrategyFailedException;

@Named
@Singleton
public class RemoteContentDiscovererImpl
    implements RemoteContentDiscoverer
{
    private final List<RemoteStrategy> remoteStrategies;

    @Inject
    public RemoteContentDiscovererImpl( final List<RemoteStrategy> remoteStrategies )
    {
        this.remoteStrategies = checkNotNull( remoteStrategies );
    }

    @Override
    public DiscoveryResult discoverRemoteContent( final MavenProxyRepository mavenProxyRepository )
        throws IOException
    {
        final ArrayList<RemoteStrategy> appliedStrategies = new ArrayList<RemoteStrategy>( remoteStrategies );
        Collections.sort( appliedStrategies, new StrategyPriorityOrderingComparator<RemoteStrategy>() );
        final DiscoveryResult discoveryResult = new DiscoveryResult( mavenProxyRepository );
        for ( RemoteStrategy remoteStrategy : appliedStrategies )
        {
            discoverRemoteContentWithStrategy( remoteStrategy, mavenProxyRepository, discoveryResult );
            if ( discoveryResult.isSuccessful() )
            {
                break;
            }
        }
        return discoveryResult;
    }

    // ==

    protected void discoverRemoteContentWithStrategy( final RemoteStrategy strategy,
                                                      final MavenProxyRepository mavenProxyRepository,
                                                      final DiscoveryResult discoveryResult )
        throws IOException
    {
        try
        {
            final EntrySource entrySource = strategy.discover( mavenProxyRepository );
            discoveryResult.recordSuccess( strategy, entrySource );
        }
        catch ( StrategyFailedException e )
        {
            discoveryResult.recordFailure( strategy, e );
        }
    }
}
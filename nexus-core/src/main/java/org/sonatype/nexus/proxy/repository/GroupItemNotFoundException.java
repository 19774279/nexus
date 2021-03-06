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
package org.sonatype.nexus.proxy.repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;

/**
 * Thrown by the {@link GroupRepository#retrieveItem(ResourceStoreRequest)},
 * {@link GroupRepository#retrieveItem(boolean, ResourceStoreRequest)} and
 * {@link GroupRepository#doRetrieveItems(ResourceStoreRequest)} methods only, when all the members and group repository
 * itself failed to retrieve item corresponding to request in non-hard-fail (ie. some internal error or some other
 * condition that stops group processing immediately) way.
 * 
 * @author cstamas
 * @since 2.1
 */
public class GroupItemNotFoundException
    extends ItemNotFoundException
{
    private static final long serialVersionUID = -863009398540333419L;

    private final Map<Repository, Throwable> memberReasons;

    /**
     * Constructor for group thrown "not found" exception providing information about whole tree being processed and
     * reasons why the grand total result is "not found.
     * 
     * @param request
     * @param repository
     */
    public GroupItemNotFoundException( final ResourceStoreRequest request, final GroupRepository repository,
                                       final Map<Repository, Throwable> memberReasons )
    {
        super( request, repository );
        // copy it and make it unmodifiable
        this.memberReasons = Collections.unmodifiableMap( new HashMap<Repository, Throwable>( memberReasons ) );
    }

    @Override
    public GroupRepository getRepository()
    {
        return (GroupRepository) super.getRepository();
    }

    public Map<Repository, Throwable> getMemberReasons()
    {
        return memberReasons;
    }
}

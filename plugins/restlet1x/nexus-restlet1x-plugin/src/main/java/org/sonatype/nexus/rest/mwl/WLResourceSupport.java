package org.sonatype.nexus.rest.mwl;

import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.wl.WLManager;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.WLConfigMessage;
import org.sonatype.nexus.rest.model.WLConfigMessageWrapper;
import org.sonatype.nexus.rest.model.WLDiscoveryStatusMessage;
import org.sonatype.nexus.rest.model.WLStatusMessage;
import org.sonatype.nexus.rest.model.WLStatusMessageWrapper;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

import com.thoughtworks.xstream.XStream;

/**
 * WL REST resource support.
 * 
 * @author cstamas
 * @since 2.4
 */
public abstract class WLResourceSupport
    extends AbstractNexusPlexusResource
{
    protected static final String REPOSITORY_ID_KEY = "repositoryId";

    @Requirement
    private WLManager wlManager;

    /**
     * Constructor needed to set resource modifiable.
     */
    public WLResourceSupport()
    {
        setModifiable( true );
    }

    protected WLManager getWLManager()
    {
        return wlManager;
    }

    @Override
    public void configureXStream( final XStream xstream )
    {
        xstream.processAnnotations( WLDiscoveryStatusMessage.class );
        xstream.processAnnotations( WLStatusMessage.class );
        xstream.processAnnotations( WLStatusMessageWrapper.class );
        xstream.processAnnotations( WLConfigMessage.class );
        xstream.processAnnotations( WLConfigMessageWrapper.class );
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repositories/*", "authcBasic,perms[nexus:repositories]" );
    }

    /**
     * Returns properly adapted {@link MavenRepository} instance, or handles cases like not exists or not having
     * required type (kind in Nx lingo).
     * 
     * @param request
     * @param clazz
     * @return
     * @throws ResourceException
     */
    protected <T extends MavenRepository> T getMavenRepository( final Request request, Class<T> clazz )
        throws ResourceException
    {
        final String repositoryId = request.getAttributes().get( REPOSITORY_ID_KEY ).toString();
        try
        {
            final Repository repository = getRepositoryRegistry().getRepository( repositoryId );
            final T mavenRepository = repository.adaptToFacet( clazz );
            if ( mavenRepository != null )
            {
                return mavenRepository;
            }
            else
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Repository with ID=\"" + repositoryId
                    + "\" is not a required type of " + clazz.getSimpleName() + "." );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "No repository with ID=\"" + repositoryId
                + "\" found.", e );
        }
    }
}
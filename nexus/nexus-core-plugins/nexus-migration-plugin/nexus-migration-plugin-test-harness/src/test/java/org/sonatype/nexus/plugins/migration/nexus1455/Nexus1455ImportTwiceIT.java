/**
 * Copyright (c) 2008-2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.migration.nexus1455;

import java.io.FileReader;

import org.codehaus.plexus.util.IOUtil;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;

public class Nexus1455ImportTwiceIT
    extends AbstractMigrationIntegrationTest
{
    @Test
    public void importTwice()
        throws Exception
    {
        if ( true )
        {
            super.printKnownErrorButDoNotFail( getClass(), "importTwice" );
            return;
        }

        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        Assert.assertNotNull( migrationSummary.getId() );
        commitMigration( migrationSummary );
        commitMigration( migrationSummary );

        Assert.assertTrue( "Migration log file not found", migrationLogFile.isFile() );

        String log = IOUtil.toString( new FileReader( migrationLogFile ) );
        Assert.assertTrue( "Didn't skip second migration " + log,
                           log.contains( "Trying to import the same package twice" ) );
        Assert.assertFalse( "Error during migration", log.toLowerCase().contains( "error" ) );
    }

}

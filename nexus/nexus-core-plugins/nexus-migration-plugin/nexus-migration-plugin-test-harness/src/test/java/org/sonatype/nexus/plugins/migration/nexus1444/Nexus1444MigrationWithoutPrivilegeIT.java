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
package org.sonatype.nexus.plugins.migration.nexus1444;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.plugins.migration.AbstractMigrationPrivilegeTest;

public class Nexus1444MigrationWithoutPrivilegeIT
    extends AbstractMigrationPrivilegeTest
{

    @Test
    public void doMigrationWithoutPermission()
        throws Exception
    {
        removePrivilege( TEST_USER_NAME, ARTIFACTORY_MIGRATOR );

        try
        {
            doMigration();
        }
        catch ( Exception e )
        {
            Assert.assertTrue( "Exception message should contain 401 error:\n" + e.getStackTrace(), e
                .getMessage().contains( "401" ) );
        }
    }

    @Override
    protected File getBackupFile()
    {
        return getTestFile( "artifactory125.zip" );
    }
}

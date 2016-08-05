/*
 * Copyright 2013 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.objectify.deadbolt.java.test.controllers;

import org.dbunit.DefaultDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import play.Application;
import play.db.Database;
import play.utils.PlayIO;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.InputStream;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class DataLoader
{

    @Inject
    public DataLoader(final Database database,
                      final Provider<Application> application)
    {
        database.withConnection(conn ->
                                {
                                    InputStream inputStream = null;
                                    try
                                    {
                                        inputStream = application.get().resourceAsStream("/be/objectify/deadbolt/java/test/standard.xml");
                                        final FlatXmlDataSet xmlDataSet = new FlatXmlDataSetBuilder().build(inputStream);
                                        IDatabaseTester dbTester = new DefaultDatabaseTester(new DatabaseConnection(conn));
                                        dbTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
                                        dbTester.setDataSet(xmlDataSet);
                                        dbTester.onSetup();
                                    }
                                    catch (Exception e)
                                    {
                                        throw new RuntimeException("Could not initialise database",
                                                                   e);
                                    }
                                    finally
                                    {
                                        PlayIO.closeQuietly(inputStream);
                                    }
                                });
    }
}

package be.objectify.deadbolt.java.test.controllers;

import java.io.InputStream;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.dbunit.DefaultDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import play.Application;
import play.db.Database;
import play.utils.PlayIO;

/**
 * @author Steve Chaloner (SCHA)
 */
@Singleton
public class DataLoader {

    @Inject
    public DataLoader(final Database database,
                      final Provider<Application> application) {
            database.withConnection(conn -> {
                InputStream inputStream =  null;
                try {
                    inputStream = application.get().resourceAsStream("/be/objectify/deadbolt/java/test/standard.xml");
                    final FlatXmlDataSet xmlDataSet = new FlatXmlDataSetBuilder().build(inputStream);
                    IDatabaseTester dbTester = new DefaultDatabaseTester(new DatabaseConnection(conn));
                    dbTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
                    dbTester.setDataSet(xmlDataSet);
                    dbTester.onSetup();
                } catch (Exception e) {
                    throw new RuntimeException("Could not initialise database",
                                               e);
                } finally {
                    PlayIO.closeQuietly(inputStream);
                }
            });
    }
}

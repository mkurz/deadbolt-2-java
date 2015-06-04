package be.objectify.deadbolt.java.test;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Application;
import play.Configuration;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DataLoader extends Global
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DataLoader.class);

    private final String dataFile;

    public DataLoader(String dataFile)
    {
        this.dataFile = dataFile;
    }

    @Override
    public void onStart(Application application)
    {
        super.onStart(application);

        try
        {
            final Configuration configuration = application.configuration();
            final FlatXmlDataSet xmlDataSet = new FlatXmlDataSetBuilder().build(DataLoader.class.getResourceAsStream(dataFile));
            IDatabaseTester dbTester = new JdbcDatabaseTester(configuration.getString("db.default.driver"),
                                                              configuration.getString("db.default.url"),
                                                              configuration.getString("db.default.user"),
                                                              configuration.getString("db.default.password"));
            dbTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
            dbTester.setDataSet(xmlDataSet);
            dbTester.onSetup();
        }
        catch (Exception e)
        {
            LOGGER.error("Could not load database",
                         e);
        }
    }
}

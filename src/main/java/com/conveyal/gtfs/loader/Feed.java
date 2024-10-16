package com.conveyal.gtfs.loader;

import com.conveyal.gtfs.error.NewGTFSError;
import com.conveyal.gtfs.error.SQLErrorStorage;
import com.conveyal.gtfs.model.*;
import com.conveyal.gtfs.storage.StorageException;
import com.conveyal.gtfs.util.InvalidNamespaceException;
import com.conveyal.gtfs.validator.*;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.conveyal.gtfs.error.NewGTFSErrorType.VALIDATOR_FAILED;

/**
 * This connects to an SQL RDBMS containing GTFS data and lets you fetch elements out of it.
 */
public class Feed {

    private static final Logger LOG = LoggerFactory.getLogger(Feed.class);

    private final DataSource dataSource;

    // The unique database schema name for this particular feed, including the separator character (dot).
    // This may be the empty string if the feed is stored in the root ("public") schema.
    private final String databaseSchemaPrefix;

    public final TableReader<Agency>        agencies;
    public final TableReader<Calendar>      calendars;
    public final TableReader<CalendarDate>  calendarDates;
    public final TableReader<FareAttribute> fareAttributes;
    public final TableReader<Frequency>     frequencies;
    public final TableReader<Route>         routes;
    public final TableReader<Stop>          stops;
    public final TableReader<Trip>          trips;
    public final TableReader<StopTime>      stopTimes;
    public final TableReader<Pattern>       patterns;

    /**
     * Create a feed that reads tables over a JDBC connection. The connection should already be set to the right
     * schema within the database.
     * @param databaseSchemaPrefix the unique prefix for the table (may be null for no prefix)
     */
    public Feed (DataSource dataSource, String databaseSchemaPrefix) {
        this.dataSource = dataSource;
        // Ensure separator dot is present
        if (databaseSchemaPrefix != null && !databaseSchemaPrefix.endsWith(".")) databaseSchemaPrefix += ".";
        this.databaseSchemaPrefix = databaseSchemaPrefix == null ? "" : databaseSchemaPrefix;
        agencies = new JDBCTableReader(Table.AGENCY, dataSource, databaseSchemaPrefix, EntityPopulator.AGENCY);
        fareAttributes = new JDBCTableReader(Table.FARE_ATTRIBUTES, dataSource, databaseSchemaPrefix, EntityPopulator.FARE_ATTRIBUTE);
        frequencies = new JDBCTableReader(Table.FREQUENCIES, dataSource, databaseSchemaPrefix, EntityPopulator.FREQUENCY);
        calendars = new JDBCTableReader(Table.CALENDAR, dataSource, databaseSchemaPrefix, EntityPopulator.CALENDAR);
        calendarDates = new JDBCTableReader(Table.CALENDAR_DATES, dataSource, databaseSchemaPrefix, EntityPopulator.CALENDAR_DATE);
        routes = new JDBCTableReader(Table.ROUTES, dataSource, databaseSchemaPrefix, EntityPopulator.ROUTE);
        stops = new JDBCTableReader(Table.STOPS, dataSource, databaseSchemaPrefix, EntityPopulator.STOP);
        trips = new JDBCTableReader(Table.TRIPS, dataSource, databaseSchemaPrefix, EntityPopulator.TRIP);
        stopTimes = new JDBCTableReader(Table.STOP_TIMES, dataSource, databaseSchemaPrefix, EntityPopulator.STOP_TIME);
        patterns = new JDBCTableReader(Table.PATTERNS, dataSource, databaseSchemaPrefix, EntityPopulator.PATTERN);
    }

    /**
     * Run the standard validation checks for this feed and store the validation errors in the database. Optionally,
     * takes one or more {@link FeedValidatorCreator} in the form of lambda method refs (e.g., {@code MTCValidator::new}),
     * which this method will instantiate and run after the standard validation checks have been completed.
     * 
     * TODO check whether validation has already occurred, overwrite results.
     * TODO allow validation within feed loading process, so the same connection can be used, and we're certain loaded
     *   data is 100% visible. That would also avoid having to reconnect the error storage to the DB.
     */
    public ValidationResult validate (FeedValidatorCreator... additionalValidators) {
        long validationStartTime = System.currentTimeMillis();
        // Create an empty validation result that will have its fields populated by certain validators.
        ValidationResult validationResult = new ValidationResult();
        // Error tables should already be present from the initial load.
        // Reconnect to the existing error tables.
        SQLErrorStorage errorStorage;
        try {
            errorStorage = new SQLErrorStorage(dataSource.getConnection(), databaseSchemaPrefix, false);
        } catch (SQLException | InvalidNamespaceException ex) {
            throw new StorageException(ex);
        }
        int errorCountBeforeValidation = errorStorage.getErrorCount();
        // Create list of standard validators to run on every feed.
        List<FeedValidator> feedValidators = Lists.newArrayList(
            new MisplacedStopValidator(this, errorStorage, validationResult),
            new DuplicateStopsValidator(this, errorStorage),
            new ParentStationValidator(this, errorStorage),
            new FaresValidator(this, errorStorage),
            new FrequencyValidator(this, errorStorage),
            new TimeZoneValidator(this, errorStorage),
            new NewTripTimesValidator(this, errorStorage),
            new NamesValidator(this, errorStorage)
        );
        // Create additional validators specified in this method's args and add to list of feed validators to run.
        for (FeedValidatorCreator creator : additionalValidators) {
            if (creator != null) feedValidators.add(creator.create(this, errorStorage));
        }

        for (FeedValidator feedValidator : feedValidators) {
            String validatorName = feedValidator.getClass().getSimpleName();
            try {
                LOG.info("Running {}.", validatorName);
                int errorCountBefore = errorStorage.getErrorCount();
                feedValidator.validate();
                LOG.info("{} found {} errors.", validatorName, errorStorage.getErrorCount() - errorCountBefore);
            } catch (Exception e) {
                // store an error if the validator fails
                // FIXME: should the exception be stored?
                // 5T un po' meno log
            	if(!e.toString().contains("ERROR: column \"pattern_id\" of relation \"trips\" already exists")) {
	                String badValue = String.join(":", validatorName, e.toString());
	                errorStorage.storeError(NewGTFSError.forFeed(VALIDATOR_FAILED, badValue));
            	}
                LOG.error("{} failed.", validatorName);
                LOG.error(e.toString());
                e.printStackTrace();
            }
        }
        // Signal to all validators that validation is complete and allow them to report on results / status.
        for (FeedValidator feedValidator : feedValidators) {
            try {
                feedValidator.complete(validationResult);
            } catch (Exception e) {
                String badValue = String.join(":", feedValidator.getClass().getSimpleName(), e.toString());
                // 5T un po' meno log
                if(!e.toString().contains("ERROR: column \"pattern_id\" of relation \"trips\" already exists")) {
	                errorStorage.storeError(NewGTFSError.forFeed(VALIDATOR_FAILED, badValue));
            	}
                LOG.error("Validator failed completion stage.", e);
            }
        }
        // Total validation errors accounts for errors found during both loading and validation. Otherwise, this value
        // may be confusing if it reads zero but there were a number of data type or referential integrity errors found
        // during feed loading stage.
        int totalValidationErrors = errorStorage.getErrorCount();
        LOG.info("Errors found during load stage: {}", errorCountBeforeValidation);
        LOG.info("Errors found by validators: {}", totalValidationErrors - errorCountBeforeValidation);
        errorStorage.commitAndClose();
        long validationEndTime = System.currentTimeMillis();
        long totalValidationTime = validationEndTime - validationStartTime;
        LOG.info("{} validators completed in {} milliseconds.", feedValidators.size(), totalValidationTime);

        // update validation result fields
        validationResult.errorCount = totalValidationErrors;
        validationResult.validationTime = totalValidationTime;

        // FIXME: Validation result date and int[] fields need to be set somewhere.
        return validationResult;
    }

    /**
     * @return a JDBC connection to the database underlying this Feed.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * @return the table name prefixed with this feed's database schema.
     */
    public String getTableNameWithSchemaPrefix(String tableName) {
        return databaseSchemaPrefix + tableName;
    }
}

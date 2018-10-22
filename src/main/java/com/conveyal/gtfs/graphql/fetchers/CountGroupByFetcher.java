package com.conveyal.gtfs.graphql.fetchers;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.conveyal.gtfs.graphql.GTFSGraphQL;
import com.conveyal.gtfs.loader.JDBCTableReader;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;

/**
 * Get quantity of rows in the given table.
 */
public class CountGroupByFetcher implements DataFetcher {

    public static final Logger LOG = LoggerFactory.getLogger(CountGroupByFetcher.class);

    private final String tableName;
    private final String filterField;
    private final String grouByField;

    public CountGroupByFetcher(String tableName) {
        this(tableName, null, null);
    }

    public CountGroupByFetcher(String tableName, String filterField, String grouByField) {
        this.tableName = tableName;
        this.filterField = filterField;
        this.grouByField = grouByField;
    }

    @Override
    public List<GroupByCount> get(DataFetchingEnvironment environment) {
        Map<String, Object> parentFeedMap = environment.getSource();
        String namespace = (String) parentFeedMap.get("namespace");
        Connection connection = null;
        List<GroupByCount> resultList = new ArrayList<GroupByCount>();
        try {
            connection = GTFSGraphQL.getConnection();
            Statement statement = connection.createStatement();
            String sql = String.format("select %s, count(*) from %s.%s ", grouByField, namespace, tableName);

            if (filterField != null) {
                String filterValue = (String) parentFeedMap.get(filterField);
                sql += String.format(" where %s = '%s'", filterField, filterValue);
            }

            sql += String.format(" group by %s ", grouByField);
            LOG.info(sql);

            if (statement.execute(sql)) {

                ResultSet resultSet = statement.getResultSet();
                while (resultSet.next()) {
                  String field = resultSet.getString(1);
                  Integer count = resultSet.getInt(2);
                  resultList.add(new GroupByCount(field, count));
                }
            }

        } catch (SQLException e) {
            // In case the table doesn't exist in this feed, just return zero and don't print noise to the log.
            // Unfortunately JDBC doesn't seem to define reliable error codes.
            if (! JDBCTableReader.SQL_STATE_UNDEFINED_TABLE.equals(e.getSQLState())) {
                e.printStackTrace();
            }
        } finally {
            DbUtils.closeQuietly(connection);
        }
        return resultList;
    }

    /**
     * Convenience method to create a field in a GraphQL schema that fetches the number of rows in a table.
     * Must be on a type that has a "namespace" field for context.
     */
    public static GraphQLFieldDefinition field (String fieldName, String tableName) {
        return newFieldDefinition()
                .name(fieldName)
                .type(Scalars.GraphQLInt)
                .dataFetcher(new CountGroupByFetcher(tableName))
                .build();
    }

    /**
     * Convenience method to create a field in a GraphQL schema that fetches the number of rows in a table with a filter
     * or where clause. If a filter field is provided, count only the rows that match the parent entity's value for the
     * given field. For example, adding a trip_count field to routes (filter field route_id) would add a trip count to
     * each route entity with the number of trips that operate under each route's route_id.
     */
    public static GraphQLFieldDefinition field (String fieldName, String tableName, String filterField, String grouByField) {
        return newFieldDefinition()
                .name(fieldName)
                .type(Scalars.GraphQLInt)
                .dataFetcher(new CountGroupByFetcher(tableName, filterField, grouByField))
                .build();
    }

    /**
     * For cases where the GraphQL field name is the same as the table name itself.
     */
    public static GraphQLFieldDefinition field (String tableName) {
        return field(tableName, tableName);
    }

    public static class GroupByCount {
        public String field;
        public int count;

        public GroupByCount(String field, int count) {
            this.field = field;
            this.count = count;
        }
    }

}

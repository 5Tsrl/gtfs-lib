package com.conveyal.gtfs.loader;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JdbcGtfsCleaner {

	private static final Logger LOG = LoggerFactory.getLogger(JdbcGtfsCleaner.class);

	private final String nameSpace;
	private final DataSource dataSource;

	private Connection connection;


	public JdbcGtfsCleaner(String nameSpace, DataSource dataSource) {
		this.nameSpace = nameSpace;
		this.dataSource = dataSource;
	}


	public void deleteFeedVersion() {

		if(this.nameSpace == null) {
			return;
		}

		try {
			connection = dataSource.getConnection();

			Map<String, Object> feeds = getFeed(this.nameSpace);
			String filePath = (String)feeds.get("filename");

			if(filePath != null) {
				deleteFile(filePath);
			}

			deleteFromFeedTable(this.nameSpace);
			deleteNameSpace(this.nameSpace);

			connection.commit();
		}
		catch (Exception ex) {

			try {
				connection.rollback();
			} catch (SQLException e) { }

			LOG.error("Exception while deleting feed version namespace in feeds table: {}", ex.getMessage());
			DbUtils.closeQuietly(connection);
		}
	}


	public void deleteFeedSource() {

		if(this.nameSpace == null) {
			return;
		}

		try {
			connection = dataSource.getConnection();

			Map<String, Object> feeds = getFeed(this.nameSpace);
			String filePath = (String)feeds.get("filename");

			if(filePath != null) {
				deleteFile(filePath);
			}

			deleteFromFeedTable(this.nameSpace);
			deleteNameSpace(this.nameSpace);

//			String snapshotOf = (String)feeds.get("snapshot_of");
//			if(snapshotOf != null) {
//
//				Map<String, Object> feedSnapShot = getFeed(snapshotOf);
//				String filePathSnapShot = (String)feedSnapShot.get("filename");
//				if(filePathSnapShot != null) {
//					deleteFile(filePathSnapShot);
//				}
//
//				deleteFromFeedTable(snapshotOf);
//				deleteNameSpace(snapshotOf);
//			}

			connection.commit();
		}
		catch (Exception ex) {

			try {
				connection.rollback();
			} catch (SQLException e) { }

			LOG.error("Exception while deleting feed: {}", ex.getMessage());
			DbUtils.closeQuietly(connection);
		}
	}



	private void deleteFile(String path) {

		try {
			File f = new File(path);

			if(f.exists()) {
				f.delete();
			}
		}
		catch (Exception e) {
			LOG.warn("Exception while deleting file: " +path);
		}

	}


	private Map<String, Object> getFeed(String nameSpace) throws Exception {

		Statement statement = connection.createStatement();

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(String.format("select * from feeds where namespace = '%s'", nameSpace));
		statement.execute(sqlBuilder.toString());
		ResultSet resultSet = statement.getResultSet();
		ResultSetMetaData meta = resultSet.getMetaData();
		int nColumns = meta.getColumnCount();
		Map<String, Object> resultMap = new HashMap<>();

		if (resultSet.next()) {
			for (int i = 1; i <= nColumns; i++) {
				resultMap.put(meta.getColumnName(i), resultSet.getObject(i));
			}
		}

		return resultMap;
	}


	private void deleteFromFeedTable(String nameSpace) throws Exception  {

		Statement statement = connection.createStatement();

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(String.format("delete from feeds where namespace = '%s'", nameSpace));
		statement.executeUpdate(sqlBuilder.toString());
	}


	private void deleteNameSpace(String nameSpace) throws Exception {

		Statement statement = connection.createStatement();
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(String.format("drop schema if exists %s cascade", nameSpace));

		statement.execute(sqlBuilder.toString());
	}


}

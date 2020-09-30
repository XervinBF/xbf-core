package org.xbf.core.Data;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.LoggerFactory;
import org.xbf.core.XBF;
import org.xbf.core.Config.XDBConfig;
import org.xbf.core.Data.Connector.DBResult;
import org.xbf.core.Data.Connector.IDBProvider;

import ch.qos.logback.classic.Logger;

public class DBConnector {

	static final Logger logger = (Logger) LoggerFactory.getLogger(DBConnector.class);
	
	XDBConfig config;
	
	public String connectionString;
	public String dbtype;
	
	public String username;
	public String password;
	
	public DBConnector() {
		this(null);
	}
	
	public DBConnector(String dbname) {
		if(dbname == null) {
			XDBConfig db = XBF.getConfig().defaultDatabase;
			connectionString = db.connectionString;
			dbtype = db.dbtype;
			username = db.username;
			password = db.password;
			config = db;
		} else {
			if(XBF.getConfig().databases.containsKey(dbname)) {
				XDBConfig db = XBF.getConfig().databases.get(dbname);
				connectionString = db.connectionString;
				dbtype = db.dbtype;
				username = db.username;
				password = db.password;
				config = db;
			} else {
				logger.warn("Connection options for database '" + dbname + "' was not found");
			}
		}
		if(config == null) {
			logger.warn("No connection configuration was specified");
		}
	}
	
	IDBProvider provider;
	
	IDBProvider getConnector() {
		if(provider == null) {
			provider = XBF.getDatabaseProvider(dbtype);
			provider.openConnection(config);
		}
		return provider;
	}
	
	public void createTable(String tableName, HashMap<String, String> fields) throws Exception {
		getConnector().createTable(tableName, fields);
	}
	
	public void addFieldToTable(String table, String fieldName, String fieldType) throws Exception {
		getConnector().addFieldToTable(table, fieldName, fieldType);
	}
	
	public String getFieldType(String javaFieldClassName) {
		return getConnector().getFieldType(javaFieldClassName);
	}
	
	public String getMaxValueForField(String fieldType) {
		return getConnector().getMaxValueForField(fieldType);
	}

	public HashMap<String, String> getFields(String table) throws Exception {
		return getConnector().getFields(table);
	}
	
	public DBResult getData(String table) throws Exception {
		return getConnector().getData(table);
	}
	
	public DBResult getData(String table, ArrayList<String> fieldsToGet) throws Exception {
		return getConnector().getData(table, fieldsToGet);
	}
	
	public DBResult getData(String table, HashMap<String, String> where) throws Exception {
		return getConnector().getData(table, where);
	}
	
	public DBResult getData(String table, ArrayList<String> fieldsToGet, HashMap<String, String> where) throws Exception {
		return getConnector().getData(table, fieldsToGet, where);
	}
	
	public DBResult getData(String table, HashMap<String, String> where, int limit) throws Exception {
		return getConnector().getData(table, where, limit);
	}
	
	public DBResult getData(String table, ArrayList<String> fieldsToGet, HashMap<String, String> where, int limit) throws Exception {
		return getConnector().getData(table, fieldsToGet, where, limit);
	}
	
	public int getMax(String table, String field) throws Exception {
		return getConnector().getMax(table, field);
	}
	
	public boolean has(String table, HashMap<String, String> where) throws Exception {
		return getConnector().has(table, where);
	}
	
	public void insertData(String table, HashMap<String, String> set) throws Exception {
		getConnector().insertData(table, set);
	}
	
	public void updateData(String table, HashMap<String, String> set, HashMap<String, String> where) throws Exception {
		getConnector().updateData(table, set, where);
	}
	
	public boolean shouldCreateTable(String exceptionMessage) {
		return getConnector().shouldCreateTable(exceptionMessage);
	}
	
	public boolean shouldAddField(String exceptionMessage) {
		return getConnector().shouldAddField(exceptionMessage);
	}
	
	public void delete(String table, HashMap<String, String> where) throws Exception {
		getConnector().delete(table, where);
	}

	

	
	
	public static String safe(String s) {
		if(s.contains("--")) return safePreventionStop(s, "DashDashComment");
		if(s.contains("'")) s = s.replace("'", "''");
		if(s.contains(";")) return safePreventionStop(s, "SemiColon");
		return s;
	}
	
	private static String safePreventionStop(String s, String stop) {
		LoggerFactory.getLogger("SafePrevention").warn("Cause: '" + stop + "'. Parameter: '" + s + "'");
		return "SafePrevention<" + stop + ">";
	}
	
	
}

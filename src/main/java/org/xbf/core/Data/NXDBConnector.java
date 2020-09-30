package org.xbf.core.Data;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import org.xbf.core.Data.Connector.DBResult;

/**
 * No Exception DBConnector
 * 
 * @author BL19
 *
 */
public class NXDBConnector extends DBConnector {

	public NXDBConnector() {
		super();
	}

	public NXDBConnector(String dbName) {
		super(dbName);
	}

	public void addFieldToTable(String table, String fieldName, String fieldType) {
		try {
			super.addFieldToTable(table, fieldName, fieldType);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createTable(String tableName, HashMap<String, String> fields) {
		try {
			super.createTable(tableName, fields);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void delete(String table, HashMap<String, String> where) {
		try {
			super.delete(table, where);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DBResult getData(String table) {
		try {
			return super.getData(table);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public DBResult getData(String table, ArrayList<String> fieldsToGet) {
		try {
			return super.getData(table, fieldsToGet);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public DBResult getData(String table, ArrayList<String> fieldsToGet, HashMap<String, String> where) {
		try {
			return super.getData(table, fieldsToGet, where);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public DBResult getData(String table, ArrayList<String> fieldsToGet, HashMap<String, String> where, int limit) {
		try {
			return super.getData(table, fieldsToGet, where, limit);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public DBResult getData(String table, HashMap<String, String> where) {
		try {
			return super.getData(table, where);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public DBResult getData(String table, HashMap<String, String> where, int limit) {
		try {
			return super.getData(table, where, limit);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public HashMap<String, String> getFields(String table) {

		try {
			return super.getFields(table);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int getMax(String table, String field) {

		try {
			return super.getMax(table, field);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public boolean has(String table, HashMap<String, String> where) {

		try {
			return super.has(table, where);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void insertData(String table, HashMap<String, String> set) {

		try {
			super.insertData(table, set);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	@Override
	public void updateData(String table, HashMap<String, String> set, HashMap<String, String> where) {

		try {
			super.updateData(table, set, where);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

}

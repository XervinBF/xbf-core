package org.xbf.core.Data.Connector;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.directory.InvalidAttributeValueException;

import org.slf4j.LoggerFactory;
import org.xbf.core.Data.AnyMapper;

import com.google.gson.Gson;

import ch.qos.logback.classic.Logger;

public class DBResult {

	public ArrayList<String> columns = new ArrayList<String>();
	public ArrayList<Object[]> rows = new ArrayList<>();
	int rowIndex = -1;
	
	static final Logger logger = (Logger) LoggerFactory.getLogger(DBResult.class);
	
	public void addRow(Object[] row) {
		if(row.length != columns.size())
			throw new RuntimeException("Row length must be equal to the amount of columns");
		rows.add(row);
		if(logger.isTraceEnabled()) {
			String[] objects = new String[row.length];
			for (int i = 0; i < row.length; i++) {
				objects[i] = row[i] + "";
				if(row[i] instanceof String) {
					objects[i] = objects[i].trim();
				}
			}
			logger.trace("Adding row: " + new Gson().toJson(objects));
		}
	}
	
	public void addColumn(String columnName) {
		columnName = columnName.toLowerCase();
		columns.add(columnName);
		logger.trace("Adding column: " + columnName);
	}
	
	public boolean next() {
		if(rows.size() == rowIndex + 1) return false;
		if(rows.size() == 0) return false;
		rowIndex++;
		return true;
	}
	
	/**
	 * Gets the object of the column at the columnIndex
	 * @param columnIndex The index of the column, starts with 1
	 * @return The object at the selected column
	 */
	public Object getObject(int columnIndex) {
		if(rowIndex == -1) return null;
		if(columns.size() < columnIndex) return null; // Dont throw exceptions
		return rows.get(rowIndex)[columnIndex - 1];
	}
	
	/**
	 * Gets the object of the column with the specified columnName
	 * @param columnName The name of the column
	 * @return The object at the selected column
	 */
	public Object getObject(String columnName) {
		columnName = columnName.toLowerCase();
		if(!columns.contains(columnName)) {
			throw new NoSuchColumnException("The column name " + columnName + " is not valid.");
		}
		return getObject(columns.indexOf(columnName) + 1);
	}
	
	public String getString(int columnIndex) {
		return (String) AnyMapper.mapObject(getObject(columnIndex), String.class);
	}
	
	public String getString(String columnName) {
		return (String) AnyMapper.mapObject(getObject(columnName), String.class);

	}
	
	public int getInt(int columnIndex) {
		return (int) AnyMapper.mapObject(getObject(columnIndex), Integer.class);
	}
	
	public int getInt(String columnName) {
		return (int) AnyMapper.mapObject(getObject(columnName), Integer.class);
	}
	
	public long getLong(int columnIndex) {
		return (long) AnyMapper.mapObject(getObject(columnIndex), Long.class);
	}
	
	public long getLong(String columnName) {
		return (long) AnyMapper.mapObject(getObject(columnName), Long.class);
	}
	
	public double getDouble(int columnIndex) {
		return (double) AnyMapper.mapObject(getObject(columnIndex), Double.class);
	}
	
	public double getDouble(String columnName) {
		return (double) AnyMapper.mapObject(getObject(columnName), Double.class);
	}
	
	public boolean getBoolean(int columnIndex) {
		return (boolean) AnyMapper.mapObject(getObject(columnIndex), Boolean.class);
	}
	
	public boolean getBoolean(String columnName) {
		return (boolean) AnyMapper.mapObject(getObject(columnName), Boolean.class);
	}
	
	public static DBResult fromResultSet(ResultSet set) throws SQLException {
		DBResult r = new DBResult();
		for (int i = 0; i < set.getMetaData().getColumnCount(); i++) {
			r.addColumn(set.getMetaData().getColumnName(i + 1));
		}
		while(set.next()) {
			Object[] arr = new Object[r.columns.size()];
			for (int i = 0; i < r.columns.size(); i++) {
				arr[i] = set.getObject(1 + i);
			}
			r.addRow(arr);
		}
		return r;
	}
	
}

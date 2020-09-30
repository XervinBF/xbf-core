package org.xbf.core.Data.Connector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.directory.InvalidAttributeValueException;

public class DBResult {

	public ArrayList<String> columns = new ArrayList<String>();
	public ArrayList<Object[]> rows = new ArrayList<>();
	int rowIndex = -1;
	
	public void addRow(Object[] row) {
		if(row.length != columns.size())
			throw new RuntimeException("Row length must be equal to the amount of columns");
		rows.add(row);
	}
	
	public void addColumn(String columnName) {
		columns.add(columnName);
	}
	
	public boolean next() {
		if(rows.size() == rowIndex) return false;
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
		if(!columns.contains(columnName)) {
			throw new NoSuchColumnException("The column name " + columnName + " is not valid.");
		}
		return getObject(columns.indexOf(columnName) + 1);
	}
	
	public String getString(int columnIndex) {
		return ((String) getObject(columnIndex));
	}
	
	public String getString(String columnName) {
		return (String) getObject(columnName);
	}
	
	public int getInt(int columnIndex) {
		return (Integer) getObject(columnIndex);
	}
	
	public int getInt(String columnName) {
		return (Integer) getObject(columnName);
	}
	
	public long getLong(int columnIndex) {
		return (Long) getObject(columnIndex);
	}
	
	public long getLong(String columnName) {
		return (Long) getObject(columnName);
	}
	
	public double getDouble(int columnIndex) {
		return (Double) getObject(columnIndex);
	}
	
	public double getDouble(String columnName) {
		return (Double) getObject(columnName);
	}
	
	public boolean getBoolean(int columnIndex) {
		return (Boolean) getObject(columnIndex);
	}
	
	public boolean getBoolean(String columnName) {
		return (Boolean) getObject(columnName);
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

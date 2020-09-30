package org.xbf.core.Data.Connector;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import org.xbf.core.Config.XDBConfig;

public interface IDBProvider {
	
	public void openConnection(XDBConfig config);
	
	/**
	 * 
	 * @param fields HashMap<Field_Name, Field_Type>
	 * @return
	 */
	public void createTable(String tableName, HashMap<String, String> fields) throws Exception;
	
	public void addFieldToTable(String table, String fieldName, String fieldType) throws Exception;
	
	public String getFieldType(String javaFieldClassName);
	
	public String getMaxValueForField(String fieldType);
	
	public HashMap<String, String> getFields(String table) throws Exception;
	
	public DBResult getData(String table) throws Exception;
	
	public DBResult getData(String table, ArrayList<String> fieldsToGet) throws Exception;
	
	public DBResult getData(String table, HashMap<String, String> where) throws Exception;
	
	public DBResult getData(String table, ArrayList<String> fieldsToGet, HashMap<String, String> where) throws Exception;
	
	public DBResult getData(String table, HashMap<String, String> where, int limit) throws Exception;
	
	public DBResult getData(String table, ArrayList<String> fieldsToGet, HashMap<String, String> where, int limit) throws Exception;
	
	public int getMax(String table, String field) throws Exception;
	
	public boolean has(String table, HashMap<String, String> where) throws Exception;
	
	public void insertData(String table, HashMap<String, String> set) throws Exception;
	
	public void updateData(String table, HashMap<String, String> set, HashMap<String, String> where) throws Exception;
	
	public void delete(String table, HashMap<String, String> where) throws Exception;
	
	
	
	public boolean shouldCreateTable(String exceptionMessage);
	public boolean shouldAddField(String exceptionMessage);
	
	
}

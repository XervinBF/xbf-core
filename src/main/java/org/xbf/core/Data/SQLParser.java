package org.xbf.core.Data;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class SQLParser {

	public static ArrayList<HashMap<String, Object>> parseResult(ResultSet set) throws SQLException {
		ArrayList<HashMap<String, Object>> obj = new ArrayList<>();
		while(set.next()) {
			HashMap<String, Object> m = new HashMap<>();
			ResultSetMetaData metaData = set.getMetaData();
			for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
				String label = metaData.getColumnLabel(i);
				m.put(label, set.getObject(i).toString().trim());
			}
			obj.add(m);
		}
		return obj;
	}
	
}

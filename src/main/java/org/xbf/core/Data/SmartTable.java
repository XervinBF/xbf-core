package org.xbf.core.Data;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.xbf.core.Cache.ObjectCache;
import org.xbf.core.Data.Annotations.Database;
import org.xbf.core.Data.Annotations.Ignore;
import org.xbf.core.Data.Annotations.Include;
import org.xbf.core.Data.Annotations.IncludeAll;
import org.xbf.core.Data.Connector.DBResult;
import org.xbf.core.Utils.Map.FastMap;
import org.xbf.core.Utils.Map.MapUtils;

import ch.qos.logback.classic.Logger;

public class SmartTable<T extends SmartTableObject> {

	public boolean followIds = true;
	public String addition = " ORDER BY id ASC";
	public ObjectCache c;
	public static boolean UseMSSQL = true;
	public String getMax(String type) {
		return connector.getMaxValueForField(type);
	}

	static boolean IncludeAllFields = false;
	Logger l;

	String tb;
	Class<? extends SmartTableObject> ref;
	
	String database = null;
	
	DBConnector connector;

	public SmartTable(String tableName, Class<? extends SmartTableObject> refClass) {
		tb = tableName;
		ref = refClass;
		c = new ObjectCache("SMTBL." + tableName, 60000 * 8);
		l = (Logger) LoggerFactory.getLogger("SMTBL - " + tableName);
		IncludeAllFields = ref.isAnnotationPresent(IncludeAll.class);
		if(ref.isAnnotationPresent(Database.class)) {
			database = refClass.getAnnotation(Database.class).value();
		}
		connector = new DBConnector(database);
	}

	public boolean IncludeField(Field f) {
		if (f.isAnnotationPresent(Ignore.class)) {
			return false;
		}
		if (f.isAnnotationPresent(Include.class)) {
			return true;
		}
		if (ref.isAnnotationPresent(IncludeAll.class)) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public T Parse(DBResult r)
			throws IllegalArgumentException, IllegalAccessException, SQLException, InstantiationException, SmartTableException {
		try {
			if (ref == null)
				return null;
			if (!SmartTableObject.class.isAssignableFrom(ref))
				return null;
			if (!r.next())
				return null;
			SmartTableObject o = null;
			o = ref.newInstance();
			for (Field f : o.getClass().getFields()) {
				if (!IncludeField(f))
					continue;

				try {
					String fnam = f.getName();
//					System.out.println(o.getClass().getSimpleName() + " - " + fnam + " - " + r.findColumn(fnam));
					Class<?> type = f.getType();
					String typeName = type.getName();
					boolean sto = SmartTableObject.class.isAssignableFrom(type);
					boolean inverseSto = type.isAssignableFrom(SmartTableObject.class);
					boolean arrList = ArrayList.class.isAssignableFrom(type);
					boolean inverseArrList = type.isAssignableFrom(ArrayList.class);
					
					
					if (typeName.contains("String")) {
						String value = r.getString(fnam);
						if(value != null) value = value.trim();
						f.set(o, value);
					} else if (typeName.contains("int")) {
						f.set(o, r.getInt(fnam));
					} else if (typeName.contains("long")) {
						f.set(o, r.getLong(fnam));
					} else if (typeName.contains("double")) {
						f.set(o, r.getDouble(fnam));
					} else if (typeName.contains("boolean")) {
						f.set(o, r.getBoolean(fnam));
					} else if (SmartTableObject.class.isAssignableFrom(type)) {
						int smtblid = r.getInt(fnam);
						f.set(o, new SmartTable<SmartTableObject>(
								((SmartTableObject) f.getType().newInstance()).getTable(),
								(Class<? extends SmartTableObject>) f.getType()).get(new FastMap<String, String>()
										.add("id", smtblid + "").getMap()));
					} else if (ArrayList.class.isAssignableFrom(f.getType())) {
						Class<? extends SmartTableArrayObject> mapping = o.getArrayMappings().get(fnam);
						ArrayList<Object> a = new ArrayList<Object>();
						SmartTable<SmartTableArrayObject> t = new SmartTable<SmartTableArrayObject>(
								(mapping.newInstance()).getTable(), mapping);
						for (SmartTableArrayObject object : t.getMultiple(new FastMap<String, String>().add("parent", r.getInt("id") + "").getMap())) {
							a.add(object);
						}
						f.set(o, a);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					if(ex.getMessage().equals("The column name " + f.getName() + " is not valid.")) {
						// Invalid table structure
						addFields();
						throw new SmartTableException("[RETRY]");
					}
				}
			}
			o.objectLoaded();
			return (T) o;
		} catch (Exception e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<T> ParseAll(DBResult r)
			throws IllegalArgumentException, IllegalAccessException, InstantiationException, SQLException, SmartTableException {
		ArrayList<T> tarr = new ArrayList<T>();
		Object obj = Parse(r);
		while (obj != null) {
			tarr.add((T) obj);
			obj = Parse(r);
		}
		return tarr;
	}

	public List<T> getAll() {
		try {
			List<T> res = ParseAll(connector.getData(tb));
			Collections.sort(res, new Comparator<SmartTableObject>() {
				@Override
				public int compare(SmartTableObject o1, SmartTableObject o2) {
					return o1.id < o2.id ? 1 : o1.id == o2.id ? 0 : -1;
				}
			});
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			if (HandleException(e)) {
				return getAll();
			}
			if(e.getMessage().equals("[RETRY]")) {
				return getAll();
			}
		}
		return null;
	}

	public HashMap<String, String> getDBFields() {
		FastMap<String, String> map = new FastMap<String, String>();
		for (Field f : ref.getFields()) {
			if (!IncludeField(f))
				continue;
			if(IncludeField(f))
				map.add(f.getName(), classNameToDBType(f.getType().getSimpleName()));
		}
		return map.getMap();
	}

	public HashMap<String, String> getCurrentDBFields() throws Exception {
		return connector.getFields(tb); 
	}

	public void addFields() throws Exception {
		HashMap<String, String> expected = getDBFields();
		HashMap<String, String> current = getCurrentDBFields();
		for (String fieldName : current.keySet()) {
			if (expected.containsKey(fieldName))
				expected.remove(fieldName);
		}

		for (String fieldName : expected.keySet()) {
			String type = expected.get(fieldName);
			l.warn("Performing column migration! Adding field: field '" + fieldName + "' of type '" + type + "' to table '" + tb + "'");
			connector.addFieldToTable(tb, fieldName, type);
		}
	}

	public void createTable() throws Exception {
		HashMap<String, String> expected = getDBFields();
		connector.createTable(tb, expected);
	}

	String classNameToDBType(String className) {
		return connector.getFieldType(className);
	}

	public List<T> getMultiple(FastMap<String, String> where) {
		return getMultiple(where.getMap());
	}
	
	@SuppressWarnings("unchecked")
	public List<T> getMultiple(HashMap<String, String> where) {
		try {
			List<T> res = ParseAll(connector.getData(tb, where));
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			if (HandleException(e)) {
				return getMultiple(where);
			}
			if(e.getMessage().equals("[RETRY]")) {
				return getMultiple(where);
			}
		}
		return new ArrayList<T>();
	}
	
	public T get(FastMap<String, String> where) {
		return get(where.getMap());
	}

	public T get(HashMap<String, String> where) {
		try {
			T res = Parse(connector.getData(tb, where, 1));
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			if (HandleException(e)) {
				return get(where);
			}
			if(e.getMessage().equals("[RETRY]")) {
				return get(where);
			}
		}
		return null;
	}

	private boolean HandleException(Exception e) {
		e.printStackTrace();
		if (connector.shouldCreateTable(e.getMessage())) {
			try {
				createTable();
			} catch (Exception e1) {
				e1.printStackTrace();
				return false;
			}
			return true;
		}
		if(connector.shouldAddField(e.getMessage())) {
			try {
				addFields();
			} catch (Exception e2) {
				e2.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public T insert(Object obj) {
		int objid = -1;
		if (followIds) {
			try {
				objid = obj.getClass().getField("id").getInt(obj);
				if (hasWithQuery(new FastMap<String, String>()
						.add("id", objid + "")))
					return null;
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				if (HandleException(e)) {
					return insert(obj);
				}
			}
		}
		HashMap<String, String> datamap = getSqlDataMap(obj, objid);
		try {
			connector.insertData(tb, datamap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (T) obj;
	}

	public boolean hasWithQuery(FastMap<String, String> where) {
		return hasWithQuery(where.getMap());
	}
	
	public boolean hasWithQuery(HashMap<String, String> where) {
		try {
			return connector.has(tb, where);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public HashMap<String, String> getSqlDataMap(Object obj, int objid) {
		HashMap<String, String> data = new HashMap<>();
		for (Field f : obj.getClass().getFields()) {
			if (!IncludeField(f))
				continue;
			if (SmartTableObject.class.isAssignableFrom(f.getType())) {
				try {
					Object smo = f.get(obj);
					if (smo == null)
						continue;
					String table = ((SmartTableObject) smo).getTable();
					SmartTable<SmartTableObject> t = new SmartTable<SmartTableObject>(table,
							(Class<? extends SmartTableObject>) f.getType());
					int id = t.set(f.get(obj)).id;
					data.put(f.getName(), id + "");
				} catch (Exception e) {
					System.err.println("[SET] Failed to parse " + f.getName() + " as SmartTableObject from class "
							+ obj.getClass().getSimpleName() + ". Maybe.");
					e.printStackTrace();
				}
			} else if (ArrayList.class.isAssignableFrom(f.getType())) {
				// This is array
				try {
					ArrayList<? extends SmartTableArrayObject> a = (ArrayList<? extends SmartTableArrayObject>) f
							.get(obj);
					if (a == null || a.size() == 0)
						continue;
					ArrayList<Integer> oldObjects = new ArrayList<Integer>();
					ArrayList<Integer> newObjects = new ArrayList<Integer>();
					SmartTableArrayObject first = a.get(0);
					Class<? extends SmartTableObject> c = first.getClass();
					SmartTable<SmartTableArrayObject> t = new SmartTable<SmartTableArrayObject>(first.getTable(), c);
//					for (SmartTableArrayObject p : t.getMultiple("parent=" + objid)) {
					for (SmartTableArrayObject p : t.getMultiple(
							new FastMap<String, String>()
							.add("parent", objid + "")
							.getMap())) {
						oldObjects.add(p.id);
					}
					for (SmartTableArrayObject b : a) {
						String q = "parent=" + objid + " AND " + b.getQuery();
						SmartTableArrayObject stao = t.get(new MapUtils<String, String>().mergeMap(b.getQuery(), new FastMap<String, String>()
								.add("parent", objid + "").getMap()));
						if (stao != null)
							b.id = stao.id;
						b.parent = objid;
						newObjects.add(b.id);
						t.set(b);
					}

					for (Integer integer : newObjects) {
						oldObjects.remove(integer);
					}
					for (Integer integer : oldObjects) {
						t.delete(new SmartTableArrayObject(t.tb) {
							{
								id = integer;
							}
						});
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			} else {
				try {
					data.put(f.getName(), f.get(obj) + "");
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return data;
	}

	@SuppressWarnings("unchecked")
	public T update(Object obj) {
		HashMap<String, String> data;
		try {
			data = getSqlDataMap(obj, obj.getClass().getField("id").getInt(obj));
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e1) {
			throw new RuntimeException(
					"Entity " + obj.getClass().getName() + " does not contain an int field named 'id'.");
		}

		String vals = "";
		for (String key : data.keySet()) {
			vals += key + "='" + data.get(key) + "',";
		}
		vals = vals.substring(0, vals.length() - 1);
		try {
			connector.updateData(tb, data, new FastMap<String, String>().add("id", obj.getClass().getField("id").getInt(obj) + "").getMap());
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Entity " + obj.getClass().getName() + " does not contain an int field named 'id'.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return (T) obj;
	}

	@SuppressWarnings("unchecked")
	public T set(Object obj) {
		int id = -1;
		try {
			id = obj.getClass().getField("id").getInt(obj);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Entity " + obj.getClass().getName() + " does not contain an int field named 'id'.");
		}
		if (id == 0) {
			id = getNextId();
			try {
				obj.getClass().getField("id").setInt(obj, id);
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
		}
		
		if (hasWithQuery(new FastMap<String, String>().add("id", id + ""))) {
			update(obj);
		} else {
			insert(obj);
		}
		c.clear();
		return (T) obj;

	}

	public int getNextId() {
		try {
			return connector.getMax(tb, "id") + 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	public T delete(Object obj) {
		int id = -1;
		try {
			id = obj.getClass().getField("id").getInt(obj);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Entity " + obj.getClass().getName() + " does not contain an int field named 'id'.");
		}
		try {
			connector.delete(tb, new FastMap<String, String>().add("id", id + "").getMap());
		} catch (Exception e) {
			e.printStackTrace();
		}
		c.clear();
		return (T) obj;
	}

}

package org.xbf.core.Data;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.xbf.core.Cache.ObjectCache;
import org.xbf.core.Data.Annotations.Database;
import org.xbf.core.Data.Annotations.Ignore;
import org.xbf.core.Data.Annotations.Include;
import org.xbf.core.Data.Annotations.IncludeAll;
import org.xbf.core.Data.Connector.DBResult;
import org.xbf.core.Utils.Map.FastMap;
import org.xbf.core.Utils.Map.MapUtils;

import com.google.gson.Gson;

import ch.qos.logback.classic.Logger;

public class SmartTable<T extends SmartTableObjectNoKey> {

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
	Class<? extends SmartTableObjectNoKey> ref;

	String database = null;

	DBConnector connector;

	public SmartTable(String tableName, Class<? extends SmartTableObjectNoKey> refClass) {
		tb = tableName;
		ref = refClass;
		c = new ObjectCache("SMTBL." + tableName, 60000 * 8);
		l = (Logger) LoggerFactory.getLogger("SMTBL - " + tableName);
		IncludeAllFields = ref.isAnnotationPresent(IncludeAll.class);
		if (ref.isAnnotationPresent(Database.class)) {
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
	public T Parse(DBResult r) throws IllegalArgumentException, IllegalAccessException, SQLException,
			InstantiationException, SmartTableException {
		try {
			if (ref == null)
				return null;
			if (!SmartTableObjectNoKey.class.isAssignableFrom(ref))
				return null;
			if (!r.next())
				return null;
			SmartTableObjectNoKey o = null;

			o = ref.getConstructor().newInstance();
			o.smrtref = ref.getConstructor().newInstance();
			for (Field f : o.getClass().getFields()) {
				if (!IncludeField(f))
					continue;

				try {
					String fnam = f.getName();
//					System.out.println(o.getClass().getSimpleName() + " - " + fnam + " - " + r.findColumn(fnam));
					Class<?> type = f.getType();
					String typeName = type.getName();
					boolean sto = SmartTableObjectNoKey.class.isAssignableFrom(type);
					boolean inverseSto = type.isAssignableFrom(SmartTableObjectNoKey.class);
					boolean arrList = ArrayList.class.isAssignableFrom(type);
					boolean inverseArrList = type.isAssignableFrom(ArrayList.class);

					if (typeName.contains("String")) {
						String value = r.getString(fnam);
						if (value != null)
							value = value.trim();
						if(value != null && !value.isBlank() && value.equals("null"))
							value = null;
						f.set(o, value);
						f.set(o.smrtref, value);
					} else if (typeName.contains("int")) {
						int value = r.getInt(fnam);
						f.set(o, value);
						f.set(o.smrtref, value);
					} else if (typeName.contains("long")) {
						long value = r.getLong(fnam);
						f.set(o, value);
						f.set(o.smrtref, value);
					} else if (typeName.contains("double")) {
						double value = r.getDouble(fnam);
						f.set(o, value);
						f.set(o.smrtref, value);
					} else if (typeName.contains("boolean")) {
						boolean value = r.getBoolean(fnam);
						f.set(o, value);
						f.set(o.smrtref, value);
					} else if (SmartTableObjectNoKey.class.isAssignableFrom(type)) {
						String smtblid = r.getString(fnam);
						SmartTableObjectNoKey nsmtbl = (SmartTableObjectNoKey) f.getType().newInstance();
						Field keyFieldN = getKeyField(nsmtbl);
						Object value = new SmartTable<SmartTableObjectNoKey>(nsmtbl.getTable(),
								(Class<? extends SmartTableObjectNoKey>) f.getType()).get(
										new FastMap<String, String>().add(keyFieldN.getName(), smtblid + "").getMap());
						f.set(o, value);
						f.set(o.smrtref, value);
					} else if (ArrayList.class.isAssignableFrom(f.getType())) {
						Class<? extends SmartTableArrayObject> mapping = o.getArrayMappings().get(fnam);
						ArrayList<Object> a = new ArrayList<Object>();
						Field keyFieldRef = getKeyField(o);
						SmartTableArrayObject nstao = mapping.newInstance();
						SmartTable<SmartTableArrayObject> t = new SmartTable<SmartTableArrayObject>(nstao.getTable(),
								mapping);
						for (SmartTableArrayObject object : t.getMultiple(new FastMap<String, String>()
								.add("parent", r.getString(keyFieldRef.getName()) + "").getMap())) {
							a.add(object);
						}
						f.set(o, a);
						f.set(o.smrtref, a);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					if (ex.getMessage().equals("The column name " + f.getName() + " is not valid.")) {
						// Invalid table structure
						addFields();
						throw new SmartTableException("[RETRY]");
					}
				}
			}
			o.objectLoaded();
			return (T) o;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<T> ParseAll(DBResult r) throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			SQLException, SmartTableException {
		ArrayList<T> tarr = new ArrayList<T>();
		Object obj = Parse(r);
		while (obj != null) {
			tarr.add((T) obj);
			obj = Parse(r);
		}
		return tarr;
	}

	Object getKey(Object obj) {
		try {
			return getKeyField(obj).get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	void setKey(Object obj, Object value) {
		try {
			getKeyField(obj).set(obj, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	Field getKeyField(Object cls) {
		if (cls == null)
			return null;
		Field idField = null;
		for (Field f : cls.getClass().getFields()) {
			if (f.isAnnotationPresent(org.xbf.core.Data.Annotations.Key.class))
				try {
					return f;
				} catch (IllegalArgumentException e) {
					l.warn("Failed to access key field of " + cls.getClass().getSimpleName());
					e.printStackTrace();
				}
			if (f.getName().equalsIgnoreCase("id"))
				idField = f;
		}
		try {
			return idField;
		} catch (IllegalArgumentException e) {
			l.warn("Failed to access fallback id field of " + cls.getClass().getSimpleName());
			e.printStackTrace();
			return null;
		}
	}

	public List<T> getAll() {
		try {
			List<T> res = ParseAll(connector.getData(tb));
			Collections.sort(res, new Comparator<SmartTableObjectNoKey>() {
				@Override
				public int compare(SmartTableObjectNoKey o1, SmartTableObjectNoKey o2) {
					Object k1 = getKey(o1);
					Object k2 = getKey(o2);
					if (k1 instanceof String && k2 instanceof String) {
						String s1 = (String) k1;
						String s2 = (String) k2;
						return s1.compareTo(s2);
					} else if (k1 instanceof Integer && k2 instanceof Integer) {
						int id1 = (int) k1;
						int id2 = (int) k2;
						return id1 < id2 ? 1 : id1 == id2 ? 0 : -1;
					}
					// TODO: Implement more compares
					return 0;
				}
			});
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			if (HandleException(e)) {
				return getAll();
			}
			if (e.getMessage().equals("[RETRY]")) {
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
			if (IncludeField(f))
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
			l.warn("Performing column migration! Adding field: field '" + fieldName + "' of type '" + type
					+ "' to table '" + tb + "'");
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
			if (e.getMessage().equals("[RETRY]")) {
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
			if (e.getMessage().equals("[RETRY]")) {
				return get(where);
			}
		}
		return null;
	}

	private boolean HandleException(Exception e) {
		l.error("Some error occured", e);
		if (connector.shouldCreateTable(e.getMessage())) {
			try {
				createTable();
			} catch (Exception e1) {
				e1.printStackTrace();
				return false;
			}
			return true;
		}
		if (connector.shouldAddField(e.getMessage())) {
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
		Object objid = -1;
		if (followIds) {
			try {
				Field f = getKeyField(obj);
				objid = f.get(obj);
				if (hasWithQuery(new FastMap<String, String>().add(f.getName(), objid + "")))
					return null;
			} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
				if (HandleException(e)) {
					return insert(obj);
				}
			}
		}
		HashMap<String, String> datamap = getSqlDataMap(obj, objid);
		try {
			connector.insertData(tb, datamap);
		} catch (Exception e) {
			if (HandleException(e))
				return insert(obj);
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
			if (HandleException(e)) {
				l.info("Triggering Retry!");
				return hasWithQuery(where);
			}
			return false;
		}
	}

	public HashMap<String, String> getSqlDataMap(Object obj, Object objid) {
		HashMap<String, String> data = new HashMap<>();
		for (Field f : obj.getClass().getFields()) {
			if (!IncludeField(f))
				continue;
			if (SmartTableObjectNoKey.class.isAssignableFrom(f.getType())) {
				try {
					Object smo = f.get(obj);
					if (smo == null)
						continue;
					String table = ((SmartTableObjectNoKey) smo).getTable();
					SmartTable<SmartTableObjectNoKey> t = new SmartTable<SmartTableObjectNoKey>(table,
							(Class<? extends SmartTableObjectNoKey>) f.getType());

					Object id = getKey(t.set(f.get(obj)));
					data.put(f.getName(), id + "");
				} catch (Exception e) {
					System.err.println("[SET] Failed to parse " + f.getName() + " as SmartTableObjectNoKey from class "
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
					List<Object> oldObjects = new ArrayList<Object>();
					ArrayList<Object> newObjects = new ArrayList<Object>();
					SmartTableArrayObject first = a.get(0);
					Class<? extends SmartTableObjectNoKey> c = first.getClass();
					SmartTable<SmartTableArrayObject> t = new SmartTable<SmartTableArrayObject>(first.getTable(), c);
//					for (SmartTableArrayObject p : t.getMultiple("parent=" + objid)) {
					for (SmartTableArrayObject p : t
							.getMultiple(new FastMap<String, String>().add("parent", objid + "").getMap())) {
						oldObjects.add(getKey(p));
					}
					for (SmartTableArrayObject b : a) {
						String q = "parent=" + objid + " AND " + b.getQuery();
						SmartTableArrayObject stao = t.get(new MapUtils<String, String>().mergeMap(b.getQuery(),
								new FastMap<String, String>().add("parent", objid + "").getMap()));
						if (stao != null)
							setKey(b, getKey(stao));
						b.parent = objid;
						newObjects.add(getKey(b));
						t.set(b);
					}

					for (Object integer : newObjects) {
						oldObjects.remove(integer);
					}
					
					oldObjects = oldObjects.stream().filter(x -> newObjects.stream().anyMatch(y -> y == x)).collect(Collectors.toList());
					
					try {
						oldObjects = oldObjects.stream().filter(x -> newObjects.stream().anyMatch(y -> new Gson().toJson(x).equals(new Gson().toJson(y)))).collect(Collectors.toList());
					} catch (Exception ex) {}
					
					for (Object integer : oldObjects) {
						Object newInstance = null;
						try {
							newInstance = a.get(0).getClass().newInstance();
						} catch (InstantiationException e) {
							e.printStackTrace();
						}
						if (newInstance == null)
							continue;
						setKey(newInstance, integer);
						t.delete(newInstance);
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
		// Get the new changed object
		HashMap<String, String> dataObj;
		try {
			dataObj = getSqlDataMap(obj, getKey(obj));
		} catch (IllegalArgumentException | SecurityException e1) {
			throw new RuntimeException("Entity " + obj.getClass().getName() + " does not contain a key field.");
		}

		// Get the previously cached version of the db image
		HashMap<String, String> dataRef = null;
		Object smrtref = ((SmartTableObjectNoKey) obj).smrtref;
		if (smrtref != null) {
			try {
				dataRef = getSqlDataMap(smrtref, getKey(smrtref));
			} catch (IllegalArgumentException | SecurityException e1) {
				throw new RuntimeException("Entity " + smrtref.getClass().getName() + " does not contain a key field.");
			}
		}

		// Get the fields that have been changed and put them to the map
		HashMap<String, String> data = new HashMap<String, String>();

		if (dataRef == null)
			data = dataObj;
		else {
			for (String s : dataObj.keySet()) {
				if (!dataObj.get(s).equals(dataRef.get(s))) {
					data.put(s, dataObj.get(s));
				}
			}
		}

		if (data.size() != 0) {
			// Only update the changed values
			try {
				Field key = getKeyField(obj);
				connector.updateData(tb, data,
						new FastMap<String, String>().add(key.getName(), key.get(obj) + "").getMap());
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
				throw new RuntimeException("Entity " + obj.getClass().getName() + " does not contain a key field.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return (T) obj;
	}

	@SuppressWarnings("unchecked")
	public T set(Object obj) {
		Object id = null;
		Field key = getKeyField(obj);
		try {
			id = getKey(obj);
		} catch (IllegalArgumentException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException("Entity " + obj.getClass().getName() + " does not contain a key field.");
		}
		if (id == null || (id instanceof Integer && (int) id == 0)) {
			id = getNextId(key.getName());
			try {
				setKey(obj, id);
			} catch (IllegalArgumentException | SecurityException e) {
				e.printStackTrace();
			}
		}

		if (hasWithQuery(new FastMap<String, String>().add(key.getName(), id + ""))) {
			update(obj);
		} else {
			insert(obj);
		}
		c.clear();
		return (T) obj;

	}

	public int getNextId(String fName) {
		try {
			int max = connector.getMax(tb, fName);
			return max + 1;
		} catch (Exception e) {
			if (HandleException(e))
				return getNextId(fName);
			e.printStackTrace();
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	public T delete(Object obj) {
		Object id = null;
		Field key = getKeyField(obj);
		try {
			id = getKey(obj);
		} catch (IllegalArgumentException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException("Entity " + obj.getClass().getName() + " does not contain a key field.");
		}
		try {
			connector.delete(tb, new FastMap<String, String>().add(key.getName(), id + "").getMap());
		} catch (Exception e) {
			e.printStackTrace();
		}
		c.clear();
		return (T) obj;
	}

}

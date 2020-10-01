package org.xbf.core;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.LoggerFactory;
import org.xbf.core.Models.RunningService;
import org.xbf.core.Plugins.Service;
import org.xbf.core.Plugins.XService;
import org.xbf.core.Utils.Map.FastMap;
import org.xbf.core.Utils.Time.TimeSync;

import ch.qos.logback.classic.Logger;

public class ServiceController {

	static ArrayList<Class<? extends Service>> services = new ArrayList<>();
	static HashMap<Integer, Service> runningServices = new HashMap<>();
	static ArrayList<Long> launchingServices = new ArrayList<>();
	public static boolean dontRemoveFromDatabase = false;
	
	static Logger l = (Logger) LoggerFactory.getLogger(ServiceController.class);
	
	public synchronized static Service Register(Service t) {
		return t;
//		if(ServiceRegistered(t.name))
//			return t;
//		services.add(t);
//		return t;
	}
	
	public static String getServiceNameFromClass(Class<? extends Service> serviceClass) {
		return getServiceData(serviceClass).name();
	}
	
	public static XService getServiceData(Class<? extends Service> serviceClass) {
		return serviceClass.getAnnotation(XService.class);
	}
	
	public static XService getServiceDataByName(String name) {
		return getServiceByName(name).getAnnotation(XService.class);
	}
	
	public static boolean ServiceRegistered(String name) {
		for (Class<? extends Service> s : services) {
			if(getServiceNameFromClass(s).equals(name))
				return true;
		}
		return false;
	}

	public static String[] getServiceNames() {
		String[] names = new String[services.size()];
		int i = 0;
		for (Class<? extends Service> s : services) {
			names[i] = getServiceNameFromClass(s);
			System.out.println("S" + i + ": " + names[i]);
			i++;
		}
		return names;
	}

	public static boolean HasService(String name) {
		return RunningService.getSmartTable().hasWithQuery(new FastMap<String, String>().add("serviceName", name));
	}
	
	public synchronized static void XStartedTrigger() {
		for (Class<? extends Service> service : services) {
//			System.out.println("Trying " + service.name);
			if(HasService(getServiceNameFromClass(service))) {
//				System.out.println("Launching instances of " + service.name);
				for (Service s : GetServices(getServiceNameFromClass(service))) {
					TriggerStop(s.id); // Removes old instance of service
					l.debug("Starting Service" + s.name + ": " + String.join(" ", s.args));
					startService(s.name, s.args);
				}
			}
		}
	}
	
	private static List<Service> GetServices(String name) {
		ArrayList<Service> sr = new ArrayList<>();
		try {
			List<RunningService> rServices = RunningService.getSmartTable().getMultiple(new FastMap<String, String>()
					.add("serviceName", name));
			Class<? extends Service> s = getServiceByName(name);
			for (RunningService r : rServices) {
				Service a = s.newInstance();
				a.args = new String[0];
				if(r.args != null)
					a.args = r.args.split(" ");
				a.id = r.serviceId;
				sr.add(a);
			}
		} catch (Exception e) {
			
		}
		return sr;
	}



	public synchronized static Service startService(String name, String[] args) {
		if(!getServiceDataByName(name).multipleInstances() && IsRunning(name))
			throw new RuntimeException("Can't start service marked with allowMultipleInstances as false with another instance running");
		try {
			return startService(getServiceByName(name).newInstance(), args);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public synchronized static Service startService(Service s, String[] args) {
		s.name = getServiceNameFromClass(s.getClass());
		if(!getServiceDataByName(s.name).multipleInstances() && IsRunning(s.name)) {
			throw new RuntimeException("Can't start service marked with allowMultipleInstances as false with another instance running");
		}
		s.args = args;
		s.id = new Random().nextInt(Integer.MAX_VALUE);
		s.thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				boolean boo = ServiceController.requestServiceStartKey();
				if(boo)
					return;
				s.l = (Logger) LoggerFactory.getLogger(s.getClass());
				s.l.info("Service " + s.name + " is launching (TID: " + Thread.currentThread().getId() + ", SID: " + s.id + ", ARGS: " + String.join(" ", args) + ")");
				s.onStart();
				if(s.timeSync) {
					TimeSync.timeSyncToMinute();
				}
				if(s.once) {
					try {
						s.run();
					} catch (Exception e) {
						s.l.error("An error occured in a service", e);
					}
					s.onStop();
					s.l.info("Service " + s.name + " has been stopped ( ran once )");
					ServiceController.TriggerStop(s.id);
					return;
				}
				
				while(!Thread.currentThread().isInterrupted() && !s.once) {
					try {
						s.run();
					} catch (Exception e) {
						if(e instanceof InterruptedException) {
//							Services.TriggerStop(s.id);
							break;
						}
						if(s.errors++ >= 50)
							stopService(s.id);
						s.errors++;
						s.l.error("An error occured in a service", e);
					}
				}
				
				s.onStop();
				s.l.info("Service " + s.name + " has been stopped");
				ServiceController.TriggerStop(s.id);
			}
		}, "Serv." + s.name);
		s.thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				// Uncaught error will cause the thread to terminate therefor reporting the error and calling stop on the database and local image
				s.l.error("A uncaught exception occured in a service", e);
				TriggerStop(s.id);
			}
		});
		s.thread.setDaemon(false);
		s.thread.start();
		RunningService srv = new RunningService();
		srv.serviceId = s.id;
		srv.serviceName = s.name;
		srv.args = String.join(" ", args);
		RunningService.getSmartTable().set(srv);
		runningServices.put(s.id, s);
		return runningServices.get(s.id);
	}

	/**
	 * 
	 * @return Can launch or has service thread already called this?
	 */
	protected synchronized static boolean requestServiceStartKey() {
		long id = Thread.currentThread().getId();
		if(launchingServices.contains(id))
			return true;
		
		launchingServices.add(id);
		return false;
	}



	protected synchronized static Service TriggerStop(int id) {
		
		if(!dontRemoveFromDatabase ) {
			RunningService srv = RunningService.getSmartTable().get(new FastMap<String, String>().add("serviceId", id + ""));
			RunningService.getSmartTable().delete(srv);
		}
		return runningServices.remove(id); // Remove the running service instance
	}
	
	public synchronized static Class<? extends Service> getServiceByName(String name) {
		for (Class<? extends Service> service : services) {
			String n = getServiceNameFromClass(service);
			if(n.equalsIgnoreCase(name))
				return service;
		}
		return null;
	}
	
	public synchronized static Service getRunningServiceByName(String name) {
		for (Service service : runningServices.values()) {
			if(service.name.equalsIgnoreCase(name))
				return service;
		}
		return null;
	}
	
	public synchronized static List<Service> getRunningServicesByName(String name) {
		ArrayList<Service> srvs = new ArrayList<>();
		for (Service service : runningServices.values()) {
			if(service.name.equalsIgnoreCase(name))
				srvs.add(service);
		}
		return srvs;
	}
	
	public synchronized static boolean stopService(String name) {
		return stopService(name, false);
	}
	
	public synchronized static boolean stopService(String name, boolean force) {
		boolean success = true;
		for (Service service : getRunningServicesByName(name)) {
			if(!stopService(service.id, force))
				success = false;
		}
		return success;
	}
	
	public synchronized static boolean stopService(int id, boolean force) {
		if(!runningServices.containsKey(id)) {
			return false;
		}
		try {
			Service s = runningServices.get(id);
			if(force) {
				s.thread.interrupt();
				s.thread.stop();
				TriggerStop(id);
			} else {
				s.thread.interrupt();
			}
		} catch (Exception e) {
			return false;
		}
		
		return true;
		
	}
	
	public synchronized static boolean stopService(int id) {
		return stopService(id, false);
	}
	
	public synchronized static void stopAllServices() {
		for (Integer i : runningServices.keySet()) {
			stopService(i);
		}
	}



	public static boolean IsRunning(String name) {
		return HasService(name);
	}



	public static List<Class<? extends Service>> getServices() {
		return services;
	}
	
}

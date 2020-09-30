package org.xbf.core.Utils.Arrays;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ArrayUtils {

	/**
	 * Removes all values from an array until the specified index
	 * @param array The array to remove from
	 * @param index The index to remove until
	 * @return The removed map
	 * @see subarrayStart
	 */
	@Deprecated
	public static String[] removeUntil(String[] array, int index) {
		String[] a = new String[array.length - index];
		for (int i = 0; i < a.length; i++) {
			a[i] = array[i + index];
		}
		return a;
	}
	
	/**
	 * Gets the last string entries from an array
	 * @param last The amount of values to get
	 * @param arr The array to get from
	 * @return The last values from the array
	 * @see subarrayEnd
	 */
	@Deprecated
	public static String[] getLast(int last, String[] arr) {
		if(last > arr.length) last = arr.length;
		String[] res = new String[last];
		for (int i = 0; i < last; i++) {
			res[i] = arr[arr.length - last + i];
		}
		return res;
	}
	
	/**
	 * Converts a set to a list
	 * @param set The set
	 * @return A list
	 */
	public static <T> List<T> convertSetToList(Set<T> set) {
		// create an empty list
		List<T> list = new ArrayList<>();

		// push each element in the set into the list
		for (T t : set)
			list.add(t);

		// return the list
		return list;
	}
	
	/**
	 * Like substring but for arrays
	 * @param arr The array
	 * @param start The start index
	 * @param end The end index
	 * @return The array with values between the start and end
	 */
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> subarray(T[] arr, int start, int end) {
		ArrayList<T> a = new ArrayList<T>();
		for (int i = start; i < end; i++) {
			a.add(arr[i]);
		}
		return a;
	}
	
	/**
	 * Like substring but for arrays
	 * @param arr The array
	 * @param start The start index
	 * @return The array with values between the start and end
	 */
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> subarrayStart(T[] arr, int start) {
		return subarray(arr, start, arr.length);
	}
	
	/**
	 * Like substring but for arrays
	 * @param arr The array
	 * @param end The end index
	 * @return The array with values between the start and end
	 */
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> subarrayEnd(T[] arr, int end) {
		return subarray(arr, 0, end);
	}

	public static String objectArrayToString(String separator, ArrayList<Object> evaluation) {
		String str = "";
		for (Object object : evaluation) {
			str += object + separator;
		}
		return str.substring(0, str.length() - separator.length());
	}
	
	
	
	
}

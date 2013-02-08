package utils;

import java.util.HashMap;
import java.util.Map;

public class CaseInsensitiveHashMap<T> extends HashMap<String, T> {

	@Override
	public T put(String key, T value) {
		return super.put(key.toLowerCase(), value);
	}
	
	@Override
	public void putAll(Map map) {
		for (Object s : map.keySet()) {
			put((String) s, (T) map.get((String) s));
		}
	}
	
	@Override
	public T get(Object key) {
		if(key == null) return null;
		return super.get(((String) key).toLowerCase());
	}
	
	@Override
	public boolean containsKey(Object key) {
		return super.containsKey(((String)key).toLowerCase());
	}
	
//	public static void main(String[] args) {
//		Map<String, String> m = new CaseInsensitiveHashMap<String>();
//		m.put("HallO", "Hei");
//		System.out.println(m.get("hallo"));
//		System.out.println(m.get("hAlLo"));
//		System.out.println(m.get("hAlLo22222222222"));
//	}
}

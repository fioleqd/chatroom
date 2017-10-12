package changer;

import java.util.HashMap;

public class DataChanger {
	public static HashMap<String, String> returnIDName(String string){
		String[] ids=string.substring(0,string.indexOf('|')).split(",");
		String[] names=string.substring(string.indexOf('|')+1).split(",");
		HashMap<String, String> idNameMap=new HashMap<>();
		for(int i=0;i<ids.length;i++){
			idNameMap.put(ids[i],names[i]);
		}
		return idNameMap;
	}
	public static String[] returnID(String string){
		String[] ids=string.substring(0,string.indexOf('|')).split(",");
		return ids;
	}
}

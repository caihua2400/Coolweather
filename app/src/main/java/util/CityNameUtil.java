package util;

import android.text.TextUtils;
import android.util.Log;

public class CityNameUtil {

	public static String SelectCity(String SelectedLoc,String SelectCityName,String SelectProvinceName){
//        Log.d("CityNameUt:",SelectCityName);
		String locLevel ="";
		if(!TextUtils.isEmpty(SelectedLoc)&&SelectedLoc.length()>2){
			locLevel = SelectedLoc.substring(SelectedLoc.length()-1);
            Log.d("CityNameUtil",locLevel);
		}else{
			return SelectedLoc;
		}
		if("县".equals(locLevel)){
			return SelectedLoc.substring(0,SelectedLoc.length()-1);
		}else if("区".equals(locLevel)){
			return SelectCityName.substring(0,SelectCityName.length()-1);
		}else if("道".equals(locLevel)){
			return SelectProvinceName.substring(0,SelectProvinceName.length()-1);
		}
		return null;
	}
	
	
	
}

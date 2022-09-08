package org.cocos2dx.cpp;

import android.content.Context;
import android.telephony.TelephonyManager;


/**
 * @author skywar
 * 运营商帮助类
 */
public class Carrier {
	/**
	 * 未知运营商
	 */
	static public final int Unknown = -1;

	/**
	 * 中国移动
	 */
	static public final int ChinaMobile = 10; 
	
	/**
	 * 中国电信
	 */
	static public final int ChinaTelcom = 20;
	
	/**
	 * 中国联通
	 */
	static public final int ChinaUnicom = 30;
	
	/**
	 * 无运营商，不调用计费
	 */
	static public final int Simulation = 99;
	
	static private int mCarrier = ChinaMobile;
	
    static public int getCarrier()
    {
        return mCarrier;
    }

    /**
	 * @param context
	 * @return 当前用户号码归属的运营商
	 */
	static public int getCarrier(Context context)
	{
		return ChinaMobile;
	}
}

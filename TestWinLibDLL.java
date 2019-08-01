package com.test.date;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

/**
 * Function description:
 * 		This service will support to ZKTECO REK system drivers, than include fetching log data, getting hardware information, 
 * 		control hardware lock, and other remote operation.
 * 
 * Support Require:
 * 		This Service need Java COM Bridge (jacob.jar) support
 * 
 * Configuration:
 * 		1. Import jacob.jar to java project library.
 * 		2. Import java COM bridge jacob-1.19-xXX.dll into installed JDK bin directory.
 *		3. Register zkemkeeper.dll into running windows environment using Register_SDK.bat at ZKTECO develop package SDK.
 *		4. The above description java-COM-Bridge and ZKEMKEEPER DLL has different 64bits and 32bits, than according with your running environment choice.
 * 
 * Author:
 * 		Name:	Cheny
 * 		Email:	mimi2008yu@163.com
 * */

public class TestWinLibDLL {	
	private static ActiveXComponent zkem = new ActiveXComponent("zkemkeeper.ZKEM.1");
	
	public static boolean connect(String address, int port){
		boolean result = zkem.invoke("Connect_Net", address, port).getBoolean();
		return result;
	}
	
	public static void disConnect(){
		zkem.invoke("Disconnect");
	}
	
	public static int getConnectStatus(){
		Variant code = new Variant(0, true);
		zkem.invoke("GetConnectStatus", code);
		return code.getIntRef();
	}
	
	public static Boolean ClearGLog(Integer dwMachineNumber){
		boolean result = zkem.invoke("ClearGLog", dwMachineNumber).getBoolean();
		return result;
	}
	
	public static Boolean ACUnlock(Integer dwMachineNumber, Integer Delay){
		boolean result = zkem.invoke("ACUnlock", dwMachineNumber, Delay).getBoolean();
		if (result) {
			PlayVoiceByIndex(0);
		}
		return result;
	}
	
	/**
	 * Inner voice index member
	 * 0.  谢谢/Thanks
	 * 1.  密码错误/Password fault
	 * 2.  非法管理/illegal manage
	 * 3.  ID号错误/ID fault
	 * 4.  请重按手指/Retry fingerprint
	 * 5.  ID号重复/ID repeat
	 * 6.  记录溢出/Out of record
	 * 7. 	
	 * 8.  指纹重复/Fingerprint repeat
	 * 9.  已签到谢谢/Thanks registered
	 * 10. (啊偶)/AOH
	 * 11. (警报1)/Emergency voice
	 * 12. 	
	 * 13. (警报2)/Emergency voice
	 * 14. 
	 * */
	public static Boolean PlayVoiceByIndex(Integer Index){
		boolean result = zkem.invoke("PlayVoiceByIndex", Index).getBoolean();
		return result;
	}
	
	public static Boolean ReadNewGLogData(Integer dwMachineNumber){
		boolean result = zkem.invoke("ReadNewGLogData", dwMachineNumber).getBoolean();
		return result;
	}
	
	public static List<Map<String,Object>> GetGeneralLogData(boolean readNewLog){
		Integer dwMachineNumber = 1;		
		Variant dwEnrollNumber = new Variant("", true);
		Variant dwVerifyMode = new Variant(0, true);
		Variant dwInOutMode = new Variant(0, true);
		Variant dwYear = new Variant(0, true);
		Variant dwMonth = new Variant(0, true);
		Variant dwDay = new Variant(0, true);
		Variant dwHour = new Variant(0, true);
		Variant dwMinute = new Variant(0, true);
		Variant dwSecond = new Variant(0, true);
		Variant dwWorkCode = new Variant(0, true);
		List<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();
		boolean hasData = false;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		boolean hasNewLog = false;
		
		if (readNewLog) {
			hasNewLog = ReadNewGLogData(dwMachineNumber);
		}
		
		if ( readNewLog==false || (readNewLog && hasNewLog)) {			
			do {
				Variant result = Dispatch.call(zkem, "SSR_GetGeneralLogData", dwMachineNumber, dwEnrollNumber, dwVerifyMode, dwInOutMode, dwYear, dwMonth, dwDay, dwHour, dwMinute, dwSecond, dwWorkCode);
				hasData = result.getBoolean();			
				if (hasData) {
					try {
						Map<String, Object> data = new HashMap<String, Object>();
						data.put("dwEnrollNumber", dwEnrollNumber.getStringRef());
						data.put("dwVerifyMode" , dwVerifyMode.getIntRef()); 
						data.put("dwInOutMode" , dwInOutMode.getIntRef());				
						data.put("date" , dateFormat.parseObject(dwYear.getIntRef() + "/" + dwMonth.getIntRef() + "/" + dwDay.getIntRef() + " " + dwHour.getIntRef()+  ":" + dwMinute.getIntRef() + ":" + dwSecond.getIntRef()) );				
						data.put("dwWorkCode" , dwWorkCode.getIntRef());				
						dataList.add(data);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} while (hasData);
		}
		
		return dataList;
	}
	
	public static void main(String[] args) {
		boolean CONNECT_STATUS = connect("192.168.20.192", 4370);		
		System.out.println("Connect: " + CONNECT_STATUS);
		/*
		boolean READ_NEWLOG = ReadNewGLogData(1);
		System.out.println("NewLog: " + READ_NEWLOG);*/
		
		List<Map<String, Object>> dataList = GetGeneralLogData(true);		
		
		/*
		boolean CLEAR_STATUS = ClearGLog(1);
		System.out.println("Clear: " + CLEAR_STATUS);
		*/
		
		for (int i = 0; i < dataList.size(); i++) {
			Map<String, Object> d0 = dataList.get(i);
			
			System.out.print((i+1) + "\t");
			for (Entry<String, Object> map : d0.entrySet()) {
				System.out.print(map.getKey() + ":" + map.getValue() + "\t");
			}
			System.out.println();
		}
		
		ACUnlock(1, 5);
		
		disConnect();
	}

}

package com.asiainfo.aiedri;

import com.asiainfo.aiedri.util.IPv4Util;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

public class Test {

	public static void testObject(String test){
		String ipAndMask = "219.157.0.0/15";

		String ipMaskWL = "219.158.0.0/16";

		int[] ipScope = IPv4Util.getIPIntScope(ipAndMask);


			int[] tmpIpScope = IPv4Util.getIPIntScope(ipMaskWL);

			if (!(ipScope[1] < tmpIpScope[0] || ipScope[0] > tmpIpScope[1])) {
				System.out.println(ipAndMask + " is in " + ipMaskWL);
			}else
				System.out.println(ipAndMask + " is not in " + ipMaskWL);

	}



	public static StringBuffer toBin(int x)
	{
		StringBuffer result=new StringBuffer();
		result.append(x%2);
		x/=2;
		while(x>0){
			result.append(x%2);
			x/=2;
		}
		return result;
	}

	public static int maskToInt(String strip){
		StringBuffer sbf;
		String str;
		int inetmask=0,count=0;
		String[] ipList=strip.split("\\.");
		for(int n=0;n<ipList.length;n++)
		{

			sbf = toBin(Integer.parseInt(ipList[n]));
			str=sbf.reverse().toString();
			System.out.println(ipList[n]+"---"+str);


			count=0;
			for(int i=0;i<str.length();i++){
				i=str.indexOf('1',i);
				if(i==-1){break;}
				count++;
			}
			inetmask+=count;

		}
		System.out.println(""+inetmask);
		return inetmask;
	}

	public static void main(String[] args) {

		test1();

		System.exit(0);

		Integer addCfgId;
		Integer curMaxCfgId = 2277;
		Integer MAX_CFG_ID = 65535;
		do {
			curMaxCfgId++;
			addCfgId = curMaxCfgId % MAX_CFG_ID;
			System.out.println(addCfgId +"|" + curMaxCfgId);
			try {
				Thread.sleep(1000l);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (addCfgId < 1000000);




		try {
			File file = new File("/Users/jiaojian/Downloads/bgp.idr");
			BufferedReader reader = new BufferedReader(new FileReader(file));


			String line ;

			while((line = reader.readLine()) != null){
				String[] items = line.split("\\|",-1);

				String cfgId = items[0];

				String sip = items[2].split("/")[0];

				String dip = items[3].split("/")[0];;

				if(!sip.trim().equals(""))
					System.out.println(String.format("insert into aiedri_rule_cfg_rel(cfg_id , rule_id) values(%s , (select rule_id from aiedri_task_tuple where sip = \"%s\" and request_id = 17263));", cfgId ,sip));

				if(!dip.trim().equals(""))
					System.out.println(String.format("insert into aiedri_rule_cfg_rel(cfg_id , rule_id) values(%s , (select rule_id from aiedri_task_tuple where dip = \"%s\" and request_id = 17263));", cfgId ,dip));


			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static String changePropertyToSql(String prop){
		if(StringUtils.isEmpty(prop)){
			return "";
		}
		StringBuilder sb=new StringBuilder();
		int prePos=0,pos=0;
		for(int i=0;i<prop.length();i++){
			if(Character.isUpperCase(prop.charAt(i))){
				pos=prePos;
				prePos=i;
				sb.append(prop.subSequence(pos, prePos));
				sb.append("_");
			}
		}
		sb.append(prop.subSequence(prePos, prop.length()));
		System.out.println(sb.toString().toUpperCase());
		return sb.toString().toUpperCase();
		
	}

	public static void test1(){
		int b;
		int a= b=5;
		String s1="祝你今天考出好成绩!";
		String s2="祝你今天考出好成绩!";

		System.out.println((a==b) + "" + (s1==s2));
	}
}

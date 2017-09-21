package com.asiainfo.aiedri.util;

import org.apache.log4j.Logger;

/**
 * Created by jiaojian on 17/3/13.
 */
public class AiedriUtil {

    private static Logger log = Logger.getLogger(AiedriUtil.class);

    private static StringBuffer toBin(int x) {
        StringBuffer result = new StringBuffer();
        result.append(x % 2);
        x /= 2;
        while (x > 0) {
            result.append(x % 2);
            x /= 2;
        }
        return result;
    }

    public static int maskToInt(String strip) {
        StringBuffer sbf;
        String str;
        int inetmask = 0, count = 0;
        String[] ipList = strip.split("\\.");
        for (int n = 0; n < ipList.length; n++) {

            sbf = toBin(Integer.parseInt(ipList[n]));
            str = sbf.reverse().toString();
            log.debug(ipList[n] + "---" + str);


            count = 0;
            for (int i = 0; i < str.length(); i++) {
                i = str.indexOf('1', i);
                if (i == -1) {
                    break;
                }
                count++;
            }
            inetmask += count;

        }
        log.debug("" + inetmask);
        return inetmask;
    }

    public static void main(String[] args) {
        System.out.println(maskToInt("1.1.1.1"));


    }

    public static String getAinxHome() {
        String path = System.getProperty("ainx.home");
        if (path == null) {
            path = System.getProperty("AINX_HOME");
        }
        if (path == null) {
            path = System.getProperty("user.home");
        }
        return path;

    }
}

package com.asiainfo.aiedri.vo;

/**
 * Created by jiaojian on 17/3/13.
 */
public class ProtectedIpMask {

    String ipAndMask ;

    int[] ipscope;

    public String getIpAndMask() {
        return ipAndMask;
    }

    public void setIpAndMask(String ipAndMask) {
        this.ipAndMask = ipAndMask;
    }

    public int[] getIpscope() {
        return ipscope;
    }

    public void setIpscope(int[] ipscope) {
        this.ipscope = ipscope;
    }
}

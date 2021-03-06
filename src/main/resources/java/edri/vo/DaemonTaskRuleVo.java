package com.asiainfo.aiedri.vo;

/**
 * Created by jiaojian on 17/2/27.
 */
public class DaemonTaskRuleVo {
    private Integer cfgId;

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    private Integer requestId;

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    private Integer taskType;
    private Integer taskId;


    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    //REQUEST
    private Integer requestType;

    //TASK

    //rule
    private Integer id;
    private String validAfter;
    private String validBefore;
    private String validScope;
    private String ruleId;
    private String ruleType;
    private String sip;
    private String dip;
    private String smask;
    private String dmask;
    private Integer sport;
    private Integer dport;
    private String spmask;
    private String dpmask;
    private String proto;
    private String pmask;
    private Integer uplink;
    private Integer downlink;

    //task



    public Integer getRequestType() {
        return getTaskType();
    }

    public void setRequestType(Integer requestType) {

        setTaskType(requestType);
    }



    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getValidAfter() {
        return validAfter;
    }
    public void setValidAfter(String validAfter) {
        this.validAfter = validAfter;
    }
    public String getValidBefore() {
        return validBefore;
    }
    public void setValidBefore(String validBefore) {
        this.validBefore = validBefore;
    }
    public String getValidScope() {
        return validScope;
    }
    public void setValidScope(String validScope) {
        this.validScope = validScope;
    }
    public String getRuleId() {
        return ruleId;
    }
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
    public String getRuleType() {
        return ruleType;
    }
    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }
    public String getSip() {
        return sip;
    }
    public void setSip(String sip) {
        this.sip = sip;
    }
    public String getDip() {
        return dip;
    }
    public void setDip(String dip) {
        this.dip = dip;
    }
    public String getSmask() {
        return smask;
    }
    public void setSmask(String smask) {
        this.smask = smask;
    }
    public String getDmask() {
        return dmask;
    }
    public void setDmask(String dmask) {
        this.dmask = dmask;
    }
    public Integer getSport() {
        return sport;
    }
    public void setSport(Integer sport) {
        this.sport = sport;
    }
    public Integer getDport() {
        return dport;
    }
    public void setDport(Integer dport) {
        this.dport = dport;
    }
    public String getSpmask() {
        return spmask;
    }
    public void setSpmask(String spmask) {
        this.spmask = spmask;
    }
    public String getDpmask() {
        return dpmask;
    }
    public void setDpmask(String dpmask) {
        this.dpmask = dpmask;
    }
    public String getProto() {
        return proto;
    }
    public void setProto(String proto) {
        this.proto = proto;
    }
    public String getPmask() {
        return pmask;
    }
    public void setPmask(String pmask) {
        this.pmask = pmask;
    }
    public Integer getUplink() {
        return uplink;
    }
    public void setUplink(Integer uplink) {
        this.uplink = uplink;
    }
    public Integer getDownlink() {
        return downlink;
    }
    public void setDownlink(Integer downlink) {
        this.downlink = downlink;
    }

    public Integer getCfgId() {
        return cfgId;
    }

    public void setCfgId(Integer cfgId) {
        this.cfgId = cfgId;
    }
}

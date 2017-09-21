package com.asiainfo.aiedri.rest.bean;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by jiaojian on 17/9/8.
 */
@XmlRootElement
public class WhiteListBean {
    private String rule_id;
    private String rule_type;
    private QunitWhiteList rule;

}

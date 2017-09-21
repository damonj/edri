package com.asiainfo.aiedri.util;

public class RuleConstant {
	public static final Integer RULE_WAIT_CHECK=0;//待审核
	public static final Integer RULE_CHECK_FAIL=1; //不合法规则，不予下发
	public static final Integer RULE_WAIT_AGAIN=2;//环境不允许当前下发，可以等待
	public static final Integer RULE_WAIT_ISSUE=3;//check 成功，等待生成下发任务
	public static final Integer RULE_ISSUING=4;//生成下发任务
	public static final Integer RULE_ISSUED=5;//下发完成
	public static final Integer RULE_DELETED=6;//规则失效
	public static final Integer RULE_EXIST=7;//规则生效
	public static final Integer RULE_MERGED=8;//规则合并
}

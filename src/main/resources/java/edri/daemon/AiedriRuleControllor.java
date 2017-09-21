package com.asiainfo.aiedri.daemon;

import ainx.common.spring.BeanFactoryUtil;
import ainx.common.spring.jdbc.EJB3AnnontationRowMapper;
import ainx.common.util.SequenceUtil;
import com.asiainfo.aiedri.bo.IAiedriDeviceBo;
import com.asiainfo.aiedri.bo.IAiedriRuleConfigBo;
import com.asiainfo.aiedri.dao.IAiedriTaskManageDAO;
import com.asiainfo.aiedri.util.*;
import com.asiainfo.aiedri.vo.*;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AiedriRuleControllor {

    List<AiedriDevices> devList = null;

    //Hashtable<String, String> aclPyFileHash = new Hashtable<String, String>();

    private String flowSpecPath;
    private ArrayList<DaemonTaskRuleVo> taskRuleVoList = null;
    private Boolean stopFlag = false;
    private boolean isDangerous = false;
    private ArrayList<ProtectedIpMask> ipMaskList = new ArrayList<ProtectedIpMask>();

    public void setAclPyPath(String aclPyPath) {
        this.aclPyPath = standardPath(aclPyPath);
    }

    private String aclPyPath;

    public String standardPath(String path) {
        if (path.endsWith(File.separator)) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    public void setFlowSpecPath(String flowSpecPath) {
        this.flowSpecPath = standardPath(flowSpecPath);
    }

    public void setIssueType(Integer issueType) {
        this.issueType = issueType;
    }

    private Integer issueType;
    Logger log = Logger.getLogger(getClass());

    //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    SimpleDateFormat cfgFileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    Hashtable<String, Integer> ruleCfgIdHash = new Hashtable<String, Integer>();

    Integer curMaxCfgId = 0;

    final static Integer MAX_CFG_ID = 65535;

    final static Integer ACL = 1;

    final static Integer FLOWSPEC = 2;

    final static Integer ACL_PY = 3;

    JdbcTemplate jdbcTemplate = (JdbcTemplate) BeanFactoryUtil
            .getBean("jdbcTemplate");

    IAiedriRuleConfigBo ruleConfigBo = (IAiedriRuleConfigBo) BeanFactoryUtil.getBean("aiedriRuleConfigBo");
    IAiedriDeviceBo deviceBo = (IAiedriDeviceBo) BeanFactoryUtil.getBean("aiedriDeviceBo");

    IAiedriTaskManageDAO aiedriTaskManageDAO = (IAiedriTaskManageDAO)BeanFactoryUtil.getBean("aiedriTaskManageDAO");

    List<DaemonRequestRuleVo> requestRuleVoList = new ArrayList<DaemonRequestRuleVo>();//first order rule list


    public static void main(String[] args) {

        if (args.length == 1) {
            String typeStr = args[0];
            Integer type = 0;
            if (typeStr.equals("acl"))
                type = AiedriRuleControllor.ACL;
            else {
                System.out.println("Usage: rule-center.sh acl [$path]|flowspec $path");
                System.exit(-1);
            }
            AiedriRuleControllor c = new AiedriRuleControllor();
            c.setIssueType(type);
            System.out.println("Start ACL model...");
            c.start();
        } else if (args.length == 2) {//flowspec /Users/jiaojian/Documents/IntelliJWorkspace/aiedri/flowspec
            AiedriRuleControllor c = new AiedriRuleControllor();
            String typeStr = args[0];
            String Path = args[1];
            Integer type = 0;
            if (typeStr.equals("flowspec")) {
                type = AiedriRuleControllor.FLOWSPEC;
                c.setFlowSpecPath(Path);
                System.out.println("Start FLOWSPEC model...");
            } else if (typeStr.equals("acl")) {
                type = AiedriRuleControllor.ACL_PY;
                c.setAclPyPath(Path);
                System.out.println("Start Acl Python model...");
            } else
                System.out.println("Usage: rule-center.sh acl [$path]|flowspec $path");


            c.setIssueType(type);

            c.start();

        } else
            System.out.println("Usage: rule-center.sh acl [$path]|flowspec $path");

    }

    public AiedriRuleControllor() {
    }

    /**
     * main process
     */
    public void start() {

        init();

//        mergeRules();//for test
//
        updateRuleStatus();
        System.exit(0);

        try {
            while (true) {
                syncCfgFromDB();

                checkTaskResult();

                getRequestRules();

                dealRequestRules();

                exeTasks();

                updateRuleStatus();


                if (stopFlag)
                    break;
                Thread.sleep(1000L);


            }
        } catch (InterruptedException e) {
            log.error(e.toString());
            e.printStackTrace();
        }


    }


    private void syncCfgFromDB() {
        //get rules config
        RuleGlobalConfig.MAX_RULES = Integer.parseInt(ruleConfigBo.findConfigParamByPName("maxNum"));


        Double minMask = (Double.parseDouble(ruleConfigBo.findConfigParamByPName("minMask")));
        RuleGlobalConfig.MIN_MASK = minMask.intValue();
        //get protectd list
        List<ProtectedIpsVo> protectedIpsVoList = ruleConfigBo.findAllProtectedIps();

        ipMaskList.clear();
        for (ProtectedIpsVo vo : protectedIpsVoList) {
            String ipAndMask = vo.getIp() + "/" + vo.getIpMask();
            int[] ipscope = IPv4Util.getIPIntScope(ipAndMask);

            ProtectedIpMask ipMask = new ProtectedIpMask();
            ipMask.setIpAndMask(ipAndMask);
            ipMask.setIpscope(ipscope);

            ipMaskList.add(ipMask);
        }

        if (devList != null) devList.clear();
        devList = deviceBo.findAllAiedriDevice();


    }

    private void
    dealRequestRules() {
        final ArrayList<String> ruleList = new ArrayList<String>();
        final ArrayList<Integer> requestIdList = new ArrayList<Integer>();
        for (DaemonRequestRuleVo vo : requestRuleVoList) {
            /**
             * check max rules
             */
            if (ruleCfgIdHash.size() >= RuleGlobalConfig.MAX_RULES) {
                //update other status
                String sql = "update aiedri_task_tuple set status = ? , status_desc = ? where rule_id = ? and REQUEST_ID = ?";
                Object[] param = new Object[]{RuleConstant.RULE_WAIT_AGAIN, "超出最大规则数", vo.getRuleId(),vo.getRequestId()};

                jdbcTemplate.update(sql, param);
                continue;
            }

            /**
             * check min mask
             */
            String smask = vo.getSmask();
            if (smask == null || smask.equals("")) {
                smask = "255.255.255.255";
                vo.setSmask(smask);
            }
            String dmask = vo.getDmask();
            if (dmask == null || dmask.equals("")) {
                dmask = "255.255.255.255";
                vo.setDmask(dmask);
            }
            Integer smaskInt = AiedriUtil.maskToInt(smask);
            Integer dmaskInt = AiedriUtil.maskToInt(dmask);
            log.debug("check min mask");
            if (smaskInt < RuleGlobalConfig.MIN_MASK || dmaskInt < RuleGlobalConfig.MIN_MASK) {
                String sql = "update aiedri_task_tuple set status = ? , status_desc = ? where rule_id = ? and REQUEST_ID = ?";
                Object[] param = new Object[]{RuleConstant.RULE_CHECK_FAIL, "掩码小于" + RuleGlobalConfig.MIN_MASK, vo.getRuleId(), vo.getRequestId()};
                jdbcTemplate.update(sql, param);
                continue;
            }

            log.debug("check port proto");
            /**
             * check port is not null, proto is tcp (6) or udp(17)
             */
            Integer dport = vo.getDport();
            Integer sport = vo.getSport();
            String proto = vo.getProto();
            if (proto == null || proto.trim().equals("")) {
                proto = "0";
                vo.setProto(proto);
            }
            if (dport != null || sport != null) {
                if (proto.equals("0") || proto.equals("ip")) {
                    String sql = "update aiedri_task_tuple set status = ? ,status_desc = ? where rule_id = ? and REQUEST_ID = ?";
                    Object[] param = new Object[]{RuleConstant.RULE_CHECK_FAIL, "端口不为空，协议不应为IP协议", vo.getRuleId(), vo.getRequestId()};
                    jdbcTemplate.update(sql, param);
                    continue;
                }
            }

            /**
             *  check protected list
             */
            log.debug("check proteced list");
            if (vo.getRequestType().equals(RequestConstant.REQUEST_TYPE_ADD) && vo.getSip() != null) {

                String ipAndMask = vo.getSip() + "/" + smaskInt;

                int[] ipScope = IPv4Util.getIPIntScope(ipAndMask);

                for (ProtectedIpMask ipMask : ipMaskList) {
                    int[] tmpIpScope = ipMask.getIpscope();

                    if (!(ipScope[1] < tmpIpScope[0] || ipScope[0] > tmpIpScope[1])) {
                        String sql = "update aiedri_task_tuple set status = ? , status_desc = ? where rule_id = ? and REQUEST_ID = ?";
                        Object[] param = new Object[]{RuleConstant.RULE_CHECK_FAIL, "规则符合白名单", vo.getRuleId(), vo.getRequestId()};
                        jdbcTemplate.update(sql, param);
                        break;
                    }
                }
            }

            /**
             * check flow more than threshold
             */
            Integer flowThresholdSign = getFlowThresholdSign();

            if (flowThresholdSign < 5 && flowThresholdSign > 0) {//if flow have some problem

                if (isDangerous) {//ever more than 5
                    if (flowThresholdSign <= 1) { //refuse add
                        if (vo.getRequestType().equals(RequestConstant.REQUEST_TYPE_ADD)) {
                            String sql = "update aiedri_task_tuple set status = ? , status_desc = ? where rule_id = ? and REQUEST_ID = ?";
                            Object[] param = new Object[]{RuleConstant.RULE_WAIT_AGAIN, "流速超限，拒绝新增规则", vo.getRuleId(), vo.getRequestId()};
                            jdbcTemplate.update(sql, param);
                            continue;
                        }
                    } else {//refuse all
                        String sql = "update aiedri_task_tuple set status = ? , status_desc = ? where rule_id = ? and REQUEST_ID = ?";
                        Object[] param = new Object[]{RuleConstant.RULE_WAIT_AGAIN, "流速超限，拒绝所有规则", vo.getRuleId(), vo.getRequestId()};
                        jdbcTemplate.update(sql, param);
                        continue;

                    }

                } else {//refuse add
                    if (vo.getRequestType().equals(RequestConstant.REQUEST_TYPE_ADD)) {
                        String sql = "update aiedri_task_tuple set status = ? , status_desc = ? where rule_id = ? and REQUEST_ID = ?";
                        Object[] param = new Object[]{RuleConstant.RULE_WAIT_AGAIN, "流速超限，拒绝新增规则", vo.getRuleId(), vo.getRequestId()};
                        jdbcTemplate.update(sql, param);
                        continue;
                    }
                }
            } else if (flowThresholdSign >= 5) {//refuse all
                isDangerous = true;
                String sql = "update aiedri_task_tuple set status = ? , status_desc = ? where rule_id = ? and REQUEST_ID = ?";
                Object[] param = new Object[]{RuleConstant.RULE_WAIT_AGAIN, "流速超限，拒绝所有规则", vo.getRuleId(), vo.getRequestId()};
                jdbcTemplate.update(sql, param);
                continue;

            }else if(flowThresholdSign == 0 ){//alarm release
                isDangerous = false;
            }
            String ruleId = vo.getRuleId();

            Integer taskType = vo.getRequestType();

            /**
             * make cfg id
             */
            log.debug("make cfg id");
            if (taskType.equals(TaskConstant.TASK_TYPE_ADD)) {
                if (ruleCfgIdHash.containsKey(ruleId)) {
                    Integer cfgId = ruleCfgIdHash.get(ruleId);
                    log.error(ruleId + " rule is exist,cfgid is " + cfgId);
                    vo.setCfgId(ruleCfgIdHash.get(ruleId));
                } else {
                    Integer addCfgId;
                    do {
                        curMaxCfgId++;
                        addCfgId = curMaxCfgId % MAX_CFG_ID;
                        log.debug(addCfgId +"|" + curMaxCfgId);
                    } while (ruleCfgIdHash.containsValue(addCfgId));

                    ruleCfgIdHash.put(ruleId, addCfgId);

                    insertRuleCfg(ruleId, addCfgId);

                    vo.setCfgId(addCfgId);
                }
            } else if (taskType.equals(TaskConstant.TASK_TYPE_DELETE)) {
                if (ruleCfgIdHash.containsKey(ruleId)) {
                    Integer delCfgId = ruleCfgIdHash.get(ruleId);
                    vo.setCfgId(delCfgId);
                    ruleCfgIdHash.remove(ruleId);
                    //delRuleCfg(ruleId);
                } else {//delete no cfg_id
                    log.error(ruleId + " is not exist , can not delete,refuze it!");
                    String sql = "update aiedri_task_tuple set status = ? , status_desc = ? where rule_id = ? and REQUEST_ID = ?";
                    Object[] param = new Object[]{RuleConstant.RULE_CHECK_FAIL, "规则未曾下发过，不能执行删除", vo.getRuleId(), vo.getRequestId()};
                    jdbcTemplate.update(sql, param);
                    continue;
                }
            }

            /**
             * record ip ruleId and requestId
             */

            ruleList.add(vo.getRuleId());
            requestIdList.add(vo.getRequestId());

            /**
             * update rule status
             */
            String sql = "update aiedri_task_tuple set status = ? where RULE_ID = ? and request_id = ? ";
            Object[] param = new Object[]{RuleConstant.RULE_WAIT_ISSUE, ruleId, vo.getRequestId()};

            jdbcTemplate.update(sql, param);

        }

        /**
         * insert task
         */
        if (ruleList.size() != 0) {
            for (AiedriDevices dev : devList) {

                final Integer taskId = SequenceUtil.getSequenceId("aiedriTaskId");

                String sql = "insert into aiedri_task(TASK_ID,CREATE_DATE,STATUS,DEAL_DATE,IP,RULE_ID,EXE_RESULT) values(?,?,?,?,?,?,?)";

                Object[] param = new Object[]{taskId, System.currentTimeMillis() / 1000l, TaskConstant.TASK_STATUS_CREATE, 0, dev.getIp(), "", ""};

                jdbcTemplate.update(sql, param);

                /**
                 * insert task_rule_rel
                 */
                sql = "insert into aiedri_task_rule_rel(TASK_ID,RULE_ID,REQUEST_ID) VALUES (?,?,?)";


                jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                        String ruleId = ruleList.get(i);
                        Integer requestId = requestIdList.get(i);
                        preparedStatement.setInt(1, taskId);
                        preparedStatement.setString(2, ruleId);
                        preparedStatement.setInt(3,requestId);
                    }

                    @Override
                    public int getBatchSize() {
                        return ruleList.size();
                    }
                });
            }

        }


    }

    private Integer getFlowThresholdSign() {
        File signFile = new File(AiedriUtil.getAinxHome() + File.separator + "etc" + File.separator + "flowThreshold.sign");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(signFile));

            String line = reader.readLine();

            if(line == null || line.trim().equals(""))
                line = "0";

            log.debug("flowThreshold.sign = " + line);


            return Integer.parseInt(line);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void getRequestRules() {
        //merge rules
        mergeRules();


        //get request rules
        try {
            if (requestRuleVoList != null)
                requestRuleVoList.clear();

            List<DaemonRequestRuleVo> tmpList = new ArrayList<DaemonRequestRuleVo>();

            //get ordered add rule
            String sql = "select TASK_TYPE,a.id ID,VALID_AFTER,VALID_BEFORE,VALID_SCOPE,RULE_ID," +
                    "RULE_TYPE,SIP,DIP,SMASK,DMASK,SPORT,DPORT,SPMASK,DPMASK,PROTO,PMASK,UPLINK,DOWNLINK,REQUEST_ID,STATUS,STATUS_DESC" +
                    " from aiedri_task_tuple a , aiedri_request b where a.REQUEST_ID = b.ID and (a.status = ? or a.STATUS = ?)order by b.create_date";

            Object[] params = new Object[]{RuleConstant.RULE_WAIT_CHECK, RuleConstant.RULE_WAIT_AGAIN};

            tmpList = jdbcTemplate.query(sql, params, new EJB3AnnontationRowMapper(DaemonRequestRuleVo.class));

            requestRuleVoList.addAll(tmpList);
            //make cfg_id and sycn to db
            //makeCfgId();


        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
        }


        //deal rules (add rule and del rule)


        //check them

        //make tasks
    }

    /**
     * 规则合并，用于每次规则审核前将（新增－删除）相配对的规则记录状态设为“已下发”
     *
     * 从下发配置的角度考虑，这种规则下发请求不需要在设备上生效一遍了。
     */
    private void mergeRules() {

        try{
            String sql = "update aiedri_task_tuple a set a.status = 8 " +
                    "where status = 0 and a.id not in (select c.id from " +
                    "(select max(id) id from aiedri_task_tuple where status = 0 group by rule_id) c)";

            Integer upNum = jdbcTemplate.update(sql , new Object[]{RuleConstant.RULE_MERGED , RuleConstant.RULE_WAIT_CHECK,RuleConstant.RULE_WAIT_CHECK});

            log.info("merge " + upNum);
        }catch(Exception e){
            log.error(e.toString());
            e.printStackTrace();
        }
        /**
         * 筛选出"待审核"的id最大的规则，实现效率低，已有上文替代
         */
//        try{
//            String sql = "select a.TASK_TYPE , b.id , b.rule_id from aiedri_request a , aiedri_task_tuple b , (select max(id) id from aiedri_task_tuple where status = ?  group by rule_id) c " +
//                    "where a.ID = b.REQUEST_ID and b.ID = c.id ";
//
//            List<Map> ruleList = jdbcTemplate.queryForList(sql,new Object[]{RuleConstant.RULE_WAIT_CHECK});
//
//            for (Map map : ruleList) {
//                Integer taskType = (Integer)map.get("TASK_TYPE");
//                Integer id = (Integer)map.get("id");
//                String ruleId = (String)map.get("rule_id");
//                String uSql = "";
//                if(taskType.equals(TaskConstant.TASK_TYPE_ADD)){
//                    /**
//                     * 最新的规则是新增规则时，之前待审核的规则设置为已下发
//                     */
//                    uSql = "update aiedri_task_tuple set status = ? where id in (select c.id from (select id from aiedri_task_tuple where rule_id = ? and status = ? and id < ?)c )";
//                    Object[] param = new Object[]{RuleConstant.RULE_MERGED,ruleId,RuleConstant.RULE_WAIT_CHECK,id};
//                    Integer upNum = jdbcTemplate.update(uSql,param);
//                    log.info("update add " + ruleId + " " + id + " " + upNum);
//
//                }else if(taskType.equals(TaskConstant.TASK_TYPE_DELETE)){
//                    /**
//                     * 最新的规则是删除规则时，这个规则的待审核状态规则记录状态设置为已下发
//                     */
//                    uSql = "update aiedri_task_tuple set status = ? where id in (select c.id from (select id from aiedri_task_tuple where rule_id = ? and status = ? and id < ?)c )";
//                    Object[] param = new Object[]{RuleConstant.RULE_MERGED,ruleId,RuleConstant.RULE_WAIT_CHECK , id};
//                    Integer upNum = jdbcTemplate.update(uSql,param);
//                    log.info("update del " + ruleId + " " + id + " " + upNum);
//
//                }
//            }
//        }catch (Exception e){
//            log.error(e.toString());
//            e.printStackTrace();
//        }


    }

    private void checkTaskResult() {
        if (issueType.equals(FLOWSPEC))
            checkFlowSpecTaskResult();

        if (issueType.equals(ACL_PY)) {
            checkAclPyTaskResult();
        }
    }

    private void checkAclPyTaskResult() {
        log.debug("start check acl py exec result!");
        File dataPathF = new File(aclPyPath + File.separator + "cfglog");

        String[] list = dataPathF.list(new DirFilter(".*-.*-.*\\.tmplog"));

        if (list == null) {
            log.debug("no acl result file!");
            return;
        }
        for (String fileName : list) {

            try {
                String filePath;
                filePath = dataPathF + File.separator + fileName;
                BufferedReader reader = new BufferedReader(new FileReader(filePath));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(System.lineSeparator());
                }


                String exe_result = sb.toString();

                Integer taskId = Integer.parseInt(fileName.split("-")[1]);
                //update task status
                log.debug("acl py :get result ,update " + taskId);
                String sql = "update aiedri_task  set deal_date = ? ,exe_result = ? ,status = ? " +
                        "where task_id = ?";

                Object[] params = new Object[]{System.currentTimeMillis() / 1000l, exe_result, TaskConstant.TASK_STATUS_COMP, taskId};

                jdbcTemplate.update(sql, params);
                //update rule status
                sql = "select cfg_id ,b.rule_id rule_id from aiedri_task_rule_rel a , aiedri_rule_cfg_rel b where a.RULE_ID = b.rule_id and TASK_ID = ?";
                params = new Object[]{taskId};

                List<Map> cfgIds = jdbcTemplate.queryForList(sql,params);

                for (Map row :cfgIds){
                    Integer cfgIdInt = (Integer)row.get("cfg_id");
                    String ruleId = (String)row.get("rule_id");

                    sql = "update aiedri_task_tuple  set status = ? ,STATUS_DESC = ? where rule_id = ? and STATUS = ?";

                    params = new Object[]{RuleConstant.RULE_ISSUED, "ok", ruleId, RuleConstant.RULE_ISSUING};

                    jdbcTemplate.update(sql,params);
                    //transfer cfgId

                    //deal cfg id
                    if(ruleCfgIdHash.containsValue(cfgIdInt)){//add request

                    }else //del request
                        delRuleCfg(cfgIdInt);

                }
                File file = new File(filePath);

                File bakRstFile = new File(file.getParent() + File.separator + "rstbak" + File.separator + fileName);


                Boolean rs = file.renameTo(bakRstFile);

                if (!rs)
                    log.error("rename " + file.getAbsolutePath() + "- " + bakRstFile.getAbsolutePath());


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.gc();//file delete
        }

        log.info("end check acl py exec result! ");
    }

    private void checkFlowSpecTaskResult() {

        log.debug("start check flowspec exec result!");
        File dataPathF = new File(flowSpecPath);

        String[] list = dataPathF.list(new DirFilter(".*_.*_.*\\.rst"));

        for (String fileName : list) {
            log.info("Start deal flowspect exec result file " + fileName);
            try {
                String filePath;
                filePath = dataPathF + File.separator + fileName;
                BufferedReader reader = new BufferedReader(new FileReader(filePath));
                String line;

                String taskId = fileName.split("_")[1];
                StringBuilder taskResult = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    String[] items = line.split("\\|", -1);

                    if (items.length != 3) {
                        log.error("flowspce result line format wrong :" + line);
                        continue;
                    }

                    taskResult.append(line);
                    String cfgId = items[0];

                    //transfer cfgId
                    Integer cfgIdInt;
                    if(cfgId != null)
                        try{
                            cfgIdInt = Integer.parseInt(cfgId);
                        }catch(Exception e){
                            log.error(line + "|cfgid parse to int error!" + e.toString());
                            continue;
                        }

                    else{
                        log.error("cfg id is null ,line info is " + line);
                        continue;
                    }
                    String status = items[1];
                    String msg = items[2];

                    String exe_result = status + ":" + msg;

                    //update rule status
                    String sql = "update aiedri_task_tuple  set status = ? ,STATUS_DESC = ? where rule_id = " +
                            //"(select rule_id from aiedri_task_rule_rel where TASK_ID = ?)";
                            "(select rule_id from aiedri_rule_cfg_rel where cfg_id = ?) and status = ?";
                            //"request_id = (select request_id from aiedri_task_rule_rel where TASK_ID = ? and rule_id = (select rule_id from aiedri_rule_cfg_rel where cfg_id = ?))";



                    Object[] params = new Object[]{RuleConstant.RULE_ISSUED,exe_result,  cfgId ,RuleConstant.RULE_ISSUING};

                    jdbcTemplate.update(sql, params);
                    //check this cfgId is exist in ruleCfgIdHash , if no ,then is delete task
                    if(ruleCfgIdHash.containsValue(cfgIdInt)){//add request

                    }else //del request
                        delRuleCfg(cfgIdInt);
                }

                String sql = "update aiedri_task  set status = ? ,EXE_RESULT = ? where TASK_ID = ? and status = ?";

                Object[] params = new Object[]{TaskConstant.TASK_STATUS_COMP,taskResult.toString(),  taskId , TaskConstant.TASK_STATUS_EXEC};

                jdbcTemplate.update(sql, params);
                File file = new File(filePath);

                File bakRstFile = new File(file.getParent() + File.separator + "rstbak" + File.separator + fileName);


                Boolean rs = file.renameTo(bakRstFile);

                if (!rs)
                    log.error("rename " + file.getAbsolutePath() + "- " + bakRstFile.getAbsolutePath());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.gc();//file delete

            log.info("End deal flowspect exec result file " + fileName);

        }

        log.debug("end check flowspec exec result! ");

    }

    private boolean isDelReq(String cfgId) {
        String sql = "select * from aiedri_request a , aiedri_rule_cfg_rel b WHERE cfg_id = ? ";
        Object[] params = new Object[]{cfgId};
        jdbcTemplate.update(sql, params);
        return false;
    }

//    private void updateTasks(String result) {
//
//        log.info("update task result!");
//        String deal_date = dateFormat.format(System.currentTimeMillis());
//        HashMap<Integer, Integer> taskMap = new HashMap<Integer, Integer>();
//        for (DaemonRequestRuleVo vo : requestRuleVoList) {
//            if (taskMap.containsKey(vo.getId()))
//                continue;
//
//            String sql = "update aiedri_task  set deal_date = ? ,exe_result = ? ,status = ? where task_id = ? ";
//
//            Object[] params = new Object[]{System.currentTimeMillis() / 1000l, result, TaskConstant.TASK_STATUS_COMP, vo.getId()};
//
//            jdbcTemplate.update(sql, params);
//
//            taskMap.put(vo.getId(), vo.getId());
//
//
//        }
//
//
//    }


//    /**
//     * make cfgid to ruleid
//     */
//    private void makeCfgId() {
//        for (DaemonRequestRuleVo vo : requestRuleVoList) {
//            String ruleId = vo.getRuleId();
//            Integer cfgId = vo.getCfgId();
//            Integer taskType = vo.getRequestType();
//
//            if (taskType.equals(TaskConstant.TASK_TYPE_ADD)) {
//                if (ruleCfgIdHash.containsKey(ruleId)) {
//                    log.error(ruleId + " rule is exist,cfgid is " + cfgId);
//                    vo.setCfgId(ruleCfgIdHash.get(ruleId));
//                } else {
//                    Integer addCfgId = 0;
//                    do {
//                        addCfgId = ++curMaxCfgId / MAX_CFG_ID;
//                    } while (ruleCfgIdHash.containsValue(addCfgId));
//
//                    ruleCfgIdHash.put(ruleId, addCfgId);
//
//                    insertRuleCfg(ruleId, addCfgId);
//
//                    vo.setCfgId(addCfgId);
//                }
//            } else if (taskType.equals(TaskConstant.TASK_TYPE_DELETE)) {
//                if (ruleCfgIdHash.containsKey(ruleId)) {
//                    Integer delCfgId = ruleCfgIdHash.get(ruleId);
//                    vo.setCfgId(delCfgId);
//                    ruleCfgIdHash.remove(ruleId);
//                    delRuleCfg(ruleId);
//                } else {
//                    log.error(ruleId + " is not exist , can not delete!");
//                }
//            }
//
//
//        }
//    }


    /**
     * accept stop command by socket
     */
    public void stop() {
        stopFlag = true;
    }

    private void insertRuleCfg(String ruleId, Integer addCfgId) {
        String sql = "INSERT INTO AIEDRI_RULE_CFG_REL (RULE_ID , CFG_ID ) VALUES(?,?)";

        Object[] params = new Object[]{ruleId, addCfgId};

        jdbcTemplate.update(sql, params);
    }

    private void delRuleCfg(Integer cfgId) {
            String sql = "DELETE FROM AIEDRI_RULE_CFG_REL WHERE cfg_id = ? ";
            Object[] params = new Object[]{cfgId};
            jdbcTemplate.update(sql, params);


    }

    public void init() {
        //init cfgIdHash
        String sql = "SELECT RULE_ID , CFG_ID FROM AIEDRI_RULE_CFG_REL";

        List<RuleCfgVo> ruleCfgVos = (List<RuleCfgVo>)jdbcTemplate.query(sql, new EJB3AnnontationRowMapper(RuleCfgVo.class));


        for (RuleCfgVo vo : ruleCfgVos) {
            Integer cfgId = vo.getCfgId();
            if (cfgId > curMaxCfgId)
                curMaxCfgId = cfgId;

            ruleCfgIdHash.put(vo.getRuleId(), cfgId);
        }

        log.info("init cfg id end , max cfg id is " + curMaxCfgId);
    }

    /**
     * 1. reinit dev conn info
     * 2. reinit dev cmd info
     * 3. make task list
     */
    public String exeTasks() {
        //if flow sign >

        if (getFlowThresholdSign() >= 5) {

            exeClearTask();

            makeAllRuleDeleted();
        }


        //get Tasks

        getTasks();

        //make Remote Tasks
        /**
         * 1. get edpi task info from db
         * 2. new
         */

        if (taskRuleVoList == null || taskRuleVoList.size() == 0) {
            log.debug("There is no Task!");
            return "";
        }else
            log.info("There is " + taskRuleVoList.size() + " tasks!");

        if (issueType.equals(FLOWSPEC))
            return exeTasksFlowspec();
        else if (issueType.equals(ACL_PY))
            return exeTasksAclPy();
//        else if (issueType.equals(ACL)) {
////            String exeResult =  exeTasksAcl();
////            updateTasks(exeResult);
//        }

            return "";
    }

    private void makeAllRuleDeleted() {
        String sql = "UPDATE aiedri_task_tuple a , aiedri_request b SET a. STATUS = ? WHERE a.REQUEST_ID = b.ID AND b.TASK_TYPE = ?";

        jdbcTemplate.update(sql , new Object[]{RuleConstant.RULE_DELETED , TaskConstant.TASK_TYPE_ADD});

    }

    private void exeClearTask() {
        String filePath = null;
        String fileDateStr = cfgFileDateFormat.format(new Date());
        Integer taskId = 0;
        String fileInfo = null;
        if (issueType.equals(FLOWSPEC)) {
            filePath = flowSpecPath + File.separator +
                    "bfs_" + taskId + "_" + fileDateStr + ".cfg";
            fileInfo = "0|flush";

        } else if (issueType.equals(ACL_PY)) {
            filePath = aclPyPath + File.separator +
                    "hw5k-" + taskId + "-" + fileDateStr + ".cfg";
            fileInfo = "undo acl 3999";
        }

        if (filePath != null) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath + ".tmp"));

                writer.write(fileInfo);

                writer.flush();

                File tmpFile = new File(filePath + ".tmp");
                File file = new File(filePath);

                if (tmpFile.renameTo(file)) {
                    log.debug("make  cfg file " + filePath + " succ!");
                } else {
                    log.debug("make  cfg file " + filePath + " fail!");
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                log.error(exception.toString());
            }
        } else {
            log.error("flow sign > 5 ,but filePath is null or fileInfo is null");
        }

    }

    private void getTasks() {
//        String sql = "select A.TASK_ID TASK_ID,TASK_TYPE,CFG_ID,E.ID ID,VALID_AFTER,VALID_BEFORE,VALID_SCOPE,E.RULE_ID RULE_ID," +
//                "  RULE_TYPE,SIP,DIP,SMASK,DMASK,SPORT,DPORT,SPMASK,DPMASK,PROTO,PMASK,UPLINK,DOWNLINK,TASK_TYPE" +
//                " from aiedri_task a , aiedri_request b , aiedri_task_rule_rel C,AIEDRI_RULE_CFG_REL D ,aiedri_task_tuple e " +
//                "where a.TASK_ID = c.task_id and c.RULE_ID = e.RULE_ID and d.RULE_ID = e.RULE_ID and a.status = ?";

        String sql = "select d.REQUEST_ID, d.status,a.TASK_ID TASK_ID,TASK_TYPE,CFG_ID,E.ID ID,VALID_AFTER,VALID_BEFORE,VALID_SCOPE,d.RULE_ID RULE_ID," +
                "  RULE_TYPE,SIP,DIP,SMASK,DMASK,SPORT,DPORT,SPMASK,DPMASK,PROTO,PMASK,UPLINK,DOWNLINK,TASK_TYPE " +
                "from aiedri_task a join aiedri_task_rule_rel b on a.TASK_ID = b.TASK_ID and a.STATUS = ?  " +
                "join aiedri_task_tuple d on d.RULE_ID = b.RULE_ID and d.STATUS = ? " +
                " join aiedri_request e on e.ID = d.REQUEST_ID " +
                "left join aiedri_rule_cfg_rel c on c.rule_id = b.RULE_ID";

        Object[] param = new Object[]{TaskConstant.TASK_STATUS_CREATE , RuleConstant.RULE_WAIT_ISSUE};

        taskRuleVoList = (ArrayList<DaemonTaskRuleVo>) jdbcTemplate.query(sql, param, new EJB3AnnontationRowMapper(DaemonTaskRuleVo.class));
    }

    private String exeTasksAclPy() {
        log.debug("start make acl python .cfg file!");
        //Hashtable<String, String> fileHash = new Hashtable<String, String>();
        Hashtable<Integer, String> taskidIdrHash = new Hashtable<Integer, String>();


        for (DaemonTaskRuleVo vo : taskRuleVoList) {
            Integer taskId = vo.getTaskId();
            if (vo.getRequestType().equals(RequestConstant.REQUEST_TYPE_ADD)) {
                String idr = makeAddRuleFromVo(vo);

                if (taskidIdrHash.containsKey(taskId)) {
                    String fileInfo = taskidIdrHash.get(taskId);

                    fileInfo += idr + System.lineSeparator();

                    taskidIdrHash.put(taskId, fileInfo);
                } else {
                    idr = "acl number 3999" + System.lineSeparator() + idr + System.lineSeparator();
                    taskidIdrHash.put(taskId, idr);
                }
            } else if (vo.getRequestType().equals(RequestConstant.REQUEST_TYPE_DELETE)) {
                String idr = "undo rule " + vo.getCfgId();

                if (taskidIdrHash.containsKey(taskId)) {
                    String fileInfo = taskidIdrHash.get(taskId);

                    fileInfo += idr + System.lineSeparator();
                    taskidIdrHash.put(taskId, fileInfo);
                } else {
                    idr = "acl number 3999" + System.lineSeparator() + idr + System.lineSeparator();
                    taskidIdrHash.put(taskId, idr);
                }
                //delRuleCfg(vo.getRuleId());
            }
        }

        writeCfgFile(taskidIdrHash);

        updateTaskStatusExec(taskidIdrHash);
        log.debug("end make acl python .cfg file!");


        return "succ";
    }

    private void updateTaskStatusExec(Hashtable<Integer, String> taskidIdrHash) {


        //make task id list
        StringBuilder sb = new StringBuilder(65535);

        Enumeration<Integer> e = taskidIdrHash.keys();
        while (e.hasMoreElements()) {

            sb.append(e.nextElement()).append(",");
        }

        sb.deleteCharAt(sb.length() - 1);
        String taskIdListStr = sb.toString();
        log.debug("task id list " + taskIdListStr);

        String sql = "update aiedri_task set status = ? where task_id in (" + taskIdListStr + ")";

        Object[] param = new Object[]{TaskConstant.TASK_STATUS_EXEC};

        jdbcTemplate.update(sql, param);
        //update rule list
//        sb.delete(0, sb.length());//clear stringbuild
//        for (DaemonTaskRuleVo vo : taskRuleVoList) {
//            sb.append("'").append(vo.getRuleId()).append("',");
//        }
//
//        sb.deleteCharAt(sb.length() - 1);
//        String ruleIdListStr = sb.toString();
//        log.debug("rule id list " + ruleIdListStr);
//
//        sql = "update aiedri_task_tuple set status = ? where rule_id in (" + ruleIdListStr + ")";
//
//        param = new Object[]{RuleConstant.RULE_ISSUING};
//
//        jdbcTemplate.update(sql, param);

        for (DaemonTaskRuleVo vo : taskRuleVoList) {
            sql = "update aiedri_task_tuple set status = ? where rule_id = ? and REQUEST_ID = ?";

            param = new Object[]{RuleConstant.RULE_ISSUING,vo.getRuleId(),vo.getRequestId()};

            jdbcTemplate.update(sql, param);
        }



    }

    private String exeTasksFlowspec() {
        log.info("start make flowspec .cfg file!");
        Hashtable<Integer, String> taskIdrHash = new Hashtable<Integer, String>();

        for (DaemonTaskRuleVo vo : taskRuleVoList) {
            Integer taskId = vo.getTaskId();
            if (vo.getRequestType().equals(RequestConstant.REQUEST_TYPE_ADD)) {
                String sMask = vo.getSmask();
                if (sMask == null)
                    sMask = "255.255.255.255";
                String dMask = vo.getDmask();
                if (dMask == null) dMask = "255.255.255.255";

                String sip = vo.getSip();
                if (sip == null)
                    sip = "";
                else
                    sip += "/" + AiedriUtil.maskToInt(sMask);

                String dip = vo.getDip();
                if (dip == null)
                    dip = "";
                else
                    dip += "/" + AiedriUtil.maskToInt(dMask);


                String idr = String.format("%s|add|%s|%s|%d|%d|%s",
                        vo.getCfgId(), sip, dip, vo.getSport(), vo.getDport(), vo.getProto());

                idr = idr.replaceAll("null", "");


                if (taskIdrHash.containsKey(taskId)) {
                    String fileInfo = taskIdrHash.get(taskId);

                    fileInfo = fileInfo + idr + System.lineSeparator();

                    taskIdrHash.put(taskId, fileInfo);
                } else {
                    taskIdrHash.put(taskId, idr + System.lineSeparator());
                }
            } else if (vo.getRequestType().equals(RequestConstant.REQUEST_TYPE_DELETE)) {
                String sMask = vo.getSmask();
                if (sMask == null) sMask = "255.255.255.255";
                String dMask = vo.getDmask();
                if (dMask == null) dMask = "255.255.255.255";

                String sip = vo.getSip();
                if (sip == null)
                    sip = "";
                else
                    sip += "/" + sMask;

                String dip = vo.getDip();
                if (dip == null)
                    dip = "";
                else
                    dip += "/" + dMask;


                String idr = String.format("%s|del|%s|%s|%d|%d|%s",
                        vo.getCfgId(), sip, dip, vo.getSport(), vo.getDport(), vo.getProto());

                idr = idr.replaceAll("null", "");

                if (taskIdrHash.containsKey(taskId)) {
                    String fileInfo = taskIdrHash.get(taskId);

                    fileInfo += idr + System.lineSeparator();

                    taskIdrHash.put(taskId, fileInfo);
                } else {
                    taskIdrHash.put(taskId, idr + System.lineSeparator());
                }

                //delRuleCfg(vo.getRuleId());

            }
        }

        String writeRs;
        writeRs = writeCfgFile(taskIdrHash);

        updateTaskStatusExec(taskIdrHash);

        log.info("end make flowspec .cfg file!" + writeRs);
        return "succ";
    }

    private String writeCfgFile(Hashtable<Integer, String> fileHash) {
        String fileDateStr = cfgFileDateFormat.format(new Date());

        Enumeration<Integer> e = fileHash.keys();

        while (e.hasMoreElements()) {
            Integer taskId = e.nextElement();
            String filePath = "";
            if (issueType.equals(ACL_PY))
                filePath = aclPyPath + File.separator +
                        "hw5k-" + taskId + "-" + fileDateStr + ".cfg";
            else if (issueType.equals(FLOWSPEC))
                filePath = flowSpecPath + File.separator +
                        "bfs_" + taskId + "_" + fileDateStr + ".cfg";

            String fileInfo = fileHash.get(taskId);

            log.debug("filePath is " + filePath + " fileInfo is " + fileInfo);


            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath + ".tmp"));

                writer.write(fileInfo);

                writer.flush();

                File tmpFile = new File(filePath + ".tmp");
                File file = new File(filePath);

                if (tmpFile.renameTo(file)) {
                    log.debug("make  cfg file " + filePath + " succ!");
                } else {
                    log.debug("make  cfg file " + filePath + " fail!");
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                return "fail";
            }


        }


        return "succ";
    }

//    private String exeTasksAcl() {
//        //DesCrptBean crpt = DesCrptBean.getDesCrptBean();
//
//        //String password = crpt.encrypt("as1a1nf0");
//        File file = new File(System.getProperty("user.home") + File.separator + "etc" + File.separator + "taskcfg.xml");
//        InputStream in = null;
//        try {
//            in = new FileInputStream(file);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        RemoteTask rTask = new RemoteTask();
//        String[] cmdSequence = new String[]{"dealRule"};
//        rTask.setCmdSequence(cmdSequence);
//        rTask.initFromXml(in);
//        //set cmd Seq
//        cmdSequence = rTask.getCmdSequence();
//
//        for (String aCmdSequence : cmdSequence) {
//            log.info("cmd sequs = " + aCmdSequence);
//        }
//        //set some arguments by task attribute
//        setRemoteTask(rTask);
//
//
//        //exec task
//        Map<String, List<CommandRs>> ect = RemoteTaskUtil.execCmd(rTask);
//
//
//        StringBuilder stringBuilder = new StringBuilder();
//        for (String tmpKey : ect.keySet()) {
//            System.out.println("ip=" + tmpKey);
//            stringBuilder.append(tmpKey).append(System.lineSeparator());
//            for (CommandRs tmpRs : ect.get(tmpKey)) {
//                System.out.println("cmdid=" + tmpRs.getCmdId());
//
//                System.out.println("cmdrs=" + tmpRs.getStdOut());
//
//                stringBuilder.append(tmpRs.getCmdId()).append(System.lineSeparator()).append(tmpRs.getStdOut()).append(System.lineSeparator());
//            }
//
//
//        }
//
//        return stringBuilder.toString();
//    }

//    private void setRemoteTask(RemoteTask rTask) {
//
//        //set ${add/delRule} by task rulelist
//
//        Map<String, Command> cmdMap = rTask.getAllCmds();
//
//        Command dealRuleCmd = cmdMap.get("dealRule");
//
//        List<ExpectCond> expectConds = dealRuleCmd.getExpects();
//
//        for (int i = 0; i < expectConds.size(); i++) {
//            ExpectCond cond = expectConds.get(i);
//            //find Rules
//            if (cond.getNextText().contains("${Rules}")) {
//                //remove
//                expectConds.remove(i);
//                //add rules
//                for (DaemonRequestRuleVo taskRuleVo : requestRuleVoList) {
//                    Integer taskType = taskRuleVo.getRequestType();
//                    StringBuilder sb = new StringBuilder();
//                    if (taskType.equals(TaskConstant.TASK_TYPE_ADD)) {
//                        String addRule = makeAddRuleFromVo(taskRuleVo);
//                        ExpectCond addCond = new ExpectCond();
//                        addCond.setNextText(addRule);
//                        expectConds.add(i++, addCond);
//
//                    } else if (taskType.equals(TaskConstant.TASK_TYPE_DELETE)) {
//                        sb.append("undo rule ").append(taskRuleVo.getCfgId());
//                        ExpectCond delCond = new ExpectCond();
//                        delCond.setNextText(sb.toString());
//                        expectConds.add(i++, delCond);
//                    } else {
//                        log.error("There is wrong task type " + taskType);
//                    }
//
//                }
//
//                //break
//                break;
//            }
//        }
//
//        log.debug("setRemoteTask end!");
//
//    }


    private String makeAddRuleFromVo(DaemonTaskRuleVo taskRuleVo) {
        StringBuilder sb = new StringBuilder();
        //add rule_id
        sb.append("rule ").append(taskRuleVo.getCfgId()).append(" permit ");
        String ruleType = taskRuleVo.getRuleType();

        //add prot
        if (ruleType.contains("proto")) {
            sb.append(taskRuleVo.getProto());
        } else {//default
            sb.append("ip");
        }
        //add sip
        if (ruleType.contains("sip")) {
            sb.append(" source ").append(taskRuleVo.getSip());

            //add smask
            if (ruleType.contains("smask"))

                sb.append(" ").append(AiedriUtil.maskToInt(taskRuleVo.getSmask()));
            else
                sb.append(" 0");
        }

        //add sport
        if (ruleType.contains("sport"))
            sb.append(" source-port eq ").append(taskRuleVo.getSport());

        //add dip
        if (ruleType.contains("dip")) {
            sb.append(" destination ").append(taskRuleVo.getDip());
            //add dmask
            if (ruleType.contains("dmask"))
                sb.append(" ").append(AiedriUtil.maskToInt(taskRuleVo.getDmask()));
            else
                sb.append(" 0");
        }

        //add dport
        if (ruleType.contains("dport"))
            sb.append(" destination-port eq ").append(taskRuleVo.getDport());

        String ruleInfo = sb.toString();
        log.debug(ruleInfo);

        return ruleInfo;
    }

    public void updateRuleStatus(){
        log.info("Start update rule status...");

        Integer num = aiedriTaskManageDAO.updateRuleStatus();

        log.info("Update rule status succ, total: " + num);

        /**
         * move to history
         */

        Integer moveNum = aiedriTaskManageDAO.moveNotExistRules();

        Integer deleteNum = aiedriTaskManageDAO.deleteNotExistRules();

        log.info("Move :" + moveNum + " Delete :" + deleteNum);

    }

}



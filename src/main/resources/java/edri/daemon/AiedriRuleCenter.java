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

public class AiedriRuleCenter {

    List<AiedriDevices> devList = null;


    private String flowSpecPath;
    private ArrayList<DaemonTaskRuleVo> taskRuleVoList = null;
    private Boolean stopFlag = false;
    private boolean isDangerous = false;
    private ArrayList<ProtectedIpMask> ipMaskList = new ArrayList<ProtectedIpMask>();
    private int updateCount = 1;


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

    final static Integer FLOWSPEC = 2;




    JdbcTemplate jdbcTemplate = (JdbcTemplate) BeanFactoryUtil
            .getBean("jdbcTemplate");

    IAiedriRuleConfigBo ruleConfigBo = (IAiedriRuleConfigBo) BeanFactoryUtil.getBean("aiedriRuleConfigBo");
    IAiedriDeviceBo deviceBo = (IAiedriDeviceBo) BeanFactoryUtil.getBean("aiedriDeviceBo");

    IAiedriTaskManageDAO aiedriTaskManageDAO = (IAiedriTaskManageDAO)BeanFactoryUtil.getBean("aiedriTaskManageDAO");

    List<DaemonRequestRuleVo> requestRuleVoList = new ArrayList<DaemonRequestRuleVo>();//first order rule list


    public static void main(String[] args) {

        if (args.length == 2) {//flowspec /Users/jiaojian/Documents/IntelliJWorkspace/aiedri/flowspec
            AiedriRuleCenter c = new AiedriRuleCenter();
            String typeStr = args[0];
            String Path = args[1];
            Integer type = 0;
            if (typeStr.equals("flowspec")) {
                type = AiedriRuleCenter.FLOWSPEC;
                c.setFlowSpecPath(Path);
                System.out.println("Start FLOWSPEC model...");
            }  else
                System.out.println("Usage: rule-center.sh flowspec $path");


            c.setIssueType(type);

            c.start();

        } else
            System.out.println("Usage: rule-center.sh flowspec $path");

    }

    public AiedriRuleCenter() {
    }

    /**
     * main process
     */
    public void start() {

        init();

//        mergeRules();//for test
//
//        updateRuleStatus();
//        System.exit(0);

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

//

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
        log.info("#####filter rules " + ruleList.size());
        /**
         * insert task
         */
        if (ruleList.size() != 0) {
            //for (AiedriDevices dev : devList) {// flowspec

                final Integer taskId = SequenceUtil.getSequenceId("aiedriTaskId");

                String devIp = "";//dev.getIp()

                String sql = "insert into aiedri_task(TASK_ID,CREATE_DATE,STATUS,DEAL_DATE,IP,RULE_ID,EXE_RESULT) values(?,?,?,?,?,?,?)";

                Object[] param = new Object[]{taskId, System.currentTimeMillis() / 1000l, TaskConstant.TASK_STATUS_CREATE, 0, devIp, "", ""};

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
                        preparedStatement.setInt(3, requestId);
                    }

                    @Override
                    public int getBatchSize() {
                        return ruleList.size();
                    }
                });

                //log.info("#####insert agent " + dev.getIp() + " task " + taskId + " rules " + ruleList.size());
            //}

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
            log.info("#####get rule " + tmpList.size());
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
            String sql = "update aiedri_task_tuple a set a.status = ? " +
                    "where status = ? and a.id not in (select c.id from " +
                    "(select max(id) id from aiedri_task_tuple where status = ? group by rule_id) c)";

            Integer upNum = jdbcTemplate.update(sql , new Object[]{RuleConstant.RULE_MERGED , RuleConstant.RULE_WAIT_CHECK,RuleConstant.RULE_WAIT_CHECK});

            log.info("#####merge " + upNum);
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
                    String id = items[0];

                    //transfer cfgId
                    Integer idInt;
                    if(id == null){
                        log.error(" id is null ,line info is " + line);
                        continue;
                    }
                    String status = items[1];
                    String msg = items[2];

                    String exe_result = status + ":" + msg;

                    //update rule status
                    String sql = "update aiedri_task_tuple  set status = ? ,STATUS_DESC = ? where id = ? and status = ?";



                    Object[] params = new Object[]{RuleConstant.RULE_ISSUED,exe_result, id ,RuleConstant.RULE_ISSUING};

                    jdbcTemplate.update(sql, params);


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

//    private boolean isDelReq(String cfgId) {
//        String sql = "select * from aiedri_request a , aiedri_rule_cfg_rel b WHERE cfg_id = ? ";
//        Object[] params = new Object[]{cfgId};
//        jdbcTemplate.update(sql, params);
//        return false;
//    }




    /**
     * accept stop command by socket
     */
    public void stop() {
        stopFlag = true;
    }



    public void init() {

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

        String sql = "select d.REQUEST_ID, d.status,a.TASK_ID TASK_ID,TASK_TYPE,d.ID ID,VALID_AFTER,VALID_BEFORE,VALID_SCOPE,d.RULE_ID RULE_ID," +
                "  RULE_TYPE,SIP,DIP,SMASK,DMASK,SPORT,DPORT,SPMASK,DPMASK,PROTO,PMASK,UPLINK,DOWNLINK,TASK_TYPE " +
                "from aiedri_task a join aiedri_task_rule_rel b on a.TASK_ID = b.TASK_ID and a.STATUS = ?  " +
                "join aiedri_task_tuple d on d.RULE_ID = b.RULE_ID and d.STATUS = ? " +
                " join aiedri_request e on e.ID = d.REQUEST_ID ";

        Object[] param = new Object[]{TaskConstant.TASK_STATUS_CREATE , RuleConstant.RULE_WAIT_ISSUE};

        taskRuleVoList = (ArrayList<DaemonTaskRuleVo>) jdbcTemplate.query(sql, param, new EJB3AnnontationRowMapper(DaemonTaskRuleVo.class));

        log.info("#####get task rules " + taskRuleVoList.size());
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
                        vo.getId(), sip, dip, vo.getSport(), vo.getDport(), vo.getProto());

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
                        vo.getId(), sip, dip, vo.getSport(), vo.getDport(), vo.getProto());

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
            if (issueType.equals(FLOWSPEC))
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


//    private String makeAddRuleFromVo(DaemonTaskRuleVo taskRuleVo) {
//        StringBuilder sb = new StringBuilder();
//        //add rule_id
//        sb.append("rule ").append(taskRuleVo.getCfgId()).append(" permit ");
//        String ruleType = taskRuleVo.getRuleType();
//
//        //add prot
//        if (ruleType.contains("proto")) {
//            sb.append(taskRuleVo.getProto());
//        } else {//default
//            sb.append("ip");
//        }
//        //add sip
//        if (ruleType.contains("sip")) {
//            sb.append(" source ").append(taskRuleVo.getSip());
//
//            //add smask
//            if (ruleType.contains("smask"))
//
//                sb.append(" ").append(AiedriUtil.maskToInt(taskRuleVo.getSmask()));
//            else
//                sb.append(" 0");
//        }
//
//        //add sport
//        if (ruleType.contains("sport"))
//            sb.append(" source-port eq ").append(taskRuleVo.getSport());
//
//        //add dip
//        if (ruleType.contains("dip")) {
//            sb.append(" destination ").append(taskRuleVo.getDip());
//            //add dmask
//            if (ruleType.contains("dmask"))
//                sb.append(" ").append(AiedriUtil.maskToInt(taskRuleVo.getDmask()));
//            else
//                sb.append(" 0");
//        }
//
//        //add dport
//        if (ruleType.contains("dport"))
//            sb.append(" destination-port eq ").append(taskRuleVo.getDport());
//
//        String ruleInfo = sb.toString();
//        log.debug(ruleInfo);
//
//        return ruleInfo;
//    }

    public void updateRuleStatus(){
        if(updateCount++ > 3600) {
            try {
                log.info("Start update rule status...");

                Integer num = aiedriTaskManageDAO.updateRuleStatus();

                log.info("####Update rule status succ, total: " + num);

                /**
                 * move to history
                 */

                Integer moveNum = aiedriTaskManageDAO.moveNotExistRules();

                Integer deleteNum = aiedriTaskManageDAO.deleteNotExistRules();

                log.info("####Move :" + moveNum + " Delete :" + deleteNum);
            } catch (Exception e) {
                log.error(e.toString());
                e.printStackTrace();
            }

            updateCount = 1;

        }

    }


    public void mergeRulesNew(){

    }

}
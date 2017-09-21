package com.asiainfo.aiedri.daemon;

import ainx.common.spring.BeanFactoryUtil;
import ainx.common.spring.jdbc.EJB3AnnontationRowMapper;
import com.asiainfo.aiedri.util.TaskConstant;
import com.asiainfo.aiedri.vo.DaemonTaskRuleVo;
import com.asiainfo.aiedri.vo.RuleCfgVo;
import com.asiainfo.ainx.rc.server.po.Command;
import com.asiainfo.ainx.rc.server.po.CommandRs;
import com.asiainfo.ainx.rc.server.po.ExpectCond;
import com.asiainfo.ainx.rc.server.po.RemoteTask;
import com.asiainfo.ainx.rc.server.util.RemoteTaskUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class EddiRuleCustomer {

    Hashtable<String , String> aclPyFileHash = new Hashtable<String, String>();

    private String flowSpecPath;

    public void setAclPyPath(String aclPyPath) {
        this.aclPyPath = standardPath(aclPyPath);
    }

    private String aclPyPath;

    public String standardPath(String path){
        if(path.endsWith(File.separator)){
            return path.substring(0,path.length() -1);
        }
        return path;
    }

    public void setFlowSpecPath(String flowSpecPath) {
        this.flowSpecPath = standardPath(flowSpecPath);
    }

    public void setIssueType(Integer issueType) {
        this.issueType = issueType;
    }

    private  Integer issueType;
    Logger log = Logger.getLogger(getClass());

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    SimpleDateFormat cfgFileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    Hashtable<String,Integer> ruleCfgIdHash = new Hashtable<String, Integer>();

    Integer curMaxCfgId = 0;

    final static Integer MAX_CFG_ID = 65535;

    final static Integer ACL = 1;

    final static Integer FLOWSPEC = 2;

    final static Integer ACL_PY = 3;

    JdbcTemplate jdbcTemplate = (JdbcTemplate) BeanFactoryUtil
            .getBean("jdbcTemplate");
    List<DaemonTaskRuleVo> taskRuleVoList = new ArrayList<DaemonTaskRuleVo>();//first order rule list



    public static void main(String[] args) {
        // TODO Auto-generated method stub

        if(args.length == 1){
            String typeStr = args[0];
            Integer type= 0;
            if(typeStr.equals("acl"))
                type = EddiRuleCustomer.ACL;
            else {
                System.out.println("Usage: edpiRuleCustomer.sh acl|flowspec $path");
                System.exit(-1);
            }

            // TODO Auto-generated method stub
            EddiRuleCustomer c = new EddiRuleCustomer();
            c.setIssueType(type);
            System.out.println("Start ACL model...");
            c.start();
        }else if(args.length == 2){//flowspec /Users/jiaojian/Documents/IntelliJWorkspace/aiedri/flowspec
            EddiRuleCustomer c = new EddiRuleCustomer();
            String typeStr = args[0];
            String Path = args[1];
            Integer type= 0;
            if(typeStr.equals("flowspec")) {
                type = EddiRuleCustomer.FLOWSPEC;
                c.setFlowSpecPath(Path);
                System.out.println("Start FLOWSPEC model...");
            }
            else if(typeStr.equals("acl")) {
                type = EddiRuleCustomer.ACL_PY;
                c.setAclPyPath(Path);
                System.out.println("Start Acl Python model...");
            }else
                System.out.println("Usage: edpiRuleCustomer.sh acl [$path]|flowspec $path");


            c.setIssueType(type);

            c.start();

        }else
            System.out.println("Usage: edpiRuleCustomer.sh acl|flowspec $path");

    }

    public EddiRuleCustomer() {
    }

    /**
     * main process
     */
    public void start() {
        init();

        try {
            while (true) {

                checkTaskResult();

                //get Task from db
                getTasks();


                if (CollectionUtils.isEmpty(taskRuleVoList)) {
                    log.debug("task list is empty!");
                    Thread.sleep(10L);
                }else{
                    //exe Tasks to device
                    String exeResult = exeTasks();
                    //update tasks status and result to device
                    if(issueType.equals(ACL))
                        updateTasks(exeResult);

                    log.debug("a total cycle is end, sleep 1s!");

                }
                Thread.sleep(1000L);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    private void checkTaskResult() {
        if(issueType.equals(FLOWSPEC))
            checkFlowSpecTaskResult();

        if(issueType.equals(ACL_PY)){
            checkAclPyTaskResult();
        }
    }

    private void checkAclPyTaskResult() {
        log.info("start check acl py exec result!");
        File dataPathF = new File(aclPyPath+File.separator + "cfglog");

        String[] list = dataPathF.list(new DirFilter(".*-.*\\.tmplog"));

        for(String fileName:list){

            try {
                String filePath;
                filePath = dataPathF+File.separator + fileName;
                BufferedReader reader = new BufferedReader(new FileReader(filePath));
                String line ;
                StringBuilder sb = new StringBuilder();
                while((line = reader.readLine()) != null){
                   sb.append(line).append(System.lineSeparator());
                }


                String exe_result = sb.toString();
                String cfgPath = filePath.replaceAll("\\.tmplog","").replaceAll("cfglog/","");
                log.debug("acl py filePath is " + filePath + "-" + cfgPath);
                String taskIdListStr = aclPyFileHash.get(cfgPath);

                if(taskIdListStr == null)
                    continue;

                log.debug("acl py :get result ,update " + taskIdListStr);
                String sql = "update aiedri_task  set deal_date = ? ,exe_result = ? ,status = ? " +
                        "where task_id in (" + taskIdListStr + ")";

                Object[] params = new Object[]{System.currentTimeMillis()/1000l, exe_result, TaskConstant.TASK_STATUS_COMP};

                jdbcTemplate.update(sql, params);
                File file = new File(filePath);

                Boolean rs = file.delete();

                log.debug("delete " +file.getAbsolutePath() + " " + rs);

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

        log.info("start check flowspec exec result!");
        File dataPathF = new File(flowSpecPath);

        String[] list = dataPathF.list(new DirFilter(".*_.*\\.rst"));

        for(String fileName:list){

            try {
                String filePath;
                filePath = dataPathF+File.separator + fileName;
                BufferedReader reader = new BufferedReader(new FileReader(filePath));
                String line ;
                while((line = reader.readLine()) != null){
                    String[] items = line.split("\\|",-1);

                    if(items.length != 3){
                        log.error("flowspce result line format wrong :" + line);
                        continue;
                    }
                    String cfgId = items[0];
                    String status = items[1];
                    String msg = items[2];

                    String exe_result = status + ":" + msg ;



                    String sql = "update aiedri_task  set deal_date = ? ,exe_result = ? ,status = ? where status = ? and task_id IN " +
                            "(select id from aiedri_task_tuple a , aiedri_rule_cfg_rel b where a.RULE_ID = b.rule_id and b.cfg_id = ?) ";

                    Object[] params = new Object[]{System.currentTimeMillis()/1000l, exe_result, TaskConstant.TASK_STATUS_COMP,TaskConstant.TASK_STATUS_EXEC,cfgId};

                    jdbcTemplate.update(sql, params);

                }


                File file = new File(filePath);

                File bakRstFile = new File(file.getParent() + File.separator + "rstbak" + File.separator + fileName);



                Boolean rs = file.renameTo(bakRstFile);

                if(!rs)
                    log.error("rename " + file.getAbsolutePath() + "- " + bakRstFile.getAbsolutePath());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.gc();//file delete
        }

        log.info("end check flowspec exec result! ");

    }

    private void updateTasks(String result) {

        log.info("update task result!");
        String deal_date = dateFormat.format(System.currentTimeMillis());
        HashMap<Integer, Integer> taskMap = new HashMap<Integer, Integer>();
        for (DaemonTaskRuleVo vo : taskRuleVoList) {
            if (taskMap.containsKey(vo.getId()))
                continue;

            String sql = "update aiedri_task  set deal_date = ? ,exe_result = ? ,status = ? where task_id = ? ";

            Object[] params = new Object[]{System.currentTimeMillis()/1000l, result, TaskConstant.TASK_STATUS_COMP,vo.getId()};

            jdbcTemplate.update(sql, params);

            taskMap.put(vo.getId(), vo.getId());


        }


    }


    private void getTasks() {

        try {
            if(taskRuleVoList!=null)
                taskRuleVoList.clear();
            Long curTime = System.currentTimeMillis() / 1000l;

            List<DaemonTaskRuleVo> tmpList = new ArrayList<DaemonTaskRuleVo>();

            //get ordered add rule
            String sql = "select TASK_TYPE,ID,VALID_AFTER,VALID_BEFORE,VALID_SCOPE,RULE_ID," +
                    "RULE_TYPE,SIP,DIP,SMASK,DMASK,SPORT,DPORT,SPMASK,DPMASK,PROTO,PMASK,UPLINK,DOWNLINK" +
                    " from aiedri_task_tuple a , aiedri_task b where a.id = b.task_id and a.valid_after < ?" +
                    " and a.valid_before > ? and b.status = ? and b.task_type = ? order by b.create_date";

            Object[] params = new Object[]{curTime, curTime, TaskConstant.TASK_STATUS_CREATE, TaskConstant.TASK_TYPE_ADD};

            tmpList = jdbcTemplate.query(sql, params, new EJB3AnnontationRowMapper(DaemonTaskRuleVo.class));

            taskRuleVoList.addAll(tmpList);
            //get ordered del rule
            sql = "select TASK_TYPE,ID,VALID_AFTER,VALID_BEFORE,VALID_SCOPE,RULE_ID," +
                    "RULE_TYPE,SIP,DIP,SMASK,DMASK,SPORT,DPORT,SPMASK,DPMASK,PROTO,PMASK,UPLINK,DOWNLINK" +
                    " from aiedri_task_tuple a , aiedri_task b where a.id = b.task_id and b.status = ? " +
                    "and b.task_type = ? order by b.create_date";

            params = new Object[]{ TaskConstant.TASK_STATUS_CREATE, TaskConstant.TASK_TYPE_DELETE};

            tmpList = jdbcTemplate.query(sql, params, new EJB3AnnontationRowMapper(DaemonTaskRuleVo.class));
            taskRuleVoList.addAll(tmpList);
            //make cfg_id and sycn to db
            makeCfgId();


        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
        }


        //deal rules (add rule and del rule)


    }

    /**
     * make cfgid to ruleid
     */
    private void makeCfgId() {
        for(DaemonTaskRuleVo vo : taskRuleVoList){
            String ruleId = vo.getRuleId();
            Integer cfgId = vo.getCfgId();
            Integer taskType = vo.getTaskType();

            if(taskType.equals(TaskConstant.TASK_TYPE_ADD)){
                if(ruleCfgIdHash.containsKey(ruleId)){
                    log.error(ruleId + " rule is exist,cfgid is " + cfgId);
                    vo.setCfgId(ruleCfgIdHash.get(ruleId));
                }else{
                    Integer addCfgId = 0;
                    do {
                        addCfgId = ++curMaxCfgId / MAX_CFG_ID;
                    }while(ruleCfgIdHash.containsValue(addCfgId));

                    ruleCfgIdHash.put(ruleId,addCfgId);

                    insertRuleCfg(ruleId, addCfgId);

                    vo.setCfgId(addCfgId);
                }
            }else if(taskType.equals(TaskConstant.TASK_TYPE_DELETE)){
                if(ruleCfgIdHash.containsKey(ruleId)){
                    Integer delCfgId = ruleCfgIdHash.get(ruleId);
                    vo.setCfgId(delCfgId);
                    ruleCfgIdHash.remove(ruleId);
                    delRuleCfg(ruleId);
                }else{
                    log.error(ruleId + " is not exist , can not delete!");
                }
            }


        }
    }



    /**
     * accept stop command by socket
     */
    public void stop() {

    }

    private void insertRuleCfg(String ruleId, Integer addCfgId) {
        String sql = "INSERT INTO AIEDRI_RULE_CFG_REL (RULE_ID , CFG_ID ) VALUES(?,?)";

        Object[] params = new Object[]{ruleId , addCfgId};

        jdbcTemplate.update(sql, params);
    }

    private void delRuleCfg(String ruleId){
        String sql = "DELETE FROM AIEDRI_RULE_CFG_REL WHERE RULE_ID = ? ";
        Object[] params = new Object[]{ruleId };
        jdbcTemplate.update(sql, params);
    }

    public void init() {
        //init cfgIdHash
        String sql = "SELECT RULE_ID , CFG_ID FROM AIEDRI_RULE_CFG_REL";

        List<RuleCfgVo> ruleCfgVos = jdbcTemplate.query(sql, new EJB3AnnontationRowMapper(RuleCfgVo.class));


        for(RuleCfgVo vo:ruleCfgVos){
            Integer cfgId = vo.getCfgId();
            if(cfgId > curMaxCfgId)
                curMaxCfgId = cfgId;

            ruleCfgIdHash.put(vo.getRuleId(),cfgId);
        }

        log.info("init cfg id end , max cfg id is " + curMaxCfgId);
    }

    /**
     * 1. reinit dev conn info
     * 2. reinit dev cmd info
     * 3. make task list
     */
    public String exeTasks() {
        //get Tasks

        //make Remote Tasks
        /**
         * 1. get edpi task info from db
         * 2. new
         */

        if (taskRuleVoList == null) {
            log.info("There is no Task!");
            return "";
        }

        if(issueType.equals(ACL)){
            return exeTasksAcl();
        }else if(issueType.equals(FLOWSPEC))
            return exeTasksFlowspec();
        else if(issueType.equals(ACL_PY))
            return exeTasksAclPy();


        return "";
    }

    private String exeTasksAclPy() {
        log.debug("start make acl python .cfg file!");
        Hashtable<String , String> fileHash = new Hashtable<String, String>();
        String fileDateStr = cfgFileDateFormat.format(new Date());
        String filePath = aclPyPath + File.separator +
                "hw5k-" + fileDateStr + ".cfg";

        for(DaemonTaskRuleVo vo : taskRuleVoList){

            if(vo.getTaskType().equals(TaskConstant.TASK_TYPE_ADD)){
                String idr = makeAddRuleFromVo(vo);

                if(fileHash.containsKey(filePath)){
                    String fileInfo = fileHash.get(filePath);

                    fileInfo += idr + System.lineSeparator();

                    fileHash.put(filePath,fileInfo);
                }else {
                    idr = "acl number 3999" + System.lineSeparator() + idr + System.lineSeparator();
                    fileHash.put(filePath, idr);
                }
            }else if(vo.getTaskType().equals(TaskConstant.TASK_TYPE_DELETE)){
                String idr = "undo rule " + vo.getCfgId();

                if(fileHash.containsKey(filePath)){
                    String fileInfo = fileHash.get(filePath);

                    fileInfo += idr + System.lineSeparator();
                    fileHash.put(filePath,fileInfo);
                }else {
                    idr = "acl number 3999" + System.lineSeparator() + idr + System.lineSeparator();
                    fileHash.put(filePath, idr);
                }

            }
        }

        writeCfgFile(fileHash);

        updateTaskStatusExec(filePath);
        log.debug("end make acl python .cfg file!");


        return "succ";
    }

    private void updateTaskStatusExec(String filePath) {
        //make task id list
        StringBuilder sb = new StringBuilder(65535);
        for(DaemonTaskRuleVo vo : taskRuleVoList){
            sb.append(vo.getId()).append(",");
        }

        sb.deleteCharAt(sb.length() - 1);
        String taskIdListStr = sb.toString();
        log.debug("task id list " + taskIdListStr);

        String sql = "update aiedri_task set status = ? where task_id in (" + taskIdListStr +")";

        Object[] param = new Object[]{TaskConstant.TASK_STATUS_EXEC};

        jdbcTemplate.update(sql,param);


        aclPyFileHash.put(filePath, taskIdListStr);

        log.debug("acl py put hash " + filePath + "=" + taskIdListStr);
    }

    private String exeTasksFlowspec() {
        log.info("start make flowspec .cfg file!");
        Hashtable<String , String> fileHash = new Hashtable<String, String>();
        String fileDateStr = cfgFileDateFormat.format(new Date());
        String filePath = flowSpecPath + File.separator +
                "bfs_" + fileDateStr + ".cfg";
        for(DaemonTaskRuleVo vo : taskRuleVoList){

            if(vo.getTaskType().equals(TaskConstant.TASK_TYPE_ADD)){
                String sMask = vo.getSmask();
                if(sMask == null)sMask = "32";
                String dMask = vo.getDmask();
                if(dMask == null)dMask = "32";

                String sip = vo.getSip();
                if(sip == null)
                    sip = "";
                else
                    sip += "/" + sMask;

                String dip = vo.getDip();
                if(dip == null)
                    dip = "";
                else
                    dip += "/" + dMask;


                String idr = String.format("%s|add|%s|%s|%d|%d|%s",
                        vo.getCfgId(),sip,dip,vo.getSport(),vo.getDport(),vo.getProto());

                idr = idr.replaceAll("null","");



                if(fileHash.containsKey(filePath)){
                    String fileInfo = fileHash.get(filePath);

                    fileInfo = fileInfo + idr + System.lineSeparator();

                    fileHash.put(filePath,fileInfo);
                }else {
                    fileHash.put(filePath, idr +  System.lineSeparator());
                }
            }else if(vo.getTaskType().equals(TaskConstant.TASK_TYPE_DELETE)){
                String sMask = vo.getSmask();
                if(sMask == null)sMask = "32";
                String dMask = vo.getDmask();
                if(dMask == null)dMask = "32";

                String sip = vo.getSip();
                if(sip == null)
                    sip = "";
                else
                    sip += "/" + sMask;

                String dip = vo.getDip();
                if(dip == null)
                    dip = "";
                else
                    dip += "/" + dMask;


                String idr = String.format("%s|del|%s|%s|%d|%d|%s",
                        vo.getCfgId(), sip, dip, vo.getSport(), vo.getDport(), vo.getProto());

                idr = idr.replaceAll("null","");

                if(fileHash.containsKey(filePath)){
                    String fileInfo = fileHash.get(filePath);

                    fileInfo += idr + System.lineSeparator();

                    fileHash.put(filePath,fileInfo);
                }else {
                    fileHash.put(filePath, idr + System.lineSeparator());
                }

            }
        }

        String writeRs;
        writeRs = writeCfgFile(fileHash);

        updateTaskStatusExec(filePath);

        log.info("end make flowspec .cfg file!" + writeRs);
        return "succ";
    }

    private String writeCfgFile(Hashtable<String ,String> fileHash) {

        Enumeration<String> e = fileHash.keys();

        while(e.hasMoreElements()){
            String filePath = e.nextElement();

            String fileInfo = fileHash.get(filePath);

            log.debug("filePath is " + filePath + " fileInfo is " + fileInfo);


            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath + ".tmp"));

                writer.write(fileInfo);

                writer.flush();

                File tmpFile = new File(filePath + ".tmp");
                File file = new File(filePath);

                if(tmpFile.renameTo(file)){
                    log.debug("make  cfg file " + filePath + " succ!");
                }else{
                    log.debug("make  cfg file " + filePath + " fail!");
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                return "fail";
            }


        }


        return "succ";
    }

    private String exeTasksAcl() {
        //DesCrptBean crpt = DesCrptBean.getDesCrptBean();

        //String password = crpt.encrypt("as1a1nf0");
        File file = new File(System.getProperty("user.home") + File.separator + "etc" + File.separator + "taskcfg.xml");
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        RemoteTask rTask = new RemoteTask();
        String[] cmdSequence = new String[]{"dealRule"};
        rTask.setCmdSequence(cmdSequence);
        rTask.initFromXml(in);
        //set cmd Seq
        cmdSequence = rTask.getCmdSequence();

        for (String aCmdSequence : cmdSequence) {
            log.info("cmd sequs = " + aCmdSequence);
        }
        //set some arguments by task attribute
        setRemoteTask(rTask);


        //exec task
        Map<String, List<CommandRs>> ect = RemoteTaskUtil.execCmd(rTask);


        StringBuilder stringBuilder = new StringBuilder();
        for (String tmpKey : ect.keySet()) {
            System.out.println("ip=" + tmpKey);
            stringBuilder.append(tmpKey).append(System.lineSeparator());
            for (CommandRs tmpRs : ect.get(tmpKey)) {
                System.out.println("cmdid=" + tmpRs.getCmdId());

                System.out.println("cmdrs=" + tmpRs.getStdOut());

                stringBuilder.append(tmpRs.getCmdId()).append(System.lineSeparator()).append(tmpRs.getStdOut()).append(System.lineSeparator());
            }


        }

        return stringBuilder.toString();
    }

    private void setRemoteTask(RemoteTask rTask) {

        //set ${add/delRule} by task rulelist

        Map<String, Command> cmdMap = rTask.getAllCmds();

        Command dealRuleCmd = cmdMap.get("dealRule");

        List<ExpectCond> expectConds = dealRuleCmd.getExpects();

        for (int i = 0; i < expectConds.size(); i++) {
            ExpectCond cond = expectConds.get(i);
            //find Rules
            if (cond.getNextText().contains("${Rules}") ) {
                //remove
                expectConds.remove(i);
                //add rules
                for (DaemonTaskRuleVo taskRuleVo : taskRuleVoList) {
                    Integer taskType = taskRuleVo.getTaskType();
                    StringBuilder sb = new StringBuilder();
                    if (taskType.equals(TaskConstant.TASK_TYPE_ADD)) {
                        String addRule = makeAddRuleFromVo(taskRuleVo);
                        ExpectCond addCond = new ExpectCond();
                        addCond.setNextText(addRule);
                        expectConds.add(i++, addCond);

                    } else if (taskType.equals(TaskConstant.TASK_TYPE_DELETE)) {
                        sb.append("undo rule ").append(taskRuleVo.getCfgId());
                        ExpectCond delCond = new ExpectCond();
                        delCond.setNextText(sb.toString());
                        expectConds.add(i++, delCond);
                    } else {
                        log.error("There is wrong task type " + taskType);
                    }

                }

                //break
                break;
            }
        }

        log.debug("setRemoteTask end!");

    }




    private String makeAddRuleFromVo(DaemonTaskRuleVo taskRuleVo) {
        StringBuilder sb = new StringBuilder();
        //add rule_id
        sb.append("rule ").append(taskRuleVo.getCfgId()).append(" permit ");
        String ruleType = taskRuleVo.getRuleType();

        //add prot
        if(ruleType.contains("proto")){
            sb.append(taskRuleVo.getProto());
        }else{//default
            sb.append("ip");
        }
        //add sip
        if (ruleType.contains("sip")) {
            sb.append(" source ").append(taskRuleVo.getSip());

            //add smask
            if (ruleType.contains("smask"))
                sb.append(" ").append(taskRuleVo.getSmask());
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
                sb.append(" ").append(taskRuleVo.getDmask());
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


}

class DirFilter implements FilenameFilter {
    private Pattern pattern;

    public DirFilter(String regex) {
        pattern = Pattern.compile(regex);
    }

    public boolean accept(File dir, String name) {
        // Strip path information, search for regex:
        return pattern.matcher(new File(name).getName()).matches();
    }
}


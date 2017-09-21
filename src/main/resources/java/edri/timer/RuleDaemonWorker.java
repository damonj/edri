package com.asiainfo.aiedri.timer;

import com.asiainfo.aiedri.dao.IAiedriTaskManageDAO;
import org.apache.log4j.Logger;

/**
 * Created by jiaojian on 17/7/27.
 */
public class RuleDaemonWorker {

    Logger logger = Logger.getLogger(this.getClass());

    IAiedriTaskManageDAO aiedriTaskManageDAO ;



    public void setAiedriTaskManageDAO(IAiedriTaskManageDAO aiedriTaskManageDAO) {
        this.aiedriTaskManageDAO = aiedriTaskManageDAO;
    }

    public void updateRuleStatus(){
        logger.info("Start update rule status...");

        Integer num = aiedriTaskManageDAO.updateRuleStatus();

        logger.info("Update rule status succ, total: " + num);

        /**
         * move to history
         */

        Integer moveNum = aiedriTaskManageDAO.moveNotExistRules();

        Integer deleteNum = aiedriTaskManageDAO.deleteNotExistRules();

        logger.info("Move :" + moveNum + " Delete :" + deleteNum);

    }
}

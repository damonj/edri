package com.asiainfo.aiedri.daemon;

import ainx.common.util.DesCrptBean;
import com.asiainfo.ainx.rc.server.po.CommandRs;
import com.asiainfo.ainx.rc.server.po.RemoteTask;
import com.asiainfo.ainx.rc.server.util.RemoteTaskUtil;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by jiaojian on 17/2/27.
 */
public class RemoteRunner {

    public static void main(String[] args) throws Exception {
        DesCrptBean crpt = DesCrptBean.getDesCrptBean();
        String password = crpt.encrypt("as1a1nf0");

        System.out.println(password);
        String log4jConfpath = System.getProperty("user.home") + File.separator + "etc" + File.separator + "log4j.prop";
        File tmpFile = new File(log4jConfpath);
        PropertyConfigurator.configure(log4jConfpath);
        File file = new File(System.getProperty("user.home") + File.separator + "etc" + File.separator + "example.xml");
        InputStream in = new FileInputStream(file);
        RemoteTask rTask = new RemoteTask();
        rTask.initFromXml(in);
        String[] cmdSequence = rTask.getCmdSequence();
        for (int i = 0; i < cmdSequence.length; i++) {
            System.out.println(cmdSequence[i]);
        }

        Map<String, List<CommandRs>> ect = RemoteTaskUtil.execCmd(rTask);
        for (String tmpKey : ect.keySet()) {
            System.out.println("ip=" + tmpKey);
            for (CommandRs tmpRs : ect.get(tmpKey)) {
                System.out.println("cmdid=" + tmpRs.getCmdId());
                System.out.println("cmdrs=" + tmpRs.getStdOut());
            }
        }
    }
}

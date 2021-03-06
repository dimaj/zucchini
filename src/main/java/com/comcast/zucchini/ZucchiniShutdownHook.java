package com.comcast.zucchini;

import java.util.LinkedList;
import java.io.File;
import java.io.FileWriter;

import com.google.gson.JsonArray;

import net.masterthought.cucumber.ReportBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ZucchiniShutdownHook extends Thread
{
    private static Logger logger = LoggerFactory.getLogger(ZucchiniShutdownHook.class);

    @Override
    public void run() {
        try {
            for(String fileName : AbstractZucchiniTest.featureSet.keySet()) {
                /* write the json first, needed for html generation */
                File json = new File(fileName);
                JsonArray features = AbstractZucchiniTest.featureSet.get(fileName);
                FileWriter writer = new FileWriter(json);
                writer.write(features.toString());
                writer.close();

                /* write the html report files */
                ZucchiniOutput options = getClass().getAnnotation(ZucchiniOutput.class);
                File html;
                if(options != null)
                    html = new File(options.html());
                else
                    html = new File("target/zucchini-reports");

                LinkedList<String> pathList = new LinkedList<String>();
                pathList.add(json.getAbsolutePath());
                ReportBuilder reportBuilder = new ReportBuilder(pathList, html, "", "1", "Zucchini", true, true, true, false, false, "", false);
                reportBuilder.generateReports();

                boolean buildResult = reportBuilder.getBuildStatus();
                if(!buildResult)
                    throw new Exception("BUILD FAILED - Check Report For Details");
            }
        }
        catch(Throwable t)
        {
            //System.out.print("FATAL ERROR:  " + t.toString());
            logger.error("FATAL ERROR: " + t.getMessage());
            /* must use system.halt here, system.exit stalls */
            Runtime.getRuntime().halt(-1);
        }
    }
}

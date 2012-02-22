package org.freeeed.main;

import org.freeeed.services.Util;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.freeeed.services.History;
import org.freeeed.services.Project;

/**
 * Maps input key/value pairs to a set of intermediate key/value pairs.
 *
 * @author mark
 */
public class Map extends Mapper<LongWritable, Text, MD5Hash, MapWritable> {

    static private OfficeManager officeManager = null;
    private int skip;

    public static OfficeManager getOfficeManager() {
        return officeManager;
    }

    /**
     * Called once for each key/value pair in the input split.
     *
     * @param key Key of input
     * @param value Value of input
     * @param context Holds result key/value after process, as well as other
     * params
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        // package (zip) file to be processed
        String zipFile = value.toString();

        History.appendToHistory("Processing: " + zipFile);

        // if we are in Hadoop, copy to local tmp         
        if (Util.getEnv() == Util.ENV.HADOOP) {
            String tmpDir = ParameterProcessing.TMP_DIR_HADOOP;
            if (new File(tmpDir + "/temp.zip").exists()) {
                new File(tmpDir + "/temp.zip").delete();
            }
            String cmd = "";
            if (Util.getFs() == Util.FS.HDFS || Util.getFs() == Util.FS.LOCAL) {
                cmd = "hadoop fs -copyToLocal " + zipFile + " " + tmpDir + "/temp.zip";
            } else if (Util.getFs() == Util.FS.S3) {
                cmd = "s3cmd get " + zipFile + " " + tmpDir + "/temp.zip";
            }

            PlatformUtil.runUnixCommand(cmd);
            zipFile = tmpDir + "/temp.zip";
        }
        // process archive file
        ZipFileProcessor processor = new ZipFileProcessor(zipFile, context);        
        processor.process();
    }

    @Override
    protected void setup(Mapper.Context context) {
        String projectStr = context.getConfiguration().get(ParameterProcessing.PROJECT);        
        Project project = Project.loadFromString(projectStr);
        
        Util.setEnv(project.getProperty(ParameterProcessing.PROCESS_WHERE));
        Util.setFs(project.getProperty(ParameterProcessing.FILE_SYSTEM));        

        if (project.containsKey(ParameterProcessing.CREATE_PDF)) {
            String status = PlatformUtil.verifyWkhtmltopdf();
            if (status != null) {
                System.out.println("Warning: " + status);
            }
            officeManager = new DefaultOfficeManagerConfiguration().buildOfficeManager();
            officeManager.start();
        }
        try {
            skip = 0;
            skip = Integer.parseInt(project.getProperty(ParameterProcessing.SKIP));
            Util.setSkip(skip);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            History.appendToHistory("Warning: could not parse 'skip' parameter");
        }
        Util.setProject(project);        
    }

    @Override
    protected void cleanup(Mapper.Context context) {
        Properties project = Util.getProject();
        if (project.containsKey(ParameterProcessing.CREATE_PDF)) {
            officeManager.stop();
        }
    }
}

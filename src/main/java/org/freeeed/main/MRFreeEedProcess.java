package org.freeeed.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.freeeed.main.PlatformUtil.PLATFORM;

/**
 * Configure and start Hadoop process
 */
public class MRFreeEedProcess extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        // inventory dir holds all package (zip) files resulting from stage
        String project = args[0];
        String outputPath = args[1];
        System.out.println("Running Hadoop job");
        System.out.println("Input project file = " + project);
        System.out.println("Output path = " + outputPath);

        // Hadoop configuration class
        Configuration configuration = getConf();

        Job job = new Job(configuration);
        job.setJarByClass(MRFreeEedProcess.class);
        job.setJobName("FreeEedProcess");

        // Hadoop processes key-value pairs
        job.setOutputKeyClass(MD5Hash.class);
        job.setOutputValueClass(MapWritable.class);

        // set map and reduce classes
        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        // Hadoop TextInputFormat class
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        // configure Hadoop input files
        Properties props = new Properties();
        props.load(new FileInputStream(project));
        // send complete project information to all mappers and reducers
        job.getConfiguration().set(ParameterProcessing.PROJECT, project.toString());

        String inputPath = project;
        String processWhere = props.getProperty(ParameterProcessing.PROCESS_WHERE);
        if (ParameterProcessing.PROCESS_WHERE_HADOOP.equalsIgnoreCase(processWhere)) {
            inputPath = formInputPath(props);
        }
        FileInputFormat.setInputPaths(job, inputPath);
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        configuration.set("mapred.reduce.tasks.speculative.execution", "false");

        // current decision to have one reducer -
        // combine all metadata in one place
        job.setNumReduceTasks(1);

        boolean success = job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        PLATFORM platform = PlatformUtil.getPlatform();
        int ret = 0;
        switch (platform) {
            case MACOSX:
            case LINUX:
                ret = ToolRunner.run(new MRFreeEedProcess(), args);
                break;
            case WINDOWS:
                WindowsRunner.run(args);
                break;
            default:
                System.out.println("Unknown platform: " + platform);
        }
    }

    private String formInputPath(Properties props) throws IOException {
        // TODO redo this with HDFS API
        String projectCode = props.getProperty(ParameterProcessing.PROJECT_CODE).trim();
        String cmd = "hadoop fs -rmr " + ParameterProcessing.WORK_AREA + "/" + projectCode;
        PlatformUtil.runUnixCommand(cmd);
        cmd = "hadoop fs -mkdir " + ParameterProcessing.WORK_AREA + "/" + projectCode;
        PlatformUtil.runUnixCommand(cmd);
        String tmp = "tmpinput";
        StringBuilder builder = new StringBuilder();
        String[] inputPaths = props.getProperty(ParameterProcessing.PROJECT_INPUTS).split(",");
        cmd = "hadoop fs -mkdir " + ParameterProcessing.WORK_AREA + "/" + projectCode + "/input/";
        PlatformUtil.runUnixCommand(cmd);
        int inputNumber = 0;
        for (String inputPath : inputPaths) {
            inputPath = inputPath.trim();
            FileUtils.writeStringToFile(new File(tmp), inputPath);
            ++inputNumber;
            cmd = "hadoop fs -copyFromLocal " + tmp + " "
                    + ParameterProcessing.WORK_AREA + "/" + projectCode + "/input" + inputNumber;
            PlatformUtil.runUnixCommand(cmd);
            // TODO real ip? namenode?
            builder.append("hdfs://localhost" + ParameterProcessing.WORK_AREA + "/" + projectCode + "/input" + inputNumber + ",");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
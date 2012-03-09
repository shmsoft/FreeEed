package org.freeeed.main;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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
import org.freeeed.services.Project;

/**
 * Configure and start Hadoop process
 */
public class MRFreeEedProcess extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        // inventory dir holds all package (zip) files resulting from stage
        String projectFileName = args[0];
        String outputPath = args[1];
        System.out.println("Running Hadoop job");
        System.out.println("Input project file = " + projectFileName);
        System.out.println("Output path = " + outputPath);

        // Hadoop configuration class
        Configuration configuration = getConf();
        configuration.set("mapred.reduce.tasks.speculative.execution", "false");        
        // TODO even in local mode, the first argument should not be the inventory
        // but write a complete project file instead
        Project project = Project.getProject();
        if (project == null || project.isEmpty()) {
            // configure Hadoop input files
            System.out.println("Reading project file " + projectFileName);
            project = Project.loadFromFile(new File(projectFileName));
        }
        project.setProperty(ParameterProcessing.OUTPUT_DIR_HADOOP, outputPath);
        // send complete project information to all mappers and reducers
        configuration.set(ParameterProcessing.PROJECT, project.toString());
        configuration.set(ParameterProcessing.METADATA_FILE,
                Files.toString(new File(ColumnMetadata.metadataNamesFile), Charset.defaultCharset()));
        Job job = new Job(configuration);
        job.setJarByClass(MRFreeEedProcess.class);
        job.setJobName("MRFreeEedProcess");

        // Hadoop processes key-value pairs
        job.setOutputKeyClass(MD5Hash.class);
        job.setOutputValueClass(MapWritable.class);

        // set map and reduce classes
        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        // Hadoop TextInputFormat class
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

//        String delim = "\u0001";
//        configuration.set("mapred.textoutputformat.separator", delim);
//        configuration.set("mapreduce.output.textoutputformat.separator", delim);

        System.out.println("project.isEnvHadoop() = " + project.isEnvHadoop());
        String inputPath = projectFileName;
        if (project.isEnvHadoop()) {
            inputPath = formInputPath(project);
        }
        
        System.out.println("Ready to run, inputPath = " + inputPath + ", outputPath = " + outputPath);
        FileInputFormat.setInputPaths(job, inputPath);
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        FreeEedLogging.init(false);

        boolean success = job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(Version.getVersionAndBuild());
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
        String tmp = ParameterProcessing.TMP_DIR_HADOOP + "/tmpinput";
        StringBuilder builder = new StringBuilder();
        String[] inputPaths = props.getProperty(ParameterProcessing.PROJECT_INPUTS).split(",");
        int inputNumber = 0;
        Project project = Project.getProject();
        for (String inputPath : inputPaths) {
            inputPath = inputPath.trim();
            FileUtils.writeStringToFile(new File(tmp), inputPath);
            ++inputNumber;
            if (project.isEnvHadoop() && !project.isHadoopDebug()) {
                cmd = "hadoop fs -copyFromLocal " + tmp + " "
                        + ParameterProcessing.WORK_AREA + "/" + projectCode + "/input" + inputNumber;
                PlatformUtil.runUnixCommand(cmd);
                builder.append(ParameterProcessing.WORK_AREA + "/" + projectCode + "/input" + inputNumber + ",");
            } else {
                cmd = "cp " + tmp + " " + ParameterProcessing.TMP_DIR_HADOOP + "/input" + inputNumber;
                PlatformUtil.runUnixCommand(cmd);
                builder.append(ParameterProcessing.TMP_DIR_HADOOP + "/input" + inputNumber + ",");
            }

        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
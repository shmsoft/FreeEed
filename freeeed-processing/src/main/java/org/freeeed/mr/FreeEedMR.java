/*
 *
 * Copyright SHMsoft, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeeed.mr;

import org.freeeed.util.PlatformUtil;

import com.google.common.io.Files;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.freeeed.data.index.SolrIndex;
import org.freeeed.ec2.S3Agent;
import org.freeeed.mail.EmailProperties;
import org.freeeed.main.*;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.services.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configure and start Hadoop process
 */
public class FreeEedMR extends Configured implements Tool {

    private static final Logger logger = LoggerFactory.getLogger(FreeEedMR.class);
    private final byte[] b = new byte[1024];

    @Override
    public int run(String[] args) throws Exception {
        // inventory dir holds all package (zip) files resulting from stage
        String projectFileName = args[0];
        String outputPath = args[1];
        logger.info("Running Hadoop job");
        logger.info("Input project file = " + projectFileName);
        logger.info("Output path = " + outputPath);

        SolrIndex.getInstance().init();
        
        // Hadoop configuration class
        Configuration configuration = getConf();
        // No speculative execution! Do not process the same file twice
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

        Settings.load();
        configuration.set(ParameterProcessing.SETTINGS_STR, Settings.getSettings().toString());
        configuration.set(ParameterProcessing.METADATA_FILE,
                Files.toString(new File(ColumnMetadata.metadataNamesFile), Charset.defaultCharset()));
        configuration.set(EmailProperties.PROPERTIES_FILE,
                Files.toString(new File(EmailProperties.PROPERTIES_FILE), Charset.defaultCharset()));
        Job job = new Job(configuration);
        job.setJarByClass(FreeEedMR.class);
        job.setJobName("FreeEedMR");

        // Hadoop processes key-value pairs
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(MapWritable.class);

        // set map and reduce classes
        job.setMapperClass(FreeEedMapper.class);
        job.setReducerClass(FreeEedReducer.class);
        // secondary sort for compound keys - this sorts the attachments
        job.setSortComparatorClass(KeyComparator.class);
        job.setGroupingComparatorClass(GroupComparator.class);

        // Hadoop TextInputFormat class
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        logger.debug("project.isEnvHadoop() = {} ", project.isEnvHadoop());
        String inputPath = projectFileName;
        if (project.isEnvHadoop()
                || Settings.getSettings().isHadoopDebug()) {
            inputPath = formInputPath(project);
        }

        logger.debug("Ready to run, inputPath = {}, outputPath = {}", inputPath, outputPath);
        FileInputFormat.setInputPaths(job, inputPath);
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        if (Settings.getSettings().isHadoopDebug()) {
            if (new File(outputPath).exists()) {
                Util.deleteDirectory(new File(outputPath));
            }
        }

        logger.trace("Project");
        logger.trace(project.toString());

        boolean success = job.waitForCompletion(true);
        
        SolrIndex.getInstance().destroy();
        
        if (project.isEnvHadoop() && project.isFsS3()) {
            transferResultsToS3(outputPath);
        }

        return success ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(Version.getVersionAndBuild());
        if (PlatformUtil.isNix()) {
            ToolRunner.run(new FreeEedMR(), args);
        } else {
            WindowsRunner.run(args);
        }
    }

    private String formInputPath(Properties props) throws IOException {
        String projectCode = props.getProperty(ParameterProcessing.PROJECT_CODE).trim();
        String cmd = "hadoop fs -rmr " + ParameterProcessing.WORK_AREA + "/" + projectCode;
        PlatformUtil.runCommand(cmd);
        cmd = "hadoop fs -mkdir " + ParameterProcessing.WORK_AREA + "/" + projectCode;
        PlatformUtil.runCommand(cmd);

        StringBuilder builder = new StringBuilder();
        String[] inputPaths = props.getProperty(ParameterProcessing.PROJECT_INPUTS).split(",");
        inputPaths = loadBalance(inputPaths);
        int inputNumber = 0;
        Project project = Project.getProject();
        Util.deleteDirectory(new File(ParameterProcessing.TMP_DIR_HADOOP + "/"));
        new File(ParameterProcessing.TMP_DIR_HADOOP).mkdirs();
        for (String inputPath : inputPaths) {
            ++inputNumber;
            String tmp = ParameterProcessing.TMP_DIR_HADOOP + "/input" + inputNumber;
            inputPath = inputPath.trim();
            FileUtils.writeStringToFile(new File(tmp), inputPath);

            if (project.isEnvHadoop() || Settings.getSettings().isHadoopDebug()) {
                builder.append(ParameterProcessing.WORK_AREA + "/").
                        append(projectCode).append("/input").
                        append(inputNumber).append(",");
            } else {
                builder.append(ParameterProcessing.TMP_DIR_HADOOP).append("/input").
                        append(inputNumber).append(",");
            }
        }
        if (project.isEnvHadoop() || Settings.getSettings().isHadoopDebug()) {
            File[] files = new File(ParameterProcessing.TMP_DIR_HADOOP).listFiles();
            cmd = "hadoop fs -put ";
            for (File file : files) {
                if (file.getName().startsWith("input")) {
                    cmd = cmd + file.getPath() + " ";
                }
            }
            cmd = cmd + ParameterProcessing.WORK_AREA + "/" + projectCode + "/";
            PlatformUtil.runCommand(cmd);
        } else {
            // files already in the right place
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    private void transferResultsToS3(String hdfsOutputPath) {
        try {
            String outputPath = "/mnt/tmp/results";
            File localOutput = new File(outputPath);
            if (localOutput.exists()) {
                Util.deleteDirectory(localOutput);
            }
            localOutput.mkdirs();
            if (!Settings.getSettings().isHadoopDebug()) {
                String cmd = "hadoop fs -copyToLocal "
                        + hdfsOutputPath + "/* "
                        + outputPath;
                PlatformUtil.runCommand(cmd);
            } else {
                String cmd = "cp " + hdfsOutputPath + "/* " + outputPath;
                PlatformUtil.runCommand(cmd);
            }

            File[] parts = localOutput.listFiles();
            S3Agent s3agent = new S3Agent();
            Project project = Project.getProject();
            String run = project.getRun();
            if (!run.isEmpty()) {
                run = run + "/";
            }
            for (File part : parts) {
                String s3key = project.getProjectCode() + "/"
                        + "output/"
                        + run
                        + "results/"
                        + part.getName();
                if (part.getName().startsWith("part")) {
                    s3agent.putFileInS3(part.getPath(), s3key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private void copyToHdfs(String from, String to) throws IOException {
        Configuration configuration = getConf();
        FileSystem fileSystem = FileSystem.get(configuration);

        // Check if the file already exists
        Path path = new Path(to);
        if (fileSystem.exists(path)) {
            System.out.println("File " + to + " already exists");
            return;
        }

        // Create a new file and write data to it.
        FSDataOutputStream out = fileSystem.create(path);
        InputStream in = new BufferedInputStream(new FileInputStream(
                new File(from)));


        int numBytes;
        while ((numBytes = in.read(b)) > 0) {
            out.write(b, 0, numBytes);
        }

        // Close all the file descripters
        in.close();
        out.close();
        fileSystem.close();
    }

    private String[] loadBalance(String[] inputPaths) {
        Settings settings = Settings.getSettings();
        if (!settings.isLoadBalance()) {
            return inputPaths;
        }
        S3Agent s3agent = new S3Agent();
        ArrayList<String> balancedPaths = new ArrayList<>();
        for (String fileName : inputPaths) {
            // right now balance only s3 files
            // local cluster remains unbalanced
            if (fileName.startsWith("s3://")) {
                try {
                    long size = s3agent.getFileSize(fileName);
                    long chunks = size / settings.getBytesPerMapper() + 1;
                    if (chunks == 1) {
                        balancedPaths.add(fileName);
                    } else {
                        for (int chunk = 0; chunk < chunks; ++chunk) {
                            balancedPaths.add(fileName + " "
                                    + (chunk * settings.getItemsPerMapper() + 1) + " "
                                    + (chunk + 1) * settings.getItemsPerMapper());
                        }
                        balancedPaths.add(fileName + " "
                                + (chunks + 1) * settings.getItemsPerMapper()
                                + " -1");
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }

            } else {
                balancedPaths.add(fileName);
            }
        }
        return balancedPaths.toArray(new String[0]);
    }
}

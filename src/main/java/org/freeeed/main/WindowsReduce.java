package org.freeeed.main;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.freeeed.services.Stats;

/**
 *
 * @author Mark Kerzner
 */
public class WindowsReduce extends Reduce {

    private String metadataOutputFileName = ParameterProcessing.getResultsDir()
            + "/metadata.csv";
    private static WindowsReduce instance = null;

    private WindowsReduce() {
    }

    public static synchronized WindowsReduce getInstance() {
        if (instance == null) {
            instance = new WindowsReduce();
            try {
                instance.setup(null);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
        return instance;
    }

    @Override
    protected void setup(Reducer.Context context)
            throws IOException, InterruptedException {
        Configuration projectConfig = FreeEedMain.getInstance().getProcessingParameters();
        String projectFile = projectConfig.getString(ParameterProcessing.PROJECT_FILE_NAME);
        project = new Properties();
        project.load(new FileInputStream(projectFile));
        Util.setEnv(project.getProperty(ParameterProcessing.PROCESS_WHERE));
        Util.setFs(project.getProperty(ParameterProcessing.FILE_SYSTEM));

        if (Util.getEnv() == Util.ENV.HADOOP) {
            String metadataFileContents = context.getConfiguration().get(ParameterProcessing.METADATA_FILE);
            Files.write(metadataFileContents.getBytes(), new File(ColumnMetadata.metadataNamesFile));
        }
        columnMetadata = new ColumnMetadata();
        columnMetadata.setFieldSeparator(project.getProperty(ParameterProcessing.FIELD_SEPARATOR));
        columnMetadata.setAllMetadata(project.getProperty(ParameterProcessing.METADATA_OPTION));
        // write standard metadata fields
        new File(ParameterProcessing.getResultsDir()).mkdirs();
        Files.append(columnMetadata.delimiterSeparatedHeaders(),
                new File(metadataOutputFileName), Charset.defaultCharset());
        zipFileWriter.setup();
        zipFileWriter.openZipForWriting();
    }

    @Override
    protected void cleanup(Reducer.Context context)
            throws IOException, InterruptedException {
        // write summary headers with all metadata
        Files.append("\n" + columnMetadata.delimiterSeparatedHeaders(),
                new File(metadataOutputFileName), Charset.defaultCharset());
        zipFileWriter.closeZip();
        Stats.getInstance().setJobFinished();
    }

    @Override
    public void reduce(MD5Hash key, Iterable<MapWritable> values, Context context)
            throws IOException, InterruptedException {
        String outputKey = key.toString();
        for (MapWritable value : values) {
            columnMetadata.reinit();
            processMap(value);
            ++outputFileCount;
            Files.append("\n" + columnMetadata.delimiterSeparatedValues(),
                    new File(metadataOutputFileName), Charset.defaultCharset());
        }
    }
}

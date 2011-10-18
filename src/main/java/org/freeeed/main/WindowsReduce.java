package org.freeeed.main;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.freeeed.services.Stats;

/**
 *
 * @author Mark Kerzner
 */
public class WindowsReduce extends Reduce {

    private String metadataOutputFileName = ParameterProcessing.resultsDir
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
        // write standard metadata fields
        new File(ParameterProcessing.resultsDir).mkdirs();
        Files.append(columnMetadata.tabSeparatedHeaders(),
                new File(metadataOutputFileName), Charset.defaultCharset());
        zipFileWriter.openZipForWriting();
    }

    @Override
    protected void cleanup(Reducer.Context context)
            throws IOException, InterruptedException {
        // write summary headers with all metadata
        Files.append(columnMetadata.tabSeparatedHeaders(),
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
            Files.append(columnMetadata.tabSeparatedHeaders(),
                    new File(metadataOutputFileName), Charset.defaultCharset());
        }
    }
}

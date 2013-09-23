/*    
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
package org.freeeed.main;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.freeeed.services.Project;
import org.freeeed.services.Stats;

/**
 *
 * @author Mark Kerzner
 */
public class WindowsReduce extends Reduce {

    private String metadataOutputFileName = null;
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
        Project project = Project.getProject();
        metadataOutputFileName = project.getResultsDir()
                + "/metadata" + ParameterProcessing.METADATA_FILE_EXT;

        // TODO what is this doing in Windows environment?
        if (project.isEnvHadoop()) {
            String metadataFileContents = context.getConfiguration().get(ParameterProcessing.METADATA_FILE);
            Files.write(metadataFileContents.getBytes(), new File(ColumnMetadata.metadataNamesFile));
        }
        columnMetadata = new ColumnMetadata();
        String fileSeparatorStr = project.getFieldSeparator();
        char fieldSeparatorChar = Delim.getDelim(fileSeparatorStr);
        columnMetadata.setFieldSeparator(String.valueOf(fieldSeparatorChar));
        columnMetadata.setAllMetadata(project.getMetadataCollect());
        // write standard metadata fields
        new File(project.getResultsDir()).mkdirs();
        Files.append(columnMetadata.delimiterSeparatedHeaders() + "\n",
                new File(metadataOutputFileName), Charset.defaultCharset());
        zipFileWriter.setup();
        zipFileWriter.openZipForWriting();
    }

    @Override
    protected void cleanup(Reducer.Context context)
            throws IOException, InterruptedException {
        
        if (!Project.getProject().isMetadataCollectStandard()) {
            // write summary headers with all metadata
            Files.append("\n" + columnMetadata.delimiterSeparatedHeaders(),
                    new File(metadataOutputFileName), Charset.defaultCharset());
        }
        
        zipFileWriter.closeZip();
        Stats.getInstance().setJobFinished();
        String outputSuccess = Project.getProject().getResultsDir() + "/_SUCCESS";
        Files.write("", new File(outputSuccess), Charset.defaultCharset());
    }

    @Override
    public void reduce(MD5Hash key, Iterable<MapWritable> values, Context context)
            throws IOException, InterruptedException {
        isMaster = true;
        for (MapWritable value : values) {
            columnMetadata.reinit();
            ++outputFileCount;
            processMap(value);
            Files.append(columnMetadata.delimiterSeparatedValues() + "\n",
                    new File(metadataOutputFileName), Charset.defaultCharset());
            isMaster = false;
        }
    }

    public static void reinit() {
        instance = null;
    }
}

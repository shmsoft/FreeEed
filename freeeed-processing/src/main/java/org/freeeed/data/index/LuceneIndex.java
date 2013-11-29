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
package org.freeeed.data.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.freeeed.services.History;
import org.freeeed.services.Project;
import org.freeeed.services.Util;
import org.freeeed.util.ZipUtil;



/**
 *
 * Class LuceneIndex.
 *
 * @author ilazarov
 *
 */
public class LuceneIndex implements ComponentLifecycle {

    private FSDirectory fsDir;
    private IndexWriter writer;
    private String projectId;
    private String taskId;
    private String path;
    private String baseDir;

    public LuceneIndex(String baseDir, String projectId, String taskId) {
        this.baseDir = baseDir;
        this.projectId = projectId;
        this.taskId = taskId;
    }

    @Override
    public void init() {
        if (Project.getProject().isLuceneFSIndexEnabled()) {
            try {
                path = baseDir + File.separator + projectId;
                if (taskId != null) {
                    path += File.separator + taskId;
                }

                File luceneIndexDir = new File(path);

                if (luceneIndexDir.exists()) {
                    Util.deleteDirectory(luceneIndexDir);
                }

                fsDir = FSDirectory.open(luceneIndexDir);
                IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, 
                        new StandardAnalyzer(Version.LUCENE_36));
                writer = new IndexWriter(fsDir, config);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void destroy() {
        if (Project.getProject().isLuceneFSIndexEnabled()) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace(System.out);
                }
            }

            if (fsDir != null) {
                fsDir.close();
            }
        }
    }

    public void addToIndex(Directory dir) {
        try {
            writer.addIndexes(dir);
        } catch (CorruptIndexException e) {
            History.appendToHistory("Problem adding data to Lucene index - corrupt index exception");
            e.printStackTrace(System.out);
        } catch (IOException e) {
            History.appendToHistory("Problem adding data to Lucene index - IO exception");
            e.printStackTrace(System.out);
        }
    }

    public String createIndexZipFile() throws IOException {
        String zipFileName = path + ".zip";
        ZipUtil.createZipFile(zipFileName, path);

        return zipFileName;
    }
}

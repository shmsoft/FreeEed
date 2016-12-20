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
import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.freeeed.services.Project;
import org.freeeed.services.Util;
import org.freeeed.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Class LuceneIndex.
 *
 */
public class LuceneIndex implements ComponentLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(LuceneIndex.class);
    private FSDirectory fsDir;
    private IndexWriter writer;
    private String path;
    final private String projectId;
    final private String taskId;
    final private String baseDir;

    public LuceneIndex(String baseDir, String projectId, String taskId) {
        this.baseDir = baseDir;
        this.projectId = projectId;
        this.taskId = taskId;
    }

    public void init() {
        if (Project.getCurrentProject().isLuceneIndexEnabled()) {
            try {
                path = baseDir + File.separator + projectId;
                if (taskId != null) {
                    path += File.separator + taskId;
                }

                File luceneIndexDir = new File(path);

                if (luceneIndexDir.exists()) {
                    Util.deleteDirectory(luceneIndexDir);
                }

                fsDir = FSDirectory.open(luceneIndexDir.toPath());
                Analyzer analyzer = new StandardAnalyzer();
                IndexWriterConfig config = new IndexWriterConfig(analyzer);
                writer = new IndexWriter(fsDir, config);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void destroy() throws IOException {
        if (Project.getCurrentProject().isLuceneIndexEnabled()) {
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
        } catch (Exception e) {
            logger.error("Problem adding data to Lucene index", e);
        }
    }

    public String createIndexZipFile() throws IOException {
        String zipFileName = path + ".zip";
        ZipUtil.createZipFile(zipFileName, path);

        return zipFileName;
    }
}

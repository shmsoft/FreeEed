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
package org.freeeed.search.web.dao.cases;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.freeeed.search.web.model.Case;

/**
 * 
 * CaseDao implementation, using file system as storage.
 * 
 * @author ilazarov.
 *
 */
public class FSCaseDao implements CaseDao {
    private static final String CASES_FILE = "work/c.dat";
    private static final Logger logger = Logger.getLogger(FSCaseDao.class);
    
    private Map<Long, Case> casesCache;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private AtomicLong idGenerator;
    
    public void init() {
        lock.writeLock().lock();
        
        try {
            logger.info("Init FS Cases DAO...");
            casesCache = new HashMap<Long, Case>();
            
            loadCases();
            initIDGenerator();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public List<Case> listCases() {
        List<Case> result = new ArrayList<Case>();
        lock.readLock().lock();
        
        try {
            result.addAll(casesCache.values());
        } finally {
            lock.readLock().unlock();
        }
        
        return result;
    }

    @Override
    public Case findCase(long id) {
        lock.readLock().lock();
        
        try {
            return casesCache.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void saveCase(Case c) {
        lock.writeLock().lock();
        
        try {
            if (c.getId() == null) {
                c.setId(idGenerator.incrementAndGet());
            }
            
            casesCache.put(c.getId(), c);
            storeCases();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteCase(long id) {
        lock.writeLock().lock();
        
        try {
            casesCache.remove(id);
            storeCases();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void storeCases() {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        
        try {
            File dir = new File(CASES_FILE);
            File parent = dir.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            
            fos = new FileOutputStream(dir);
            oos = new ObjectOutputStream(fos);
            
            oos.writeObject(casesCache);
            
            oos.close();
            fos.close();
        } catch (Exception e) {
            logger.error("Problem storing cases from file system!", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error("Problem closing", e);
                }
            }
            
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    logger.error("Problem closing", e);
                }
            }
        }
    }
    
    private void loadCases() {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        logger.info("Preparing to open the file " + new File(CASES_FILE).getAbsolutePath());
        try {
            fis = new FileInputStream(CASES_FILE);
            ois = new ObjectInputStream(fis);
            
            @SuppressWarnings("unchecked")
            Map<Long, Case> data = (Map<Long, Case>) ois.readObject();
            if (data != null) {
                casesCache = data;
            }
        } catch (Exception e) {
            logger.error("Problem loading cases from file system!", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.error("Problem closing", e);
                }
            }
            
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    logger.error("Problem closing", e);
                }
            }
        }
    }
    
    private void initIDGenerator() {
        long max = 0;
        for (Case c : casesCache.values()) {
            if (c.getId() > max) {
                max = c.getId();
            }
        }
        
        idGenerator = new AtomicLong(max);
    }
}

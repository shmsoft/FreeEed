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
package org.freeeed.search.web.dao.user;

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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.freeeed.search.web.model.User;

/**
 * 
 * Class FSUserDao.
 * 
 * Implements UserDao interface with File system storage.
 * 
 * @author ilazarov.
 *
 */
public class FSUserDao implements UserDao {
    private static final String USERS_FILE = "work/u.dat";
    private static final Logger log = Logger.getLogger(FSUserDao.class);
    
    private Map<String, User> userCache;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    
    public void init() {
        log.info("Init FS User DAO...");
        userCache = new HashMap<String, User>();
        
        loadUsers();
        createAdminUser();
    }
    
    @Override
    public User findUser(String username) {
        lock.readLock().lock();
        
        try {
            return userCache.get(username);
        } finally {
            lock.readLock().unlock();
        }
        
    }

    @Override
    public void deleteUser(String username) {
        lock.writeLock().lock();
        
        try {
            userCache.remove(username);
            storeUsers();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void saveUser(User user) {
        lock.writeLock().lock();
        
        try {
            userCache.put(user.getUsername(), user);
            storeUsers();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public User login(String username, String password) {
        lock.readLock().lock();
        
        try {
            log.debug("Log in user: " + username);
            
            User user = userCache.get(username);
            if (user != null) {
                if (password != null) {
                    if (password.equals(user.getPassword())) {
                        return user;
                    }
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        
        return null;
    }
    
    @Override
    public List<User> listUsers() {
        List<User> result = new ArrayList<User>();
        lock.readLock().lock();
        
        try {
            result.addAll(userCache.values());
        } finally {
            lock.readLock().unlock();
        }
        
        return result;
    }

    private void storeUsers() {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        
        try {
            File dir = new File(USERS_FILE);
            File parent = dir.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            
            fos = new FileOutputStream(dir);
            oos = new ObjectOutputStream(fos);
            
            oos.writeObject(userCache);
            
            oos.close();
            fos.close();
        } catch (Exception e) {
            log.error("Problem storing users from file system!", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error("Problem closing", e);
                }
            }
            
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    log.error("Problem closing", e);
                }
            }
        }
    }
    
    private void loadUsers() {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(USERS_FILE);
            ois = new ObjectInputStream(fis);
            
            @SuppressWarnings("unchecked")
            Map<String, User> data = (Map<String, User>) ois.readObject();
            if (data != null) {
                userCache = data;
            }
        } catch (Exception e) {
            log.error("Problem loading users from file system!");
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("Problem closing", e);
                }
            }
            
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    log.error("Problem closing", e);
                }
            }
        }
    }
    
    private void createAdminUser() {
        if (userCache.containsKey("admin")) {
            return;
        }
        
        User adminUser = new User();
        adminUser.setFirstName("Admin");
        adminUser.setLastName("Super Admin");
        adminUser.setUsername("admin");
        adminUser.setPassword("admin");
        adminUser.setEmail("admin@example.com");
        
        adminUser.addRight(User.Right.APP_CONFIG);
        adminUser.addRight(User.Right.DOCUMENT_TAG);
        adminUser.addRight(User.Right.PROCESS);
        adminUser.addRight(User.Right.USERS_ADMIN);
        adminUser.addRight(User.Right.CASES);
        
        userCache.put(adminUser.getUsername(), adminUser);
    }

}

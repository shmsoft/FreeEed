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
package org.freeeed.search.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.freeeed.search.web.WebConstants;
import org.freeeed.search.web.dao.user.UserDao;
import org.freeeed.search.web.model.User;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * Class UserController.
 * 
 * @author ilazarov
 *
 */
public class UserController extends SecureController {
    private static final Logger log = Logger.getLogger(UserController.class);
    private UserDao userDao;
    
    @Override
    public ModelAndView execute() {
        if (!loggedSiteVisitor.getUser().hasRight(User.Right.USERS_ADMIN)) {
            try {
                response.sendRedirect(WebConstants.MAIN_PAGE_REDIRECT);
                return new ModelAndView(WebConstants.USER_PAGE);
            } catch (IOException e) {
            }
        }
        
        String action = (String) valueStack.get("action");
        
        log.debug("Action called: " + action);
        
        if ("delete".equals(action)) {
            String username = (String) valueStack.get("username");
            userDao.deleteUser(username);
            
            try {
                response.sendRedirect(WebConstants.LIST_USERS_PAGE_REDIRECT);
            } catch (IOException e) {
            }
        } else if ("edit".equals(action)) {
            String username = (String) valueStack.get("username");
            User user = userDao.findUser(username);
            
            valueStack.put("user", user);
        } else if ("delete".equals(action)) {
            String username = (String) valueStack.get("username");
            userDao.deleteUser(username);
            
            return new ModelAndView(WebConstants.LIST_USERS_PAGE);
        } else if ("save".equals(action)) {
            List<String> errors = new ArrayList<String>();
            
            String mode = (String) valueStack.get("mode");
            String username = (String) valueStack.get("username");
            User testUser = userDao.findUser(username);
            if (testUser != null && "new".equals(mode)) {
                errors.add("User with this user name already exists!");
            }
            
            if (!isValidField(username)) {
                errors.add("User name is missing");
            }
            
            String firstName = (String) valueStack.get("firstName");
            if (!isValidField(firstName)) {
                errors.add("First name is missing");
            }
            
            String lastName = (String) valueStack.get("lastName");
            if (!isValidField(lastName)) {
                errors.add("Last name is missing");
            }
            
            String email = (String) valueStack.get("email");
            if (!isValidField(email)) {
                errors.add("Email is missing");
            }
            
            String password1 = (String) valueStack.get("password1");
            String password2 = (String) valueStack.get("password2");
            if ("new".equals(mode)) {
                if (!isValidField(password1) || !isValidField(password2)) {
                    errors.add("Please fill in the password fields!");
                }
                
                if (!password1.equals(password2)) {
                    errors.add("Entered passwords mismatch!");
                }
            } else {
                if (isValidField(password1)) {
                    if (!password1.equals(password2)) {
                        errors.add("Entered passwords mismatch!");
                    }   
                }
            }
            
            User user = new User();
            
            user.setUsername(username);
            user.setEmail(email);
            user.setLastName(lastName);
            user.setFirstName(firstName);
            if (testUser != null) {
                user.setPassword(testUser.getPassword());
            }
            
            if (isValidField(password1)) {
                user.setPassword(password1);
            }
            
            user.clearRights();
            if (valueStack.get("PROCESS") != null) {
                user.addRight(User.Right.PROCESS);
            }
            if (valueStack.get("APP_CONFIG") != null) {
                user.addRight(User.Right.APP_CONFIG);
            }
            if (valueStack.get("CASES") != null) {
                user.addRight(User.Right.CASES);
            }
            if (valueStack.get("DOCUMENT_TAG") != null) {
                user.addRight(User.Right.DOCUMENT_TAG);
            }
            if (valueStack.get("USERS_ADMIN") != null) {
                user.addRight(User.Right.USERS_ADMIN);
            }
            
            valueStack.put("errors", errors);
            valueStack.put("user", user);
            
            if (errors.size() > 0) {
                return new ModelAndView(WebConstants.USER_PAGE);
            }
            
            userDao.saveUser(user);
            
            try {
                response.sendRedirect(WebConstants.LIST_USERS_PAGE_REDIRECT);
            } catch (IOException e) {
            }
        }
        
        return new ModelAndView(WebConstants.USER_PAGE);
    }

    private boolean isValidField(String value) {
        return value != null && !value.isEmpty();
    }
    
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}

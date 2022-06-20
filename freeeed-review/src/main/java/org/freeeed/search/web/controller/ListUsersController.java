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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.freeeed.search.web.WebConstants;
import org.freeeed.search.web.dao.user.UserDao;
import org.freeeed.search.web.model.User;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * Class ListUsersController.
 * 
 * @author ilazarov.
 *
 */
public class ListUsersController extends SecureController {
    private static final Logger log = Logger.getLogger(ListUsersController.class);
    private UserDao userDao;
    
    @Override
    public ModelAndView execute() {
        log.debug("List users called...");
        
        if (!loggedSiteVisitor.getUser().hasRight(User.Right.USERS_ADMIN)) {
            try {
                response.sendRedirect(WebConstants.MAIN_PAGE_REDIRECT);
            } catch (IOException e) {
            }
        }
        
        List<User> users = userDao.listUsers();
        Collections.sort(users, new Comparator<User>() {

            @Override
            public int compare(User o1, User o2) {
                return o1.getUsername().compareTo(o2.getUsername());
            }
            
        });
        
        valueStack.put("users", users);
        
        return new ModelAndView(WebConstants.LIST_USERS_PAGE);
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}

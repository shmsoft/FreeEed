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

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.freeeed.search.web.WebConstants;
import org.freeeed.search.web.dao.user.UserDao;
import org.freeeed.search.web.model.User;
import org.freeeed.search.web.session.LoggedSiteVisitor;
import org.springframework.web.servlet.ModelAndView;


/**
 * 
 * Class LoginController.
 * 
 * @author ilazarov.
 *
 */
public class LoginController extends BaseController {
    private static final Logger log = Logger.getLogger(LoginController.class);
    private UserDao userDao;
    
    @Override
    public ModelAndView execute() {
        String username = (String) valueStack.get("username");
        String password = (String) valueStack.get("password");
        
        log.debug("Login attempt for: " + username);
        
        User user = null;
        
        if (username != null && password != null) {
            user = userDao.login(username, password);
            if (user != null) {
                HttpSession session = request.getSession();
                LoggedSiteVisitor loggedSiteVisitor = new LoggedSiteVisitor();
                loggedSiteVisitor.setUser(user);
                
                session.setAttribute(WebConstants.LOGGED_SITE_VISITOR_SESSION_KEY, loggedSiteVisitor);
                
                this.loggedSiteVisitor = loggedSiteVisitor;
                
                log.debug("User: " + username + " logged in! IP address: " + request.getRemoteHost());
            }
        }
        
        if (user == null) {
            valueStack.put("loginError", true);
        }
        
        return new ModelAndView(WebConstants.MAIN_PAGE);
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}

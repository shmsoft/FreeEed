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
package org.freeeed.search.web.interceptors;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.freeeed.search.web.WebConstants;
import org.freeeed.search.web.controller.SecureController;
import org.freeeed.search.web.session.LoggedSiteVisitor;
import org.freeeed.search.web.session.LoggedSiteVisitorAware;
import org.freeeed.search.web.session.SiteVisitor;
import org.freeeed.search.web.session.SiteVisitorAware;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;


/**
 * 
 * The Application Session Interceptor
 * 
 * @author ilazarov
 *
 */
public class SessionInterceptor extends HandlerInterceptorAdapter {
	private static final Logger log = Logger.getLogger(SessionInterceptor.class);

	private boolean isFreePage(Object handle) {
		return !(handle instanceof SecureController);
	}
	
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handle) throws Exception {
		HttpSession session = req.getSession();
		SiteVisitor siteVisitor = null;
		LoggedSiteVisitor loggedSiteVisitor = (LoggedSiteVisitor) session.getAttribute(WebConstants.LOGGED_SITE_VISITOR_SESSION_KEY);
		
		if (!isFreePage(handle)) {
			log.debug("The page is NOT free for not logged users!");
			if (loggedSiteVisitor == null) {
				log.debug("The user is not logged in, will redirect the login required page!");
				
				redirectToLoginRequired(res);
				return false;
			}
		}
		
		log.debug("The page is free for not logged users!");
		
		if (handle instanceof SiteVisitorAware) {
			siteVisitor = (SiteVisitor) session.getAttribute(WebConstants.SITE_VISITOR_SESSION_KEY);
			if (siteVisitor == null) {
				siteVisitor = new SiteVisitor();
				siteVisitor.setVisitorIP(req.getRemoteHost());
				
				session.setAttribute(WebConstants.SITE_VISITOR_SESSION_KEY, siteVisitor);
			}
			
			((SiteVisitorAware) handle).setSiteVisitor(siteVisitor);
		}
		
		if (handle instanceof LoggedSiteVisitorAware) {
			((LoggedSiteVisitorAware) handle).setLoggedVisitor(loggedSiteVisitor);
		}
		
		return true;
	}
	
	private void redirectToLoginRequired(HttpServletResponse res) throws IOException {
        String url = res.encodeRedirectURL(WebConstants.LOGIN_REQUIRED_PAGE);
        res.sendRedirect(url);
	}
}

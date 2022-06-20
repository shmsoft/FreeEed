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
package org.freeeed.search.web.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * Class User.
 * 
 * @author ilazarov
 *
 */
public class User implements Serializable {
    private static final long serialVersionUID = -183549366648840820L;

    private Long id;
    
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    
    private Set<Right> rights = new HashSet<Right>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void addRight(Right right) {
        rights.add(right);
    }
    
    public boolean hasRight(Right right) {
        return rights.contains(right);
    }

    public void clearRights() {
        rights.clear();
    }
    
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static enum Right {
        PROCESS,
        APP_CONFIG,
        USERS_ADMIN,
        DOCUMENT_TAG,
        CASES
    }
}

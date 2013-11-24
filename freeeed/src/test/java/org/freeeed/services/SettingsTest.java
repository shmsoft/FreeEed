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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.services;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mark
 */
public class SettingsTest {
    
    public SettingsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }


    /**
     * Test of loadFromString method, of class Settings.
     */
    @Test
    public void testLoadFromString() {
        String str = "a=1\nb=2";
        Settings result = Settings.loadFromString(str);
        assertNotNull(result);
        
        String a = result.getProperty("a");
        assertEquals("1", a);
        
        String b = result.getProperty("b");
        assertEquals("2", b);
    }
    
    @Test
    public void testGetLastProjectCode() {
        String str = "last-project-code=1011";
        Settings result = Settings.loadFromString(str);
        assertNotNull(result);
        
        String s = result.getLastProjectCode();
        assertEquals("1011", s);
        
        String str2 = "a=1";
        Settings result2 = Settings.loadFromString(str2);
        String s2 = result2.getLastProjectCode();
        assertNull(s2);
    }
    
    @Test
    public void testGetInstanceType() {
        String str2 = "a=1";
        Settings result2 = Settings.loadFromString(str2);
        
        String s = result2.getInstanceType();
        assertEquals("c1.medium" , s);
    }
    
    @Test
    public void testGetAvailabilityZone() {
        String str2 = "a=1";
        Settings result2 = Settings.loadFromString(str2);
        
        String s = result2.getAvailabilityZone();
        assertEquals("us-east-1a" , s);
    }
    
    @Test
    public void testGetSolrEndpoint() {
        String str1 = "solr_endpoint=http://ivan.com";
        Settings result1 = Settings.loadFromString(str1);
        
        String s1 = result1.getSolrEndpoint();
        assertEquals("http://ivan.com" , s1);
        
        String str2 = "a=1";
        Settings result2 = Settings.loadFromString(str2);
        
        String s2 = result2.getSolrEndpoint();
        assertEquals("http://localhost:8983" , s2);
    }
    
    @Test
    public void testGetSolrCloudReplicaCount() {
        String str1 = "solrcloud_replica_count=0";
        Settings result1 = Settings.loadFromString(str1);
        
        int i1 = result1.getSolrCloudReplicaCount();
        assertEquals(1 , i1);
        
        String str2 = "solrcloud_replica_count=2";
        Settings result2 = Settings.loadFromString(str2);
        
        int i2 = result2.getSolrCloudReplicaCount();
        assertEquals(2 , i2);
        
        String str3 = "a=3";
        Settings result3 = Settings.loadFromString(str3);
        
        int i3 = result3.getSolrCloudReplicaCount();
        assertEquals(1 , i3);
    }
    
    @Test
    public void testGetSolrCloudShardCount() {
        String str1 = "solrcloud_shard_count=0";
        Settings result1 = Settings.loadFromString(str1);
        
        int i1 = result1.getSolrCloudShardCount();
        assertEquals(1 , i1);
        
        String str2 = "solrcloud_shard_count=2";
        Settings result2 = Settings.loadFromString(str2);
        
        int i2 = result2.getSolrCloudShardCount();
        assertEquals(2 , i2);
        
        String str3 = "a=3";
        Settings result3 = Settings.loadFromString(str3);
        
        int i3 = result3.getSolrCloudShardCount();
        assertEquals(1 , i3);
    }
    
    @Test
    public void testGetClusterTimeoutMin() {
        String str2 = "cluster-timeout=3";
        Settings result2 = Settings.loadFromString(str2);
        
        int i2 = result2.getClusterTimeoutMin();
        assertEquals(3 , i2);
        
        String str3 = "a=3";
        Settings result3 = Settings.loadFromString(str3);
        
        int i3 = result3.getClusterTimeoutMin();
        assertEquals(5 , i3);
    }
    
    @Test
    public void testGetRecentProjects() {
        String str1 = "recent-projects=";
        Settings result1 = Settings.loadFromString(str1);
        
        List<Project> list1 = result1.getRecentProjects();
        assertNotNull(list1);
        assertEquals(0, list1.size());
        
        String str2 = "a=1";
        Settings result2 = Settings.loadFromString(str2);
        
        List<Project> list2 = result2.getRecentProjects();
        assertNotNull(list2);
        assertEquals(0, list2.size());
    }
}

/*    
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
package org.freeeed.ec2;


import org.freeeed.ec2.SSHAgent;
import org.freeeed.services.FreeEedUtil;
import org.junit.*;

/**
 *
 * @author mark
 */
public class SSHAgentTest {
    
    private String urlToTest = "ec2-23-22-212-64.compute-1.amazonaws.com";
    private String user = "ubuntu";
    
    public SSHAgentTest() {
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

    @Test
    public void testMethod() {
    	//TODO add testing code
    }
    
    /**
     * Test of executeCommand method, of class SSHAgent.
     */
    //@Test
    public void testExecuteCommand() throws Exception {
        System.out.println("executeCommand");
        String cmdStr = "hadoop fs -ls /test-output";
        SSHAgent agent = new SSHAgent();
        agent.setHost(urlToTest);
        agent.setUser(user);
        agent.setKey("freeeed.pem");
        String [] result = agent.executeCommand(cmdStr);
        System.out.println(agent.getLastOutputLine());
        System.out.println(FreeEedUtil.arrayToString(result));
    }
    //@Test
    public void testScpTo() throws Exception {
        System.out.println("testScpTo");
        String fileFrom = "how-to-run.txt";
        String fileTo = "how-to-run-remote";
        SSHAgent agent = new SSHAgent();
        agent.setHost(urlToTest);
        agent.setUser("ubuntu");
        agent.setKey("freeeed.pem");
        agent.scpTo(fileFrom, fileTo);        
    }
    
}

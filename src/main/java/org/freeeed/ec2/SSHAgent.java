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
package org.freeeed.ec2;

import com.jcraft.jsch.*;
import java.io.*;

/**
 *
 * @author mark
 */
public class SSHAgent {

    private String user = "ubuntu";
    private String host;
    private String key;
    private StringBuilder builder = new StringBuilder();

    public String[] executeCommand(String cmdStr) throws IOException, JSchException {
        bufferInit();
        JSch jsch = new JSch();
        jsch.addIdentity(key);
        Session session = jsch.getSession(user, host, 22);
        UserInfo ui = new FreeEedUserInfo();
        session.setUserInfo(ui);
        int connectTimeout = 5000;
        session.connect(connectTimeout);

        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(cmdStr);

        channel.setInputStream(null);
        ((ChannelExec) channel).setErrStream(System.err);

        InputStream in = channel.getInputStream();

        channel.connect();

        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                bufferAppend(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                //System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
        }
        channel.disconnect();
        session.disconnect();
        return builder.toString().split("\n");
    }

    public void scpTo(String fileFrom, String fileTo) throws IOException, JSchException {
        JSch jsch = new JSch();
        jsch.addIdentity(key);
        Session session = jsch.getSession(user, host, 22);
        UserInfo ui = new FreeEedUserInfo();
        session.setUserInfo(ui);
        session.connect();

        // exec 'scp -t rfile' remotely
        String command = "scp -p -t " + fileTo;
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();
        if (checkAck(in) != 0) {
            return;
        }

        // send "C0644 filesize filename", where filename should not include '/'
        long filesize = (new File(fileFrom)).length();
        command = "C0644 " + filesize + " ";
        if (fileFrom.lastIndexOf('/') > 0) {
            command += fileFrom.substring(fileFrom.lastIndexOf('/') + 1);
        } else {
            command += fileFrom;
        }
        command += "\n";
        out.write(command.getBytes());
        out.flush();
        if (checkAck(in) != 0) {
            System.exit(0);
        }

        // send a content of lfile
        FileInputStream fis = new FileInputStream(fileFrom);
        byte[] buf = new byte[1024];
        while (true) {
            int len = fis.read(buf, 0, buf.length);
            if (len <= 0) {
                break;
            }
            out.write(buf, 0, len); //out.flush();
        }
        fis.close();
        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();
        if (checkAck(in) != 0) {
            System.exit(0);
        }
        out.close();

        channel.disconnect();
        session.disconnect();
    }

    private int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) {
            return b;
        }
        if (b == -1) {
            return b;
        }

        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    public static class FreeEedUserInfo implements UserInfo {

        @Override
        public String getPassphrase() {
            // TODO This is just for testing, change it later            
            return "welcome31415";
        }

        @Override
        public String getPassword() {
            return "";
        }

        @Override
        public boolean promptPassword(String string) {
            //System.out.println("WARN: promptPassword called");
            return true;
        }

        @Override
        public boolean promptPassphrase(String string) {
            //System.out.println("WARN: promptPassphrase called");
            return true;
        }

        @Override
        public boolean promptYesNo(String string) {
            //System.out.println("WARN: promptYesNo called");
            return true;
        }

        @Override
        public void showMessage(String string) {
            System.out.println("WARN: showMessage called");
        }
    }

    synchronized private void bufferInit() {
        builder = new StringBuilder();
    }

    synchronized private void bufferAppend(String s) {
        builder.append(s);
    }

    synchronized public String getLastOutputLine() {
        String[] lines = builder.toString().split("\n");
        if (lines != null && lines.length > 0) {
            return lines[lines.length - 1];
        } else {
            return "";
        }
    }
}

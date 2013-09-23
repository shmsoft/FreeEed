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
package org.freeeed.main;

/**
 * Processing of command-line options
 */
public enum CommandLineOption {
	HELP		("help", false, "print this help"), 
	PARAM_FILE	("param_file", true, "parameter file"), 
	DRY		("dry", false, "dry run - only read and echo parameters, but do no processing"),
        GUI             ("gui", false, "start graphical user interface"),
        ENRON		("enron", false, "process the enron data set (specific test script)"),
	VERSION		("version", false, "print the version of the software");
	private String name;
	private String help;
	private boolean hasArg;
	CommandLineOption(String name, boolean hasArg, String help) {
		this.name = name;
		this.hasArg = hasArg;
		this.help = help;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the help
	 */
	public String getHelp() {
		return help;
	}

	/**
	 * @return the hasArg
	 */
	public boolean isHasArg() {
		return hasArg;
	}
	@Override
	public String toString() {
		return name;
	}
}

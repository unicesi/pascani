/*
 * Copyright © 2015 Universidad Icesi
 * 
 * This file is part of the Pascani project.
 * 
 * The Pascani project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Pascani project is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with The Pascani project. If not, see <http://www.gnu.org/licenses/>.
 */
package org.pascani.dsl.lib.compiler.templates

import java.util.Map

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
class DeploymentTemplates {
	
	def static deployment(String packageName, String deploymentName) {
		'''
			package «packageName»
			
			deployment «deploymentName» {
			
				// Deploy and wait
				start(true, false)
			
			}
		'''
	}
	
	def static subsystems(String packageName, String subsystemName, Map<String, Integer> monitors) {
		'''
			package «packageName»
			
			includes Prerequisites
			
			subsystem «subsystemName» {
				
				on host {
					«FOR monitor : monitors.keySet»
						«monitor.toFirstLower»: compilation;
							run -r «monitors.get(monitor)» "«monitor.toFirstUpper»" -libpath classpath + #["monitors.jar"]
					«ENDFOR»
				}
			
			}
		'''
	}
	
	def static prerequisites(String packageName, String projectPath) {
		'''
			package «packageName»
			
			import org.amelia.dsl.lib.descriptors.Host
			import java.util.List
			
			subsystem Prerequisites {
				
				param Host host = host("<hostname>", 21, 22, "<user>", "<password>", "<identifier>")
				param String projectPath = "«projectPath»"
				param String temporalDirectory = "/tmp/"
				param String dependencies = 'dependencies'
				param List<String> classpath = #[
					'«"«"»dependencies»/org.eclipse.xtext.xbase.lib-2.9.2.jar',
					'«"«"»dependencies»/org.pascani.dsl.lib-1.0.0-20160502.201847-1.jar',
					'«"«"»dependencies»/org.pascani.dsl.lib.sca-1.0.0-20160502.202247-1.jar'
				]
				
				val List<String> downloads = #[
					"http://central.maven.org/maven2/org/eclipse/xtext/org.eclipse.xtext.xbase.lib/2.9.2/org.eclipse.xtext.xbase.lib-2.9.2.jar",
					"https://oss.sonatype.org/content/repositories/snapshots/org/pascani/org.pascani.dsl.lib/1.0.0-SNAPSHOT/org.pascani.dsl.lib-1.0.0-20160502.201847-1.jar",
					"https://oss.sonatype.org/content/repositories/snapshots/org/pascani/org.pascani.dsl.lib.sca/1.0.0-SNAPSHOT/org.pascani.dsl.lib.sca-1.0.0-20160502.202247-1.jar"
				]
				val List<String> downloadErrors = #["Connection refused"]
				val Integer _timeout = 3600 * 1000
				
				on host {
					prerequisites:
						cmd 'mkdir -p «"«"»temporalDirectory»'
						cd temporalDirectory
						cmd 'mkdir -p «"«"»dependencies»'
						cmd 'wget -c «"«"»downloads.get(0)» -P «"«"»dependencies»'... => [
							errorTexts = downloadErrors
							withTimeout = _timeout
						]
						cmd 'wget -c «"«"»downloads.get(1)» -P «"«"»dependencies»'... => [
							errorTexts = downloadErrors
							withTimeout = _timeout
						]
						cmd 'wget -c «"«"»downloads.get(2)» -P «"«"»dependencies»'... => [
							errorTexts = downloadErrors
							withTimeout = _timeout
						]
			
					compilation: prerequisites;
						scp '«"«"»projectPath»/pascani-gen' to '«"«"»temporalDirectory»/pascani-gen'
						compile "pascani-gen" "monitors" -classpath classpath
				}
			}
		'''
	}	
}

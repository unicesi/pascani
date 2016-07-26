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
import java.util.List
import java.util.HashMap

/**
 * @author Miguel Jiménez - Initial contribution and API
 */
class DeploymentTemplates {
	
	def static deployment(String packageName, List<String> subsystemNames, String deploymentName) {
		'''
			package «packageName»
			
			«subsystemNames.join("\n", [s|'''includes «packageName».«s»'''])»
			
			deployment «deploymentName» {
			
				// Deploy and wait
				start(true, false)
			
			}
		'''
	}
	
	def static subsystem(String packageName, String prerequisitesPackage, String component, int port) {
		val components = new HashMap()
		components.put(component, port)
		return subsystems(packageName, prerequisitesPackage, component, components)
	}
	
	def static subsystems(String packageName, String prerequisitesPackage, String subsystemName,
		Map<String, Integer> components) {
		'''
			package «packageName»
			
			includes «prerequisitesPackage».Prerequisites
			
			subsystem «subsystemName» {
				
				var Iterable<String> libpath = classpath + #['«"«"»project»/«"«"»project».jar']
				var Iterable<String> errors = #["Connection refused"]
				
				on host {
					«FOR c : components.keySet»
						«c»: compilation;
							run -r «components.get(c)» "«c»" -libpath libpath -s "r" -m "run"... => [
								errorTexts = errorTexts + errors
							]
					«ENDFOR»
				}
			
			}
		'''
	}
	
	def static prerequisites(String packageName, String projectPath, String projectName) {
		'''
			package «packageName»
			
			import java.util.List
			import org.amelia.dsl.lib.descriptors.Host
			
			subsystem Prerequisites {
				
				param Host host = host("hostname", 21, 22, "user", "password", "identifier")
				param String projectPath = "«projectPath»"
				param String project = "«projectName»"
				param String root = "~/.pascani"
				param String dependencies = "dependencies"
				param List<String> classpath = #[
					'«"«"»dependencies»/org.eclipse.xtext.xbase.lib-2.9.2.jar',
					'«"«"»dependencies»/org.pascani.dsl.lib-1.0.0.jar',
					'«"«"»dependencies»/org.pascani.dsl.lib.sca-1.0.0.jar'
				]
				
				var List<String> downloads = #[
					"http://central.maven.org/maven2/org/eclipse/xtext/org.eclipse.xtext.xbase.lib/2.9.2/org.eclipse.xtext.xbase.lib-2.9.2.jar",
					"http://central.maven.org/maven2/org/pascani/org.pascani.dsl.lib/1.0.0/org.pascani.dsl.lib-1.0.0.jar",
					"http://central.maven.org/maven2/org/pascani/org.pascani.dsl.lib.sca/1.0.0/org.pascani.dsl.lib.sca-1.0.0.jar"
				]
				var List<String> downloadErrors = #["Connection refused"]
				var Integer _timeout = 3600 * 1000
				
				on host {
					prerequisites:
						cmd 'mkdir -p «"«"»root»'
						cd root
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
						cmd 'cp «"«"»classpath.get(1)» $FRASCATI_HOME/lib'
						cmd 'cp «"«"»classpath.get(2)» $FRASCATI_HOME/lib'
						cmd 'rm -rf «"«"»root»/«"«"»project»/source'
			
					compilation: prerequisites;
						scp '«"«"»projectPath»/pascani' to '«"«"»root»/«"«"»project»/source'
						compile '«"«"»project»/source' '«"«"»project»/«"«"»project»' -classpath classpath
				}
			}
		'''
	}	
}

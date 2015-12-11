/*
 * Copyright © 2015 Universidad Icesi
 * 
 * This file is part of the Pascani DSL.
 * 
 * The Pascani DSL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Pascani DSL is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with The Pascani DSL. If not, see <http://www.gnu.org/licenses/>.
 */
package org.pascani.outputconfiguration

import org.eclipse.xtext.generator.IOutputConfigurationProvider
import org.eclipse.xtext.generator.OutputConfiguration
import org.eclipse.xtext.generator.IFileSystemAccess
import java.util.Set

class PascaniOutputConfigurationProvider implements IOutputConfigurationProvider {

	public static final String MONITORS_OUTPUT = "monitors";
	public static final String NAMESPACES_OUTPUT = "namespaces";
	public static final String TARGET_SYSTEM_OUTPUT = "target";

	/**
	 * @return a set of {@link OutputConfiguration} available for the generator
	 */
	override Set<OutputConfiguration> getOutputConfigurations() {

		val defaultOutput = new OutputConfiguration(IFileSystemAccess.DEFAULT_OUTPUT)
		defaultOutput.setDescription("Output Folder")
		defaultOutput.setOutputDirectory("./src-gen")
		defaultOutput.setOverrideExistingResources(true)
		defaultOutput.setCreateOutputDirectory(true)
		defaultOutput.setCleanUpDerivedResources(true)
		defaultOutput.setSetDerivedProperty(true)

		val monitorsOutput = new OutputConfiguration(MONITORS_OUTPUT)
		monitorsOutput.setDescription("Output folder for monitoring components")
		monitorsOutput.setOutputDirectory("./src-gen-monitors")
		monitorsOutput.setOverrideExistingResources(true)
		monitorsOutput.setCreateOutputDirectory(true)
		monitorsOutput.setCleanUpDerivedResources(true)
		monitorsOutput.setSetDerivedProperty(true)

		val namespacesOutput = new OutputConfiguration(NAMESPACES_OUTPUT)
		namespacesOutput.setDescription("Output folder for namespace components")
		namespacesOutput.setOutputDirectory("./src-gen-namespaces")
		namespacesOutput.setOverrideExistingResources(true)
		namespacesOutput.setCreateOutputDirectory(true)
		namespacesOutput.setCleanUpDerivedResources(true)
		namespacesOutput.setSetDerivedProperty(true)

		val targetOutput = new OutputConfiguration(TARGET_SYSTEM_OUTPUT)
		targetOutput.setDescription("Output folder for target-system components")
		targetOutput.setOutputDirectory("./src-gen-target")
		targetOutput.setOverrideExistingResources(true)
		targetOutput.setCreateOutputDirectory(true)
		targetOutput.setCleanUpDerivedResources(true)
		targetOutput.setSetDerivedProperty(true)

		return newHashSet(defaultOutput, monitorsOutput, namespacesOutput, targetOutput)
	}

}

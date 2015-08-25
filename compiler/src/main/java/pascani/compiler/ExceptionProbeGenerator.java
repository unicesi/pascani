/*
 * Copyright © 2015 Universidad Icesi
 * 
 * This file is part of the Pascani library.
 * 
 * The Pascani library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Pascani library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with The SLR Support Tools. If not, see <http://www.gnu.org/licenses/>.
 */
package pascani.compiler;

import java.io.File;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import pascani.compiler.util.FilenameProposal;

/**
 * TODO: documentation
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class ExceptionProbeGenerator {

	/**
	 * The directory in which the java file will be written
	 */
	private final String path;

	public ExceptionProbeGenerator(final String directoryPath) {
		this.path = directoryPath;
	}

	public JavaClassSource interceptor(String packageName) {
		File directory = new File(this.path);
		String className = new FilenameProposal("ExceptionInterceptor.java",
				directory).getNewName();

		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);

		// Set general properties
		javaClass.setPackage(packageName);
		javaClass.setName(className);

		return javaClass;
	}

}

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
package pascani.compiler.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * Utility class to name variables and files avoiding collisions with sibling
 * elements.
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class NameProposal {

	/**
	 * Sibling names (e.g., file names within the same parent)
	 */
	private final ArrayList<String> siblingsNames;

	/**
	 * An initial name. If there is no collision with sibling elements, the name
	 * remains equal
	 */
	private String intendedName;

	public NameProposal(final String intendedName, final File parentDirectory) {
		final String extension = FilenameUtils.getExtension(intendedName);

		// List java files within the parent directory
		List<File> siblings = Arrays.asList(parentDirectory
				.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return FilenameUtils.getExtension(name).equals(
								extension);
					}
				}));

		// Convert the listed files to strings (their names)
		Collection<String> siblingsNames = Collections2.transform(siblings,
				new Function<File, String>() {
					public String apply(File input) {
						return FilenameUtils.removeExtension(input.getName());
					}
				});

		this.intendedName = FilenameUtils.removeExtension(intendedName);
		this.siblingsNames = new ArrayList<String>(siblingsNames);
	}

	public NameProposal(Collection<String> siblingsNames) {
		this.siblingsNames = new ArrayList<String>(siblingsNames);
	}

	/**
	 * @return a name that does not collide with sibling elements. If there was
	 *         a collision, an index is added as suffix until there is no
	 *         collision
	 */
	public String getNewName() {
		return getNewName(this.intendedName);
	}

	/**
	 * @param intendedName
	 *            An initial name. If there is no collision with sibling
	 *            elements, the name remains equal
	 * @return a name that does not collide with sibling elements. If there was
	 *         a collision, an index is added as suffix until there is no
	 *         collision
	 */
	public String getNewName(String intendedName) {
		String definitiveName = intendedName;
		int index = 0;

		while (this.siblingsNames.contains(definitiveName)) {
			definitiveName = intendedName + index;
			index++;
		}

		this.siblingsNames.add(definitiveName);
		return definitiveName;
	}

}

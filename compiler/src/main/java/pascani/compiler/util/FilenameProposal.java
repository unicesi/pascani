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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * TODO: documentation
 * 
 * @author Miguel Jiménez - Initial contribution and API
 */
public class FilenameProposal {

	private final String intendedName;

	private final Collection<String> siblingsNames;

	public FilenameProposal(final String intendedName, final File parentDirectory) {
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
		this.siblingsNames = siblingsNames;
	}

	public FilenameProposal(String intendedName, Collection<String> siblingsNames) {
		this.intendedName = intendedName;
		this.siblingsNames = siblingsNames;
	}

	public String getNewName() {
		String definitiveName = this.intendedName;
		int index = 0;

		while (this.siblingsNames.contains(definitiveName)) {
			definitiveName = this.intendedName + index;
			index++;
		}

		return definitiveName;
	}

}

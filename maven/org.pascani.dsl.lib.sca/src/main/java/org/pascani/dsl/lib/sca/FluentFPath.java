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
 * along with The Pascani library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.pascani.dsl.lib.sca;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent interface to generate clear and understandable FPath expressions
 * 
 * @author Miguel Jiménez - Initial API and contribution
 */
public class FluentFPath {
	
	private enum Axis {
		ATTIBUTE("attribute"),
		INTERFACE("interface"),
		SCA_PARENT("scaparent"),
		SCA_CHILD("scachild"),
		SCA_SERVICE("scaservice"),
		SCA_REFERENCE("scareference"),
		SCA_PROPERTY("scaproperty"),
		SCA_WIRE("scawire"),
		SCA_BINDING("scabinding"),
		SCA_INTENT("scaintent"),
		SCA_DESCENDANT("scadescendant"),
		SCA_ANCESTOR("scaancestor"),
		SCA_SIBLING("scasibling");

		private String term;

		Axis(final String term) {
			this.term = term;
		}

		public String toString() {
			return this.term;
		}
	}
	
	private static class Step {
		private final Axis axis;
		private final String selector;
		private final boolean self;

		public Step(final String selector) {
			this(null, selector, false);
		}
		
		public Step(final Axis axis, final String selector) {
			this(axis, selector, false);
		}

		public Step(final Axis axis, final String selector,
				final boolean self) {
			this.axis = axis;
			this.selector = selector;
			this.self = self;
		}

		@Override public String toString() {
			StringBuilder sb = new StringBuilder();
			if (this.axis != null) {
				sb.append(this.axis);
				sb.append(this.self ? "-or-self" : "");
				sb.append("::");
				sb.append(this.selector);
			} else {
				sb.append("$" + this.selector);
			}
			return sb.toString();
		}
	}

	public static class Builder {
		
		public static class FirstLevelBuilder extends Builder {
			
			private FirstLevelBuilder(List<Step> steps) {
				super(steps);
			}
			
			/**
			 * Adds a step to the FPath expression of type scachild
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @param self
			 *            Indicates whether this step applies to the selector itself
			 * @return this builder
			 */
			public SecondLevelBuilder child(final String selector, final boolean self) {
				this.steps.add(new Step(Axis.SCA_CHILD, selector, self));
				return new SecondLevelBuilder(this.steps);
			}

			/**
			 * Adds a step to the FPath expression of type scachild
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @return this builder
			 */
			public SecondLevelBuilder child(final String selector) {
				return child(selector, false);
			}
			
			/**
			 * Adds a step to the FPath expression of type scaparent
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @param self
			 *            Indicates whether this step applies to the selector itself
			 * @return this builder
			 */
			public SecondLevelBuilder parent(final String selector, final boolean self) {
				this.steps.add(new Step(Axis.SCA_PARENT, selector, self));
				return new SecondLevelBuilder(this.steps);
			}

			/**
			 * Adds a step to the FPath expression of type scaparent
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @return this builder
			 */
			public SecondLevelBuilder parent(final String selector) {
				return parent(selector, false);
			}
			
			/**
			 * Adds a step to the FPath expression of type scadescendant
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @param self
			 *            Indicates whether this step applies to the selector itself
			 * @return this builder
			 */
			public SecondLevelBuilder descendant(final String selector, final boolean self) {
				this.steps.add(new Step(Axis.SCA_DESCENDANT, selector, self));
				return new SecondLevelBuilder(this.steps);
			}

			/**
			 * Adds a step to the FPath expression of type scadescendant
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @return this builder
			 */
			public SecondLevelBuilder descendant(final String selector) {
				return descendant(selector, false);
			}

			/**
			 * Adds a step to the FPath expression of type scaancestor
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @param self
			 *            Indicates whether this step applies to the selector itself
			 * @return this builder
			 */
			public SecondLevelBuilder ancestor(final String selector, final boolean self) {
				this.steps.add(new Step(Axis.SCA_ANCESTOR, selector, self));
				return new SecondLevelBuilder(this.steps);
			}

			/**
			 * Adds a step to the FPath expression of type scaancestor
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @return this builder
			 */
			public SecondLevelBuilder ancestor(final String selector) {
				return ancestor(selector, false);
			}

			/**
			 * Adds a step to the FPath expression of type scasibling
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @param self
			 *            Indicates whether this step applies to the selector itself
			 * @return this builder
			 */
			public SecondLevelBuilder sibling(final String selector, final boolean self) {
				this.steps.add(new Step(Axis.SCA_ANCESTOR, selector, self));
				return new SecondLevelBuilder(this.steps);
			}

			/**
			 * Adds a step to the FPath expression of type scasibling
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @return this builder
			 */
			public SecondLevelBuilder sibling(final String selector) {
				return sibling(selector, false);
			}
		}

		public static class SecondLevelBuilder extends FirstLevelBuilder {
			
			private SecondLevelBuilder(List<Step> steps) {
				super(steps);
			}
			
			/**
			 * Adds a step to the FPath expression of type interface
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @return this builder
			 */
			public ThirdLevelBuilder interface$(final String selector) {
				this.steps.add(new Step(Axis.INTERFACE, selector));
				return new ThirdLevelBuilder(this.steps);
			}
			
			/**
			 * Adds a step to the FPath expression of type scaservice
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @return this builder
			 */
			public ThirdLevelBuilder service(final String selector) {
				this.steps.add(new Step(Axis.SCA_SERVICE, selector));
				return new ThirdLevelBuilder(this.steps);
			}

			/**
			 * Adds a step to the FPath expression of type scareference
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @return this builder
			 */
			public ThirdLevelBuilder reference(final String selector) {
				this.steps.add(new Step(Axis.SCA_REFERENCE, selector));
				return new ThirdLevelBuilder(this.steps);
			}
			
			/**
			 * Adds a step to the FPath expression of type scaproperty
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @return this builder
			 */
			public Builder property(final String selector) {
				this.steps.add(new Step(Axis.SCA_PROPERTY, selector));
				return new Builder(this.steps);
			}
		}

		public class ThirdLevelBuilder extends Builder {
			
			private ThirdLevelBuilder(List<Step> steps) {
				super(steps);
			}

			/**
			 * Adds a step to the FPath expression of type scabinding
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @return this builder
			 */
			public FourLevelBuilder binding(final String selector) {
				this.steps.add(new Step(Axis.SCA_BINDING, selector));
				return new FourLevelBuilder(this.steps);
			}

			/**
			 * Adds a step to the FPath expression of type scawire
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @return this builder
			 */
			public Builder wire(final String selector) {
				this.steps.add(new Step(Axis.SCA_WIRE, selector));
				return new Builder(this.steps);
			}

			/**
			 * Adds a step to the FPath expression of type scaintent
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @return this builder
			 */
			public Builder intent(final String selector) {
				this.steps.add(new Step(Axis.SCA_INTENT, selector));
				return new Builder(this.steps);
			}
		}
		
		public class FourLevelBuilder extends Builder {

			private FourLevelBuilder(List<Step> steps) {
				super(steps);
			}

			/**
			 * Adds a step to the FPath expression of type attribute
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @return this builder
			 */
			public Builder attribute(final String selector) {
				this.steps.add(new Step(Axis.ATTIBUTE, selector));
				return new Builder(this.steps);
			}
		}

		/**
		 * The list containing all the expression's steps
		 */
		protected List<Step> steps;
		
		protected Builder(List<Step> steps) {
			this.steps = steps;
		}
		
		/**
		 * @return A valid FPath expression based on the configured steps
		 */
		public String build() {
			return toString();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < this.steps.size(); i++) {
				sb.append(this.steps.get(i)
						+ (i == this.steps.size() - 1 ? "" : "/"));
			}
			return sb.toString();
		}

	}

	/**
	 * @return A new builder to create a FPath expression, starting from the
	 *         domain (i.e., $domain/)
	 */
	public static Builder.FirstLevelBuilder $domain() {
		List<Step> steps = new ArrayList<Step>();
		steps.add(new Step("domain"));
		return new Builder.FirstLevelBuilder(steps);
	}

	/**
	 * @return A new builder to create a FPath expression, starting from the
	 *         given variable (i.e., $variable/)
	 */
	public static Builder.SecondLevelBuilder $(final String variable) {
		List<Step> steps = new ArrayList<Step>();
		steps.add(new Step(variable));
		return new Builder.SecondLevelBuilder(steps);
	}

}

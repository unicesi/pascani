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
			return this.axis + (this.self ? "-or-self" : "") + "::"
					+ this.selector;
		}
	}

	public static class Builder {
		
		public static class FirstLevelBuilder extends Builder {
			
			public FirstLevelBuilder(List<Step> steps) {
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

		public class SecondLevelBuilder extends FirstLevelBuilder {
			
			public SecondLevelBuilder(List<Step> steps) {
				super(steps);
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
			
			public ThirdLevelBuilder(List<Step> steps) {
				super(steps);
			}

			/**
			 * Adds a step to the FPath expression of type scabinding
			 * 
			 * @param selector
			 *            A valid FPath selector
			 * @return this builder
			 */
			public Builder binding(final String selector) {
				this.steps.add(new Step(Axis.SCA_BINDING, selector));
				return new Builder(this.steps);
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
			sb.append("$domain/");
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
	public static Builder.FirstLevelBuilder $() {
		return new Builder.FirstLevelBuilder(new ArrayList<Step>());
	}

}

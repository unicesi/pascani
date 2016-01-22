package org.pascani.dsl.lib.sca;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent interface to generate clear and understandable FPath expressions
 * 
 * @author Miguel Jiménez - Initial API and contribution
 */
public class FluentFPath {

	public static class Builder {

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
		
		private class Step {
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

		/**
		 * The list containing all the expression's steps
		 */
		private List<Step> steps;

		public Builder() {
			this.steps = new ArrayList<Step>();
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
		public Builder child(final String selector, final boolean self) {
			this.steps.add(new Step(Axis.SCA_CHILD, selector, self));
			return this;
		}

		/**
		 * Adds a step to the FPath expression of type scachild
		 * 
		 * @param selector
		 *            A valid FPath selector
		 * @return this builder
		 */
		public Builder child(final String selector) {
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
		public Builder parent(final String selector, final boolean self) {
			this.steps.add(new Step(Axis.SCA_PARENT, selector, self));
			return this;
		}

		/**
		 * Adds a step to the FPath expression of type scaparent
		 * 
		 * @param selector
		 *            A valid FPath selector
		 * @return this builder
		 */
		public Builder parent(final String selector) {
			return parent(selector, false);
		}

		/**
		 * Adds a step to the FPath expression of type scaservice
		 * 
		 * @param selector
		 *            A valid FPath selector
		 * @return this builder
		 */
		public Builder service(final String selector) {
			this.steps.add(new Step(Axis.SCA_SERVICE, selector));
			return this;
		}

		/**
		 * Adds a step to the FPath expression of type scareference
		 * 
		 * @param selector
		 *            A valid FPath selector
		 * @return this builder
		 */
		public Builder reference(final String selector) {
			this.steps.add(new Step(Axis.SCA_REFERENCE, selector));
			return this;
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
			return this;
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
			return this;
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
			return this;
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
			return this;
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
		public Builder descendant(final String selector, final boolean self) {
			this.steps.add(new Step(Axis.SCA_DESCENDANT, selector, self));
			return this;
		}

		/**
		 * Adds a step to the FPath expression of type scadescendant
		 * 
		 * @param selector
		 *            A valid FPath selector
		 * @return this builder
		 */
		public Builder descendant(final String selector) {
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
		public Builder ancestor(final String selector, final boolean self) {
			this.steps.add(new Step(Axis.SCA_ANCESTOR, selector, self));
			return this;
		}

		/**
		 * Adds a step to the FPath expression of type scaancestor
		 * 
		 * @param selector
		 *            A valid FPath selector
		 * @return this builder
		 */
		public Builder ancestor(final String selector) {
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
		public Builder sibling(final String selector, final boolean self) {
			this.steps.add(new Step(Axis.SCA_ANCESTOR, selector, self));
			return this;
		}

		/**
		 * Adds a step to the FPath expression of type scasibling
		 * 
		 * @param selector
		 *            A valid FPath selector
		 * @return this builder
		 */
		public Builder sibling(final String selector) {
			return sibling(selector, false);
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
	public static Builder $() {
		return new Builder();
	}

}

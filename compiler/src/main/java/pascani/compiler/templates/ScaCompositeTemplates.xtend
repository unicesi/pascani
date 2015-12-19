package pascani.compiler.templates

import org.ow2.frascati.remote.introspection.resources.Component
import org.ow2.frascati.remote.introspection.resources.Port
import org.ow2.frascati.remote.introspection.resources.Binding

class ScaCompositeTemplates {

	def static String parse(Component composite) {
		'''
			<composite>
				«FOR component : composite.components»
					«parseComponent(composite)»
				«ENDFOR»
			</composite>
		'''
	}

	/**
	 * TODO: These classes are not enough to export the XML composite file, 
	 * so new classes extending them must be created to add the missing 
	 * information.
	 */
	def private static String parseComponent(Component component) {
		'''
			<component name="«component.name»">
				<implementation.java class="«component»" />
				«FOR service : component.services»
					«parseService(service)»
				«ENDFOR»
				«FOR reference : component.references»
					«parseReference(reference)»
				«ENDFOR»
				«FOR property : component.properties»
					«parseProperty(property)»
				«ENDFOR»
			</component>
		'''
	}

	def private static parseProperty(org.ow2.frascati.remote.introspection.resources.Property property) {
		'''
			<property name="«property.name»">«property.value»</property>
		'''
	}

	def private static parseService(Port service) {
		'''
			<service name="«service.name»">
				<interface.java interface="«service.implementedInterface.clazz»"/>
				«FOR binding : service.bindings»
					«parseBinding(binding)»
				«ENDFOR»
			</service>
		'''
	}

	def private static parseReference(Port reference) {
		'''
			<reference name="«reference.name»">
				<interface.java interface="«reference.implementedInterface.clazz»"/>
				«FOR binding : reference.bindings»
					«parseBinding(binding)»
				«ENDFOR»
			</reference>
		'''
	}

	def private static parseBinding(Binding binding) {
	}

}

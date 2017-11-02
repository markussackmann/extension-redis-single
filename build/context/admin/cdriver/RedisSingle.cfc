<cfcomponent extends="Cache">
	<cfset fields = array(
		field(displayName = "Server Host",
			name = "hosts",
			defaultValue = "localhost:6379",
			required = true,
			description = "Redis host and port",
			type = "textarea"
		),
		field(
			displayName = "Namespace",
			name = "namespace",
			defaultValue = "lucee:cache",
			required = true,
			description = "Keys namespace. Be sure that any cache use a unique namespace to avoid keys names clashing."
		)
	)>

	<cffunction name="getClass" returntype="string">
		<cfreturn "{class}">
	</cffunction>

	<cffunction name="getLabel" returntype="string" output="no">
		<cfreturn "{label}">
	</cffunction>

	<cffunction name="getDescription" returntype="string" output="no">
		<cfreturn "{desc}">
	</cffunction>

</cfcomponent>

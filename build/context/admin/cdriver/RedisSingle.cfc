<cfcomponent extends="Cache">
	<cfset fields = array(
		field(
			displayName = "Server Host",
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
			description = "Keys namespace. Be sure that any cache use a unique namespace to avoid keys names clashing.",
			type = "text"
		),
                field(
                        displayName = "setMaxTotal",
                        name = "setMaxTotal",
                        defaultValue = "128",
                        required = true,
                        description = "",
                        type = "text"
                ),
                field(
                        displayName = "setMaxIdle",
                        name = "setMaxIdle",
                        defaultValue = "128",
                        required = true,
                        description = "",
                        type = "text"
                ),
                field(
                        displayName = "setMinIdle",
                        name = "setMinIdle",
                        defaultValue = "16",
                        required = true,
                        description = "",
                        type = "text"
                ),
                field(
                        displayName = "setMinEvictableIdleTimeMillis",
                        name = "setMinEvictableIdleTimeMillis",
                        defaultValue = "60000",
                        required = true,
                        description = "",
                        type = "text"
                ),
                field(
                        displayName = "setTimeBetweenEvictionRunsMillis",
                        name = "setTimeBetweenEvictionRunsMillis",
                        defaultValue = "30000",
                        required = true,
                        description = "",
                        type = "text"
                ),
                field(
                        displayName = "setNumTestsPerEvictionRun",
                        name = "setNumTestsPerEvictionRun",
                        defaultValue = "3",
                        required = true,
                        description = "",
                        type = "text"
                ),
                field(
                        displayName = "setTestOnBorrow",
                        name = "setTestOnBorrow",
                        defaultValue = "true",
                        required = true,
                        description = "",
                        type = "checkbox"
                ),
                field(
                        displayName = "setTestOnReturn",
                        name = "setTestOnReturn",
                        defaultValue = "true",
                        required = true,
                        description = "",
                        type = "checkbox"
                ),
                field(
                        displayName = "setTestWhileIdle",
                        name = "setTestWhileIdle",
                        defaultValue = "true",
                        required = true,
                        description = "",
                        type = "checkbox"
                ),
                field(
                        displayName = "setBlockWhenExhausted",
                        name = "setBlockWhenExhausted",
                        defaultValue = "true",
                        required = true,
                        description = "",
                        type = "checkbox"
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

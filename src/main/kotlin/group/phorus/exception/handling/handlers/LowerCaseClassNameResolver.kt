package group.phorus.exception.handling.handlers

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase

class LowerCaseClassNameResolver : TypeIdResolverBase() {

    override fun idFromValue(value: Any): String = value.javaClass.simpleName.lowercase()

    override fun idFromValueAndType(value: Any, suggestedType: Class<*>?): String = idFromValue(value)

    override fun getMechanism(): JsonTypeInfo.Id = JsonTypeInfo.Id.CUSTOM
}
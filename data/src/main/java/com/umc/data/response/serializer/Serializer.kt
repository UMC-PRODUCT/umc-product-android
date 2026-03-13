package com.umc.data.response.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.intOrNull

object FlexibleLongSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleLong", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Long {
        val input = decoder as? JsonDecoder ?: return decoder.decodeLong()
        val el: JsonElement = input.decodeJsonElement()
        if (el is JsonNull) return 0L
        val prim = el as? JsonPrimitive
        return prim?.longOrNull
            ?: prim?.contentOrNull?.toLongOrNull()
            ?: 0L
    }

    override fun serialize(encoder: Encoder, value: Long) {
        encoder.encodeLong(value)
    }
}

object FlexibleLongNullableSerializer : KSerializer<Long?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleLongNullable", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Long? {
        val input = decoder as? JsonDecoder ?: return decoder.decodeLong()
        val el: JsonElement = input.decodeJsonElement()
        if (el is JsonNull) return null
        val prim = el as? JsonPrimitive
        return prim?.longOrNull ?: prim?.contentOrNull?.toLongOrNull()
    }

    override fun serialize(encoder: Encoder, value: Long?) {
        if (value == null) encoder.encodeNull() else encoder.encodeLong(value)
    }
}

object FlexibleIntSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleInt", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int {
        val input = decoder as? JsonDecoder ?: return decoder.decodeInt()
        val el: JsonElement = input.decodeJsonElement()
        if (el is JsonNull) return 0
        val prim = el as? JsonPrimitive
        return prim?.intOrNull
            ?: prim?.contentOrNull?.toIntOrNull()
            ?: 0
    }

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeInt(value)
    }
}
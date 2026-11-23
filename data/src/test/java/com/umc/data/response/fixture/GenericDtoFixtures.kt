package com.umc.data.response.fixture

import com.umc.data.fixture.OutsideKeepDto

/** GsonDtoKeepPackageTest 가 제네릭 타입 인자를 끝까지 따라가는지 확인하는 keep 패키지 안의 DTO. */
class NestedEnvelope<T>(val pages: List<List<T>>)

class NestedHolder(val envelope: NestedEnvelope<OutsideKeepDto>)

open class PayloadBase<T>(val payload: T)

class InheritedPayload(payload: OutsideKeepDto) : PayloadBase<OutsideKeepDto>(payload)

class InheritedHolder(val child: InheritedPayload)

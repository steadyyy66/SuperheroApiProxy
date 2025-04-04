package com.kody.com.kody.utils

import com.google.protobuf.util.JsonFormat
import com.kody.grpc.SearchHeroResponse

object JsonUtils {

    fun JsonToSearchHeroResponse(json: String): SearchHeroResponse {
        val builder = SearchHeroResponse.newBuilder()
        JsonFormat.parser().ignoringUnknownFields().merge(json, builder)
        return builder.build()
    }

    fun SearchHeroResponseToJson(resp: SearchHeroResponse): String {
        val json2 = JsonFormat.printer().print(resp)
        return json2
    }

}
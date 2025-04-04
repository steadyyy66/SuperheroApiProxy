package json1

import com.google.protobuf.util.JsonFormat
import com.kody.grpc.SearchHeroResponse



fun jsonToProto(json: String): SearchHeroResponse {
    val builder = SearchHeroResponse.newBuilder()
    JsonFormat.parser().ignoringUnknownFields().merge(json, builder)
    return builder.build()
}

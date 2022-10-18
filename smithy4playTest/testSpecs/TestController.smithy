$version: "2"
namespace testDefinitions.test

use smithy4s.api#simpleRestJson

@httpBearerAuth
@simpleRestJson
service TestControllerService {
    version: "0.0.1",
    operations: [Test, TestWithOutput, Health, TestWithBlob, TestWithQuery, TestThatReturnsError, TestAuth]
}

@auth([])
@http(method: "POST", uri: "/blob", code: 200)
operation TestWithBlob {
    input: BlobRequest,
    output: BlobResponse
}
@auth([])
@readonly
@http(method: "GET", uri: "/error", code: 200)
operation TestThatReturnsError {
}
@auth([])
@readonly
@http(method: "GET", uri: "/query", code: 200)
operation TestWithQuery {
    input: QueryRequest
}

structure QueryRequest {
    @httpQuery("testQuery")
    @required
    testQuery: String

}

structure BlobRequest {
    @httpPayload
    @required
    body: Blob,
    @httpHeader("cOnTeNt-TyPe")
    @required
    contentType: String
}

structure BlobResponse {
    @httpPayload
    @required
    body: Blob,
    @httpHeader("cOnTeNt-TyPe")
    @required
    contentType: String
}

@auth([])
@readonly
@http(method: "GET", uri: "/health", code: 200)
operation Health {
}

@auth([])
@readonly
@http(method: "GET", uri: "/", code: 200)
operation Test {
    output: SimpleTestResponse
}

@auth([])
@http(method: "POST", uri: "/test/{pathParam}", code: 200)
operation TestWithOutput {
    input: TestRequestWithQueryAndPathParams,
    output: TestWithOutputResponse
}

structure SimpleTestResponse {
    message: String
}

structure TestRequestWithQueryAndPathParams {
    @httpLabel
    @required
    pathParam: String,
    @httpQuery("testQuery")
    @required
    testQuery: String,
    @httpHeader("Test-Header")
    @required
    testHeader: String,
    @httpPayload
    @required
    body: TestRequestBody
}

structure TestRequestBody {
    @required
    message: String
}

structure TestWithOutputResponse {
    @httpPayload
    @required
    body: TestResponseBody
}

structure TestResponseBody {
    @required
    testHeader: String,
    @required
    pathParam: String,
    @required
    testQuery: String,
    @required
    bodyMessage: String
}


@auth([httpBearerAuth])
@http(method: "GET", uri: "/auth", code: 200)
operation TestAuth {

}



package de.innfactory.smithy4play.client

import de.innfactory.smithy4play.ClientResponse
import smithy4s.http.HttpEndpoint

import scala.concurrent.ExecutionContext

class SmithyPlayClient[Alg[_[_, _, _, _, _]], F[_]](
  baseUri: String,
  val service: smithy4s.Service[Alg],
  client: RequestClient
)(implicit executionContext: ExecutionContext) {

  def send[I, E, O, SI, SO](
    op: service.Operation[I, E, O, SI, SO],
    additionalHeaders: Option[Map[String, Seq[String]]]
  ): ClientResponse[O] = {

    val (input, endpoint) = service.endpoint(op)
    HttpEndpoint
      .cast(endpoint)
      .map(httpEndpoint =>
        new SmithyPlayClientEndpoint(endpoint, baseUri, additionalHeaders, httpEndpoint, input, client).send()
      )
      .toOption
      .get
  }

}

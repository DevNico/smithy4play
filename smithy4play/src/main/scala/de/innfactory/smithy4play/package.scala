package de.innfactory.smithy4play

import cats.data.{EitherT, Kleisli}
import de.innfactory.smithy4play.client.{SmithyPlayClientEndpointErrorResponse, SmithyPlayClientEndpointResponse}
import de.innfactory.smithy4play.middleware.MiddlewareBase
import org.slf4j
import play.api.Logger
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.{Headers, RequestHeader}
import play.api.routing.Router.Routes
import smithy4s.http.{CaseInsensitive, HttpEndpoint}

import scala.annotation.{MacroAnnotation, experimental}
import scala.concurrent.Future
import scala.language.experimental.macros
import scala.quoted.{Expr, Quotes}

trait ContextRouteError extends StatusResult[ContextRouteError] {
  def message: String
  def toJson: JsValue
}

type ClientResponse[O]        = Future[
  Either[SmithyPlayClientEndpointErrorResponse, SmithyPlayClientEndpointResponse[O]]
]
type RunnableClientRequest[O] = Kleisli[ClientResponse, Option[Map[String, Seq[String]]], O]
type RouteResult[O]           = EitherT[Future, ContextRouteError, O]
type ContextRoute[O]          = Kleisli[RouteResult, RoutingContext, O]

trait StatusResult[S <: StatusResult[S]] {
  def status: Status
  def addHeaders(headers: Map[String, String]): S
}

case class Status(headers: Map[String, String], statusCode: Int)
object Status {
  implicit val format: OFormat[Status] = Json.format[Status]
}

case class EndpointResult(body: Option[Array[Byte]], status: Status) extends StatusResult[EndpointResult] {
  override def addHeaders(headers: Map[String, String]): EndpointResult = this.copy(
    status = status.copy(
      headers = status.headers ++ headers
    )
  )
}

private[smithy4play] case class Smithy4PlayError(
  message: String,
  status: Status,
  additionalInformation: Option[String] = None
) extends ContextRouteError {
  override def toJson: JsValue                                            = Json.toJson(this)(Smithy4PlayError.format)
  override def addHeaders(headers: Map[String, String]): Smithy4PlayError = this.copy(
    status = status.copy(
      headers = status.headers ++ headers
    )
  )
}

object Smithy4PlayError {
  implicit val format: OFormat[Smithy4PlayError] = Json.format[Smithy4PlayError]
}

private[smithy4play] val logger: slf4j.Logger = Logger("smithy4play").logger

private[smithy4play] def getHeaders(headers: Headers): Map[CaseInsensitive, Seq[String]] =
  headers.headers.groupBy(_._1).map { case (k, v) =>
    (CaseInsensitive(k), v.map(_._2))
  }

private[smithy4play] def matchRequestPath(
  x: RequestHeader,
  ep: HttpEndpoint[_]
): Option[Map[String, String]] =
  ep.matches(x.path.replaceFirst("/", "").split("/").filter(_.nonEmpty))

@experimental
class AutoRouting extends MacroAnnotation {
  override def transform(using quotes: Quotes)(
    tree: quotes.reflect.Definition
  ): List[quotes.reflect.Definition] = {
    import quotes.reflect.*

    tree match {
      case ClassDef(name, constr, parents, selfOpt, body) =>
        val classSymbol = constr.symbol.owner
        val thisRef: Term = This(classSymbol)

        val cls = ClassDef.copy(tree)(
          name = name,
          constr = constr,
          parents = parents ++ List(Applied(TypeTree.of[AutoRoutableController], Nil)),
          selfOpt = selfOpt,
          body = body
            ++ List(
            //            ValDef(
            //              Symbol.newVal("router"),
            //              Applied(Inferred(), List(Applied(TypeIdent(TypeRepr.of[Seq[?]]), List(TypeIdent(TypeRepr.of[MiddlewareBase]))), TypeIdent(TypeRepr.of[Routes]))),
            //              Some(
            //                Apply(
            //                  Apply(
            //                    TypeApply(
            //                      Select(This(Some("TestController")), "transformToRouter"),
            //                      List(Inferred(), Inferred())
            //                    ),
            //                    List(This(None))
            //                  ),
            //                  List(Ident("serviceInstance"), Ident("executionContext"), Ident("cc"))))))
            //          )
            ValDef(Symbol.newVal(classSymbol, "router", TypeRepr.of[Any], Flags.Override, Symbol.noSymbol), Some(thisRef))
          )
        )

        report.info(cls.show(using Printer.TreeStructure))

        List(cls)
      case _ =>
        report.error("Annotation @AutoRouting can only be used on classes")
        List(tree)
    }
  }
}

private[smithy4play] trait Showable {
  this: Product =>
  override def toString: String = this.show
}

private[smithy4play] object Showable {
  implicit class ShowableProduct(product: Product) {
    def show: String = {
      val className   = product.productPrefix
      val fieldNames  = product.productElementNames.toList
      val fieldValues = product.productIterator.toList
      val fields      = fieldNames.zip(fieldValues).map { case (name, value) =>
        value match {
          case subProduct: Product => s"$name = ${subProduct.show}"
          case _                   => s"$name = $value"
        }
      }
      fields.mkString(s"$className(", ", ", ")")
    }
  }
}

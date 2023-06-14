package controller.middlewares

import cats.data.Kleisli
import de.innfactory.smithy4play.{ MiddlewareBase, RouteResult, RoutingContext }
import smithy4s.ShapeTag
import testDefinitions.test.DisableTestMiddleware

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DisableMiddleware @Inject() (implicit
  executionContext: ExecutionContext
) extends MiddlewareBase {

  override val middlewareEnableHint: Option[ShapeTag[_]]  = None
  override val middlewareDisableFlag: Option[ShapeTag[_]] = Some(DisableTestMiddleware.tagInstance)

  override def logic: Kleisli[RouteResult, RoutingContext, RoutingContext] = {
    disableLogic { r =>
    logger.info("[DisableMiddleware.logic]")
      r.copy(attributes = r.attributes + ("Not" -> "Disabled"))
    }
  }
}

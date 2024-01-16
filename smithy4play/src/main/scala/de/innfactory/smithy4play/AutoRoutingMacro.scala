import scala.annotation.{MacroAnnotation, experimental}
import scala.quoted.Quotes
//package de.innfactory.smithy4play
//
//import scala.reflect.macros.whitebox
//
//object AutoRoutingMacro {
//  def impl(c: whitebox.Context)(annottees: c.Tree*): c.Tree = {
//    import c.universe._
//    annottees match {
//      case List(
//            q"$mods class $className $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parentss { $self => ..$body }"
//          ) =>
//        q"""$mods class $className $ctorMods(...$paramss)
//                 extends { ..$earlydefns }
//                 with ..$parentss
//                 with de.innfactory.smithy4play.AutoRoutableController
//              { $self =>
//                  override val router: Seq[de.innfactory.smithy4play.middleware.MiddlewareBase] => play.api.routing.Router.Routes = this
//                ..$body }
//                """
//      case _ =>
//        c.abort(
//          c.enclosingPosition,
//          "RegisterClass: An AutoRouter Annotation on this type of Class is not supported."
//        )
//    }
//  }
//}

//@experimental
//class cached extends MacroAnnotation {
//  override def transform(using quotes: Quotes)(
//    tree: quotes.reflect.Definition
//  ): List[quotes.reflect.Definition] = ???
//}
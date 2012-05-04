package uk.gov.gds.router.repository.route

import uk.gov.gds.router.model._
import com.mongodb.casbah.Imports._
import uk.gov.gds.router.repository._

object Routes extends MongoRepository[Route]("routes", "incoming_path") {

  override def load(id: String) = super.load(id) match {
    case None =>
      val prefixPath = id.split("/").take(1).mkString("/")
      collection.findOne(MongoDBObject("incoming_path" -> prefixPath, "route_type" -> "prefix"))

    case Some(route) =>
      Some(route)
  }


  override def store(obj: Route) = super.load(obj.id) match {
    case Some(route) if (obj.route_type == "prefix") => Conflict
    case Some(route) if (obj.incoming_path == route.incoming_path) => Conflict
    case Some(route) if (obj.application_id == ApplicationForRedirectRoutes.application_id) =>
      val newObj = obj.copy(route_action = "redirect")
      collection += newObj
      NewlyCreated
    case None =>
      collection += obj
      NewlyCreated
  }

  def deactivateFullRoute(route: Route) = {
    Routes.simpleAtomicUpdate(route.id, Map(
      "application_id" -> ApplicationForGoneRoutes.application_id,
      "route_action" -> "gone")
    )

    route.copy(
      application_id = ApplicationForGoneRoutes.application_id,
      route_action = "gone"
    )
  }

  private[repository] def deactivateAllRoutesForApplication(id: String) {
    val routesForApp: List[Route] = collection.find(MongoDBObject("application_id" -> id)).toList //todo make implicit to convert Seq here to List

    routesForApp.foreach {
      route =>
        route.proxyType match {
          case FullRoute => deactivateFullRoute(route)
          case PrefixRoute => collection -= MongoDBObject("incoming_path" -> route.incoming_path)
        }
    }
  }
}
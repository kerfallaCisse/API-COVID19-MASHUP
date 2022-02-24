package controllers


import models.objectToReturn

import javax.inject._
import play.api.mvc._
import play.api.libs.json._

import java.io.FileInputStream



/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def deaths() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.cases())
  }

  def vaccines() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.vaccines())
  }

  def getAllcountries = Action { implicit request: Request[AnyContent] =>
    val availableCountries = getDatas.getAllcountries()
    Ok(Json.prettyPrint(Json.toJson(availableCountries)))
  }

  // Fonction qui permet de récupérer toutes les informations concernant un pays.
  def getAcountry(countryName: String) = Action { implicit request: Request[AnyContent] =>

    // On parse le fichier cases.json
    var stream = new FileInputStream("cases.json")
    val jsonCountries = try {
      Json.parse(stream)
    }
    finally {
      stream.close()
    }
    // On parse le fichier vaccins.json
    stream = new FileInputStream("vaccins.json")
    val jsVaccins = try {
      Json.parse(stream)
    }
    finally {
      stream.close()
    }

    val jsCountriesList = List(jsonCountries)
    var correspondingObject = List(jsCountriesList.head(countryName) \ "All")
    var confirmed: Double = 0
    var deaths: Double = 0
    var abbreviation: String = ""

    for (i <- correspondingObject) {
      // Pour le Sri Lanka
      if(!(i \ "abbreviation").isDefined) {
        abbreviation = "LK"
        confirmed = (i \ "confirmed").as[Double]
        deaths = (i \ "deaths").as[Double]
      }
      else {
        abbreviation = (i \ "abbreviation").as[String]
        confirmed = (i \ "confirmed").as[Double]
        deaths = (i \ "deaths").as[Double]
      }
    }

    // On traite les données sur les vaccins.
    val jsVaccinsList = List(jsVaccins)
    correspondingObject = List(jsVaccinsList.head(countryName) \ "All")
    var administered: Double = 0
    var people_vaccinated: Double = 0
    var people_partially_vaccinated: Double = 0

    for (j <- correspondingObject) {
      administered = (j \ "administered").as[Double]
      people_vaccinated = (j \ "people_vaccinated").as[Double]
      people_partially_vaccinated = (j \ "people_partially_vaccinated").as[Double]
    }

    // On crée l'objet à retourner
    val objToReturn = new objectToReturn(countryName, abbreviation, confirmed, deaths,
      administered, people_vaccinated, people_partially_vaccinated)

    Ok(Json.prettyPrint(Json.toJson(objToReturn)))

  }
  // Données globaux mondiaux sur les cas et les vaccins
  def global() = Action { implicit request: Request[AnyContent] =>
    val global_cases = getDatas.globalCases()
    val global_vaccins = getDatas.globalVaccins()
    Ok(Json.prettyPrint(Json.obj("cas_confirmes"->global_cases,"vaccins_administres"->global_vaccins)))
  }

}

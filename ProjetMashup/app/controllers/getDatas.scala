package controllers

import play.api.libs.json.{JsValue, Json}
import sttp.client3.playJson.asJson
import sttp.client3.{HttpURLConnectionBackend, UriContext, basicRequest}
import util.control.Breaks._
import java.util.{Timer, TimerTask}

import java.io.FileInputStream

import models._

import scala.collection.mutable.ArrayBuffer


object getDatas {

  def getRequest(): Unit = {
    // Première requête pour récupérer les données sur les cas.
    val first_backend = HttpURLConnectionBackend()
    val first_request = basicRequest
      .get(uri"https://covid-api.mmediagroup.fr/v1/cases")
      .response(asJson[JsValue].getRight)
    val first_response = first_request.send(first_backend).body
    // On stocke le résultat dans un fichier json dans le répertoire courant
    os.write.over(os.pwd / "cases.json", first_response.toString())
    // Deuxième requête pour récupérer les données sur les vaccins.
    val second_backend = HttpURLConnectionBackend()
    val second_request = basicRequest
      .get(uri"https://covid-api.mmediagroup.fr/v1/vaccines")
      .response(asJson[JsValue].getRight)
    val second_response = second_request.send(second_backend).body
    // On stocke le résultat dans un autre fichier json également dans le répertoire courant
    os.write.over(os.pwd / "vaccins.json", second_response.toString())
  }

  def getAllcountries(): ArrayBuffer[objectToReturn] = {
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
    val allCountriesName = ArrayBuffer[JsValue]()
    val cases = jsonCountries \\ "All"
    for (i <- cases) {
      // On récupère uniquement les données qui ont une classe définie afin d'éliminer les données inutiles.
      // Puis on les ajoutes dans le tableau
      if ((i \ "country").isDefined) allCountriesName.addOne((i \ "country").get)
    }
    val jsCountriesList = List(jsonCountries)
    val jsVaccinsList = List(jsVaccins)
    val arrayOfObject = ArrayBuffer[objectToReturn]()

    for (j <- allCountriesName) {
      breakable {
        // On récupère les données sur les cas
        // L'abbréviation pour le Sri lanka n'existe pas dans la BD.
        var abbreviation = ""
        var country = ""
        var confirmed = 0.0
        var deaths = 0.0
        // Abbreviation pour le Sri Lanka
        if(!(jsCountriesList.head(j.as[String]) \ "All" \ "abbreviation").isDefined) {
          abbreviation = "LK"
          country = j.as[String]
          confirmed = (jsCountriesList.head(j.as[String]) \ "All" \ "confirmed").as[Double]
          deaths = (jsCountriesList.head(j.as[String]) \ "All" \ "deaths").as[Double]
        }
        else {
          country = (jsCountriesList.head(j.as[String]) \ "All" \ "country").as[String]
          abbreviation = (jsCountriesList.head(j.as[String]) \ "All" \ "abbreviation").as[String]
          confirmed = (jsCountriesList.head(j.as[String]) \ "All" \ "confirmed").as[Double]
          deaths = (jsCountriesList.head(j.as[String]) \ "All" \ "deaths").as[Double]
        }

        try {
          // On essaie de récupérer les données sur les vaccins
          (jsVaccinsList.head(j.as[String]) \ "All" \ "administered").as[Double]
          (jsVaccinsList.head(j.as[String]) \ "All" \ "people_vaccinated").as[Double]
          (jsVaccinsList.head(j.as[String]) \ "All" \ "people_partially_vaccinated").as[Double]
        }
        catch {
          case _: java.util.NoSuchElementException =>
            println("Les données sur " + j.toString() + " concernant les vaccins n'existe pas")
            // On passe donc à la prochaine itération
            break()
        }
        // On récupère les données sur les vaccins
        val administered = (jsVaccinsList.head(j.as[String]) \ "All" \ "administered").as[Double]
        val people_vaccinated = (jsVaccinsList.head(j.as[String]) \ "All" \ "people_vaccinated").as[Double]
        val people_partially_vaccinated = (jsVaccinsList.head(j.as[String]) \ "All" \ "people_partially_vaccinated").as[Double]

        val objToReturn = new objectToReturn(country, abbreviation, confirmed, deaths,
          administered, people_vaccinated, people_partially_vaccinated)
        arrayOfObject.addOne(objToReturn)
      }
    }
    return arrayOfObject
  }

  // ON fait une autre fonction qui permet uniquement de rensigner les données sur les cas mondiaux, y compris pour les vaccins

  def globalCases(): Int = {
    // On parse le fichier cases.json
    val stream = new FileInputStream("cases.json")
    val jsonCases = try {
      Json.parse(stream)
    }
    finally {
      stream.close()
    }
    val global = (jsonCases \ "Global" \ "All" \ "confirmed").get.as[Int]
    return global
  }

  // Pour les vaccins
  def globalVaccins(): JsValue = {
    // On parse le fichier vaccins.json
    val stream = new FileInputStream("vaccins.json")
    val jsonVaccins = try {
      Json.parse(stream)
    }
    finally {
      stream.close()
    }
    val global = (jsonVaccins \ "Global" \ "All" \ "administered").get
    return global
  }

  // Fonctions qui va exécuter toutes les tâches.
  def executeTask(): Unit = {
    val timer = new Timer()
    val taskRequest = new TimerTask {
      override def run(): Unit = {
        getRequest()
      }
    }


    // On planifie les tâches à exécuter.
    // On envoie les requêtes toutes les heures. On exécute uniquement la fonction getRequest qui va permettre
    // la mise à jour des deux fichiers json. L'api M-Media effectue une mise à jour une fois toutes les heures
    timer.schedule(taskRequest,0L,12*300000L)

    Thread.sleep(2*300000L)

    timer.cancel()
    taskRequest.cancel()



  }


  def main(args: Array[String]): Unit = {
    //println(getAllcountries().length) -> 178 pays pour lesquels on a des données correctement.
    executeTask()

  }

}

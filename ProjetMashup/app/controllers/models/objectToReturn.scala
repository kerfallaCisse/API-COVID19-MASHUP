package models

import play.api.libs.json.Json


case class objectToReturn(countryName: String, abbreviation: String, confirmed: Double, deaths: Double, administered: Double,
                          people_vaccinated: Double, people_partially_vaccinated: Double
                         )

object objectToReturn {

  // Pour lire et écrire un objet de la class objectToReturn
  implicit val readObject = Json.reads[objectToReturn]
  implicit val writeObject = Json.writes[objectToReturn]

}

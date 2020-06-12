package nl.kevinvandervlist.ohti.portal

import java.util.UUID

import io.circe.{Decoder, Encoder, HCursor, Json}
import nl.kevinvandervlist.ohti.api.model
import nl.kevinvandervlist.ohti.api.model.{DeviceProperty, ScheduledChoice}
import nl.kevinvandervlist.ohti.portal.TokenManager._
import nl.kevinvandervlist.ohti.portal.Schedules._
import nl.kevinvandervlist.ohti.portal.Devices._
import sttp.client._
import sttp.client.circe._

object Devices {
  implicit val decodeDevice: Decoder[nl.kevinvandervlist.ohti.api.model.Device] = new Decoder[nl.kevinvandervlist.ohti.api.model.Device] {
    final def apply(c: HCursor): Decoder.Result[nl.kevinvandervlist.ohti.api.model.Device] = for {
      tpe <- c.downField("type").as[Int]
      scheduleId <- c.downField("scheduleId").as[Option[String]]
      id <- c.downField("id").as[String]
      name <- c.downField("name").as[Option[String]]
      isOnline <- c.downField("isOnline").as[Boolean]
      model <- c.downField("model").as[String]
      manufacturer <- c.downField("manufacturer").as[String]
      props <- c.downField("properties").as[List[DeviceProperty]]
      params <- c.downField("parameters").as[Map[String, String]]
      _etag <- c.downField("_etag").as[Long]
      bdrSetting <- c.downField("bdrSetting").as[Int]
    } yield {
      nl.kevinvandervlist.ohti.api.model.Device(
        tpe,
        scheduleId.map(UUID.fromString),
        UUID.fromString(id),
        name,
        isOnline,
        model,
        manufacturer,
        props,
        params,
        _etag,
        bdrSetting
      )
    }
  }

  implicit val encodeDevice: Encoder[nl.kevinvandervlist.ohti.api.model.Device] = new Encoder[nl.kevinvandervlist.ohti.api.model.Device] {
    override def apply(a: model.Device): Json = Json.obj(
      ("type", Json.fromInt(a.`type`)),
      ("scheduleId", a.scheduleId match {
        case Some(id) => Json.fromString(id.toString)
        case None => Json.Null
      }),
      ("id", Json.fromString(a.id.toString)),
      ("name", a.name match {
        case Some(n) => Json.fromString(n)
        case None => Json.Null
      }),
      ("isOnline", Json.fromBoolean(a.isOnline)),
      ("model", Json.fromString(a.model)),
      ("manufacturer", Json.fromString(a.manufacturer)),
      ("properties", Json.arr(a.properties.map(encodeDeviceProperty.apply): _*)),
      ("parameters", Json.obj(a.parameters.toList.map { case (k, v) => (k, Json.fromString(v)) }: _*)),
      ("_etag", Json.fromLong(a._etag)),
      ("bdrSetting", Json.fromInt(a.bdrSetting)),
    )
  }

  implicit val decodeDeviceProperty: Decoder[DeviceProperty] = new Decoder[DeviceProperty] {
    final def apply(c: HCursor): Decoder.Result[DeviceProperty] = for {
      // scheduleChoices can be missing completely!
      choices <- c.downField("scheduleChoices").as[Option[List[ScheduledChoice]]]
      label <- c.downField("label").as[String]
      tpe <- c.downField("type").as[String]
      typeCustom <- c.downField("typeCustom").as[String]
      statusModified <- c.downField("statusModified").as[Boolean]
      id <- c.downField("id").as[String]
      status <- c.downField("status").as[Option[String]]
      canControl <- c.downField("canControl").as[Boolean]
      hasLogging <- c.downField("hasLogging").as[Boolean]
      hasStatus <- c.downField("hasStatus").as[Boolean]
      isAvailable <- c.downField("isAvailable").as[Boolean]
      statusLastUpdated <- c.downField("statusLastUpdated").as[Option[String]]
    } yield {
      DeviceProperty(
        choices,
        label,
        tpe,
        typeCustom,
        statusModified,
        id,
        status,
        canControl,
        hasLogging,
        hasStatus,
        isAvailable,
        statusLastUpdated
      )
    }
  }

  implicit val encodeDeviceProperty: Encoder[DeviceProperty] = new Encoder[DeviceProperty] {
    override def apply(a: DeviceProperty): Json = {
      // TODO: Do this in a better way. The point is that 'scheduledChoices' can be missng.
      // But, if it's missing we cannot encode it with null, whilst all other fields should be null.
      val sched: List[(String, Json)] = a.scheduleChoices match {
        case Some(c) => List("scheduleChoices" -> Json.arr(c.map(encodeScheduledChoice.apply): _*))
        case None => List.empty
      }
      Json.obj(sched ++ List(
        ("label", Json.fromString(a.label)),
        ("type", Json.fromString(a.`type`)),
        ("typeCustom", Json.fromString(a.typeCustom)),
        ("statusModified", Json.fromBoolean(a.statusModified)),
        ("id", Json.fromString(a.id)),
        ("status", a.status match {
          case Some(s) => Json.fromString(s)
          case None => Json.Null
        }),
        ("canControl", Json.fromBoolean(a.canControl)),
        ("hasLogging", Json.fromBoolean(a.hasLogging)),
        ("hasStatus", Json.fromBoolean(a.hasStatus)),
        ("isAvailable", Json.fromBoolean(a.isAvailable)),
        ("statusLastUpdated", a.statusLastUpdated match {
          case Some(s) => Json.fromString(s)
          case None => Json.Null
        }),
      ): _*)
    }
  }
}

class Devices(private implicit val endpoint: Endpoint,
              private implicit val tokenProvider: TokenProvider,
              protected implicit val backend: SttpBackend[Identity, Nothing, NothingT]) extends Client[List[nl.kevinvandervlist.ohti.api.model.Device]] {

  def retrieveDevices(): Option[List[nl.kevinvandervlist.ohti.api.model.Device]] = {
    val request = Util.authorizedRequest(tokenProvider)
      .get(endpoint.devices)

    val response = request
      .response(asJson[List[nl.kevinvandervlist.ohti.api.model.Device]])

    doRequest("Failed to retrieve devices, got code {} - {}", response)
  }
}

class Device(private implicit val endpoint: Endpoint,
              private implicit val tokenProvider: TokenProvider,
              protected implicit val backend: SttpBackend[Identity, Nothing, NothingT]) extends Client[nl.kevinvandervlist.ohti.api.model.Device] {

  def retrieveDevice(id: UUID): Option[nl.kevinvandervlist.ohti.api.model.Device] = {
    val request = Util.authorizedRequest(tokenProvider)
      .get(endpoint.device(id.toString))

    val response = request
      .response(asJson[nl.kevinvandervlist.ohti.api.model.Device])

    doRequest("Failed to retrieve device, got code {} - {}", response)
  }

  def updateDevice(dev: nl.kevinvandervlist.ohti.api.model.Device): Option[nl.kevinvandervlist.ohti.api.model.Device] = {
    val request = Util.authorizedRequest(tokenProvider)
      .put(endpoint.device(dev.id.toString))
      .body(dev)

    val response = request
      .response(asJson[nl.kevinvandervlist.ohti.api.model.Device])

    doRequest("Failed to update device, got code {} - {}", response)
  }
}
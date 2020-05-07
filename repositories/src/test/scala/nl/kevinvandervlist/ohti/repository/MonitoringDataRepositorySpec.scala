package nl.kevinvandervlist.ohti.repository

import java.sql.DriverManager
import java.util.UUID

import nl.kevinvandervlist.ohti.repository.data.{MonitoringDataIndex, MonitoringDataValue}
import nl.kevinvandervlist.ohti.repository.impl.MonitoringDataRepositoryImpl
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MonitoringDataRepositorySpec extends AnyWordSpec with Matchers {
  private val memory = DriverManager.getConnection("jdbc:sqlite::memory:")
  private val repo = new MonitoringDataRepositoryImpl(memory)
  private val one = MonitoringDataValue(MonitoringDataIndex(UUID.randomUUID(), 15, 30), null, "CO2")
  private val two = MonitoringDataValue(MonitoringDataIndex(UUID.randomUUID(), 30, 45), BigDecimal(123), "CO2")
  private val items = List(one, two)
  "The periodic usage repository" should {
    "not have an item when its empty" in {
      val result = repo.exists(one.index)
      result.isSuccess shouldBe true
      result.get shouldBe false
    }
    "insert an item" in {
      val result = repo.put(items)
      result.isSuccess shouldBe true
      result.get shouldBe items
    }
    "have that item available" in {
      val result = repo.exists(one.index)
      result.isSuccess shouldBe true
      result.get shouldBe true
    }
    "retrieve one item" in {
      val result = repo.get(one.index)
      result.isSuccess shouldBe true
      result.get shouldBe Some(one)
    }
    "retrieve two item" in {
      val result = repo.get(one.index)
      result.isSuccess shouldBe true
      result.get shouldBe Some(one)
    }
  }
}

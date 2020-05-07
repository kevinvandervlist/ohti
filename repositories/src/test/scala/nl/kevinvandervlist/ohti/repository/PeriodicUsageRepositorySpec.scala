package nl.kevinvandervlist.ohti.repository

import java.sql.DriverManager

import nl.kevinvandervlist.ohti.repository.data.{PeriodicUsage, TimeSpan}
import nl.kevinvandervlist.ohti.repository.impl.PeriodicUsageRepositoryImpl
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PeriodicUsageRepositorySpec extends AnyWordSpec with Matchers {
  private val memory = DriverManager.getConnection("jdbc:sqlite::memory:")
  private val repo = new PeriodicUsageRepositoryImpl(memory)
  private val tsOne = TimeSpan(10, 100)
  private val tsTwo = TimeSpan(10, 11)
  private val puOne = PeriodicUsage(tsOne, "one", BigDecimal(1), BigDecimal(2), BigDecimal(3), BigDecimal(4))
  "The periodic usage repository" should {
    "not have an item when its empty" in {
      val result = repo.exists(tsOne)
      result.isSuccess shouldBe true
      result.get shouldBe false
    }
    "unable to retrieve that item" in {
      val result = repo.get(tsOne)
      result.isSuccess shouldBe true
      result.get shouldBe None
    }
    "insert an item" in {
      val result = repo.put(puOne)
      result.isSuccess shouldBe true
      result.get shouldBe puOne
    }
    "have that item available" in {
      val result = repo.exists(tsOne)
      result.isSuccess shouldBe true
      result.get shouldBe true
    }
    "retrieve that item" in {
      val result = repo.get(tsOne)
      result.isSuccess shouldBe true
      result.get shouldBe Some(puOne)
    }
    "not have another item available" in {
      val result = repo.exists(tsTwo)
      result.isSuccess shouldBe true
      result.get shouldBe false
    }
  }
}

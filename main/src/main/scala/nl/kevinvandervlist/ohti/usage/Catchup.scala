package nl.kevinvandervlist.ohti.usage

import java.time.temporal.ChronoUnit

import nl.kevinvandervlist.ohti.api.model.IthoZonedDateTime

trait Catchup {
  protected def dailyRange(start: IthoZonedDateTime, inclusiveEnd: IthoZonedDateTime): Iterable[IthoZonedDateTime] = new Iterable[IthoZonedDateTime] {
    override def iterator: Iterator[IthoZonedDateTime] = new Iterator[IthoZonedDateTime] {
      private var cur: IthoZonedDateTime = start
      private val end = inclusiveEnd.add(1, ChronoUnit.DAYS).endOfDay
      override def hasNext: Boolean = cur.endOfDay != end

      override def next(): IthoZonedDateTime = {
        val ret = cur
        cur = ret.add(1, ChronoUnit.DAYS)
        ret
      }
    }
  }
}

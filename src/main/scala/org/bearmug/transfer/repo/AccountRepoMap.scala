package org.bearmug.transfer.repo

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import org.bearmug.transfer.model.{Account, EmptyAccount}

import collection.concurrent
import collection.JavaConverters._

class AccountRepoMap extends AccountRepo {

  val map: concurrent.Map[Long, Account] = new ConcurrentHashMap[Long, Account]().asScala
  val counter = new AtomicInteger()

  override def list(): List[Account] = map.values.toList

  override def create(account: Account): Option[Account] = {
    val toPersist = account.copy(id = counter.incrementAndGet())
    map.putIfAbsent(toPersist.id, toPersist) match {
      case None => Some(toPersist)
      case _ => None
    }
  }

  override def find(id: Long): Option[Account] = map.getOrElse(id, EmptyAccount) match {
    case EmptyAccount => None
    case acc => Some(acc)
  }

  override def update(account: Account): Option[Account] = find(account.id) match {
    case None => None
    case Some(_) => delete(account.id) match {
      case None => None
      case Some(toUpdate) =>
        val updatedAcc = toUpdate.copy(owner = account.owner, balance = account.balance)
        map.putIfAbsent(updatedAcc.id, updatedAcc) match {
          case None => Some(updatedAcc)
          case _ => None
        }
    }
  }

  override def delete(id: Long): Option[Account] = find(id) match {
    case None => None
    case Some(acc) => if (map.remove(acc.id, acc)) Some(acc) else None
  }
}

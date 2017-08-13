package org.bearmug.transfer.repo

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import org.bearmug.transfer.model.{Account, EmptyAccount}

import scala.collection.JavaConverters._
import scala.collection.concurrent

class AccountRepoMap extends AccountRepo {

  val map: concurrent.Map[Int, Account] = new ConcurrentHashMap[Int, Account]().asScala
  val counter = new AtomicInteger()

  override def list(): List[Account] = map.values.toList

  override def create(account: Account): Option[Account] = {
    val newId = counter.incrementAndGet()
    val toPersist = account.copy(id = Some(newId))
    map.putIfAbsent(newId, toPersist) match {
      case None => Some(toPersist)
      case _ => None
    }
  }

  override def find(id: Int): Option[Account] = map.getOrElse(id, EmptyAccount) match {
    case EmptyAccount => None
    case acc => Some(acc)
  }

  override def update(account: Account): Option[Account] = account.id match {
    case None => None
    case Some(validId) => find(validId) match {
      case None => None
      case Some(_) => delete(validId) match {
        case None => None
        case Some(toUpdate) =>
          val updatedAcc = toUpdate.copy(owner = account.owner, balance = account.balance)
          map.putIfAbsent(validId, updatedAcc) match {
            case None => Some(updatedAcc)
            case _ => None
          }
      }
    }


  }

  override def delete(id: Int): Option[Account] = find(id) match {
    case None => None
    case Some(acc) => if (map.remove(id, acc)) Some(acc) else None
  }
}

package org.bearmug.transfer.repo

import org.bearmug.transfer.model.{Account, AccountFactory}
import org.bearmug.transfer.tool.FutureO
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class AccountRepoSlickSuite extends FunSuite with BeforeAndAfterAll with ScalaFutures {

  var repo: AccountRepoSlick = _

  override def beforeAll() {
    repo = new AccountRepoSlick()
    Await.ready(repo.createDb, Duration.Inf)
    println("test database created")
  }

  test("new account listed after creation") {
    val account = AccountFactory.createNew("mine", 11)
    val createdAccount = repo.create(account)
    whenReady(createdAccount) {
      case None => fail()
      case Some(acc) =>
        whenReady(repo.find(acc)) {
          case None => fail()
          case Some(_) =>
        }
    }
  }

  test("account lookup works fine for added account") {
    for {
      id <- FutureO(repo.create(AccountFactory.createNew("mine", 10)))
      found <- FutureO(repo.find(id))
    } yield found match {
      case (acc: Account) =>
        assert(acc == AccountFactory.createNew("mine", 10).copy(id = Some(id)))
      case _ => fail()
    }
  }

  private val NonExistingId = 99999
  test("account lookup does not work for non-existent account") {
    whenReady(repo.find(NonExistingId)) {
      case Some(_) => fail()
      case None =>
    }
  }

  test("existing account removed ok") {
    val account = AccountFactory.createNew("mine", 10)
    repo.create(account).map {
      case None => fail()
      case Some(id) => whenReady(repo.delete(id)) {
        case None => fail()
        case Some(removedId) =>
          assert(removedId == id)
          whenReady(repo.find(removedId)) {
            case Some(_) => fail()
            case None =>
          }
      }
    }
  }

  test("non-existing account can not be removed") {
    whenReady(repo.delete(NonExistingId)) {
      case Some(_) =>
        fail()
      case None =>
    }
  }

  test("persisted account updated ok") {
    val account = AccountFactory.createNew("mine", 10)
    whenReady(repo.create(account)) {
      case None => fail()
      case Some(acc) => whenReady(repo.find(acc)) {
        case None => fail()
        case Some(found) => whenReady(repo.update(found.copy(owner = "other", balance = 100))) {
          case None => fail()
          case oid@Some(_) => assert(oid == found.id)
        }
      }
    }
  }

  test("non-existent account can not be updated") {
    whenReady(repo.update(AccountFactory.createNew("mine", 10))) {
      case None =>
      case Some(_) => fail()
    }
  }

  test("funds transfer works fine for positive scenario") {
    for {
      fromId <- repo.create(AccountFactory.createNew("f", 10))
      toId <- repo.create(AccountFactory.createNew("t", 10))
      fromAcc <- repo.find(fromId.get)
      toAcc <- repo.find(toId.get)
      (idFrom, idTo) <- repo.transfer(fromAcc.get, toAcc.get, 5)
      fromUpdated <- repo.find(idFrom.get)
      toUpdated <- repo.find(idTo.get)
    } yield (fromUpdated, toUpdated) match {
      case (Some(f), Some(t)) =>
        assert(f.balance == 5)
        assert(t.balance == 15)
      case _ => fail()
    }
  }
  test("funds transfer failed for insufficient funds") {
    for {
      fromId <- repo.create(AccountFactory.createNew("f", 10))
      toId <- repo.create(AccountFactory.createNew("t", 10))
      fromAcc <- repo.find(fromId.get)
      toAcc <- repo.find(toId.get)
      (idFrom, idTo) <- repo.transfer(fromAcc.get, toAcc.get, 50)
    } yield (idFrom, idTo) match {
      case (None, None) =>
      case _ => fail()
    }
  }

  test("funds transfer failed for concurrently updated source") {
    for {
      fromId <- repo.create(AccountFactory.createNew("f", 10))
      toId <- repo.create(AccountFactory.createNew("t", 10))
      fromAcc <- repo.find(fromId.get)
      toAcc <- repo.find(toId.get)
      updated <- repo.update(fromAcc.get.copy(balance = 1000))
      (idFrom, idTo) <- repo.transfer(fromAcc.get, toAcc.get, 1)
    } yield (idFrom, idTo) match {
      case (None, None) =>
      case _ => fail()
    }
  }

  test("funds transfer failed for concurrently updated recipient") {
    for {
      fromId <- repo.create(AccountFactory.createNew("f", 10))
      toId <- repo.create(AccountFactory.createNew("t", 10))
      fromAcc <- repo.find(fromId.get)
      toAcc <- repo.find(toId.get)
      updated <- repo.update(toAcc.get.copy(balance = 1000))
      (idFrom, idTo) <- repo.transfer(fromAcc.get, toAcc.get, 1)
    } yield (idFrom, idTo) match {
      case (None, None) =>
      case _ => fail()
    }
  }
}

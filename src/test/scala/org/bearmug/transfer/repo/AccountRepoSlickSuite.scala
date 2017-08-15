package org.bearmug.transfer.repo

import org.bearmug.transfer.model.AccountFactory
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class AccountRepoSlickSuite extends FunSuite with BeforeAndAfter with ScalaFutures {

  var repo: AccountRepoSlick = _

  before {
    repo = new AccountRepoSlick()
    Await.ready(repo.createDb, 10 seconds)
    println("test database created")
  }

  test("new account listed after creation") {
    val account = AccountFactory.createNew("mine", 10)
    val createdAccount = repo.create(account)
    whenReady(createdAccount) {
      case None => fail()
      case Some(acc) =>
        assert(acc > 0)
    }
  }

  test("account lookup works fine for added account") {
    val account = AccountFactory.createNew("mine", 10)
    whenReady(repo.create(account)) {
      case None => fail()
      case Some(acc) => whenReady(repo.find(acc)) {
        case None => fail()
        case Some(found) =>
          assert(found.id.head == acc)
          assert(found.balance == 10)
          assert(found.owner == "mine")
      }
    }
  }

  test("account lookup does not work for non-existent account") {
    whenReady(repo.find(99999)) {
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
    whenReady(repo.delete(99999)) {
      case Some(id) =>
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
}

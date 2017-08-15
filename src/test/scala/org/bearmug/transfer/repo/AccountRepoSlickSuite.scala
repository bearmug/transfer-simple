package org.bearmug.transfer.repo

import org.bearmug.transfer.model.AccountFactory
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.Await
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
    val createdAccount = repo.create(account)
    whenReady(createdAccount) {
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
//  test("account lookup does not work for non-existent account") {}
//  test("existing account removed ok") {}
//  test("non-existing account can not be removed") {}
//  test("persisted account updated ok") {}
//  test("non-existent account can not be updated") {}
}

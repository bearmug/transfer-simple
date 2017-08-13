package org.bearmug.transfer.repo

import org.scalatest.{BeforeAndAfter, FunSuite}

class AccountRepoSlickSuite extends FunSuite with BeforeAndAfter {

  var repo: AccountRepo = _

  before {
    repo = new AccountRepoSlick()
  }

  test("new account listed after creation") {}
  test("account lookup works fine for added account") {}
  test("account lookup does not work for non-existent account") {}
  test("existing account removed ok") {}
  test("non-existing account can not be removed") {}
  test("persisted account updated ok") {}
  test("non-existent account can not be updated") {}
}

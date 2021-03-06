package org.bearmug.transfer.model

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AccountSuite extends FunSuite {

  test("account initiated with zero identity and proper owner/balance") {
    val account = Account.createNew("owner",10)
    assert(account.id == 0)
    assert(account.balance == 10)
    assert(account.owner == "owner")
  }

  test("new account owner name can not be empty") {
    val e = intercept[IllegalArgumentException] {
      Account.createNew("", 10)
    }
    assert(e.getMessage == "requirement failed: owner name can not be empty")
  }

  test("new account owner name can not be empty after trim") {
    val e = intercept[IllegalArgumentException] {
      Account.createNew("  ", 10)
    }
    assert(e.getMessage == "requirement failed: owner name can not be empty")
  }

  test("new account initialAmount can not be < 0") {
    val e = intercept[IllegalArgumentException] {
      Account.createNew("owner", -10)
    }
    assert(e.getMessage == "requirement failed: initial balance -10 can not be less than zero")
  }

  test("overdraft is not allowed for account outgoing transfer") {
    val e = intercept[IllegalArgumentException] {
      Account.createNew("owner", 10).transferOut(100)
    }
    assert(e.getMessage == "requirement failed: insufficient funds, current balance 10, amount to transfer out 100")
  }

  test("account transfer out is OK") {
    val account = Account.createNew("owner", 10).transferOut(10)
    assert(account.balance == 0)
    assert(account.owner == "owner")
  }

  test("account transfer in is OK") {
    val account = Account.createNew("owner", 10).transferIn(10)
    assert(account.balance == 20)
    assert(account.owner == "owner")
  }
}

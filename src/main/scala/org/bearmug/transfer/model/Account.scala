package org.bearmug.transfer.model

/**
  * Basic data class. Lets assume limitations:
  *   - there is a single available currency and no need specify it
  *   - there is no fractional part
  * @param id account identifier
  * @param balance current account balance
  */
sealed case class Account private(id: Long, owner: String, balance: Long) {

  def transferIn(amount: Long): Account = {
    require(amount >= 0, s"amount to transfer should be >= to zero, its current value: $amount")
    Account(id, owner, balance + amount)
  }

  def transferOut(amount: Long): Account = {
    require(amount >= 0, s"amount to transfer should be >= to zero, its current value: $amount")
    require(balance >= amount, s"insufficient funds, current balance $balance, amount to transfer out $amount")
    Account(id, owner, balance - amount)
  }
}

/**
  * Companion object to create pre-validated new account only
  */
object Account {
  def createNew(owner: String, initialBalance: Long): Account = {
    require(owner.trim.nonEmpty, "owner name can not be empty")
    require(initialBalance >= 0, s"initial balance $initialBalance can not be less than zero")
    Account(0, owner, initialBalance)
  }
}
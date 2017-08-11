package org.bearmug.transfer.repo

import org.bearmug.transfer.model.Account

trait AccountRepo {
  def list(): List[Account]
  def save(account: Account): Account
  def find(id: Long): Account
  def update(account: Account)
  def delete(id: Long)
}

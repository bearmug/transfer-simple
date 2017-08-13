package org.bearmug.transfer.repo
import org.bearmug.transfer.model.Account

class AccountRepoJdbc extends AccountRepo {
  override def list(): List[Account] = ???

  override def create(account: Account): Option[Account] = ???

  override def find(id: Long): Option[Account] = ???

  override def update(account: Account): Option[Account] = ???

  override def delete(id: Long): Option[Account] = ???
}

package org.bearmug.transfer.repo
import org.bearmug.transfer.model.Account
import slick.lifted.TableQuery

class AccountRepoSlick extends AccountRepo {

  override def list(): List[Account] = ???

  override def create(account: Account): Option[Account] = ???

  override def find(id: Int): Option[Account] = ???

  override def update(account: Account): Option[Account] = ???

  override def delete(id: Int): Option[Account] = ???
}

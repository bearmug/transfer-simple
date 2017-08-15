package org.bearmug.transfer.repo

import org.bearmug.transfer.model.{Account, AccountFactory}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Try}

class AccountRepoSlick extends AccountRepo with AccountTable with H2Config {

  import driver.api._

  def createDb: Future[Unit] = db.run(DBIO.seq(accounts.schema.create))

  override def list(): Future[List[Account]] =
    db.run(accounts.result).map(_.toList)

  override def create(account: Account): Future[Option[Int]] =
    db.run(accountsIns += account).map(Some(_))

  override def find(id: Int): Future[Option[Account]] =
    db.run(accounts.filter(_.id === id).result.headOption)

  override def update(account: Account): Future[Option[Int]] =
    db.run(accounts.filter(_.id === account.id).update(account)).map(Some(_))

  override def delete(id: Int): Future[Option[Int]] =
    db.run(accounts.filter(_.id === id).delete).map(Some(_))

  def transfer(from: Account, to: Account, amount: Long): Future[(Option[Int], Option[Int])] = {
    val (updatedFromAcc, updatedToAcc) = AccountFactory.transfer(from, to, amount)
    db.run {
      DBIO.seq {
        accounts.filter(e => e.id === from.id && e.balance === from.balance).update(updatedFromAcc)
        accounts.filter(e => e.id === to.id && e.balance === to.balance).update(updatedToAcc)
      }.transactionally
    }.transform((triedUnit: Try[Unit]) => triedUnit match {
      case Success(_) => Success((from.id, to.id))
      case _ => Success(None, None)
    })
  }
}

trait AccountTable {
  this: DbConfig =>

  import driver.api._

  class Accounts(tag: Tag) extends Table[Account](tag, "ACCOUNTS") {
    // columns definition
    val id = column[Int]("ACCOUNT_ID", O.PrimaryKey, O.AutoInc)
    val owner = column[String]("ACCOUNT_OWNER", O.Length(256))
    val balance = column[Long]("ACCOUNT_BALANCE")

    // selection definition
    def * = (owner, balance, id.?) <> (Account.tupled, Account.unapply)
  }

  val accounts = TableQuery[Accounts]
  lazy val accountsIns = accounts returning accounts.map(_.id)

}

trait DbConfig {
  val driver: JdbcProfile
  val db: driver.api.Database
}

trait H2Config extends DbConfig {
  val driver = slick.jdbc.H2Profile
  val db: driver.backend.DatabaseDef = driver.api.Database.forURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
}
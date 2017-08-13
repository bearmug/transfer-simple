package org.bearmug.transfer.repo

import org.bearmug.transfer.model.{Account, AccountFactory}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AccountRepoSlick extends AccountRepo with AccountTable with H2Config {

  import driver.api._

  override def list(): Future[List[Account]] = db.run(accounts.result).map(_.toList)

  override def create(account: Account): Future[Option[Account]] = db.run(accountsId += account).flatMap(find)

  override def find(id: Int): Future[Option[Account]] = db.run(accounts.filter(_.id === id).result.headOption)

  override def update(account: Account): Future[Option[Account]] =
    db.run(accounts.filter(_.id === account.id).update(account)).flatMap(_ => find(account.id.head))

  override def delete(id: Int): Future[Option[Account]] = for {
    acct <- find(id)
    removed <- db.run(accounts.filter(_.id === id).delete)
  } yield (acct, removed) match {
    case (_, 0) => None
    case (res@Some(_), _) => res
    case _ => None
  }

  def transferTransactionally(fromAcc: Option[Account], toAcc: Option[Account], amount: Long): Option[(Account, Account)] =
    (fromAcc, toAcc) match {
      case (Some(acc1), Some(acc2)) =>
        val updatedAccounts = AccountFactory.transfer(acc1, acc2, amount)
        for {
          upd <- db.run {
            DBIO.seq {
              accounts.filter(_.id === updatedAccounts._1.id).update(updatedAccounts._1)
              accounts.filter(_.id === updatedAccounts._2.id).update(updatedAccounts._2)
            }.transactionally
          }
          fromPersisted <- find(acc1.id.head)
          toPersisted <- find(acc2.id.head)
        } yield (fromPersisted, toPersisted) match {
          case (Some(resFrom), Some(resTo)) => Some(resFrom, resTo)
          case _ => None
        }
        None
      case _ => None
    }

  override def transfer(idFrom: Int, idTo: Int, amount: Long): Future[Option[(Account, Account)]] = for {
    f <- find(idFrom)
    t <- find(idTo)
  } yield transferTransactionally(f, t, amount)
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

  val accounts = TableQuery[Accounts](new Accounts(_))
  val accountsId = accounts returning TableQuery[Accounts](new Accounts(_)).map(_.id)
}

trait DbConfig {
  val driver: JdbcProfile
  val db: driver.api.Database
}

trait H2Config extends DbConfig {
  val driver = slick.jdbc.H2Profile
  val db = driver.api.Database.forURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
}
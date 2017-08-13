package org.bearmug.transfer.repo

import org.bearmug.transfer.model.Account

import scala.concurrent.Future

/**
  * Account repository abstraction, representing persistence layer.
  */
trait AccountRepo {

  /**
    * Available accounts listing. Operation with no side-effects for storage.
    * @return persisted accounts list
    */
  def list(): Future[List[Account]]

  /**
    * Create account with unique identity. No uniqueness content validation assumed.
    * @param account account content to create
    * @return None if account with assigned identity already exists or Some with actually created accounts with
    *         real accounts id
    */
  def create(account: Account): Future[Option[Account]]

  /**
    * Lookup account by account id
    * @param id account identity to lookup by
    * @return None if there are no such account or account under maintenance (mutual update) or Some with actual
    *         account content
    */
  def find(id: Int): Future[Option[Account]]

  /**
    * Updating account by specific identity
    * @param account account to update, including identity to find it
    * @return None if account under maintenance (mutual update) or Some with updated account content
    */
  def update(account: Account): Future[Option[Account]]

  /**
    * Purges specific account with specific identity from the repo.
    * @param id account identity to cleanup to
    * @return None if account under update or account updated recently or Some with purged account content
    */
  def delete(id: Int): Future[Option[Account]]

  /**
    * Accounts money transfer stuff
    * @param idFrom id to transfer money from
    * @param idTo id to transfer money to
    * @param amount amount to transfer
    */
  def transfer(idFrom: Int, idTo: Int, amount: Long): Future[Option[(Account, Account)]]
}

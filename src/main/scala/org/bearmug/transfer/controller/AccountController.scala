package org.bearmug.transfer.controller

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Future
import io.vertx.scala.ext.web.Router

class AccountController extends ScalaVerticle {

  override def start(): Unit = {
    val router = Router.router(vertx)
    router
    .get("/account")
    .handler(_.response().end("ready"))

    vertx
      .createHttpServer()
      .requestHandler(router.accept _)
      .listenFuture(8080, "0.0.0.0")
      .map(_ => ())
  }
}

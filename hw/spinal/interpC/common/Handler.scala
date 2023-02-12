package interpC.common

import spinal.core._
import spinal.lib.IMasterSlave
import spinal.lib.fsm.StateMachineAccessor

case class Handler[Item <: Data](itemType: Item, bufferSize: Int) extends Bundle with IMasterSlave {
  override def asMaster(): Unit = {

  }

  def shared(num: Int): Vector[Handler[Item]] = ???

  def addAnd[T <: Item](item: T)(thenDoThat: => Unit)(implicit stateMachineAccessor: StateMachineAccessor): Unit = {
    ???
  }

  def add[T <: Item](item: T): StateT[Unit] = StateT((k, sma)=>addAnd(item){k(())}(sma))

  def reclock(clockDomain: ClockDomain): Handler[Item] = ???
}

object Handler {
  def parallel[Item <: Data](xs: Vector[Handler[Item]]): Handler[Item] = ???
}

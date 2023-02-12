package interpC.common

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

case class Procedure[Input <: Data, Output <: Data](inputType: Input, outputType: Output) extends Bundle with IMasterSlave {
  val input = Stream(inputType)
  val output = Flow(outputType)

  override def asMaster(): Unit = {
    slave(input)
    master(output)
  }

  override def asSlave(): Unit = {
    master(input)
    slave(output)
  }

  def callAnd[T <: Input](arg: T)(doThat: Output => Unit)(implicit stateMachineAccessor: StateMachineAccessor): Unit = {
    val argCache = Reg(inputType)
    argCache := arg
    val waitData = new State {
      whenIsActive {
        input.valid := false
        when(output.valid) {
          doThat(output.payload)
        }
      }
    }

    def whenReady = {
      assert(!input.valid)
      input.valid := true
      input.payload := argCache
      stateMachineAccessor.goto(waitData)
    }

    assert(!input.valid)
    val waitReady = new State {
      whenIsActive {
        when(input.ready) {
          whenReady
        }
      }
    }
    when(input.ready) {
      whenReady
    }.otherwise {
      stateMachineAccessor.goto(waitReady)
    }
  }

  def call[T <: Input](arg: T): StateT[Output] = StateT((k, sma)=>callAnd(arg)(k)(sma))

  def shared(num: Int): Vector[Procedure[Input, Output]] = ???

  def reclock(clockDomain: ClockDomain): Procedure[Input, Output] = ???

  protected def whenCalledDo(doThat: Input => Unit)(implicit stateMachineAccessor: StateMachineAccessor) = ???

  protected def whenCalled(doThat: Input => StateT[Unit])(implicit stateMachineAccessor: StateMachineAccessor) = whenCalledDo(x=>doThat(x).execute)

  protected def ret[T <: Output](arg: T) = ???
}


object Procedure {
  def parallel[Input <: Data, Output <: Data](xs: Vector[Procedure[Input, Output]]): Procedure[Input, Output] = ???
}
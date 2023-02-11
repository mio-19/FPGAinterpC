package interpC.common

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

case class Method[Input <: Data, Output <: Data](inputType: Input, outputType: Output) extends Bundle with IMasterSlave {
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

  def call[T <: Input](arg: T)(doThat: Output => Unit)(implicit stateMachineAccessor: StateMachineAccessor): Unit = {
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

  def shared(num: Int): Vector[Method[Input, Output]] = ???
}

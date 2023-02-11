package interpC.common

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

case class Method[Input <: Data, Output <: Data](inputType: Input, outputType: Output) extends Bundle with IMasterSlave {
  val input = Stream(inputType)
  val output = Stream(outputType)

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
        assert(output.ready)
        when(output.valid) {
          output.ready := false
          doThat(output.payload)
        }
      }
    }

    def whenReady = {
      assert(!input.valid)
      assert(!output.ready)
      input.valid := true
      output.ready := true
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
}

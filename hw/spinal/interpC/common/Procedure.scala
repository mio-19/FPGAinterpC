package interpC.common

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

case class Procedure[Input <: Data, Output <: Data](inputType: Input, outputType: Output)
    extends Bundle
    with IMasterSlave {
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

  def call[T <: Input](arg: T): StateT[Output] = StateT((k, sma) => callAnd(arg)(k)(sma))

  def shared(num: Int): Vector[Procedure[Input, Output]] = ???

  def reclock(clockDomain: ClockDomain): Procedure[Input, Output] = ???

  def pipe[T <: Data](other: Procedure[Output, T]): Procedure[Input, T] = ???

  def compose[T <: Data](other: Procedure[Input, Data]): Procedure[Input, Tup2[Output, T]] = ???

  def whenCalled(body: Input => StateT[Output]): StateMachine = {
    val inputCache = Reg(inputType)
    new StateMachine {
      val stateA: State = new State with EntryPoint {
        whenIsActive {
          input.ready := true
          output.valid := false
          output.payload := outputType.getZero
          when(input.valid) {
            inputCache := input.payload
            input.ready := false
            body(inputCache).andThen { result =>
              output.valid := true
              output.payload := result
              goto(stateA)
            }
          }
        }
      }
    }
  }
}

object Procedure {
  def parallel[Input <: Data, Output <: Data](xs: Vector[Procedure[Input, Output]]): Procedure[Input, Output] = ???

  def call[Input <: Data, Output <: Data](procedure: Procedure[Input, Output], arg: Input): StateT[Output] =
    procedure.call(arg)

  final case class Call[Input <: Data, Output <: Data](procedure: Procedure[Input, Output], arg: Input)

  def callParallel(calls: Vector[Call[_, _]]): Vector[_ <: Data] = ???
}

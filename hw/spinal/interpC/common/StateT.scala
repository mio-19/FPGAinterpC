package interpC.common

import spinal.lib.fsm._

// Monad
// describe actions in a state machine
case class StateT[T](body: (((T => Unit), StateMachineAccessor) => Unit)) {
  def execute(implicit stateMachineAccessor: StateMachineAccessor): Unit = body({ ignored => () }, stateMachineAccessor)

  def loop: StateMachine = new StateMachine {
    val stateA: State = new State with EntryPoint {
      whenIsActive {
        body({ ignored => goto(stateA) }, implicitly[StateMachineAccessor])
      }
    }
  }

  def map = ???

  def flatMap = ???
}

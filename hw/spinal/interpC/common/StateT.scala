package interpC.common

import spinal.lib.fsm.StateMachineAccessor

// Monad
// describe actions in a state machine
case class StateT[T](body: (((T => Unit), StateMachineAccessor) => Unit)) {
  def execute(implicit stateMachineAccessor: StateMachineAccessor): Unit = body({ ignored => () }, stateMachineAccessor)

}

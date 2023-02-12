package interpC.common

import spinal.lib.fsm._

// Monad
// describe actions in a state machine
case class StateT[T](body: (((T => Unit), StateMachineAccessor) => Unit)) {
  def execute(implicit stateMachineAccessor: StateMachineAccessor): Unit = body({ ignored => () }, stateMachineAccessor)

  def andThen(next: (T => Unit))(implicit stateMachineAccessor: StateMachineAccessor): Unit = body(next, stateMachineAccessor)

  def loop: StateMachine = new StateMachine {
    val stateA: State = new State with EntryPoint {
      whenIsActive {
        body({ ignored => goto(stateA) }, implicitly[StateMachineAccessor])
      }
    }
  }

  def map[U](f: T => U): StateT[U] = StateT((k, sma) => body(x=>k(f(x)), sma))

  def flatMap[U](f: T => StateT[U]): StateT[U] = StateT((k, sma) => body((x=>f(x).body(k, sma)), sma))
}

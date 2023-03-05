package interpC.common

import spinal.core._

case class Tup2[T <: Data, U <: Data](t: T, u: U) extends Bundle {
  val _1 = t
  val _2 = u
}

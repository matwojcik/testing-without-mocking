package orders

import orders.domain.Order.Id
import orders.domain.Order.Status

object domain {

  case class Order(id: Id, status: Status)

  object Order {
    case class Id(value: String) extends AnyVal

    sealed trait Status extends Product with Serializable

    object Status {
      case object Initial extends Status
      case object InProgress extends Status
      case object Complete extends Status
    }
  }
}

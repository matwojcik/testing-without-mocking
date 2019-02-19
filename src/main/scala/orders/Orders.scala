package orders

import orders.domain.Order

trait Orders[F[_]] {
  def completeIfPossible(id: Order.Id): F[Option[Order]]
}

object Orders {}

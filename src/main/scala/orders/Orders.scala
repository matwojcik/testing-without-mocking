package orders

import cats.Monad
import cats.syntax.all._
import orders.domain.Order

trait Orders[F[_]] {
  def completeIfPossible(id: Order.Id): F[Option[Order]]
}

object Orders {

  def instance[F[_]: OrderRepository: Monad]: Orders[F] = new Orders[F] {
    override def completeIfPossible(id: Order.Id): F[Option[Order]] =
      for {
        order <- OrderRepository[F].find(id)
        completeOrder = order.filter(_.status == Order.Status.InProgress).map(_.copy(status = Order.Status.Complete))
        _ <- completeOrder.map(OrderRepository[F].save).getOrElse(Monad[F].unit)
      } yield completeOrder.orElse(order)
  }
}

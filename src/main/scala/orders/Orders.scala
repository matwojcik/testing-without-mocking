package orders

import cats.Monad
import cats.data.OptionT
import cats.mtl.FunctorRaise
import cats.syntax.all._
import orders.domain.Order

trait Orders[F[_]] {
  def completeOrder(id: Order.Id): F[Order]
}

object Orders {

  sealed trait OrderCompletionFailure extends Product with Serializable
  case class NotExistingOrder(id: Order.Id) extends OrderCompletionFailure
  case class OrderNotInProgress(id: Order.Id) extends OrderCompletionFailure

  def instance[F[_]: OrderRepository: Monad: FunctorRaise[?[_], OrderCompletionFailure]]: Orders[F] = new Orders[F] {
    override def completeOrder(id: Order.Id): F[Order] =
      for {
        order <- FunctorRaise[F, OrderCompletionFailure].ensure(findOrder(id))(OrderNotInProgress(id))(_.status == Order.Status.InProgress)
        completeOrder = order.copy(status = Order.Status.Complete)
        _ <- OrderRepository[F].save(completeOrder)
      } yield completeOrder


  private def findOrder(id: Order.Id) =
    OptionT(OrderRepository[F].find(id)).getOrElseF(FunctorRaise[F, OrderCompletionFailure].raise(NotExistingOrder(id)))
  }
}

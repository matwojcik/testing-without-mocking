package orders
import orders.domain.Order

trait OrderRepository[F[_]] {
  def find(id: Order.Id): F[Option[Order]]
  def save(order: Order): F[Unit]
}

object OrderRepository {
  def apply[F[_]](implicit ev: OrderRepository[F]): OrderRepository[F] = ev
}
package orders
import orders.domain.Order
import orders.domain.Pick

trait PickRepository[F[_]] {
  def findPicksOfOrder(id: Order.Id): F[List[Pick]]
}

object PickRepository {
  def apply[F[_]](implicit ev: PickRepository[F]): PickRepository[F] = ev
}

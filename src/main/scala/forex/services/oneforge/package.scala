package forex.services

import cats.data.Reader

package object oneforge {
  type OneForgeLiveConfigReader[A] =
    Reader[OneForgeLiveConfig, A]

  def configure[A](
      c: OneForgeLiveConfig
  )(
      implicit
      r: OneForgeLiveConfigReader[A]
  ): A =
    r.run(c)
}

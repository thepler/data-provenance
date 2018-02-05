package com.cibo.provenance.implicits

/**
  * Created by ssmith on 11/12/17.
  *
  * TraversableCall[A] is extended by the implicit class TraversableCallExt,
  * which extends FunctionCallWithProvenance[O] when O is an Traversable[_] (Seq, List, Vector).
  *
  */

import com.cibo.provenance.monadics.{ApplyWithProvenance, IndicesRangeWithProvenance, IndicesTraversableWithProvenance, MapWithProvenance}
import com.cibo.provenance.{Function1WithProvenance, FunctionCallWithProvenance, ResultTracker, ValueWithProvenance}

import scala.language.higherKinds
import scala.reflect.ClassTag


class TraversableCall[S[_], A](call: FunctionCallWithProvenance[S[A]])(
  implicit hok: Traversable[S],
  ctsa: ClassTag[S[A]],
  cta: ClassTag[A],
  ctsi: ClassTag[S[Int]]
) {
  def apply(n: ValueWithProvenance[Int]): ApplyWithProvenance[S, A]#Call =
    ApplyWithProvenance[S, A].apply(call, n)

  def indices: IndicesRangeWithProvenance[S, A]#Call =
    IndicesRangeWithProvenance[S, A].apply(call)

  def map[B](f: Function1WithProvenance[B, A])(implicit ctsb: ClassTag[S[B]], ctb: ClassTag[B]): MapWithProvenance[B, A, S]#Call =
    MapWithProvenance[B, A, S].apply(call, f)

  def scatter(implicit rt: ResultTracker): S[FunctionCallWithProvenance[A]] = {
    val indicesResult: IndicesTraversableWithProvenance[S, A]#Result = IndicesTraversableWithProvenance[S, A].apply(call).resolve
    val indexesTraversable: S[Int] = indicesResult.output
    def getApply(n: Int): FunctionCallWithProvenance[A] = ApplyWithProvenance[S, A].apply(call, n)
    hok.map(getApply)(indexesTraversable)
  }
}
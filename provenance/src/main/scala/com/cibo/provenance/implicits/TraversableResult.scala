package com.cibo.provenance.implicits

/**
  * Created by ssmith on 11/12/17.
  *
  * TraversableResult[A] is extended by the implicit class TraversableResultExt,
  * which extends FunctionCallResultWithProvenance[O] when O is an Traversable[_] (Seq, List, Vector).
  *
  */

import com.cibo.provenance.monadics._
import com.cibo.provenance.{ResultTracker, _}

import scala.language.higherKinds
import scala.reflect.ClassTag

class TraversableResult[S[_], A](result: FunctionCallResultWithProvenance[S[A]])(
    implicit hok: Traversable[S],
    ctsa: ClassTag[S[A]],
    cta: ClassTag[A],
    ctsi: ClassTag[S[Int]]
  ) {
    import FunctionCallWithProvenance.TraversableCallExt

    def apply(n: ValueWithProvenance[Int]): ApplyWithProvenance[S, A]#Call =
      ApplyWithProvenance[S, A].apply(result, n)

    def indices: IndicesRangeWithProvenance[S, A]#Call =
      IndicesRangeWithProvenance[S, A].apply(result)

    def map[B](f: Function1WithProvenance[B, A])(implicit ctsb: ClassTag[S[B]], ctb: ClassTag[B]): MapWithProvenance[B, A, S]#Call =
      new MapWithProvenance[B, A, S].apply(result, f)

    def scatter(implicit rt: ResultTracker): S[FunctionCallResultWithProvenance[A]] = {
      val call1: FunctionCallWithProvenance[S[A]] = result.provenance
      val call2: TraversableCall[S, A] = TraversableCallExt[S, A](call1)(hok,ctsa,cta,ctsi) // not automatic here
      val oneCallPerMember: S[FunctionCallWithProvenance[A]] = call2.scatter
      def getResult(call: FunctionCallWithProvenance[A]): FunctionCallResultWithProvenance[A] = call.resolve
      hok.map(getResult)(oneCallPerMember)
    }
  }
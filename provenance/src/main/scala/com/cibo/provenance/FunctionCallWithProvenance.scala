package com.cibo.provenance

/**
  * Created by ssmith on 9/11/17.
  *
  * Note: The FunctionCallWithProvenance code lives in VirtualValue.scala (sealed trait).
  *
  * This file contains the Function{N}CallSignatureWithProvenance subclass implementations.
  *
  */

import scala.language.implicitConversions
import scala.reflect.ClassTag

abstract class Function0CallWithProvenance[O : ClassTag](v: ValueWithProvenance[Version])(f: (Version) => O) extends FunctionCallWithProvenance[O](v) with Serializable {
  val impl = f
  def getInputs: Seq[ValueWithProvenance[_]] = Seq.empty
  def resolveInputs(implicit rt: ResultTracker): Function0CallWithProvenance[O] =
    nocopy(duplicate(v.resolve), this)
  def unresolveInputs(implicit rt: ResultTracker): Function0CallWithProvenance[O] =
    nocopy(duplicate(v.unresolve), this)
  def inflateInputs(implicit rt: ResultTracker): Function0CallWithProvenance[O] =
    nocopy(duplicate(v.inflate), this)
  def deflateInputs(implicit rt: ResultTracker): Function0CallWithProvenance[O] =
    nocopy(duplicate(v.inflate), this)
  def run(implicit rt: ResultTracker): Function0CallResultWithProvenance[O] = {
    val o: O = f(getVersionValue)
    new Function0CallResultWithProvenance(this, output = VirtualValue(o))(rt.getCurrentBuildInfo)
  }
  def newResult(value: VirtualValue[O])(implicit bi: BuildInfo): FunctionCallResultWithProvenance[O]
  def duplicate(vv: ValueWithProvenance[Version]): Function0CallWithProvenance[O]
}

abstract class Function1CallWithProvenance[O : ClassTag, I1](i1: ValueWithProvenance[I1], v: ValueWithProvenance[Version])(f: (I1, Version) => O) extends FunctionCallWithProvenance[O](v) with Serializable {
  val impl = f
  def getInputs: Seq[ValueWithProvenance[_]] = Seq(i1)
  def inputTuple: Tuple1[ValueWithProvenance[I1]] = Tuple1(i1)
  def resolveInputs(implicit rt: ResultTracker): Function1CallWithProvenance[O, I1] =
    nocopy(duplicate(i1.resolve, v.resolve), this)
  def unresolveInputs(implicit rt: ResultTracker): Function1CallWithProvenance[O, I1] =
    nocopy(duplicate(i1.unresolve, v.unresolve), this)
  def inflateInputs(implicit rt: ResultTracker): Function1CallWithProvenance[O, I1] =
    nocopy(duplicate(i1.inflate, v.inflate), this)
  def deflateInputs(implicit rt: ResultTracker): Function1CallWithProvenance[O, I1] =
    nocopy(duplicate(i1.deflate, v.inflate), this)
  def run(implicit rt: ResultTracker): Function1CallResultWithProvenance[O, I1] = {
    val output: O = f(i1.resolve.output, getVersionValue)
    new Function1CallResultWithProvenance(this, output = VirtualValue(output))(rt.getCurrentBuildInfo)
  }
  def newResult(value: VirtualValue[O])(implicit bi: BuildInfo): FunctionCallResultWithProvenance[O]
  def duplicate(v1: ValueWithProvenance[I1], vv: ValueWithProvenance[Version]): Function1CallWithProvenance[O, I1]
}


abstract class Function2CallWithProvenance[O : ClassTag, I1, I2](i1: ValueWithProvenance[I1], i2: ValueWithProvenance[I2], v: ValueWithProvenance[Version])(f: (I1, I2, Version) => O) extends FunctionCallWithProvenance[O](v) with Serializable {
  val impl = f
  def getInputs: Seq[ValueWithProvenance[_]] = Seq(i1, i2)
  def inputTuple = (i1, i2)
  def resolveInputs(implicit rt: ResultTracker): Function2CallWithProvenance[O, I1, I2] =
    nocopy(duplicate(i1.resolve, i2.resolve, v.resolve), this)
  def unresolveInputs(implicit rt: ResultTracker): Function2CallWithProvenance[O, I1, I2] =
    nocopy(duplicate(i1.unresolve, i2.unresolve, v.unresolve), this)
  def inflateInputs(implicit rt: ResultTracker): Function2CallWithProvenance[O, I1, I2] =
    nocopy(duplicate(i1.inflate, i2.inflate, v.inflate), this)
  def deflateInputs(implicit rt: ResultTracker): Function2CallWithProvenance[O, I1, I2] =
    nocopy(duplicate(i1.deflate, i2.deflate, v.inflate), this)
  private implicit val i1ct: ClassTag[I1] = i1.getOutputClassTag
  private implicit val i2ct: ClassTag[I2] = i2.getOutputClassTag
  def run(implicit rt: ResultTracker): Function2CallResultWithProvenance[O, I1, I2] = {
    val output: O = f(i1.resolve.output, i2.resolve.output, getVersionValue)
    new Function2CallResultWithProvenance(this, output = VirtualValue(output))(rt.getCurrentBuildInfo)
  }
  def newResult(value: VirtualValue[O])(implicit bi: BuildInfo): FunctionCallResultWithProvenance[O]
  def duplicate(v1: ValueWithProvenance[I1], v2: ValueWithProvenance[I2], vv: ValueWithProvenance[Version]): Function2CallWithProvenance[O, I1, I2]
}


abstract class Function3CallWithProvenance[O : ClassTag, I1, I2, I3](i1: ValueWithProvenance[I1], i2: ValueWithProvenance[I2], i3: ValueWithProvenance[I3], v: ValueWithProvenance[Version])(f: (I1, I2, I3, Version) => O) extends FunctionCallWithProvenance[O](v) {
  val impl = f
  def getInputs: Seq[ValueWithProvenance[_]] = Seq(i1, i2, i3)
  def inputTuple = (i1, i2, i3)
  def resolveInputs(implicit rt: ResultTracker): Function3CallWithProvenance[O, I1, I2, I3] =
    nocopy(duplicate(i1.resolve, i2.resolve, i3.resolve, v.resolve), this)
  def unresolveInputs(implicit rt: ResultTracker): Function3CallWithProvenance[O, I1, I2, I3] =
    nocopy(duplicate(i1.unresolve, i2.unresolve, i3.unresolve, v.unresolve), this)
  def inflateInputs(implicit rt: ResultTracker): Function3CallWithProvenance[O, I1, I2, I3] =
    nocopy(duplicate(i1.inflate, i2.inflate, i3.inflate, v.inflate), this)
  def deflateInputs(implicit rt: ResultTracker): Function3CallWithProvenance[O, I1, I2, I3] =
    nocopy(duplicate(i1.deflate, i2.deflate, i3.deflate, v.inflate), this)
  private implicit val i1ct: ClassTag[I1] = i1.getOutputClassTag
  private implicit val i2ct: ClassTag[I2] = i2.getOutputClassTag
  private implicit val i3ct: ClassTag[I3] = i3.getOutputClassTag
  def run(implicit rt: ResultTracker): Function3CallResultWithProvenance[O, I1, I2, I3] = {
    val output: O = f(i1.resolve.output, i2.resolve.output, i3.resolve.output, getVersionValue)
    new Function3CallResultWithProvenance(this, output = VirtualValue(output))(rt.getCurrentBuildInfo)
  }
  def newResult(value: VirtualValue[O])(implicit bi: BuildInfo): FunctionCallResultWithProvenance[O]
  def duplicate(v1: ValueWithProvenance[I1], v2: ValueWithProvenance[I2], v3: ValueWithProvenance[I3], vv: ValueWithProvenance[Version]): Function3CallWithProvenance[O, I1, I2, I3]
}


abstract class Function4CallWithProvenance[O : ClassTag, I1, I2, I3, I4](i1: ValueWithProvenance[I1], i2: ValueWithProvenance[I2], i3: ValueWithProvenance[I3], i4: ValueWithProvenance[I4], v: ValueWithProvenance[Version])(f: (I1, I2, I3, I4, Version) => O) extends FunctionCallWithProvenance[O](v) {
  val impl = f
  def getInputs: Seq[ValueWithProvenance[_]] = Seq(i1, i2, i3, i4, v)
  def inputTuple = (i1, i2, i3, i4)
  def resolveInputs(implicit rt: ResultTracker): Function4CallWithProvenance[O, I1, I2, I3, I4] =
    nocopy(duplicate(i1.resolve, i2.resolve, i3.resolve, i4.resolve, v.resolve), this)
  def unresolveInputs(implicit rt: ResultTracker): Function4CallWithProvenance[O, I1, I2, I3, I4] =
    nocopy(duplicate(i1.unresolve, i2.unresolve, i3.unresolve, i4.unresolve, v.unresolve), this)
  def inflateInputs(implicit rt: ResultTracker): Function4CallWithProvenance[O, I1, I2, I3, I4] =
    nocopy(duplicate(i1.inflate, i2.inflate, i3.inflate, i4.inflate, v.inflate), this)
  def deflateInputs(implicit rt: ResultTracker): Function4CallWithProvenance[O, I1, I2, I3, I4] =
    nocopy(duplicate(i1.deflate, i2.deflate, i3.deflate, i4.deflate, v.inflate), this)
  private implicit val i1ct: ClassTag[I1] = i1.getOutputClassTag
  private implicit val i2ct: ClassTag[I2] = i2.getOutputClassTag
  private implicit val i3ct: ClassTag[I3] = i3.getOutputClassTag
  private implicit val i4ct: ClassTag[I4] = i4.getOutputClassTag
  def run(implicit rt: ResultTracker): Function4CallResultWithProvenance[O, I1, I2, I3, I4] = {
    val output: O = f(i1.resolve.output, i2.resolve.output, i3.resolve.output, i4.resolve.output, getVersionValue)
    new Function4CallResultWithProvenance(this, output = VirtualValue(output))(rt.getCurrentBuildInfo)
  }
  def newResult(value: VirtualValue[O])(implicit bi: BuildInfo): FunctionCallResultWithProvenance[O]
  def duplicate(v1: ValueWithProvenance[I1], v2: ValueWithProvenance[I2], v3: ValueWithProvenance[I3], v4: ValueWithProvenance[I4], vv: ValueWithProvenance[Version]): Function4CallWithProvenance[O, I1, I2, I3, I4]
}

abstract class Function5CallWithProvenance[O : ClassTag, I1, I2, I3, I4, I5](i1: ValueWithProvenance[I1], i2: ValueWithProvenance[I2], i3: ValueWithProvenance[I3], i4: ValueWithProvenance[I4], i5: ValueWithProvenance[I5], v: ValueWithProvenance[Version])(f: (I1, I2, I3, I4, I5, Version) => O) extends FunctionCallWithProvenance[O](v) {
  val impl = f
  def getInputs: Seq[ValueWithProvenance[_]] = Seq(i1, i2, i3, i4, i5, v)
  def inputTuple = (i1, i2, i3, i4, i5)
  def resolveInputs(implicit rt: ResultTracker): Function5CallWithProvenance[O, I1, I2, I3, I4, I5] =
    nocopy(duplicate(i1.resolve, i2.resolve, i3.resolve, i4.resolve, i5.resolve, v.resolve), this)
  def unresolveInputs(implicit rt: ResultTracker): Function5CallWithProvenance[O, I1, I2, I3, I4, I5] =
    nocopy(duplicate(i1.unresolve, i2.unresolve, i3.unresolve, i4.unresolve, i5.unresolve, v.unresolve), this)
  def inflateInputs(implicit rt: ResultTracker): Function5CallWithProvenance[O, I1, I2, I3, I4, I5] =
    nocopy(duplicate(i1.inflate, i2.inflate, i3.inflate, i4.inflate, i5.inflate, v.inflate), this)
  def deflateInputs(implicit rt: ResultTracker): Function5CallWithProvenance[O, I1, I2, I3, I4, I5] =
    nocopy(duplicate(i1.deflate, i2.deflate, i3.deflate, i4.deflate, i5.deflate, v.inflate), this)
  private implicit val i1ct: ClassTag[I1] = i1.getOutputClassTag
  private implicit val i2ct: ClassTag[I2] = i2.getOutputClassTag
  private implicit val i3ct: ClassTag[I3] = i3.getOutputClassTag
  private implicit val i4ct: ClassTag[I4] = i4.getOutputClassTag
  def run(implicit rt: ResultTracker): Function5CallResultWithProvenance[O, I1, I2, I3, I4, I5] = {
    val output: O = f(i1.resolve.output, i2.resolve.output, i3.resolve.output, i4.resolve.output, i5.resolve.output, getVersionValue)
    new Function5CallResultWithProvenance(this, output = VirtualValue(output))(rt.getCurrentBuildInfo)
  }
  def newResult(value: VirtualValue[O])(implicit bi: BuildInfo): FunctionCallResultWithProvenance[O]
  def duplicate(v1: ValueWithProvenance[I1], v2: ValueWithProvenance[I2], v3: ValueWithProvenance[I3], v4: ValueWithProvenance[I4], v5: ValueWithProvenance[I5], vv: ValueWithProvenance[Version]): Function5CallWithProvenance[O, I1, I2, I3, I4, I5]
}

abstract class Function6CallWithProvenance[O : ClassTag, I1, I2, I3, I4, I5, I6](i1: ValueWithProvenance[I1], i2: ValueWithProvenance[I2], i3: ValueWithProvenance[I3], i4: ValueWithProvenance[I4], i5: ValueWithProvenance[I5], i6: ValueWithProvenance[I6], v: ValueWithProvenance[Version])(f: (I1, I2, I3, I4, I5, I6, Version) => O) extends FunctionCallWithProvenance[O](v) {
  val impl = f
  def getInputs: Seq[ValueWithProvenance[_]] = Seq(i1, i2, i3, i4, i5, v)
  def inputTuple = (i1, i2, i3, i4, i5)
  def resolveInputs(implicit rt: ResultTracker): Function6CallWithProvenance[O, I1, I2, I3, I4, I5, I6] =
    nocopy(duplicate(i1.resolve, i2.resolve, i3.resolve, i4.resolve, i5.resolve, i6.resolve, v.resolve), this)
  def unresolveInputs(implicit rt: ResultTracker): Function6CallWithProvenance[O, I1, I2, I3, I4, I5, I6] =
    nocopy(duplicate(i1.unresolve, i2.unresolve, i3.unresolve, i4.unresolve, i5.unresolve, i6.unresolve, v.unresolve), this)
  def inflateInputs(implicit rt: ResultTracker): Function6CallWithProvenance[O, I1, I2, I3, I4, I5, I6] =
    nocopy(duplicate(i1.inflate, i2.inflate, i3.inflate, i4.inflate, i5.inflate, i6.inflate, v.inflate), this)
  def deflateInputs(implicit rt: ResultTracker): Function6CallWithProvenance[O, I1, I2, I3, I4, I5, I6] =
    nocopy(duplicate(i1.deflate, i2.deflate, i3.deflate, i4.deflate, i5.deflate, i6.deflate, v.inflate), this)
  private implicit val i1ct: ClassTag[I1] = i1.getOutputClassTag
  private implicit val i2ct: ClassTag[I2] = i2.getOutputClassTag
  private implicit val i3ct: ClassTag[I3] = i3.getOutputClassTag
  private implicit val i4ct: ClassTag[I4] = i4.getOutputClassTag
  def run(implicit rt: ResultTracker): Function6CallResultWithProvenance[O, I1, I2, I3, I4, I5, I6] = {
    val output: O = f(i1.resolve.output, i2.resolve.output, i3.resolve.output, i4.resolve.output, i5.resolve.output, i6.resolve.output, getVersionValue)
    new Function6CallResultWithProvenance(this, output = VirtualValue(output))(rt.getCurrentBuildInfo)
  }
  def newResult(value: VirtualValue[O])(implicit bi: BuildInfo): FunctionCallResultWithProvenance[O]
  def duplicate(v1: ValueWithProvenance[I1], v2: ValueWithProvenance[I2], v3: ValueWithProvenance[I3], v4: ValueWithProvenance[I4], v5: ValueWithProvenance[I5], v6: ValueWithProvenance[I6], vv: ValueWithProvenance[Version]): Function6CallWithProvenance[O, I1, I2, I3, I4, I5, I6]
}

abstract class Function7CallWithProvenance[O : ClassTag, I1, I2, I3, I4, I5, I6, I7](i1: ValueWithProvenance[I1], i2: ValueWithProvenance[I2], i3: ValueWithProvenance[I3], i4: ValueWithProvenance[I4], i5: ValueWithProvenance[I5], i6: ValueWithProvenance[I6], i7: ValueWithProvenance[I7], v: ValueWithProvenance[Version])(f: (I1, I2, I3, I4, I5, I6, I7, Version) => O) extends FunctionCallWithProvenance[O](v) {
  val impl = f
  def getInputs: Seq[ValueWithProvenance[_]] = Seq(i1, i2, i3, i4, i5, i6, i7, v)
  def inputTuple = (i1, i2, i3, i4, i5, i6, i7)
  def resolveInputs(implicit rt: ResultTracker): Function7CallWithProvenance[O, I1, I2, I3, I4, I5, I6, I7] =
    nocopy(duplicate(i1.resolve, i2.resolve, i3.resolve, i4.resolve, i5.resolve, i6.resolve, i7.resolve, v.resolve), this)
  def unresolveInputs(implicit rt: ResultTracker): Function7CallWithProvenance[O, I1, I2, I3, I4, I5, I6, I7] =
    nocopy(duplicate(i1.unresolve, i2.unresolve, i3.unresolve, i4.unresolve, i5.unresolve, i6.unresolve, i7.unresolve, v.unresolve), this)
  def inflateInputs(implicit rt: ResultTracker): Function7CallWithProvenance[O, I1, I2, I3, I4, I5, I6, I7] =
    nocopy(duplicate(i1.inflate, i2.inflate, i3.inflate, i4.inflate, i5.inflate, i6.inflate, i7.inflate, v.inflate), this)
  def deflateInputs(implicit rt: ResultTracker): Function7CallWithProvenance[O, I1, I2, I3, I4, I5, I6, I7] =
    nocopy(duplicate(i1.deflate, i2.deflate, i3.deflate, i4.deflate, i5.deflate, i6.deflate, i7.deflate, v.inflate), this)
  private implicit val i1ct: ClassTag[I1] = i1.getOutputClassTag
  private implicit val i2ct: ClassTag[I2] = i2.getOutputClassTag
  private implicit val i3ct: ClassTag[I3] = i3.getOutputClassTag
  private implicit val i4ct: ClassTag[I4] = i4.getOutputClassTag
  def run(implicit rt: ResultTracker): Function7CallResultWithProvenance[O, I1, I2, I3, I4, I5, I6, I7] = {
    val output: O = f(i1.resolve.output, i2.resolve.output, i3.resolve.output, i4.resolve.output, i5.resolve.output, i6.resolve.output, i7.resolve.output, getVersionValue)
    new Function7CallResultWithProvenance(this, output = VirtualValue(output))(rt.getCurrentBuildInfo)
  }
  def newResult(value: VirtualValue[O])(implicit bi: BuildInfo): FunctionCallResultWithProvenance[O]
  def duplicate(v1: ValueWithProvenance[I1], v2: ValueWithProvenance[I2], v3: ValueWithProvenance[I3], v4: ValueWithProvenance[I4], v5: ValueWithProvenance[I5], v6: ValueWithProvenance[I6], v7: ValueWithProvenance[I7], vv: ValueWithProvenance[Version]): Function7CallWithProvenance[O, I1, I2, I3, I4, I5, I6, I7]
}

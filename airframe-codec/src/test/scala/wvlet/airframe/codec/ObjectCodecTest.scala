/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wvlet.airframe.codec

import org.msgpack.core.MessagePack

import ObjectCodecTest._

/**
  *
  */
class ObjectCodecTest extends CodecSpec {
  val codec = MessageCodec.of[A1].asInstanceOf[ObjectCodec[A1]]

  "support case classes" in {
    val v: A1 = A1(1, 2, 3, 4, 5, 6, true, "str")
    roundtrip(codec, v, DataType.ANY)

    forAll { (i: Int, l: Long, f: Float, d: Double, c: Char, st: Short) =>
      // scalacheck supports only upto 6 elements
      forAll { (b: Boolean, s: String) =>
        val v = A1(i, l, f, d, c, st, b, s)
        roundtrip[A1](codec, v, DataType.ANY)
      }
    }
  }

  "support reading map value" in {
    val v: A1  = A1(1, 2, 3, 4, 5, 6, true, "str")
    val packer = MessagePack.newDefaultBufferPacker()
    codec.packAsMap(packer, v)
    val b = packer.toByteArray

    val h = new MessageHolder
    codec.unpack(MessagePack.newDefaultUnpacker(b), h)

    h.isNull shouldBe false
    h.hasError shouldBe false
    h.getDataType shouldBe DataType.ANY
    h.getLastValue shouldBe v
  }

  "populate the default value when missing" in {
    val packer = MessagePack.newDefaultBufferPacker()
    packer.packMapHeader(1)
    packer.packString("i")
    packer.packInt(10)
    val b = packer.toByteArray

    val h = new MessageHolder
    MessageCodec.of[A2].unpack(MessagePack.newDefaultUnpacker(b), h)

    h.isNull shouldBe false
    h.hasError shouldBe false
    h.getDataType shouldBe DataType.ANY
    // map input, constructor default, Zero
    h.getLastValue shouldBe A2(10, 2L, 0)
  }

}

object ObjectCodecTest {
  case class A1(
      i: Int,
      l: Long,
      f: Float,
      d: Double,
      c: Char,
      st: Short,
      b: Boolean,
      s: String
  )

  case class A2(i: Int, l: Long = 2L, i2: Int)

}

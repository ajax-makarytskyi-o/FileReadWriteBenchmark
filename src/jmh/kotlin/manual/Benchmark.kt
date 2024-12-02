package manual

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.random.Random

val readFile = File("src/jmh/resources/text_100MB.txt")
val writeFile = File("src/jmh/resources/output_text.txt")

const val HUNDRED_MB = 100 * 1024 * 1024 // =100MB

fun readFile(file: File, bufferSize: Int) {
    val buffer = ByteArray(bufferSize)
    var size: Int
    FileInputStream(file).use { fis ->
        do {
            size = fis.read(buffer)
        } while (size > 0)
        fis.close()
    }
}

val buffers = mutableListOf<ByteArray>()

fun writeFile(file: File, buffer: ByteArray) {
    val writeTimes = HUNDRED_MB / buffer.size
    FileOutputStream(file).use { fos ->
        for (i in 1..writeTimes) {
            fos.write(buffer)
        }
        fos.flush()
        fos.close()
    }
}

private fun randomByteArray(size: Int): ByteArray {
    val buffer = ByteArray(size)
    return Random.nextBytes(buffer)
}


fun testBufferSizes() {
    val bufferSizes = intArrayOf(
        1024,
        2048,
        4096, // block size
        8192,
        16384, // page size of memory
        1024 * 1024, // = 1MB - max amount of data per I/O call
        2048 * 2048,
        HUNDRED_MB // 100MB - size of file
    )

    for (size in bufferSizes) {
        buffers.addLast(randomByteArray(size))
    }
    println("Reading------")
    for (bufferSize in bufferSizes) {
        val startTime = System.nanoTime()

        for (i in 1..100)
            readFile(readFile, bufferSize)

        val elapsedTime = System.nanoTime() - startTime
        System.out.printf(
            "\tBuffer size is %d bytes\t Time: %d ms%n",
            bufferSize,
            elapsedTime / 1000_000,
        )
    }
    println("Reading--------")

    println("Writing--------")
    for ((i, bufferSize) in bufferSizes.withIndex()) {
        val startTime = System.nanoTime()
        for (ind in 1..100) {
            writeFile(writeFile, buffers[i])
        }
        val elapsedTime = System.nanoTime() - startTime
        System.out.printf(
            "\tBuffer size is %d bytes\t Time: %d ms%n",
            bufferSize,
            elapsedTime / 1000_000,
            elapsedTime / 1000 / (HUNDRED_MB / bufferSize)
        )
    }
    println("Writing--------")
}

fun main() {
    testBufferSizes()
}

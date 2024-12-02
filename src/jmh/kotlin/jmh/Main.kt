package jmh

import manual.readFile
import manual.writeFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
open class Main {

    val readFile = File("src/jmh/resources/text_100MB.txt")
    val writeFile = File("src/jmh/resources/output_text.txt")

    val hundredMB = 100 * 1024 * 1024 // 100MB
    private lateinit var buffer: ByteArray

    @Param("1024", "2048", "4096", "8192", "16384", "1048576", "4194304", "100000000")
    private var bufferSize: Int = 0

    @Setup(Level.Iteration)
    fun setup() {
        buffer = ByteArray(bufferSize)
        Random.nextBytes(buffer)
    }

    @Benchmark
    fun readFile() {
        val buffer = ByteArray(bufferSize)
        var size: Int
        FileInputStream(readFile).use { fis ->
            do {
                size = fis.read(buffer)
            } while (size > 0)
            fis.close()
        }
    }

    val buffers = mutableListOf<ByteArray>()

    @Benchmark
    fun writeFile() {
        val writeTimes = hundredMB / buffer.size
        FileOutputStream(writeFile).use { fos ->
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
            hundredMB // 100MB - size of file
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
                writeFile(
                    writeFile,
                    buffers[i]
                )
            }
            val elapsedTime = System.nanoTime() - startTime
            System.out.printf(
                "\tBuffer size is %d bytes\t Time: %d ms%n",
                bufferSize,
                elapsedTime / 1000_000,
                elapsedTime / 1000 / (hundredMB / bufferSize)
            )
        }
        println("Writing--------")
    }

    fun main() {
        testBufferSizes()
    }
}

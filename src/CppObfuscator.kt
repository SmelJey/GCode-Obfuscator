import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.*

class CppObfuscator {
    init {

    }

    fun Obfuscate(file: File) {
        val words = mutableListOf<String>()
        val scanner = Scanner(file)

        while (scanner.hasNext()) {
            val nextLine = scanner.nextLine()
            if (nextLine.isNotEmpty() && nextLine[0] != '#'){
                val lineScanner = Scanner(nextLine)
                while (lineScanner.hasNext())
                    words.add(lineScanner.next())

                lineScanner.close()
            } else {
                words.add(nextLine)
            }
        }

        scanner.close()

        val writer = BufferedWriter(FileWriter(File("output.cpp")))
        var flag = false

        words.forEach {
            if (flag){
                writer.write(" ")
                flag = false
            } else if (it == "=") {
                writer.write(" ")
                flag = true
            } else if (it == "{") {
                writer.write(" ")
            } else {
                writer.newLine()
            }

            if (it == "public:" || it == "private:"){
                writer.write(" ")
            }

            writer.write(it)

        }

        writer.newLine()
        writer.close()
    }
}
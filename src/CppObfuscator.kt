import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.*

class CppObfuscator {
    private val maxPseudonymName : Int
    private val typeNames = listOf("char", "int",
                           "float", "double", "string", "bool",
                            "int32_t", "int64_t", "size_t")
    private val style : String
    private val rnd : Random

    init {
        maxPseudonymName = 10
        style = "oOcC"
        rnd = Random()
    }

    private fun getPseudonym() : String {
        var pseudonym = ""
        for (i in 0 until maxPseudonymName){
            pseudonym += style[rnd.nextInt(style.length)]
        }

        return pseudonym
    }

    private fun generatePseudonyms() : MutableMap<String, String> {
        val allPseudonyms = mutableMapOf<String, String>()
        val pseudonyms = mutableSetOf<String>()
        typeNames.forEach {
            var pseudonym = getPseudonym()
            while (pseudonyms.contains(pseudonym))
                pseudonym = getPseudonym()
            allPseudonyms[it] = pseudonym
            pseudonyms.add(pseudonym)
        }

        return allPseudonyms
    }

    private fun parse(scanner: Scanner) : Pair<MutableList<String>, Int> {
        val words = mutableListOf<String>()
        var defOff = 0
        var flag = false
        while (scanner.hasNext()) {
            val nextLine = scanner.nextLine()
            if (nextLine.isNotEmpty() && nextLine[0] != '#'){
                flag = true
                val lineScanner = Scanner(nextLine)
                while (lineScanner.hasNext()) {
                    words.add(lineScanner.next())
                }

                lineScanner.close()
            } else {
                if (!flag)
                    defOff++
                words.add(nextLine)
            }
        }

        return Pair(words, defOff)
    }

    private fun write(writer: BufferedWriter, words: List<String>, defOffset: Int, pseudonyms: Map<String, String>) {
        var flag = false
        var offset = 0

        words.forEach { word ->
            if (offset == defOffset) {
                typeNames.forEach {
                    writer.write("#define " + pseudonyms[it] + " " + it)
                    writer.newLine()
                }
            }

            if (flag){
                writer.write(" ")
                flag = false
            } else if (word == "=") {
                writer.write(" ")
                flag = true
            } else if (word == "{") {
                writer.write(" ")
            } else {
                writer.newLine()
            }

            if (word == "public:" || word == "private:"){
                writer.write(" ")
            }

            writer.write(word)
            offset++
        }

        writer.newLine()
    }

    fun obfuscate(file: File) {
        val scanner = Scanner(file)
        val words = parse(scanner)
        scanner.close()

        val pseudonyms = generatePseudonyms()

        typeNames.forEach {
            val pat = Regex("(?<!\\w)(" + it + ")(?!\\w)")
            for (i in 0 until words.first.size){
                words.first[i] = words.first[i].replace(pat, pseudonyms[it]!!)
            }
        }

        val writer = BufferedWriter(FileWriter(File("output.cpp")))
        write(writer, words.first, words.second, pseudonyms)
        writer.close()
    }
}
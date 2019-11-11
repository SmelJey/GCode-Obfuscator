import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.*

class CppObfuscator(private val style: String = "oOcC") {
    private val maxPseudonymName : Int = 10
    private val typeNames = listOf("char", "int",
                           "float", "double", "string", "bool",
                            "int32_t", "int64_t", "size_t")
    private val rnd : Random = Random()
    private val busyPseudonyms = mutableSetOf<String>()

    private fun getPseudonym() : String {
        var pseudonym = ""
        for (i in 0 until maxPseudonymName){
            pseudonym += style[rnd.nextInt(style.length)]
        }

        return pseudonym
    }

    private fun generatePseudonyms(keys : List<String>) : MutableMap<String, String> {
        val allPseudonyms = mutableMapOf<String, String>()

        keys.forEach {
            var pseudonym = getPseudonym()
            while (busyPseudonyms.contains(pseudonym))
                pseudonym = getPseudonym()
            allPseudonyms[it] = pseudonym
            busyPseudonyms.add(pseudonym)
        }

        return allPseudonyms
    }

    private fun parse(scanner: Scanner) : Triple<Int, MutableList<String>, MutableList<String>> {
        val words = mutableListOf<String>()
        val varList = mutableListOf<String>()
        var defOff = 0
        var flag = false

        while (scanner.hasNext()) {
            val nextLine = scanner.nextLine()
            if (nextLine.isNotEmpty() && nextLine[0] != '#'){
                flag = true
                val lineScanner = Scanner(nextLine)
                while (lineScanner.hasNext()) {
                    val curWord = lineScanner.next()
                    if (words.isNotEmpty() && typeNames.contains(words.last())
                            && Regex("[A-Za-z0-9;,]+").matches(curWord)) {
                        varList.add(Regex("[A-Za-z0-9]+").find(curWord)!!.value)
                    }

                    words.add(curWord)
                }

                lineScanner.close()
            } else {
                if (!flag)
                    defOff++
                words.add(nextLine)
            }
        }

        return Triple(defOff, words, varList)
    }

    private fun write(writer: BufferedWriter, words: List<String>, defOffset: Int, pseudonyms: Map<String, String>) {
        var flag = false
        var offset = 0

        writer.write("// Obfuscated using GCode-Obfuscator by SmelJey")
        writer.newLine()
        writer.write("// Github: https://github.com/SmelJey/GCode-Obfuscator")
        writer.newLine()

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

    fun obfuscate(file: File, out: File = File("output.cpp")) {
        busyPseudonyms.clear()
        val scanner = Scanner(file)
        val words = parse(scanner)
        scanner.close()

        val pseudonyms = generatePseudonyms(typeNames)
        val varPseudonyms = generatePseudonyms(words.third)

        typeNames.forEach {
            val pat = Regex("(?<!\\w)(" + it + ")(?!\\w)")
            for (i in 0 until words.second.size){
                words.second[i] = words.second[i].replace(pat, pseudonyms[it]!!)
            }
        }

        words.third.forEach {
            val pat = Regex("(?<=[\\s(\\[,;.&+\\-*/=^])(" + it
                    + ")(?=[\\s(\\[,;.&+\\-*/=^])|(^(" + it
                    + ")(?=[\\s(\\[,;.&+\\-*/=^]))|(?<=[\\s(\\[,;.&+\\-*/=^])(" + it
                    + ")$|^(" + it + ")$")
            for (i in 0 until words.second.size){
                words.second[i] = words.second[i].replace(pat, varPseudonyms[it]!!)
            }
        }

        val writer = BufferedWriter(FileWriter(out))
        write(writer, words.second, words.first, pseudonyms)
        writer.close()
    }
}
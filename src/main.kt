import java.io.File

fun main(args: Array<String>) {
    val obfuscator = CppObfuscator("rnm")
    var inputFile = "Source.cpp"
    var outFile = "output.cpp"
    if (args.isNotEmpty()) {
        inputFile = args[0]
        if (args.size > 1)
            outFile = args[1]
    }
    obfuscator.obfuscate(File(inputFile), File(outFile))
}

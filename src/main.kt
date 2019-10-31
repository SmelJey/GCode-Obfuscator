import java.io.File

fun main(args: Array<String>) {
    val obfuscator = CppObfuscator()
    obfuscator.obfuscate(File("Source.cpp"))
}

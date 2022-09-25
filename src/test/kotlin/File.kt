import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream


fun packJar(folder: File, target: String) {
    val jos = JarOutputStream(File(folder, "$target.jar").outputStream())
    val files = listFiles(folder)
    val dir = folder.absolutePath.length
    files.forEach {
        jos.putNextEntry(JarEntry(it.absolutePath.substring(dir)))
        jos.write(it.readBytes())
        jos.closeEntry()
    }
    jos.close()
}

fun listFiles(folder: File, suffix: String = ".class") = mutableListOf<File>().also {
    listFileRecursive(folder, suffix, it)
}

private fun listFileRecursive(folder: File, suffix: String, list: MutableList<File>) {
    folder.listFiles()?.forEach {
        if (it.isDirectory) {
            listFileRecursive(it, suffix, list)
        } else if (it.name.endsWith(suffix)) {
            list.add(it)
        }
    }
}

class Dummy
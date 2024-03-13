package shirates.builder.utility

import shirates.core.utility.exists
import shirates.core.utility.toPath
import java.io.FileNotFoundException
import java.nio.file.Files

object ProjectHelper {

    /**
     * getProjectDirectory
     */
    fun getProjectDirectory(
        file: String
    ): String {

        val filePath = file.toPath()
        var tempPath = filePath
        while (true) {
            if (tempPath.resolve("testrun.global.properties").exists()) {
                return tempPath.toString()
            }
            if (tempPath.resolve("src").exists()) {
                if (tempPath.resolve("build.gradle.kts").exists() || tempPath.resolve("build.gradle").exists()) {
                    return tempPath.toString()
                }
            }
            if (tempPath.parent.exists()) {
                tempPath = tempPath.parent
            } else {
                throw FileNotFoundException("project directory not found.")
            }
        }
    }
}
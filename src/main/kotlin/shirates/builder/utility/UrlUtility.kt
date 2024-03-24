package shirates.builder.utility

import java.awt.Desktop
import java.net.URI

object UrlUtility {

    fun openWebPage(url: String) {

        try {
            Desktop.getDesktop().browse(URI(url))
        } catch (t: Throwable) {
            println(t)
        }
    }
}